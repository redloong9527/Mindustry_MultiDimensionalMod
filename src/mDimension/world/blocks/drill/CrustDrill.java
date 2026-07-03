package mDimension.world.blocks.drill;

import arc.func.Floatf;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.IntSeq;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.world.blocks.environment.CrustFloor;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

import static mindustry.Vars.world;

public class CrustDrill extends Block {

    public float warmupSpeed = 0.012f;

    public float drillingSpeed = 1f/60f;
    public Floatf<Integer> increase= layer ->{
        return 1 + layer*layer*0.2f;
    };
    public int basicLayer = 3;
    public int maxLinks = 3;
    public float boostMulti = 0.5f;
    public float outputTime = 60f;
    public float produceMulti = 1f;

    protected ItemStack[] tempItems;
    protected LiquidStack[] tempLiquid;
    protected ObjectIntMap<CrustFloor> floorCount= new ObjectIntMap<>();
    protected final Seq<CrustFloor> floorArray = new Seq<>();
    protected CrustFloor findCrust(Tile tile){
        floorCount.clear();
        floorArray.clear();
        for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
            if(other.floor() instanceof CrustFloor crustFloor){
                floorCount.increment(crustFloor,0,1);
            }
        }

        for(CrustFloor item : floorCount.keys()){
            floorArray.add(item);
        }

        floorArray.sort((item1, item2) -> {
            int amounts = Integer.compare(floorCount.get(item1, 0), floorCount.get(item2, 0));
            if(amounts != 0) return amounts;
            return Integer.compare(item1.id, item2.id);
        });

        if(floorArray.size == 0 || floorCount.get(floorArray.peek())<8){
            return null;
        }

        return floorArray.peek();
    }


    public DrawBlock drawer = new DrawDefault();
    public CrustDrill(String name){
        super(name);
        size = 4;
        update = true;
        solid = true;
        group = BlockGroup.drills;
        hasLiquids = true;
        hasItems = true;
        ambientSound = Sounds.loopDrill;
        ambientSoundVolume = 0.2f;
        //drills work in space I guess
        envEnabled |= Env.space;
        flags = EnumSet.of(BlockFlag.drill);
    }
    @Override
    public void load(){
        super.load();

        drawer.load(this);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("layer",(CrustDrillBuild b)->
                new Bar(
                        ()->"layer:"+b.layer+"/"+b.getMaxLayer(),
                        ()-> Pal.lightOrange,
                        ()->b.getMaxLayer()> b.layer?b.progress/increase.get(b.layer):1f
                )
        );
        addBar("craft",(CrustDrillBuild b)->
                new Bar(
                        ()->"craft:"+b.outputProgress+"/"+outputTime,
                        ()-> Pal.lightOrange,
                        ()->b.outputProgress/outputTime
                )
        );


    }


    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return findCrust(tile) !=null;
    }

    protected void countOutput(CrustFloor crustFloor, int layer,@Nullable CrustDrillBuild self){
        if (crustFloor != null) {

            var strata = CrustFloor.Stratum.find(layer, crustFloor.strata);
            //Debug.string(Arrays.toString(strata),30f,8*150,8*160);
            tempItems = new ItemStack[CrustFloor.Stratum.itemCount];
            tempLiquid = new LiquidStack[CrustFloor.Stratum.liquidCount];
            int items = 0, liquids = 0;
            for (CrustFloor.Stratum stratum : strata) {
                if (stratum == null) continue;
                if (stratum.itemStack != null && stratum.itemStack.amount > 0) {
                    tempItems[items++] = stratum.itemStack;
                }
                if (stratum.liquidStack != null && stratum.liquidStack.amount > 0) {
                    tempLiquid[liquids++] = stratum.liquidStack;
                }
            }
        }else{
            tempItems = new ItemStack[0];
            tempLiquid = new LiquidStack[0];
            self.outputItems = new ItemStack[0];
            self.outputLiquids = new LiquidStack[0];
            return;
        }

        if(self!=null){
            self.outputLiquids = tempLiquid.clone();
            self.outputItems = tempItems.clone();
        }
    }
    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        drawer.getRegionsToOutline(this, out);
    }
    public class CrustDrillBuild extends Building{
        public int layer = 0;
        public float progress = 0,warmup,outputProgress;
        public float totalProgress=0;
        public IntSeq links = new IntSeq();
        public int targetLayer;

        public ItemStack[] outputItems = new ItemStack[]{};
        public LiquidStack[] outputLiquids = new LiquidStack[]{};
        public CrustFloor floor = null;
        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }
        @Override
        public float totalProgress() {
            return totalProgress;
        }

        @Override
        public void updateTile() {
            if(efficiency >0 && floor!=null){
                warmup = Mathf.approachDelta(warmup, 1, warmupSpeed);
                int maxLayer = getMaxLayer();
                if (layer<maxLayer) {
                    progress+= drillingSpeed * edelta() * (1+boostMulti * optionalEfficiency);
                    if(progress > increase.get(layer)){
                        progress = 0;
                        layer++;
                        countOutput(floor,layer,this);
                    };
                }else if(layer>maxLayer){
                    progress-= drillingSpeed * edelta() * (1+boostMulti * optionalEfficiency);
                    if(progress < 0){
                        progress = 1;
                        layer--;
                        countOutput(floor,layer,this);
                    };
                }
                outputProgress+=edelta();
                if(outputProgress>outputTime){
                    outputProgress=0;
                    output();
                }
                if(outputLiquids != null){
                    float inc = getProgressIncrease(1f);
                    for(var output : outputLiquids){
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc * produceMulti, liquidCapacity - liquids.get(output.liquid)));
                    }
                }

            }else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }
            totalProgress += warmup* Time.delta;
            dumpOutputs();
        }


        @Override
        public void placed() {
            super.placed();
            floor = findCrust(tile);
            countOutput(floor,layer,this);
        }

        public void dumpOutputs(){
            if(outputItems != null && timer(timerDump, dumpTime / timeScale)){
                for(ItemStack output : outputItems){
                    dump(output.item);
                }
            }

            if(outputLiquids != null){
                for(int i = 0; i < outputLiquids.length; i++){
                    dumpLiquid(outputLiquids[i].liquid, 2f);
                }
            }
        }

        void output(){
            //if(outputItems!=null)Debug.string("items"+ Arrays.toString(outputItems) +"\nLiquids"+ Arrays.toString(outputLiquids),30f,this);
            if(outputItems != null){
                for(var output : outputItems){
                    int amount = Mathf.round(output.amount * produceMulti);
                    for(int i = 0; i < amount; i++){
                        offload(output.item);
                    }
                }
            }
        }
        @Override
        public boolean shouldConsume(){
            if(outputItems != null){
                for(var output : outputItems){
                    if(items.get(output.item) + output.amount > itemCapacity){
                        return false;
                    }
                }
            }

            if(outputLiquids != null){
                for(var output : outputLiquids){
                    if(liquids.get(output.liquid) >= liquidCapacity - 0.001f){
                        return false;
                    }
                }
            }

            return enabled;
        }

        public int getMaxLayer(){
            int[] items = links.items;
            int res = basicLayer;
            int guide = -1;
            for(int i=0;i<items.length;i++){
                int pos = items[i];
                Building other = world.build(pos);
                if(i<maxLinks && other instanceof CrustDrillBooster.CrustDrillBoosterBuild boost && other.team == this.team && !other.dead){
                    res+=boost.boostLayers();
                    if(boost.isGuide()){
                        guide = boost.getGuide();
                    }
                }else{
                    links.removeValue(pos);
                }
            }

            return guide>0?Math.min(res,guide):res;
        }

        @Override
        public void updateProximity() {
            super.updateProximity();
            floor = findCrust(tile);
            countOutput(floor,layer,this);
        }

        @Override
        public void write(Writes w) {
            super.write(w);
            w.s(layer);
            w.f(progress);
            w.f(warmup);
            w.f(outputProgress);
        }

        @Override
        public void read(Reads r, byte revision) {
            super.read(r, revision);
            layer = r.s();
            progress = r.f();
            warmup = r.f();
            outputProgress = r.f();
        }
    }
}


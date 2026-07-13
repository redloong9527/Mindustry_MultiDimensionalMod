package mDimension.world.blocks.unit;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.draw.DrawRotation;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;

import static mindustry.Vars.tilesize;

public class AnchorRadar extends Block {
    public float radius = 8f*25;
    public String topSuffix = "-top";
    public float patience = 60f*20;
    public Effect callEffect = Fx.none;
    public Color effectColor = Color.white;
    public float callUnitInterval = 80f;
    float dst = -1f;
    Unit outUnit = null;
    public DrawBlock drawer = new DrawMulti(
            new DrawRegion(),new DrawRotation("-rotate",true),new DrawRegion(topSuffix,3.2f,false)
    );
    public AnchorRadar(String name){
        super(name);
        update = true;
        solid = false;
        sync = true;
        rotate = true;
        rotateDraw =false;
        size = 1;
        configurable = true;
        config(UnitType.class,(AnchorRadarBuild b,UnitType u)->{
            b.config= u;
        });
        configClear((AnchorRadarBuild b)->{
            b.config = null;
        });
    }
    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize,y* tilesize,radius, Pal.placing);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        drawer.getRegionsToOutline(this, out);
    }


    public class AnchorRadarBuild extends Building{
        Seq<UnitType> configs = new Seq<>();
        public UnitType config = null;
        public float totalProcess = 0;
        public float warmup = 0;
        public Unit commandUnit;
        public float timeoutProcess = 0f;
        public Interval time = new Interval();

        @Override
        public void updateTile() {
            Building b = front();
            if(b instanceof RegionReconstructor.RegionReconstructorBuild boss){
                warmup = Mathf.approachDelta(warmup,boss.amount>0?1:0, Time.delta * 0.3f/60);

                if(commandUnit!=null &&(commandUnit.dead() || !commandUnit.isAdded())) {
                    commandUnit = null;
                }
                if(commandUnit == null){
                    if(warmup>0.99f && boss.amount>0){
                        commandUnit = findUnit();
                        call();
                    }
                    timeoutProcess = 0;
                }else{
                    timeoutProcess += Time.delta;
                    if(timeoutProcess>=patience){
                        commandUnit = null;
                        timeoutProcess = 0;
                    }

                    if(time.get(0, callUnitInterval)){
                        callEffect.at(x,y,0,effectColor,commandUnit);
                        if(boss.amount>0)call();
                    }
                }


            }else{
                warmup = Mathf.approachDelta(warmup,0, Time.delta * 0.2f/60);
            }
            totalProcess += warmup *Time.delta;
        }

        void call(){
            if(commandUnit!=null && commandUnit.isCommandable()){
                if(commandUnit.type.canBoost){
                    commandUnit.updateBoosting(true);
                }
                commandUnit.command().commandPosition(Tmp.v2.set(x,y));
            }
        }

        public Unit findUnit(){
            if(config == null)return null;
            outUnit = null;
            dst = -1;
            Units.nearby(team,x,y,radius,u->{
                float dst2 = u.dst2(x,y);
                if(u.team == team&& u.type == config && (dst <0||dst2 < dst) && dst2 <= radius * radius &&
                u.isCommandable() && u.command().targetPos == null){
                    dst = dst2;
                    outUnit = u;
                }
            });

            return outUnit;
        };

        @Override
        public void buildConfiguration(Table table) {

            Building b = front();
            if(b == null)return;
            if(b.block instanceof RegionReconstructor boss){
                configs.clear();

                for(int i=0;i<boss.upgrades.size;i++){
                    UnitType e = boss.upgrades.get(i)[0];
                    configs.add(e);
                }
                ItemSelection.buildTable(block,table,configs,
                        ()->config,this::configure,true
                        );
            }

        }

        @Override
        public float totalProgress() {
            return totalProcess;
        }

        @Override
        public Object config() {
            return config;
        }

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

            Drawf.dashCircle(x, y, radius,Pal.placing);

        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public void write(Writes w) {
            super.write(w);
            TypeIO.writeUnitType(w,config);
            w.f(warmup);
            w.f(timeoutProcess);
        }

        @Override
        public void read(Reads r, byte revision){
            super.read(r, revision);

            config = TypeIO.readUnitType(r);
            warmup=r.f();
            timeoutProcess=r.f();
        }
    }
}

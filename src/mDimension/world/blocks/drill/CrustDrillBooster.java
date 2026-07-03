package mDimension.world.blocks.drill;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.tool.md_Edge;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

public class CrustDrillBooster extends Block {
    public int boostLayers = 2;
    public boolean isGuide =false;
    public DrawBlock drawer = new DrawDefault();
    public TextureRegion sideRegion,rotateRegion1,rotateRegion2;
    public float consumeTime = 60f;

    public CrustDrillBooster(String name){
        super(name);
        size = 2;
        update = true;
        solid = true;
        group = BlockGroup.drills;
        rotate = true;
        canOverdrive = false;
        rotateDraw = false;
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
        sideRegion = Core.atlas.find(name+"-side");
        rotateRegion1 = Core.atlas.find(name+"-rotate1");
        rotateRegion2 = Core.atlas.find(name+"-rotate2");
        drawer.load(this);
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

    public class CrustDrillBoosterBuild extends Building{
        public Building link = null;
        public float progress = 0;
        public float totalProgress = 0;
        public float warmup = 0;
        public int boostLayers(){
            return Mathf.round(boostLayers * efficiency);
        }
        public boolean isGuide(){return isGuide;};
        public int getGuide(){
            return -1;
        }

        @Override
        public void draw(){
            drawer.draw(this);
            if(link !=null){
                drawRotate();
            }
        }
        void drawRotate() {
            Draw.rect(rotation < 2 ? rotateRegion1 : rotateRegion2, x, y, rotation * 90f);
            Draw.z(Layer.block - 0.01f);
            float sx = sideRegion.width / 4f;
            float sy = sideRegion.height / 4f;
            sy *= rotation == 1 || rotation == 2 ? -1 : 1;
            Draw.rect(sideRegion, x, y, sx, sy, rotation * 90f);
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
        public float progress() {
            return progress;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public void updateTile() {
            if(efficiency>0){
                progress += getProgressIncrease(consumeTime);
                warmup = Mathf.approachDelta(warmup,1,1/90f);
            }else{
                warmup = Mathf.approachDelta(warmup,0,1/90f);
            }
            totalProgress+=warmup* Time.delta;
            if(progress>=1f){
                progress%=1f;
                consume();
            }
        }


        @Override
        public void updateProximity() {
            super.updateProximity();
            Building b = md_Edge.getAllFacingBuild(this);
            if(b instanceof CrustDrill.CrustDrillBuild c){
                c.links.addUnique(pos());
            }
            link = b;

        }

        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(progress);
            w.f(warmup);
        }

        @Override
        public void read(Reads r, byte v) {
            super.read(r, v);
            progress = r.f();
            warmup = r.f();
        }
    }
}

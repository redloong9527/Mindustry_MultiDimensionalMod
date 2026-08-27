package mDimension.world.blocks.farming;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.tool.Drawff;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.world;

public class HarvestingBlock extends Block {
    public int regionSize = 7;
    public float dumpSpeed = 3f;

    public float armRotateSpeed = 1.2f,teleSpeed = 0.25f,armWidth =5,armArrowInterval = 5f,armArrowOffset = 3f;
    public TextureRegion arm,armCap,armHand,armStart,armStartBottom,armArrow;
    public HarvestingBlock(String name) {
        super(name);
        solid = true;
        update = true;
        hasItems = true;
    }
    TextureRegion f(String sfx){
        return Core.atlas.find(name+"-"+sfx);
    }
    @Override
    public void load() {
        super.load();
        arm = f("arm");
        armCap = f("arm-cap");
        armHand = f("arm-hand");
        armStart = f("arm-start");
        armStartBottom = f("arm-start-bottom");
        armArrow = f("arm-arrow");
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Draw.color(Pal.placing,0.5f);

        int cx = (-regionSize/2+x)*8;
        int cy = (-regionSize/2+y)*8;
        for(int dy=0;dy<regionSize;dy++){
            for(int dx=0;dx<regionSize;dx++){
                if(dx == regionSize/2 && dy == regionSize/2)continue;
                Fill.square(dx * 8 + cx, dy * 8 + cy, 2.3f);
            }
        }
    }

    public class HarvestingBlockBuild extends Building{
        public float progress = 0;
        public Tile target;
        public MechanicalArm arm = new MechanicalArm();

        @Override
        public void update() {
            super.update();
            arm.x = x;arm.y = y;
        }

        @Override
        public void updateTile() {
            progress += edelta();
            while (progress>=dumpSpeed) {
                dump();
                progress -= dumpSpeed;
            }
            if(!isPayload())harvesting();
        }

        @Override
        public void placed() {
            super.placed();
            arm.init(this);
        }

        void harvesting(){
            if(can(target) && target.build instanceof Crop.CorpBuild c){
                arm.moveTo(target);
                if(arm.within(target,2f)){
                    c.harvest(this);
                    target = null;
                }
            }else{
                findTarget();
            }
        }
        void findTarget(){
            int cx = -regionSize/2+tile.x;
            int cy = -regionSize/2+tile.y;
            for(int dy=0;dy<regionSize;dy++){
                for(int dx=0;dx<regionSize;dx++){
                    var o = world.tile(dx+cx,dy+cy);
                    if(o == null)continue;
                    if(can(o) && o.build instanceof Crop.CorpBuild c){
                        c.tryHarvestingBuildPos = this.tile.pos();
                        target = o;
                        return;
                    }
                }
            }

        }

        boolean can(Tile t){
            if(t == null || Math.abs(t.x-tile.x)>regionSize/2+1 || Math.abs(t.y-tile.y)>regionSize/2+1){
                return false;
            }
            return t.build instanceof Crop.CorpBuild c && c.adult() && (c.tryHarvestingBuildPos == -1 || c.tryHarvestingBuildPos == this.tile.pos());
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.blockProp+1f);
            updateClipRadius(regionSize*8);
            arm.draw();
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            arm = new MechanicalArm();
            arm.init(this);
            arm.read(read);
            target = TypeIO.readTile(read);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            arm.write(write);
            TypeIO.writeTile(write,target);
        }
    }


    public class MechanicalArm{
        public float rotation = 0;
        public Building owner;
        public float len = 0;
        public float x,y;
        public void init(Building b){
            x = b.x;y = b.y;
            rotation = 90*b.rotation;
            owner = b;
        }
        public void moveTo(Tile to){
            moveTo(to.worldx(),to.worldy());
        }
        public void moveTo(float tx,float ty){
            float rt = Angles.angle(x,y,tx,ty);
            float l = Mathf.dst(x,y,tx,ty);
            rotation  = len<0.01f?rt:Angles.moveToward(rotation,rt,armRotateSpeed * owner.edelta());
            len = Mathf.approach(len,l,teleSpeed * owner.edelta());
        }

        public void draw(){
            if(owner.isPayload()){
                x = owner.x;y = owner.y;
            }
            Vec2 target = Tmp.v2.trns(rotation,len).add(x,y);
            Draw.rect(armHand,target.x,target.y);
            Draw.rect(armStartBottom,x,y,rotation);
            Lines.stroke(armWidth);
            Lines.line(arm, x, y,target.x,target.y, false);
            Lines.stroke(1f);

            for(float l = len-armArrowOffset;l>0;l-=armArrowInterval){
                Tmp.v1.set(target).sub(x,y).nor().scl(l).add(x,y);
                Drawff.flipSpinSprite(armArrow,Tmp.v1.x,Tmp.v1.y,rotation,20f,false);
            }

            Draw.rect(armCap,target.x,target.y,rotation);
            Drawf.spinSprite(armStart, x, y, rotation);

        }

        public boolean within(Tile t, float radius){
            return Tmp.v2.trns(rotation,len).add(x,y).sub(t.worldx(),t.worldy()).len2() <= radius * radius;
        }
        public void read(Reads r){
            rotation = r.f();
            len = r.f();
        }
        public void write(Writes w){
            w.f(rotation);
            w.f(len);

        }
    }
}

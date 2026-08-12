package mDimension.world.blocks.payload;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Time;
import arc.util.Tmp;
import mDimension.tool.md_Edge;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.*;

import static mindustry.Vars.tilesize;

//PayloadRouter
public class md_PayloadRouter extends PayloadRouter {
    public TextureRegion overRegion;
    public md_PayloadRouter(String name,int size){
        super(name);
        this.size = size;
    }

    @Override
    public void init() {
        super.init();
        overRegion = Core.atlas.find(name+"-over");
    }

    public class md_PayloadRouterBuild extends PayloadRouterBuild{

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            Building accept = md_Edge.getFacingBuild(this);
            //next block must be aligned and of the same size
            if(accept != null && (
                    //same size
                    (accept.block.size == size && tileX() + Geometry.d4(rotation).x * size == accept.tileX() && tileY() + Geometry.d4(rotation).y * size == accept.tileY()) ||

                            //differing sizes
                            (accept.block.size > size &&
                                    (rotation % 2 == 0 ? //check orientation
                                            Math.abs(accept.y - y) <= (accept.block.size * tilesize - size * tilesize)/2f : //check Y alignment
                                            Math.abs(accept.x - x) <= (accept.block.size * tilesize - size * tilesize)/2f   //check X alignment
                                    )))){
                next = accept;
            }else{
                next = null;
            }

            int ntrns = 1 + size/2;
            Tile next = tile.nearby(Geometry.d4(rotation).x * ntrns, Geometry.d4(rotation).y * ntrns);
            blocked = (next != null && next.solid() && !(next.block().outputsPayload || next.block().acceptsPayload)) || (this.next != null && this.next.payloadCheck(rotation));
        }
        @Override
        public float fract() {
            return progress / moveTime;
        }
        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return this.item == null
                    && payload.fits(payloadLimit)
                    && (source == this || this.enabled && (progress<10f));
        }
        @Override
        public void updateTile(){
            if(!enabled) return;

            if(item != null){
                item.update(null, this);
            }

            lastInterp = curInterp;
            curInterp = fract();
            //rollover skip
            if(lastInterp > curInterp) lastInterp = 0f;
            progress = time() % moveTime;

            updatePayload();
            if(item != null && next == null){
                PayloadBlock.pushOutput(item, 0.01f);
            }

            //TODO nondeterministic input priority
            int curStep = curStep();
            if(curStep > step){
                boolean valid = step != -1;
                step = curStep;
                boolean had = item != null;

                if(valid && stepAccepted != curStep && item != null){
                    if(next != null){
                        //trigger update forward
                        next.updateTile();

                        //TODO add self to queue of next conveyor, then check if this conveyor was selected next frame - selection happens deterministically
                        if(next != null && next.acceptPayload(this, item)){
                            //move forward.
                            next.handlePayload(this, item);
                            item = null;
                            moved();
                        }
                    }else if(!blocked){
                        //dump item forward
                        if(item.dump()){
                            item = null;
                            moved();
                        }
                    }
                }

                if(had && item != null){
                    moveFailed();
                }
            }

            /// ///
            controlTime -= Time.delta;
            smoothRot = Mathf.slerpDelta(smoothRot, rotdeg(), 0.2f);
            /// ///
        }
        @Override
        public void draw() {
            Draw.rect(region, x, y);


            Draw.mixcol(team.color, 0);
            Draw.rect(topRegion, x, y, smoothRot);
            Draw.reset();

            Draw.rect(overRegion, x, y);

            Draw.z(Layer.blockOver);

            if (item != null) {
                item.draw();
            }
//            Draw.color(team.color.a(0.6f));
//            Draw.z(140);
//            Lines.setCirclePrecision(1);
//            Lines.stroke(0.8f);
//            Lines.circle(x,y,4+0.4f);
//            Fill.circle(x,y,(progress/moveTime)*4);
//            Draw.reset();
        }

        public void updatePayload(){
            if(item != null){
                if(animation > fract()){
                    animation = Mathf.lerp(animation, 0.8f, 0.15f);
                }

                animation = Math.max(animation, fract());

                float fract = animation;
                float rot = Mathf.slerp(itemRotation, rotdeg(), fract);

                if(fract < 0.5f){
                    Tmp.v1.trns(itemRotation + 180, (0.5f - fract) * tilesize * size);
                }else{
                    Tmp.v1.trns(rotdeg(), (fract - 0.5f) * tilesize * size);
                }
                Tmp.v1.add(md_Edge.bias(Tmp.v2,this));

                float vx = Tmp.v1.x, vy = Tmp.v1.y;

                item.set(x + vx, y + vy, rot);
            }
        }

        @Override
        public int curStep(){
            return (int)(time() / moveTime);
        }

        @Override
        public void moveFailed() {
            super.moveFailed();
        }

        @Override
        public float time() {
            return Time.time;
        }
    }

}

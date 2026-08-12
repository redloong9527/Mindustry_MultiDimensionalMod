package mDimension.world.blocks.payload;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mDimension.tool.md_Edge;
import mindustry.gen.Building;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadBlock;

import static mindustry.Vars.tilesize;

public class MDPayloadBlock extends PayloadBlock {
    public MDPayloadBlock(String name){
        super(name);
    }

    public class MDPayloadBlockBuild<T extends Payload> extends PayloadBlockBuild<T>{
        @Override
        public void moveOutPayload() {
            if(payload == null) return;

            updatePayload();

            Vec2 dest = Tmp.v1.trns(rotdeg(), size * tilesize/2f);
            dest.add(md_Edge.bias(Tmp.v2,this));

            payRotation = Angles.moveToward(payRotation, rotdeg(), payloadRotateSpeed * delta());
            payVector.approach(dest, payloadSpeed * delta());

            Building front = md_Edge.getFacingBuild(this);
            boolean canDump = front == null || !front.tile.solid();
            boolean canMove = front != null && (front.block.outputsPayload || front.block.acceptsPayload);

            if(canDump && !canMove){
                pushOutput(payload, 1f - (payVector.dst(dest) / (size * tilesize / 2f)));
            }
            if(payVector.within(dest, 0.001f)){
                payVector.clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);

                if(canMove){
                    if(movePayload(payload)){
                        payload = null;
                    }
                }else if(canDump){
                    dumpPayload();
                }
            }
        }

        @Override
        public boolean movePayload(Payload todump) {
            var e = md_Edge.getFacingBuild(this);
            if (e != null && e.team == this.team && e.acceptPayload(this, todump)) {
                e.handlePayload(this, todump);
                return true;
            } else {
                return false;
            }
        }
    }

}

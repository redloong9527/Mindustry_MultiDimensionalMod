package mDimension.world.blocks.drill;

import arc.math.Mathf;
import arc.util.Time;
import mDimension.tool.Debug;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Pump;

public class DirectionalPump extends Pump {
    public DirectionalPump(String name){
        super(name);
        rotate = true;
        rotateDraw = false;
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return false;
    }



    public class DirectionalPumpBuild extends PumpBuild{
        @Override
        public void updateTile(){
            if(efficiency > 0 && liquidDrop != null){
                float maxPump = Math.min(liquidCapacity - liquids.get(liquidDrop), amount * pumpAmount * edelta());
                liquids.add(liquidDrop, maxPump);

                //does nothing for most pumps, as those do not require items.
                if((consTimer += delta()) >= consumeTime){
                    consume();
                    consTimer %= 1f;
                }

                warmup = Mathf.approachDelta(warmup, maxPump > 0.001f ? 1f : 0f, warmupSpeed);
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            totalProgress += warmup * Time.delta;

            if(liquidDrop != null){
                dumpLiquid(liquidDrop,2,0);
            }
        }
    }
}

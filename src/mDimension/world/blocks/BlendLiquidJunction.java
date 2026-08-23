package mDimension.world.blocks;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidJunction;

public class BlendLiquidJunction extends LiquidJunction {
    public TextureRegion side;

    public BlendLiquidJunction(String name){
        super(name);
    }
    public void init(){
        super.init();
        side = Core.atlas.find(name+"-side");
    }
    public class BlendLiquidJunctionBuild extends LiquidJunctionBuild{
        public int blend = 0;
        @Override
        public void updateProximity() {
            super.updateProximity();
            blend = 0;
            for(int i=0;i<proximity.size;i++){
                Building other = proximity.get(i);
                int r = relativeTo(other);
                if(!(other instanceof BlendLiquidJunctionBuild)&&
                        (other instanceof Conduit.ConduitBuild || other.block.hasLiquids)){
                    blend |= 1<<r;
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            for(int i=0;i<4;i++){
                if((blend & 1<<i) == 1){
                    Draw.rect(side,x,y,i*90);
                }
            }
        }
    }
}


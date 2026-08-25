package mDimension.world.blocks;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Time;
import mDimension.tool.Debug;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidJunction;

import static arc.math.geom.Geometry.d4;
import static arc.math.geom.Geometry.d8;

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
        @Override
        public void draw() {
            super.draw();

            for(int i =0;i<4;i++){
                Tile t = tile.nearby(d4[i]);
                if(t == null)continue;
                var other = t.build;
                if(other!=null && !(other instanceof BlendLiquidJunctionBuild)&&
                        (other instanceof Conduit.ConduitBuild || other.block.hasLiquids)){
                    Draw.rect(side,x,y,i*90);
                }
            }
        }
    }
}


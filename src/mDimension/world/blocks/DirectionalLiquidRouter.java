package mDimension.world.blocks;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.liquid.LiquidRouter;


public class DirectionalLiquidRouter extends LiquidRouter {
    public DirectionalLiquidRouter(String name){
        super(name);
        rotate = true;
        rotateDraw = false;
        drawArrow = true;
        liquidPadding = 0.75f;
        solid = false;
    }

    public TextureRegion[] icons(){
        return new TextureRegion[]{bottomRegion, region,topRegion};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(bottomRegion,plan.drawx(),plan.drawy());
        Draw.rect(region,plan.drawx(),plan.drawy());
        Draw.rect(topRegion,plan.drawx(),plan.drawy(),plan.rotation *90);
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return false;
    }

    public class DirectionalLiquidRouterBuild extends  LiquidRouterBuild{

        @Override
        public void updateTile() {
            if(front()!=null)dumpLiquid(liquids.current(),2,0);
            if(left()!=null)dumpLiquid(liquids.current(),2,1);
            if(right()!=null)dumpLiquid(liquids.current(),2,3);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return this.relativeTo(source) == (this.rotation + 2)%4 && (liquids.current() == liquid || liquids.currentAmount() < 0.3f);
        }

        @Override
        public void draw() {
            Draw.rect(bottomRegion, x, y);
            if (liquids.currentAmount() > 0.001f) {
                drawTiledFrames(size, x, y, liquidPadding, liquids.current(), liquids.currentAmount() / liquidCapacity);
            }
            Draw.rect(region, x, y);
            Draw.rect(topRegion, x, y, this.rotation *90f);
        }
    }
}

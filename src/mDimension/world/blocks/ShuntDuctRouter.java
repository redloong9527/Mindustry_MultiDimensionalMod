package mDimension.world.blocks;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.blocks.distribution.DuctRouter;

public class ShuntDuctRouter extends DuctRouter {
    public ShuntDuctRouter(String name){
        super(name);
    }

    public TextureRegion center;

    public void load(){
        super.load();
        center = Core.atlas.find(name+"-center");
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(region,plan.drawx(),plan.drawy());
        if(plan.config instanceof Item i){
            Draw.color(i.color);
            Draw.rect(center,plan.drawx(),plan.drawy());
            Draw.color();
        }
        Draw.rect(topRegion,plan.drawx(),plan.drawy(),plan.rotation*90);

    }

    public class ShuntDuctRouterBuild extends DuctRouterBuild{

        @Override
        public Building target() {
            if(current == null) return null;

            int dump = cdump;

            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + dump) % proximity.size);
                int rel = relativeTo(other);

                if(other!=null&& (this.rotation +2)%4 != rel && other.team == team && other.acceptItem(this, current)){
                    incrementDump(proximity.size);
                    if(sortItem == null)return other;
                    if(sortItem != current && rel == this.rotation){
                        return other;
                    }else if (sortItem == current){
                        return other;
                    }

                    return null;
                }

                incrementDump(proximity.size);
            }

            return null;
        }
        @Override
        public void draw(){
            Draw.rect(region, x, y);
            if(sortItem != null){
                Draw.color(sortItem.color);
                Draw.rect(center, x, y);
                Draw.color();
            }
            Draw.rect(topRegion, x, y, rotdeg());
        }
    }
}

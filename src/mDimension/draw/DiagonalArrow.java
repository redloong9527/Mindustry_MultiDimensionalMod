package mDimension.draw;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.draw.DrawBlock;

import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;

public class DiagonalArrow extends DrawBlock {
    private Seq<BuildPlan> plansOut = new Seq<>(BuildPlan.class);
    private QuadTree<BuildPlan> playerPlanTree = new QuadTree<>(new Rect());
    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        drawArrow(block, plan.tile().x, plan.tile().y, plan.rotation, validPlace(plan.tile().x, plan.tile().y, block, plan.rotation));
    }

    public void drawArrow(Block block, int x, int y, int rotation, boolean valid){
        float trns = ((float) block.size / 2) * tilesize;
        float dx = 0,dy = 0;
        switch (rotation){
            case(0)->{dx = 0.5f;dy = 0.5f;}
            case(1)->{dx = -0.5f;dy = 0.5f;}
            case(2)->{dx = -0.5f;dy = -0.5f;}
            case(3)->{dx = 0.5f;dy = -0.5f;}
        }
        float offsetx = x * tilesize + block.offset + dx*trns;
        float offsety = y * tilesize + block.offset + dy*trns;
        Draw.color((!valid ? Pal.removeBack : Pal.accentBack), Color.white,0.5f);
        TextureRegion regionArrow = Core.atlas.find("place-arrow");

        Draw.rect(regionArrow,
                offsetx,
                offsety - 1,
                regionArrow.width * regionArrow.scl(),
                regionArrow.height * regionArrow.scl(),
                rotation * 90 - 45);

        Draw.color((!valid ? Pal.removeBack : Pal.accentBack), Color.white,0.5f);
        Draw.rect(regionArrow,
                offsetx,
                offsety,
                regionArrow.width * regionArrow.scl(),
                regionArrow.height * regionArrow.scl(),
                rotation * 90 - 45);
        if(valid)Draw.reset();

    }
    public boolean validPlace(int x, int y, Block type, int rotation){
        return validPlace(x, y, type, rotation, null);
    }
    public boolean validPlace(int x, int y, Block type, int rotation, @Nullable BuildPlan ignore){
        return validPlace(x, y, type, rotation, ignore, false);
    }

    public boolean validPlace(int x, int y, Block type, int rotation, @Nullable BuildPlan ignore, boolean ignoreUnits){
        if(player.isBuilder() && player.unit().plans.size > 0){
            Tmp.r1.setCentered(x * tilesize + type.offset, y * tilesize + type.offset, type.size * tilesize);
            plansOut.clear();
            playerPlanTree.intersect(Tmp.r1, plansOut);

            for(int i = 0; i < plansOut.size; i++){
                var plan = plansOut.items[i];
                if(plan != ignore
                        && !plan.breaking
                        && plan.block.bounds(plan.x, plan.y, Tmp.r1).overlaps(type.bounds(x, y, Tmp.r2))
                        && !(type.canReplace(plan.block) && Tmp.r1.equals(Tmp.r2))){
                    return false;
                }
            }
        }

        return ignoreUnits ? Build.validPlaceIgnoreUnits(type, player.team(), x, y, rotation, true, true) : Build.validPlace(type, player.team(), x, y, rotation);
    }
}

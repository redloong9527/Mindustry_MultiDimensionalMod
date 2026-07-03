package mDimension.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;
import mindustry.world.draw.DrawBlock;

public class DrawAutotile extends DrawBlock {
    public TextureRegion[] autoTiles;
    public TextureRegion mid;

    @Override
    public void load(Block block) {
        autoTiles = TileBitmask.load(block.name);
        mid = Core.atlas.find(block.name);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(mid,plan.drawx(),plan.drawy());
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return new TextureRegion[]{mid};
    }

    @Override
    public void draw(Building b) {

        int bits = 0;
        var tile = b.tile;
        for(int i = 0; i < 8; i++){
            Tile other = tile.nearby(Geometry.d8[i]);
            if(other.build!=null && other.build.block == b.block){
                bits |= (1 << i);
            }
        }

        int bit = TileBitmask.values[bits];
        var region = autoTiles[bit];

        Draw.rect(region, b.x, b.y);
    }

}

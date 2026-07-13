package mDimension.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

public class DrawSimpleTurret extends DrawBlock {

    public String suffix = "-tower";
    public TextureRegion tower;
    @Override
    public void load(Block block) {
        tower = Core.atlas.find(block.name+suffix);
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return new TextureRegion[]{tower};
    }

    @Override
    public void drawPlan(Block block, BuildPlan p, Eachable<BuildPlan> list) {
        Draw.rect(tower,p.drawx(),p.drawy(),p.rotation * 90f-90f);
    }

    @Override
    public void draw(Building b) {
        if(b instanceof ExtraRotation r){
            float z = Draw.z();
            Draw.z(Layer.turret);
            Draw.rect(tower,b.x,b.y,r.eRotation() -90 );
            Draw.z(z);
        }
    }
}

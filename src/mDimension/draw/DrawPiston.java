package mDimension.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.util.Eachable;
import mDimension.tool.MD_Edge;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

public class DrawPiston extends DrawBlock {
    public String suffix = "-piston";
    public boolean spinIndependent = true;
    public boolean fourPiston = false;
    public Stroke strokeFunction = (b,i)->{return (float)((Math.cos((b.totalProgress()%60)/60*2*Math.PI)-1)/2);};
    public TextureRegion[] regions;

    public interface Stroke{
        float strokeFunction(Building b,int index);
    }

    public DrawPiston(String suffix){
        this.suffix = suffix;
    }

    @Override
    public void load(Block block) {
        if(!fourPiston) {
            regions = new TextureRegion[]{
                    Core.atlas.find(block.name + suffix + 1),
                    Core.atlas.find(block.name + suffix + 2)
            };
        }else{
            regions = new TextureRegion[]{
                    Core.atlas.find(block.name + suffix + 1),
                    Core.atlas.find(block.name + suffix + 2),
                    Core.atlas.find(block.name + suffix + 3),
                    Core.atlas.find(block.name + suffix + 4)
            };
        }
    }

    @Override
    public void draw(Building b) {
        for(int i = 0;i<4;i++){
            Vec2 rotat = MD_Edge.direction(i);
            float process = strokeFunction.strokeFunction(b,i);
            Draw.rect(regions[(i<2 && !fourPiston)?0:(fourPiston?i:1)],b.x+rotat.x*process,b.y+rotat.y*process,fourPiston?0:i*90);
        }
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        for(int i = 0;i<4;i++){
            Draw.rect(regions[(i<2 && !fourPiston)?0:(fourPiston?i:1)],plan.getX(),plan.getY(),i*90);
        }
    }
}

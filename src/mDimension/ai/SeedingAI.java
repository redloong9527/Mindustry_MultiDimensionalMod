package mDimension.ai;

import arc.util.Tmp;
import mDimension.world.blocks.ActiveTransferBlock;
import mDimension.world.blocks.farming.PlanterBlock;
import mDimension.world.blocks.farming.PlanterBlock.PlanterBlockBuild;
import mindustry.entities.units.AIController;
import mindustry.gen.BuildingTetherc;
import mindustry.gen.Call;

public class SeedingAI extends AIController {
    public PlanterBlockBuild boss;
    public float moveRange = 3f,moveBossRange = 8f, moveSmoothing = 20f;

    @Override
    public void updateMovement() {
        if (!(unit instanceof BuildingTetherc tether) || tether.building() == null || !(tether.building() instanceof PlanterBlockBuild)) return;

        boss = (PlanterBlockBuild)tether.building();

        if(boss.isFull || boss.target == null){
            moveTo(boss,moveBossRange,moveSmoothing);
        }else{
            moveTo(Tmp.v2.set(boss.target.worldx(),boss.target.worldy()),moveRange,moveSmoothing);
        }
    }
}

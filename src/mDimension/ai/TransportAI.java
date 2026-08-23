package mDimension.ai;

import arc.util.Nullable;
import mDimension.world.blocks.ActiveTransferBlock.ActiveTransferBlockBuild;
import mindustry.entities.units.AIController;
import mindustry.gen.BuildingTetherc;
import mindustry.gen.Call;
import mindustry.type.Item;

public class TransportAI extends AIController {
    public ActiveTransferBlockBuild target;

    public static float emptyWaitTime = 60f * 2f, dropSpacing = 60f * 1.5f;
    public static float transferRange = 20f, moveRange = 6f, moveSmoothing = 20f;

    public @Nullable Item itemTarget;
    public float noDestTimer = 0f;
    public int minTakeAmount = 10;

    @Override
    public void updateMovement() {
        if (!(unit instanceof BuildingTetherc tether) || tether.building() == null || !(tether.building() instanceof ActiveTransferBlockBuild)) return;

        target = (ActiveTransferBlockBuild)tether.building();

        var core = unit.closestCore();
        if(core == null || target == null)return;

        if(target.items == null) return;

        //empty, approach the loader, even if there's nothing to pick up (units hanging around doing nothing looks bad)
        if(unit.item() == null || !unit.hasItem() || itemTarget == null)itemTarget=getItemType(target);

        if(!unit.hasItem() && itemTarget!=null){
            moveTo(core, moveRange, moveSmoothing);

            //check if ready to pick up
            if(core.items.has(itemTarget,minTakeAmount) && unit.within(core, transferRange)){
                if(retarget()){
                    Call.takeItems(core, itemTarget, Math.min(unit.type.itemCapacity, core.items.get(itemTarget)), unit);
                }
            }
        }else{
            if(target.config != itemTarget || target.isPayload()){
                moveTo(core, moveRange, moveSmoothing);
                if(unit.within(core, transferRange)){
                    if(retarget()){
                        Call.transferItemTo(unit, unit.item(), unit.stack.amount, unit.x, unit.y, core);
                    }
                }
                return;
            }

            moveTo(target, moveRange, moveSmoothing);

            //deposit in bursts, unloading can take a while
            if(unit.within(target, transferRange) && timer.get(timerTarget2, dropSpacing)){
                int max = target.acceptStack(unit.item(), unit.stack.amount, unit);

                //deposit items when it's possible
                if(max > 0){
                    Call.transferItemTo(unit, unit.item(), max, unit.x, unit.y, target);
                }

                //keep the target for at most emptyWaitTime, then we try change if other need.
                if(!unit.hasItem() || (noDestTimer += dropSpacing) >= emptyWaitTime){
                    //oh no, it's out of space - wait for a while, and if nothing changes, try the next destination

                    //next targeting attempt will try the next destination point
                    noDestTimer = 0f;

                    //nothing found at all, clear item
                    if(target == null){
                        unit.clearItem();
                    }
                }
            }
        }
    }

    Item getItemType(ActiveTransferBlockBuild target){
        return target.config;
    }

}

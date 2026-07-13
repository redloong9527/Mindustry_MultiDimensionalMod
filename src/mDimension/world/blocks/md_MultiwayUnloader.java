package mDimension.world.blocks;

import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.world.blocks.distribution.DirectionalUnloader;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.blocks.storage.StorageBlock.*;
import mindustry.world.meta.*;

import java.util.ArrayList;

import static mindustry.Vars.*;

public class md_MultiwayUnloader extends DirectionalUnloader{
    public float speed = 1f;
    public boolean allowCoreUnload = true;
    public int unloaderNumber = 3;

    public md_MultiwayUnloader(String name){
        super(name);
        group = BlockGroup.transportation;
        outputFacing = false;
        update = true;
        hasItems = true;
        configurable = true;
        saveConfig = true;
        rotate = true;
        itemCapacity = 0;
        noUpdateDisabled = true;
        envDisabled = Env.none;
        clearOnDoubleTap = true;
        priority = TargetPriority.transport;

        config(Item.class, (md_MultiwayUnloaderBuild tile, Item item) -> tile.unloadItem = item);
        configClear((md_MultiwayUnloaderBuild tile) -> tile.unloadItem = null);
    }
    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.speed);
        stats.add(Stat.speed, (60f / speed)*unloaderNumber, StatUnit.itemsSecond);
    }
    @Override
    public boolean outputsItems(){
        return true;
    }
    @Override
    public boolean rotatedOutput(int x, int y){
        return false;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        drawPlanConfig(plan, list);
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        drawPlanConfigCenter(plan, plan.config, "duct-unloader-center");
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("items");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, topRegion, arrowRegion};
    }


    public class md_MultiwayUnloaderBuild extends Building{
        public float unloadTimer = 0f;
        public Item unloadItem = null;
        public int offset = 0;
        public int cache = 0;
        public ArrayList<Building> targets = new ArrayList<>();

        @Override
        public void updateTile(){
            if((unloadTimer += edelta()) >= speed){
                Building back  = back();
                targets.clear();
                if(front() !=null) targets.add(front());
                if(left() !=null) targets.add(left());
                if(right() !=null) targets.add(right());


                Building target = null;
                if(!targets.isEmpty() ) {

                    for (int k = 0,j = 0; j < unloaderNumber && k<unloaderNumber*2+1;) {
                        if(targets.size() == 1){
                            cache = 0;
                        }else{
                            cache %= targets.size();
                        }

                        target = targets.get(cache);
                        if (target != null && back != null && back.items != null && target.team == team && back.team == team && back.canUnload() && (allowCoreUnload || !(back instanceof CoreBuild || (back instanceof StorageBuild sb && sb.linkedCore != null)))) {
                            if (unloadItem == null) {
                                var itemseq = content.items();
                                int itemc = itemseq.size;
                                f:
                                {
                                    int h1 = 0;
                                    for (int i = 0; i < itemc; i++) {
                                        Item item = itemseq.get((i + offset) % itemc);
                                        if(target.acceptItem(this, item))h1 = 1;
                                        if (back.items.has(item) && target.acceptItem(this, item)) {
                                           int c = back.items.get(item);
                                            target.handleItem(this, item);
                                            back.items.remove(item, 1);
                                            back.itemTaken(item);
                                            j++;
                                             offset = item.id + 1;
                                            break f;
                                        }
                                    }
                                    k++;
                                }
                            } else{
                                if (back.items.has(unloadItem) && target.acceptItem(this, unloadItem)) {
                                    int c = back.items.get(unloadItem);
                                    target.handleItem(this, unloadItem);
                                    back.items.remove(unloadItem, 1);
                                    back.itemTaken(unloadItem);
                                    j++;
                                }else{
                                    k++;
                                }

                            }
                            cache++;

                        }else{
                            break;
                        }
                    }
                }
                unloadTimer %= speed;
            }
        }
        public int getAccpt(ArrayList<Building> a,Item i){
            int s = 0;
            for(Building b:a){
                if(b.acceptItem(this,i)){
                    s++;
                }
            }
            return s;
        }
        @Override
        public void drawSelect(){
            super.drawSelect();
            drawItemSelection(unloadItem);
        }

        @Override
        public void buildConfiguration(Table table){
            ItemSelection.buildTable(md_MultiwayUnloader.this, table, content.items(), () -> unloadItem, this::configure);
        }

        @Override
        public Item config(){
            return unloadItem;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.s(unloadItem == null ? -1 : unloadItem.id);
            write.s(offset);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            int id = read.s();
            unloadItem = id == -1 ? null : content.items().get(id);
            offset = read.s();
        }
    }
}

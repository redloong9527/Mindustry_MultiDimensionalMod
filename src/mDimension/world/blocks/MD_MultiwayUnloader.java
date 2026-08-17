package mDimension.world.blocks;

import arc.util.io.*;
import mindustry.world.blocks.distribution.DirectionalUnloader;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.blocks.storage.StorageBlock.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class MD_MultiwayUnloader extends DirectionalUnloader{
    public int unloaderNumber = 3;

    public MD_MultiwayUnloader(String name){
        super(name);
        allowCoreUnload = true;
    }
    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.speed);
        stats.add(Stat.speed, (60f / speed)*unloaderNumber, StatUnit.itemsSecond);
    }

    @Override
    public boolean rotatedOutput(int x, int y){
        return false;
    }




    public class md_MultiwayUnloaderBuild extends DirectionalUnloaderBuild{
        public int toffset = 0;
        public Building[] targets = new Building[3];

        @Override
        public void updateTile(){
            targets[0] = front();
            targets[1] = left();
            targets[2] = right();
            if((unloadTimer += edelta()) >= speed){
                int count=0;
                for(int i=0;i<unloaderNumber;){
                    var target = targets[toffset++];
                    if(toffset>=targets.length){
                        toffset=0;
                    }
                    if(unload(target)){
                        i++;
                        count = 0;
                    }else{
                        count++;
                        if(count>=3)break;
                    }
                }

                unloadTimer %= speed;
            }
        }

        @Override
        public void updateProximity() {
            super.updateProximity();
        }

        boolean unload(Building target){
            Building back = back();

            if(target != null && back != null && back.items != null && target.team == team && back.team == team && back.canUnload() && (allowCoreUnload || !(back instanceof CoreBuild || (back instanceof StorageBuild sb && sb.linkedCore != null)))){
                if(unloadItem == null){
                    var itemseq = content.items();
                    int itemc = itemseq.size;
                    for(int i = 0; i < itemc; i++){
                        Item item = itemseq.get((i + offset) % itemc);
                        if(back.items.has(item) && target.acceptItem(this, item)){
                            target.handleItem(this, item);
                            back.items.remove(item, 1);
                            back.itemTaken(item);
                            offset = item.id + 1;
                            return true;
                        }
                    }
                }else if(back.items.has(unloadItem) && target.acceptItem(this, unloadItem)){
                    target.handleItem(this, unloadItem);
                    back.items.remove(unloadItem, 1);
                    back.itemTaken(unloadItem);
                    return true;
                }
            }
            return false;

        }
        @Override
        public void write(Writes write){
            super.write(write);
            write.s(toffset);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            toffset = read.s();
        }
    }
}

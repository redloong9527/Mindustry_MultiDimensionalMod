package mDimension.world.blocks.farming;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.content.MD_UnitTypes;
import mDimension.tool.Debug;
import mDimension.world.data.SeedItem;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.UnitTetherBlock;


import static arc.math.geom.Geometry.d4;
import static mindustry.Vars.*;

public class PlanterBlock extends Block {
    public int regionSize = 7;
    public Effect takeEffect = Fx.itemTransfer;
    public float seedingInterval = 30f;
    public UnitType unitType = MD_UnitTypes.farmer;
    public float unitBuildTime = 5*60f;
    public PlanterBlock(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        rotate = true;
        rotateDraw = false;
    }
    @Override
    public void load() {
        super.load();
        if(itemFilter.length == 0){
            itemFilter = new boolean[content.items().size];
        }
        Vars.content.items().each(i->i instanceof SeedItem,t->itemFilter[t.id]=true);
    }



    public class PlanterBlockBuild extends Building implements UnitTetherBlock {
        public @Nullable Unit unit;
        public int readUnitId = -1;
        public float seedingProgress = 0f;
        public float buildProgress=0;
        public float totalProgress;
        public float warmup, readyness;
        public Crop crop;
        public Tile target;
        public Item consumeItem;
        public float lastTime = 0;
        public boolean isFull;
        public void spawned(int id){
            Fx.spawn.at(x, y);
            buildProgress = 0f;
            if(net.client()){
                readUnitId = id;
            }
        }

        @Override
        public void updateTile() {
            if(unit!=null && (seedingProgress += edelta())>=seedingInterval){
                findCrop();
                if(crop!=null && items.has(crop.requirements)){
                    updateSeed();
                }
            }
            //unit was lost/destroyed
            if(unit != null && (unit.dead || !unit.isAdded())){
                unit = null;
            }

            if(readUnitId != -1){
                unit = Groups.unit.getByID(readUnitId);
                if(unit != null || !net.client()){
                    readUnitId = -1;
                }
            }

            warmup = Mathf.approachDelta(warmup, efficiency, 1f / 60f);
            readyness = Mathf.approachDelta(readyness, unit != null ? 1f : 0f, 1f / 60f);

            if(unit == null && Units.canCreate(team, unitType)){
                buildProgress += edelta() / unitBuildTime;
                totalProgress += edelta();

                if(buildProgress >= 1f){
                    if(!net.client()){
                        unit = unitType.create(team);
                        if(unit instanceof BuildingTetherc bt){
                            bt.building(this);
                        }
                        unit.set(x, y);
                        unit.rotation = 90f;
                        unit.add();
                        Call.unitTetherBlockSpawned(tile, unit.id);
                        consume();
                    }
                }
            }
        }
        public void updateSeed(){
            if (!canSeeding(target) && lastTime+10< Time.time) {
                lastTime = Time.time;
                int d =  regionSize/2 + size/2+1;
                int x = this.tile.x + d4[this.rotation].x *d;
                int y = this.tile.y + d4[this.rotation].y *d;
                int mx =x+regionSize/2,my =y+regionSize/2,ox =x-regionSize/2,oy =y-regionSize/2;
                target = null;
                float dst = -1;
                for(int dy=oy;dy<=my;dy++) {
                    for (int dx = ox; dx <= mx; dx++) {
                        Tile other = world.tile(dx, dy);
                        //Debug.pointTile(dx,dy);
                        if (other == null) continue;
                        float od = Mathf.dst2(other.worldx(), other.worldy(), unit.x, unit.y);
                        if (canSeeding(other) && (od < dst || dst < 0)) {
                            target = other;
                            dst = od;
                        }
                    }
                }
            }
            if(target!=null){
                isFull = false;
                float tx = target.worldx(),ty = target.worldy();
                if(unit.within(tx,ty,5f)){
                    Call.setTile(target,crop, Team.derelict,0);
                    takeEffect.at(x,y,0, Color.white,unit);
                    items.remove(consumeItem,1);
                    seedingProgress=0;
                }
            }else{
                isFull = true;
            }


        }
        void findCrop(){
            crop = null;
            consumeItem = null;
            for(var e:SeedItem.map.entries()){
                if(items.has(e.key)){
                    crop = (Crop)e.value;
                    consumeItem = e.key;
                    return;
                }
            }
        }
        boolean canSeeding(Tile tile){

            return tile!=null && !tile.solid() && !tile.floor().isDeep() && tile.build == null && crop.canPlaceOn(tile,Team.derelict, 0);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(unit == null ? -1 : unit.id);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            readUnitId = read.i();
        }

        @Override
        public void draw() {
            super.draw();
            int d =  regionSize/2 + size/2+1;
            int x = this.tile.x + d4[this.rotation].x *d;
            int y = this.tile.y + d4[this.rotation].y *d;
            Draw.z(Layer.buildBeam);
            Drawf.dashSquare(Pal.accent,x*8,y*8,regionSize*8);
        }
    }


}

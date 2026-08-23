package mDimension.world.blocks;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.content.MD_UnitTypes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.UnitTetherBlock;
import mindustry.world.blocks.units.UnitCargoLoader;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

public class ActiveTransferBlock extends Block {
    public float speed = 60f/20;
    public UnitType unitType = MD_UnitTypes.laborers;
    public float unitBuildTime = 5*60f;
    public Color haloColor = Color.valueOf("ffffff");
    public Poly halo = new Poly(4,1f,5.8f);
    public float haloScl = 1.2f;

    public static class Poly{
        public int side;
        public float radius;
        public float stroke;


        public Poly( int side, float stroke, float radius) {
            this.side = side;
            this.stroke = stroke;
            this.radius = radius;
        }

        public Poly(Poly p) {
            this.stroke = p.stroke;
            this.radius = p.radius;
            this.side = p.side;
        }

        public Poly copy(){return new Poly(this);}
    }

    public TextureRegion content,top;

    @Override
    public void load() {
        super.load();
        content = Core.atlas.find(name+"-content");
        top = Core.atlas.find(name+"-top");
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.itemsMoved,60f/speed, StatUnit.itemsSecond);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("units", (ActiveTransferBlockBuild e) ->
                new Bar(
                        () ->
                                Core.bundle.format("bar.unitcap",
                                        Fonts.getUnicodeStr(unitType.name),
                                        e.team.data().countType(unitType),
                                        unitType.useUnitCap ? Units.getStringCap(e.team) : "∞"
                                ),
                        () -> Pal.power,
                        () -> unitType.useUnitCap ? (float)e.team.data().countType(unitType) / Units.getCap(e.team) : 1f
                ));
    }

    public ActiveTransferBlock(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        itemCapacity = 50;
        ambientSound = Sounds.loopUnitBuilding;

        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        flags = EnumSet.of(BlockFlag.unitCargoUnloadPoint);
        config(Item.class,(ActiveTransferBlockBuild b, Item i)->{
            b.config = i;
        });

        configClear((ActiveTransferBlockBuild b)->{
            b.config = null;
        });
    }

    public class ActiveTransferBlockBuild extends Building implements UnitTetherBlock {
        public Item config = null;
        public float progress = 0;
        public int readUnitId = -1;
        public float buildProgress, totalProgress;
        public float warmup, readyness;
        public @Nullable Unit unit;
        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(ActiveTransferBlock.this, table, Vars.content.items(), () -> config, this::configure, selectionRows, selectionColumns);

        }

        @Override
        public Object config() {
            return config;
        }
        public void spawned(int id){
            Fx.spawn.at(x, y);
            buildProgress = 0f;
            if(net.client()){
                readUnitId = id;
            }
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            return Math.min(itemCapacity - items.total(), amount);
        }
        @Override
        public void updateTile() {
            progress += edelta();
            while (progress>=speed) {
                dump();
                progress -= speed;
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

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(unit == null ? -1 : unit.id);
            write.i(config == null?-1:config.id);

        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            readUnitId = read.i();
            int id = read.i();
            config = id>0?Vars.content.item(id):null;
        }

        @Override
        public void draw() {
            super.draw();
            if(top.found())Draw.rect(top,x,y);
            if(config!=null){
                Draw.color(config.color);
                Draw.rect(content,x,y);
            }
            if(unit == null){
                Draw.draw(Layer.blockOver, () -> {
                    //TODO make sure it looks proper
                    Drawf.construct(this, unitType.fullIcon, 0f, buildProgress, warmup, totalProgress);
                });
            }else{
                float r = (Time.time*0.7f+ this.id * 1145) % 360;
                float r1 = (Time.time*1.1f+ this.id * 1145) % 360;
                Draw.z(Layer.effect);
                Draw.color(haloColor);
                Lines.stroke(halo.stroke);
                Lines.poly(x,y,halo.side,halo.radius + haloScl,r);

                Draw.color(haloColor);
                Lines.stroke(halo.stroke *0.7f);
                Lines.poly(x,y,halo.side,halo.radius - haloScl,r1);
            }
        }
    }
}

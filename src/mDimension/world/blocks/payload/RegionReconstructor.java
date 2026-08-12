package mDimension.world.blocks.payload;

import arc.Core;
import arc.Graphics;
import arc.audio.Sound;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.content.md_Fx;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.entities.EntityGroup;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.units.UnitBlock;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class RegionReconstructor extends UnitBlock {
    public int regionSize = 6;
    public float craftTime = 60f;
    public int capacity = 3;
    public int[] capacities;
    public Color color = null;
    private Rect rect;
    private Rect drawRect;
    private float dst;
    private Unit reast = null;
    public Seq<UnitType[]> upgrades = new Seq<>();

    public Effect
            craftEffect = md_Fx.squareWave(25f,2f,3*tilesize/2f+3f,2f).wrap(Color.valueOf("e8f0ff") , 45f), upgradeInUnitEffect = md_Fx.regionFlashColorInUnit,
    upgradeEffect = md_Fx.squareWave(20f,size * tilesize /2f,(size + regionSize*2)* tilesize,2.5f),
            upgradePosEffect = md_Fx.Line(20f,7f,12f,10f);
    public Sound
            upgradeSound = Sounds.explosionPlasmaSmall,craftSound = Sounds.shootSap;
    public float upgradeMaxPit = 1.3f, upgradeMinPit = 0.9f;
    public float craftMaxPit = 1.2f, craftMinPit = 1f;



    public RegionReconstructor(String name){
        super(name);
        acceptsUnitPayloads = false;
        outputsPayload = false;
        rotate = false;
        commandable = true;
    }

    @Override
    public void setStats(){
        stats.timePeriod = craftTime;
        super.setStats();
        stats.add(Stat.range,regionSize,StatUnit.blocks);

        stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
        stats.add(Stat.output, table -> {
            table.row();
            for(var upgrade : upgrades){
                if(upgrade[0].unlockedNow() && upgrade[1].unlockedNow()){
                    table.table(Styles.grayPanel, t -> {
                        t.left();

                        t.image(upgrade[0].uiIcon).size(40).pad(10f).left().scaling(Scaling.fit).with(i -> StatValues.withTooltip(i, upgrade[0]));
                        t.table(info -> {
                            info.add(upgrade[0].localizedName).left();
                            info.row();
                        }).pad(10).left();
                    }).fill().padTop(5).padBottom(5);

                    table.table(Styles.grayPanel, t -> {

                        t.image(Icon.right).color(Pal.darkishGray).size(40).pad(10f);
                    }).fill().padTop(5).padBottom(5);

                    table.table(Styles.grayPanel, t -> {
                        t.left();

                        t.image(upgrade[1].uiIcon).size(40).pad(10f).right().scaling(Scaling.fit).with(i -> StatValues.withTooltip(i, upgrade[1]));
                        t.table(info -> {
                            info.add(upgrade[1].localizedName).right();
                            info.row();
                        }).pad(10).right();
                    }).fill().padTop(5).padBottom(5);

                    table.row();
                }
            }
        });
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("capacity",be->{
            RegionReconstructorBuild b = (RegionReconstructorBuild)be;
            return new Bar(
                    ()-> Core.bundle.format("bar.capacity",b.amount),
                    ()->Pal.lighterOrange,
                    ()->((float)b.amount/capacity)
            );
        });

        addBar("progress",be->{
            RegionReconstructorBuild b = (RegionReconstructorBuild)be;
            return new Bar(
                    ()-> Core.bundle.format("bar.progress",(int)(b.progress / craftTime *100)),
                    ()-> Color.valueOf("FFA1A1"),
                    ()->(b.progress/craftTime)
            );
        });
    }


    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        drawRect.setCenter(x*tilesize,y*tilesize);
        Drawf.dashRect(valid ? Pal.accent : Pal.remove,drawRect);
    }


    @Override
    public void init() {
        initCapacities();
        super.init();
        drawRect = new Rect(0,0,(size+regionSize*2)*tilesize,(size+regionSize*2)*tilesize);
        rect = new Rect(drawRect).setSize((size+regionSize*2)*tilesize - 5f);
    }

    @Override
    public void afterPatch(){
        initCapacities();
        super.afterPatch();
    }

    public void initCapacities(){
        capacities = new int[Vars.content.items().size];
        itemCapacity = 10;
        ConsumeItems cons = findConsumer(c -> c instanceof ConsumeItems);
        if(cons != null){
            for(ItemStack stack : cons.items){
                capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 2);
                itemCapacity = Math.max(itemCapacity, stack.amount * 2);
            }
        }

        consumeBuilder.each(c -> c.multiplier = b -> state.rules.unitCost(b.team));
    }


    public class RegionReconstructorBuild extends UnitBuild{
        public float upgradeWarmup = 0f;
        public int amount = 0;
        public Interval time = new Interval();
        public @Nullable Vec2 commandPos;
        @Override
        public void updateTile() {
            if(progress<craftTime) {
                if(amount<capacity)progress += edelta() * state.rules.unitBuildSpeed(team);
            }else{
                progress=0;
                consume();
                amount++;
                craftSound.at(x,y,Mathf.randomSeed(id,craftMinPit,craftMaxPit),0.7f,false);
                craftEffect.at(x,y,0,team.color);
            }
            Unit unit = findTarget();
            upgradeWarmup = Mathf.approachDelta(upgradeWarmup,unit !=null && amount >0?1f:0f, delta() * 2.5f/60);
            if(upgradeWarmup > 0.8f && unit!=null && amount >0&& time.get(0,20f)){
                upgrade(unit);
            }
        }

        @Override
        public int getMaximumAccepted(Item item){
            return Mathf.round(capacities[item.id] * state.rules.unitCost(team));
        }

        public void upgrade(Unit u){
            if(!u.isAdded())return;
            float x = u.x;
            float y = u.y;
            float rotate = u.rotation;
            if(!Vars.net.server() && Vars.net.active()){
                upgradeEffect.at(this.x,this.y,color());
                upgradeInUnitEffect.at(x,y,rotate -90,color(),u);
                upgradePosEffect.at(this.x,this.y,0,color(),u);
                upgradeSound.at(x,y,Mathf.randomSeed(id,upgradeMinPit,upgradeMaxPit),1f,false);
                return;
            }
            amount--;

            UnitType type = findType(u.type);
            var e = type.create(team);
            e.x = x;e.y = y;e.rotation = rotate;
            upgradeEffect.at(this.x,this.y,color());
            upgradeInUnitEffect.at(x,y,rotate -90,color(),e);
            upgradePosEffect.at(this.x,this.y,0,color(),e);
            upgradeSound.at(x,y,Mathf.randomSeed(id,upgradeMinPit,upgradeMaxPit),1f,false);
            if(e.isCommandable()){
                if(commandPos != null){
                    e.command().commandPosition(commandPos);
                }else{
                    e.command().commandPosition(getPos(e));
                }
            }
            extendPlayer(u,e);
            u.dead = true;
            u.remove();
            Call.unitDespawn(u);
            //this.handleUnitPayload(u, p->{payload = (UnitPayload)p;});
            e.add();
            Units.notifyUnitSpawn(e);
        }

        @Override
        public void drawPayload() {
        }

        @Override
        public void updatePayload(Unit unitHolder, Building buildingHolder) {
        }

        public void handleUnitPayload(Unit unit, Cons<Payload> grabber) {
            if (unit.isPlayer()) {
                unit.getPlayer().clearUnit();
            }

            unit.remove();
            if (Vars.net.client()) {
                unit.id = EntityGroup.nextId();
            } else {
                Core.app.post(() -> unit.id = EntityGroup.nextId());
            }
            grabber.get(new UnitPayload(unit));
        }



        public void consume(){
            for(var cons:consumers){
                cons.trigger(this);
            }
        }

        public UnitType unit(){
            if(payload == null) return null;

            UnitType t = findType(findTarget().type);
            return t != null && (t.unlockedNowHost() || team.isAI()) ? t : null;
        }
        public Color color(){
            return color == null?team.color: color;
        }
        @Override
        public Vec2 getCommandPosition(){
            return commandPos;
        }

        @Override
        public void onCommand(Vec2 target){
            commandPos = target;
        }

        @Override
        public boolean acceptUnitPayload(Unit unit){
            return false;
        }

        public boolean canSetCommand(){
            var output = unit();
            return output != null && output.commands.size > 1 && output.allowChangeCommands;
        }

        @Override
        public Graphics.Cursor getCursor(){
            return canSetCommand() ? super.getCursor() : Graphics.Cursor.SystemCursor.arrow;
        }

        @Override
        public boolean shouldShowConfigure(Player player){
            return canSetCommand();
        }


        public void extendPlayer(Unit old,Unit now){
            if(old.getPlayer()!=null){
                old.getPlayer().unit(now);
            }
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume() && amount < capacity;
        }

        public Unit findTarget(){
            reast = null;
            dst = -1;
            rect.setCenter(x,y);
            Units.nearby(rect,u->{
                if(
                        canPull(u.type) &&
                        (dst <0 || u.dst2(this) <dst)&&
                        u.team == team &&
                        !u.dead && u.health>0
                ){
                    dst = u.dst2(this);
                    reast = u;
                }
            });

            return reast;
        }


        public Vec2 getPos(Posc pos){
            float ang = Angles.angle(x,y,pos.x(),pos.y());
            float len = (1+0.05f*Mathf.sin((4*ang-90f) * Mathf.degreesToRadians))*(size + regionSize) * tilesize;
            return Tmp.v1.trns(ang,len).add(x,y);
        }

        public boolean canPull(UnitType t){
            for(int i=0;i<upgrades.size;i++){
                if(t == upgrades.get(i)[0]){
                    return  true;
                }
            }
            return false;
        }

        public UnitType findType(UnitType t){
            for(int i=0;i<upgrades.size;i++){
                UnitType u= upgrades.get(i)[0];
                if(t == u && u.unlockedNowHost()){
                    return  upgrades.get(i)[1];
                }
            }
            return null;
        }

        public void draw(){
            super.draw();

            Draw.z(122f);
            Draw.color(color());
            Draw.alpha(Mathf.lerp(0.45f,1f,upgradeWarmup));
            Lines.stroke(2f);
            drawRect.setCenter(x,y);
            Drawf.dashRectBasic(drawRect.x,drawRect.y,drawRect.width,drawRect.height);
            Draw.reset();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(upgradeWarmup);
            write.i(amount);
            TypeIO.writeVecNullable(write, commandPos);
        }
        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            progress = read.f();
            upgradeWarmup = read.f();
            amount = read.i();
            commandPos = TypeIO.readVecNullable(read);
        }
    }

}

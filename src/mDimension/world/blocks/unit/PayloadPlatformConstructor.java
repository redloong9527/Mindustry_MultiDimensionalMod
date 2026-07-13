package mDimension.world.blocks.unit;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.draw.DrawRotation;
import mDimension.draw.DrawSimpleTurret;
import mDimension.draw.ExtraRotation;
import mDimension.tool.Pix;
import mDimension.tool.md_Edge;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
import mindustry.world.meta.*;

public class PayloadPlatformConstructor extends Block {
    /**Use the addUpgrade()*/
    protected Seq<UnlockableContent[]> upgrades = new Seq<>();
    public DrawBlock drawer = new DrawMulti(
            new DrawRegion(),
            new DrawRotation("-side",true),
            new DrawSimpleTurret()
    );

    public float constructTime = 60f;

    public float BeamY = 3.6f;
    public float rotateSpeed = 50/60f;

    public ObjectFloatMap<UnlockableContent> processingSpeedMapping = new ObjectFloatMap<>();
    public PayloadPlatformConstructor(String name){
        super(name);
        update = true;
        sync = true;
        group = BlockGroup.payloads;
        rotateDraw = false;
        rotate = true;
        solid = true;
        outlineIcon = true;
        envEnabled |= Env.space | Env.underwater;
        outlinedIcon = 2;
    }
    public float boostMulti = 0;

    public void addUpgrade(UnlockableContent from, UnlockableContent to,float processingTime){
        upgrades.add(new UnlockableContent[]{from, to});
        processingSpeedMapping.put(to,100/processingTime);
    }


    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void setStats(){
        stats.timePeriod = constructTime;
        super.setStats();

        stats.add(Stat.productionTime, constructTime / 60f, StatUnit.seconds);
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
                            t.add(Strings.autoFixed((100f/processingSpeedMapping.get(upgrade[1],Float.MAX_VALUE)+0.0001f ) /60f, 2) + Core.bundle.get("unit.seconds") , Pal.lightishGray).pad(8f);

                            info.row();
                        }).pad(10).right();
                    }).fill().padTop(5).padBottom(5);

                    table.row();
                }
            }
        });
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        drawer.getRegionsToOutline(this, out);
    }


    public class PayloadPlatformConstructorBuild extends Building implements ExtraRotation {
        public float warmup = 0f;
        public float progress = 0;
        public float totalProgress = 0;
        public float realBeamSize=  0;
        public float towerRotation =0;
        public PayloadPlatform.PayloadPlatformBuild boss = null;

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            towerRotation = rotation * 90f % 360;
            return super.init(tile, team, shouldAdd, rotation);
        }

        public UnlockableContent upgradeType(UnlockableContent type){
            UnlockableContent[] r =  upgrades.find(u -> u[0] == type);
            return r == null ? null : r[1];
        }

        public boolean hasUpgrade(UnlockableContent from,UnlockableContent to){
            UnlockableContent t = upgradeType(from);
            return t!=null && t == to && (t.unlockedNowHost() || team.isAI()) && !to.isBanned() && !from.isBanned();
        }

        public boolean hasUpgrade(UnlockableContent type){
            UnlockableContent t = upgradeType(type);
            return t != null && (t.unlockedNowHost() || team.isAI()) && !type.isBanned();
        }
        float DEBUG_speed = -1;
        public float getSpeed(){
            if(boss == null)return 0;
            UnlockableContent e = boss.targetType;
            float speed = processingSpeedMapping.get(e,0f);
            DEBUG_speed = speed;
            if(e !=null)return speed * edelta() *(1+boostMulti*optionalEfficiency);
            return 0f;
        }
        public Seq<UnlockableContent[]> getUpgrades() {
            return upgrades;
        }

        @Override
        public void updateProximity() {
            super.updateProximity();
            Building other = md_Edge.getAllFacingBuild(this);

            if(other instanceof PayloadPlatform.PayloadPlatformBuild b && other.team == team ){
                boss = b;
                return;
            }
            boss = null;
        }
        @Override
        public void updateTile() {
            if(boss != null){
                float target = Angles.angle(this.x,this.y,boss.x,boss.y) % 360;
                float speed = rotateSpeed *edelta();
                towerRotation  = Angles.moveToward(towerRotation,target,speed);
            }
            if(boss!=null && efficiency >0 && boss.targetType!=null && boss.payload !=null && this.hasUpgrade(boss.payload.content()) && boss.canUpgrade()){
                warmup = Mathf.approachDelta(warmup,1f,1/25f);
                progress+=edelta();
            }else{
                warmup = Mathf.approachDelta(warmup,0,1/25f);
            }
            if(progress > constructTime){
                progress%=constructTime;
                consume();
            }
            totalProgress += Time.delta * warmup;
        }



        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public float eRotation() {
            return towerRotation;
        }

        public void draw() {
            drawer.draw(this);
            Draw.z(Layer.turret);
            if(warmup>0 && boss!=null) {
                float size = boss.getBeamSize();
                if (warmup < 0.1f) realBeamSize = size;
                if (size >= 0) realBeamSize = Mathf.approachDelta(realBeamSize, size, 30f / 60f);
                Draw.color(Pal.accent, warmup);
                Draw.z(Layer.buildBeam);

                Tmp.v1.trns(towerRotation, BeamY);
                Fill.square(x + Tmp.v1.x,y + Tmp.v1.y,1.5f* (1f+Mathf.absin(3f,0.2f)) ,45f);

                Drawf.buildBeam(x + Tmp.v1.x,y + Tmp.v1.y,boss.x,boss.y,realBeamSize);
                Fill.square(boss.x,boss.y,realBeamSize);
            }
        }

        @Override
        public float progress() {
            return progress;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(progress);
            w.f(totalProgress);
        }

        @Override
        public void read(Reads r, byte v) {
            super.read(r, v);
            progress = r.f();
            totalProgress = r.f();
        }
    }


}

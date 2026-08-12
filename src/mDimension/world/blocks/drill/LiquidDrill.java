package mDimension.world.blocks.drill;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Time;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LAccess;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OverlayFloor;
import mindustry.world.blocks.production.Pump;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.*;
import static mindustry.world.meta.StatValues.withTooltip;

public class LiquidDrill extends Block {
    /** Pump amount per tile. */
    public float pumpAmount = 0.2f;
    /** Interval in-between item consumptions, if applicable. */
    public float consumeTime = 60f * 5f;
    public float warmupSpeed = 0.019f;
    public float liquidBoostIntensity = 1.6f;
    public LiquidDrill(String name){
        super(name);
        group = BlockGroup.liquids;
        rotate = true;
        rotateDraw = false;
    }
    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.drillTier,table -> {
            table.row();
            table.table(c -> {
                int i = 0;
                for(Block block : content.blocks()){
                    if(block instanceof OverlayFloor over && over.liquidDrop!=null && over.liquidMultiplier >0) {

                        c.table(Styles.grayPanel, b -> {
                            b.image(block.uiIcon).size(40).pad(10f).left().scaling(Scaling.fit);
                            b.table(info -> {
                                info.left();
                                info.add(block.localizedName).left().row();
                                info.add(over.liquidDrop.emoji()).with(l -> withTooltip(l, over.liquidDrop)).left();
                            }).grow();
                            b.add(Strings.autoFixed(pumpAmount * 60f * over.liquidMultiplier * size * size,2) + StatUnit.perSecond.localized())
                                        .right().pad(10f).padRight(15f).color(Color.lightGray);
                        }).growX().pad(5);
                        if (++i % 2 == 0) c.row();
                    }
                }
            }).growX().colspan(table.getColumns());
        });
        stats.add(Stat.drillSpeed, 60*pumpAmount * size * size, StatUnit.liquidSecond);

        if(liquidBoostIntensity != 1 && findConsumer(f -> f instanceof ConsumeLiquidBase && f.booster) instanceof ConsumeLiquidBase consBase){
            stats.remove(Stat.booster);
            stats.add(Stat.booster,
                    StatValues.speedBoosters("{0}" + StatUnit.timesSpeed.localized(),
                            consBase.amount,
                            liquidBoostIntensity * liquidBoostIntensity, false, consBase::consumes)
            );
        }
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        if(isMultiblock()){
            Liquid last = null;
            for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
                if(other.overlay().liquidDrop == null) continue;
                if(other.overlay().liquidDrop != last && last != null) return false;
                last = other.overlay().liquidDrop;
            }
            return last != null;
        }else{
            return canPump(tile);
        }
    }
    @Override
    public void setBars(){
        super.setBars();

        //replace dynamic output bar with own custom bar
        addLiquidBar((Pump.PumpBuild build) -> build.liquidDrop);
    }

    protected boolean canPump(Tile tile){
        return tile != null && tile.floor().liquidDrop != null;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Tile tile = world.tile(x, y);

        if(valid && tile != null){
            float amount = 0f;
            Liquid liquidDrop = null;

            for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
                if(canPump(other)){
                    if(liquidDrop != null && other.overlay().liquidDrop != liquidDrop){
                        liquidDrop = null;
                        break;
                    }
                    liquidDrop = other.overlay().liquidDrop;
                    amount += other.overlay().liquidMultiplier;
                }
            }

            if(liquidDrop != null){
                float width = drawPlaceText(Core.bundle.formatFloat("bar.pumpspeed", amount * pumpAmount * 60f, 0), x, y, valid);
                float dx = x * tilesize + offset - width/2f - 4f, dy = y * tilesize + offset + size * tilesize / 2f + 5, s = iconSmall / 4f;
                float ratio = (float)liquidDrop.fullIcon.width / liquidDrop.fullIcon.height;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(liquidDrop.fullIcon, dx, dy - 1, s * ratio, s);
                Draw.reset();
                Draw.rect(liquidDrop.fullIcon, dx, dy, s * ratio, s);
            }
        }
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return super.rotatedOutput(x, y);
    }

    public class LiquidDrillBuild extends Building{

        public float warmup, totalProgress;
        public float consTimer;
        public float amount = 0f;
        public @Nullable Liquid liquidDrop = null;
        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.efficiency) return shouldConsume() ? efficiency : 0f;
            if(sensor == LAccess.totalLiquids) return liquidDrop == null ? 0f : liquids.get(liquidDrop);
            return super.sense(sensor);
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            amount = 0f;
            liquidDrop = null;

            for(Tile other : tile.getLinkedTiles(tempTiles)){
                if(canPump(other)){
                    liquidDrop = other.overlay().liquidDrop;
                    amount += other.overlay().liquidMultiplier;
                }
            }
        }

        @Override
        public boolean shouldConsume(){
            return liquidDrop != null && liquids.get(liquidDrop) < liquidCapacity - 0.01f && enabled;
        }

        @Override
        public void updateTile(){
            if(efficiency > 0 && liquidDrop != null){
                float maxPump = Math.min(liquidCapacity - liquids.get(liquidDrop), amount * pumpAmount * edelta());
                liquids.add(liquidDrop, maxPump);

                //does nothing for most pumps, as those do not require items.
                if((consTimer += delta()) >= consumeTime){
                    consume();
                    consTimer %= 1f;
                }

                warmup = Mathf.approachDelta(warmup, maxPump > 0.001f ? 1f : 0f, warmupSpeed);
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            totalProgress += warmup * Time.delta;

            if(liquidDrop != null){
                dumpLiquid(liquidDrop,2f,0);
            }
        }

        @Override
        public void draw() {
            super.draw();
        }

        @Override
        public float warmup(){
            return warmup;
        }

        @Override
        public float progress(){
            return Mathf.clamp(consTimer / consumeTime);
        }

        @Override
        public float totalProgress(){
            return totalProgress;
        }
    }
}

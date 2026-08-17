package mDimension.world.blocks;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mDimension.content.MD_Fx;
import mindustry.content.Fx;
import mindustry.core.UI;
import mindustry.entities.Effect;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.ui.Bar;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.Consume;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

/**这个生产类的输出液体不是连续的，所以输出液体的数量是一次输出的
 * 但是液体消耗还是每帧的量，*/
public class MD_LaunchPadCrafter extends GenericCrafter {
    public Effect launchEffect = Fx.none;
    public Effect landEffect = Fx.none;
    public float baseHoverTime = 60f;
    public float launchTime = 400f;
    public float landTime = 250f;
    public float loadStayTime = 50f;
    public MD_LaunchPadCrafter(String name) {
        super(name);

        if(launchEffect == Fx.none){
            launchEffect = craftEffect = MD_Fx.loadLaunch(400f,this.name+"-pod",17f*8f,0f,1f,1f);
        }
    }
    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.productionTime);
        stats.remove(Stat.output);
        for(var c : consumers){
            stats.remove(Stat.input);
            stats.remove(Stat.booster);
        }
        float TCraftTime = craftTime+baseHoverTime+landTime+launchTime;
        stats.timePeriod = TCraftTime;

        for(var c : consumers){
            c.display(stats);
        }
        if((hasItems && itemCapacity > 0) || outputItems != null){
            stats.add(Stat.productionTime, TCraftTime / 60f, StatUnit.seconds);
        }

        if(outputItems != null){
            stats.add(Stat.output, StatValues.items(TCraftTime, outputItems));
        }

        if(outputLiquids != null){
            stats.add(Stat.output, StatValues.liquids(TCraftTime, outputLiquids));
        }
    }
    @Override
    public void setBars(){
        super.setBars();
        addBar("mining",(md_LaunchPadCrafterBuild e) ->new Bar(
                ()->Core.bundle.format("bar.mining",UI.formatAmount((int)(e.hoverProgress*100))),
                ()->new Color(0.5f,0.65f,1f),
                ()->(!e.isLaunched?0f:e.hoverProgress)));
    }
    public class md_LaunchPadCrafterBuild extends GenericCrafterBuild{
        public float hoverTime = baseHoverTime+landTime+launchTime;
        public boolean isLaunched = false;
        public float hoverProgress = 0f;
        public boolean isRenderLandEffect = true;
        /**0 ~ 1 不会让液体突然出现*/
        public float liquidCache= 0f;
        @Override
        public void updateTile(){
            if (outputLiquids != null&& liquidCache>0) {
                float inc = Math.min(liquidCache,delta()*0.08f);

                for (var output : outputLiquids) {
                    handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                }
                liquidCache-=inc;
            }
            if(!isLaunched) {
                if (efficiency > 0) {

                    progress += getProgressIncrease(craftTime) * Time.delta;
                    warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

//                    continuously output based on efficiency


                    if (wasVisible && Mathf.chanceDelta(updateEffectChance)) {
                        updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
                    }
                } else {
                    warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                }
            }else{
                warmup = 0f;
                progress = 0f;
                hoverProgress+=getHoverProgressIncrease(hoverTime);
            }

            //TODO may look bad, revert to edelta() if so
            totalProgress += warmup * Time.delta;

            if(progress >= 1f){
                consume();
                if(!isLaunched&&wasVisible){
                    launchEffect.at(x,y);
                }
                isLaunched = true;
                MD_Fx.regionFlash.at(x,y,0,Core.atlas.find(name+"-pod"));
                isRenderLandEffect = false;
                progress %= 1f;
            }
            if(hoverProgress >= 1f-((landTime)/hoverTime) && !isRenderLandEffect && wasVisible){
                landEffect.at(x,y);
                isRenderLandEffect = true;
            }
            if(hoverProgress >= 1f){
                if(!isLaunched&&wasVisible){
                    launchEffect.at(x,y);
                }
                isLaunched = false;
                craft();
                hoverProgress %= 1f;
            }

            dumpOutputs();
        }

        @Override
        public void craft(){
            if(outputItems != null){
                for(var output : outputItems){
                    for(int i = 0; i < output.amount; i++){
                        offload(output.item);
                    }
                }
            }
            if(outputLiquids != null){
                liquidCache = 1f;
            }
        }
        @Override
        public void updateConsumption() {
            if (this.block.hasConsumers && !this.cheating()) {
                if (!this.enabled) {
                    this.potentialEfficiency = this.efficiency = this.optionalEfficiency = 0.0F;
                    this.shouldConsumePower = false;
                } else {
                    boolean update = this.shouldConsume() && this.productionValid();
                    float minEfficiency = 1.0F;
                    this.efficiency = this.optionalEfficiency = 1.0F;
                    this.shouldConsumePower = true;

                    for(Consume cons : this.block.nonOptionalConsumers) {
                        float result = cons.efficiency(this);
                        if (cons != this.block.consPower && result <= 1.0E-7F) {
                            this.shouldConsumePower = false;
                        }

                        minEfficiency = Math.min(minEfficiency, result);
                    }

                    for(Consume cons : this.block.optionalConsumers) {
                        this.optionalEfficiency = Math.min(this.optionalEfficiency, cons.efficiency(this));
                    }

                    this.efficiency = minEfficiency;
                    this.optionalEfficiency = Math.min(this.optionalEfficiency, minEfficiency);
                    this.potentialEfficiency = this.efficiency;
                    if (!update) {
                        this.efficiency = this.optionalEfficiency = 0.0F;
                    }

                    this.updateEfficiencyMultiplier();
                    if (!isLaunched && update && this.efficiency > 0.0F) {
                        for(Consume cons : this.block.updateConsumers) {
                            cons.update(this);
                        }
                    }

                }
            } else {
                this.potentialEfficiency = this.enabled && this.productionValid() ? 1.0F : 0.0F;
                this.efficiency = this.optionalEfficiency = this.shouldConsume() ? this.potentialEfficiency : 0.0F;
                this.shouldConsumePower = true;
                this.updateEfficiencyMultiplier();
            }
        }
        public float getHoverProgressIncrease(float baseTime){
            return 1.0F / baseTime * Time.delta;
        }
        @Override
        public void draw(){
            super.draw();
            Draw.z(Layer.blockProp);
            if(!isLaunched){
                TextureRegion loadRegion = Core.atlas.find(name+"-pod");
                Draw.draw(Layer.blockBuilding,()->{
                    Draw.color(Pal.accent, warmup);

                    Shaders.blockbuild.region = loadRegion;
                    Shaders.blockbuild.time = Time.time;
                    Shaders.blockbuild.alpha = warmup;
                    Shaders.blockbuild.progress = (progress+0.51f)/1.5f;

                    Draw.rect(loadRegion, x, y);
                    Draw.flush();
                });
                Draw.reset();
            }
        }

    }

}

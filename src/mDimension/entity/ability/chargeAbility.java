package mDimension.entity.ability;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.world.consumers.ConsumePower;

//你最好把它放到第一个
public class chargeAbility extends Ability {
    public float capacity = 20000;
    public float range = 8*20f;
    public float chargingAmount = 50;
    public float chargingInterval = 30f;
    protected float timer = 0f;

    public Color glowColor = Color.valueOf("ffffe0");
    public String glow =  "-glow";
    public TextureRegion glowRegion;
    public Effect effect;
    public Color effectColor;


    @Override
    public void init(UnitType type) {
        glowRegion = Core.atlas.find(type.name + glow);
    }

    @Override
    public void displayBars(Unit unit, Table bars){
        bars.add(new Bar("stat.power", Pal.accent, () -> data / capacity)).row();
    }


    @Override
    public void update(Unit unit) {
        timer += Time.delta;
        if (data<capacity-0.01f&&timer>chargingInterval) {
            timer = 0;
            Building b = Units.closestBuilding(unit.team,unit.x,unit.y, range, other -> {
                ConsumePower cache = other.block.findConsumer(c-> c instanceof ConsumePower);
                return cache!=null&& cache.buffered&&cache.capacity>0
                        &&other.block.hasPower&& other.power.status>chargingAmount/cache.capacity;
            });

            if(b!=null){
                ConsumePower cons = b.block.findConsumer(c-> c instanceof ConsumePower);
                float amount  = Math.min(
                        capacity-data,
                        chargingAmount
                );
                b.power.status -= amount/cons.capacity;
                effect.at(unit.x,unit.y,0,effectColor,b);
                data+=amount;

            }
        }
    }

    public float efficiency(){
        return data/capacity > 0.99f?1f:data/capacity;
    }

    public void use(float amount){
        data-=amount;
    }

    @Override
    public void draw(Unit unit) {
        if(!glowRegion.found())return;
        Draw.blend(Blending.additive);
        Draw.color(glowColor,data/capacity);
        Draw.rect(glowRegion,unit.x,unit.y,unit.rotation-90);
        Draw.reset();
        Draw.blend();
    }
}

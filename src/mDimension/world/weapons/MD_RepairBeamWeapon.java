package mDimension.world.weapons;

import arc.func.Cons;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.weapons.RepairBeamWeapon;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;
import static arc.Core.*;

public class MD_RepairBeamWeapon extends RepairBeamWeapon {
    public float buildSpeed=0;
    public float buildFraction=0.1f;
    public float unitSpeed=100/60f;
    public float unitFraction=0;
    public boolean targetUnitAct = false;
    public boolean canTargetSelf = false;
    //public float threshold = -1;
    public Cons<Unit> targetUnitCons = u->{};

    public MD_RepairBeamWeapon(){super();}
    public MD_RepairBeamWeapon(String name){super(name);}

    @Override
    public void init(){
        super.init();
        bullet.healPercent = buildSpeed+buildFraction+unitSpeed+unitFraction;
    }

    @Override
    public void addStats(UnitType u, Table w){
        w.row();
        w.add("[lightgray]" + Stat.repairSpeed.localized() + ": " + (mirror ? "2x " : ""));
        w.row();
        String unit = "";
        boolean two = false;
        if(unitSpeed>0){
            unit += (int)(unitSpeed * 60f);
            two = true;
        }
        if(unitFraction>0){
            if(two)unit+=" + ";
            unit += Strings.autoFixed(unitFraction * 100f,2) + StatUnit.percent.localized();
        }
        if(unitFraction>0 || unitSpeed>0){
            unit +=  " "+ StatUnit.perSecond.localized();
        }else unit = bundle.get("stat.notselectable");
        w.add((unitFraction>0 || unitSpeed>0?"[stat]":"[lightgray]")+ bundle.get("stat.unit")+": [white]" + unit).row();
        String build = "";
        two = false;
        if(buildSpeed>0){
            build += (int)(buildSpeed * 60);
            two = true;
        }
        if(buildFraction>0){
            if(two)build+=" + ";
            build += Strings.autoFixed(buildFraction * 100f,2) + StatUnit.percent.localized();
        }
        if(buildFraction>0 || buildSpeed>0){
            build +=  " "+ StatUnit.perSecond.localized();
        }else build = bundle.get("stat.notselectable");
        w.add((buildFraction>0 || buildSpeed>0?"[stat]":"[lightgray]")+ bundle.get("stat.building")+": [white]" + build);
    }

    @Override
    protected Teamc findTarget(Unit unit, float x, float y, float range, boolean air, boolean ground){

        var out = targetUnits ? Units.closest(unit.team, x, y, range, u -> {
            return (u != unit) && u.damaged();
        }) :  null;
        if(out != null || !targetBuildings) return out;
        var build = Units.findAllyTile(unit.team, x, y, range, Building::damaged);
        if(build!=null)return build;
        return canTargetSelf && unit.damaged()?unit:null;
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        super.update(unit, mount);

        float
                weaponRotation = unit.rotation - 90,
                wx = unit.x + Angles.trnsx(weaponRotation, x, y),
                wy = unit.y + Angles.trnsy(weaponRotation, x, y);

        HealBeamMount heal = (HealBeamMount)mount;
        boolean canShoot = mount.shoot;

        if(!autoTarget){
            heal.target = null;
            if(canShoot){
                heal.lastEnd.set(heal.aimX, heal.aimY);

                if(!rotate && !Angles.within(Angles.angle(wx, wy, heal.aimX, heal.aimY), unit.rotation, shootCone)){
                    canShoot = false;
                }
            }

            //limit range
            heal.lastEnd.sub(wx, wy).limit(range()).add(wx, wy);

            if(targetBuildings){
                //snap to closest building
                World.raycastEachWorld(wx, wy, heal.lastEnd.x, heal.lastEnd.y, (x, y) -> {
                    var build = Vars.world.build(x, y);
                    if(build != null && build.team == unit.team && build.damaged()){
                        heal.target = build;
                        heal.lastEnd.set(x * tilesize, y * tilesize);
                        return true;
                    }
                    return false;
                });
            }
            if(targetUnits){
                //TODO does not support healing units manually yet
            }
        }

        heal.strength = Mathf.lerpDelta(heal.strength, Mathf.num(autoTarget ? mount.target != null : canShoot), 0.2f);

        //create heal effect periodically
        if(canShoot && mount.target instanceof Building b && b.damaged() && (heal.effectTimer += Time.delta) >= reload){
            healEffect.at(b.x, b.y, 0f, healColor, b.block);
            heal.effectTimer = 0f;
        }

        if (canShoot) {
            if( mount.target instanceof Building u){
                float baseAmount = buildSpeed * heal.strength * Time.delta + buildFraction * heal.strength * Time.delta * u.maxHealth() / 100f;
                u.heal((u.wasRecentlyDamaged() ? recentDamageMultiplier : 1f) * baseAmount);
            }else if( mount.target instanceof Unit u){
                float baseAmount = unitSpeed * heal.strength * Time.delta + unitFraction * heal.strength * Time.delta * u.maxHealth() / 100f;
                u.heal( baseAmount);
                if(targetUnitAct)targetUnitCons.get(u);
            }
        }
    }
}

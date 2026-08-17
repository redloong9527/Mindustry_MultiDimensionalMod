package mDimension.entity.ability;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.util.Time;
import mDimension.content.MD_Fx;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;

public class SprintAbility extends Ability {
    public float duration = 5f;
    public float reload = 300f;
    public float speed = 5f;

    public ObjectMap<Unit,Float> progressMap = new ObjectMap<>();

    public SprintAbility(){
    }

    @Override
    public void draw(Unit unit) {
        if(progressMap.get(unit)==null || unit.dead)return;
        float fin = Math.min(1f,progressMap.get(unit)/reload);
        float rad = Math.max(unit.type.region.width,unit.type.region.height)/8f;
        Draw.z(160f);
        Draw.color(unit.team.color);
        Lines.stroke(fin*1.2f);
        Lines.circle(unit.x,unit.y,rad);
        Draw.reset();
    }

    @Override
    public void update(Unit unit) {
        if(progressMap.get(unit)==null){
            progressMap.put(unit,0f);
        }
        if(!unit.dead()){
            progressMap.remove(unit);
            return;
        }

        if(unit.isShooting() && reload<0){
            unit.vel.add(new Vec2((float) Math.cos(unit.rotation / 57.29f) * speed, (float) Math.sin(unit.rotation / 57.29f) * speed));
        }else{
            if(unit.isShooting() && progressMap.get(unit)>reload) {

                if (progressMap.get(unit) > reload + duration) {
                    progressMap.put(unit, 0f);
                }
                unit.vel.add(new Vec2((float) Math.cos(unit.rotation / 57.29f) * speed, (float) Math.sin(unit.rotation / 57.29f) * speed));
                MD_Fx.waveColor(30f, unit.hitSize, 3f).at(unit.x, unit.y, unit.team.color);
            }else if(progressMap.get(unit)<=reload){
                progressMap.put(unit,progressMap.get(unit)+ Time.delta);
            }
        }



        if(Time.time%60<Time.delta){
            for(ObjectMap.Entry<Unit,Float> entry:progressMap.entries()){
                if(entry.key.dead){
                    progressMap.remove(entry.key);
                }
            }
        }
    }
}

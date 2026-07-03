package mDimension.entity.bullet;

import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;

import static mindustry.Vars.indexer;

public class CatapultBulletType extends BasicBulletType {

    public float catapultRange = 40f;
    public float catapultProlongLifeTime = 5f;
    public float catapultSpeedUp = 0f;

    public CatapultBulletType(float speed,float damage){
        super(speed,damage);
    }
    public CatapultBulletType(float speed,float damage,String region){
        super(speed,damage,region);
    }



    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        super.hitEntity(b, entity, health);
        if(b.type.pierce){
            Seq<Unit> all = new Seq<>(6);
            Units.nearbyEnemies(b.team, b.x, b.y, catapultRange, h -> {
                if (!b.collided.contains(h.id()) )all.add(h);
            });
            if (all.size == 0) return;
            Unit h = null;
            for(Unit u:all){
                if(h == null) {
                    h = u;
                }else if(Mathf.len2(b.x-h.x(),b.y-h.y()) > Mathf.len2(b.x-u.x(),b.y-u.y())){
                    h = u;
                }

            }
            if(h == null)return;
            float angle = Mathf.angle(h.x() - b.x, h.y() - b.y);
            b.rotation(angle);
            b.vel.trns(b.rotation(),b.vel.len() + catapultSpeedUp);
            b.lifetime+=catapultProlongLifeTime;

        }
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct) {
        super.hitTile(b, build, x, y, initialHealth, direct);
        if(b.type.pierceBuilding){
            Seq<Building> all = new Seq<>(6);
            indexer.eachBlock(b, catapultRange,
                    other ->
                            other.team != b.team &&
                            !b.collided.contains(other.id) &&
                            other.health>0 && !other.dead,
                    all::add
            );

//            if (all.size == 0) return;
            Building h = null;
            for(Building u:all){
                if(h == null) {
                    h = u;
                }else if(Mathf.len2(b.x-h.x(),b.y-h.y()) > Mathf.len2(b.x-u.x(),b.y-u.y())){
                    h = u;
                }

            }
            if(h == null)return;
            float angle = Mathf.angle(h.x() - b.x, h.y() - b.y);
            b.rotation(angle);
            b.vel.trns(b.rotation(),b.vel.len() + catapultSpeedUp);
            b.lifetime+=catapultProlongLifeTime;

        }
    }
}

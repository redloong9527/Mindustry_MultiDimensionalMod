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
    //if not find target,destroy
    public boolean nihilist = false;

    public CatapultBulletType(float speed,float damage){
        super(speed,damage);
    }
    public CatapultBulletType(float speed,float damage,String region){
        super(speed,damage,region);
    }


    public float dst = -1;
    public Unit res = null;
    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        super.hitEntity(b, entity, health);
        if(b.type.pierce){
            dst = -1;
            res = null;
            Units.nearbyEnemies(b.team, b.x, b.y, catapultRange, h -> {
                if (!b.collided.contains(h.id()) && (b.dst2(h) < dst || dst<0)){
                    res = null;
                    dst = b.dst2(h);
                }
            });

            if(res == null){
                if(nihilist)b.remove();
                return;
            }
            float angle = Mathf.angle(res.x() - b.x, res.y() - b.y);
            b.rotation(angle);
            b.vel.trns(b.rotation(),b.vel.len() + catapultSpeedUp);
            b.lifetime+=catapultProlongLifeTime;

        }
    }

}

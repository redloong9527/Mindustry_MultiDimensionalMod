package mDimension.entity.bullet;

import arc.graphics.Color;
import arc.struct.Seq;
import mDimension.content.MD_Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.gen.Posc;
//only use fragBullet
public class RefractedLaserBulletType extends BulletType {
    public RefractedLaserBulletType() {
        super();
        instantDisappear = true;
        collides = false;
        keepVelocity = false;
        pierceCap = 3;
        pierce = true;
        hittable = false;
        absorbable = false;
        fragOnDespawn = false;
        despawnHit = false;
        setDefaults = false;
        speed = 0;
    }
    public float refractedRadius = 8*8f;
    protected float dst=0;
    protected Healthc target = null;
    protected Seq<Healthc> totalTarget = new Seq<>(Healthc.class);
    protected Posc node;
    //need e.data of Healthc[]
    public Effect LaserEffect = MD_Fx.RefractedLaser;
    public Color laserColor = Color.valueOf("FFE791");


    @Override
    public void init(Bullet b) {
        super.init(b);
        totalTarget.clear();

        node = b;
        for(int i=0;i<this.pierceCap;i++) {
            target = null;
            dst = -1;
            Units.nearbyEnemies(b.team,node.x(),node.y(),refractedRadius,u->{
                float udst =u.dst2(node) * (b.collided.contains(u.id)?2:1);
                if(!u.dead && (udst< dst || dst<0) && u!= node){
                    dst = udst;
                    target =  u;
                }
            });
            if(target == null){
                dst = -1;
                Units.nearbyBuildings(node.x(),node.y(),refractedRadius,u->{
                    float udst =u.dst2(node) * (b.collided.contains(u.id)?2:1);;
                    if(b.team !=u.team&& !u.dead && (udst< dst || dst<0) && u!= node){
                        dst = udst;
                        target =  u;
                    }
                });
                if(target == null)return;
            }
            totalTarget.add(target);
            node = target;
            Damage.collidePoint(b, b.team, hitEffect, target.x(),target.y());
        }
        LaserEffect.at(b.x,b.y,b.rotation(),laserColor, totalTarget.toArray());
    }

}

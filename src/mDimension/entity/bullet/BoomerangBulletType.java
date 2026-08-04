package mDimension.entity.bullet;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import mDimension.content.md_Fx;
import mDimension.tool.Debug;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Trail;

import static mindustry.Vars.headless;

public class BoomerangBulletType extends BasicBulletType {
    public float reverseAcc = -1;
    public boolean defaultSetting = true;
    public boolean onlyBackFrag = false;

    public BoomerangBulletType(){
        super();
        shrinkY = shrinkX = 0.1f;
        spin = 17;
    }
    public BoomerangBulletType(float speed, float damage){
        super(speed,damage);
        shrinkY = shrinkX = 0.1f;
        spin = 17;
    }
    public BoomerangBulletType(float speed, float damage,String region){
        super(speed,damage,region);
        shrinkY = shrinkX = 0.1f;
        spin = 17;
    }

    @Override
    public void hit(Bullet b, float x, float y, boolean createFrags) {
        if (onlyBackFrag) {
            float lastDst = Mathf.len(b.lastX-b.originX,b.lastY-b.originY);
            createFrags = Mathf.len(b.x-b.originX,b.y-b.originY) < lastDst;
        }
        super.hit(b, x, y, createFrags);
    }

    @Override
    public void init() {
        super.init();
        if(defaultSetting){
            this.speed*=4;
            reverseAcc = 2.2f*this.speed / 60f;
        }
        if(pierceCap!=-1)pierceCap++;
    }


    @Override
    public void draw(Bullet b) {
        drawTrail(b);
        drawParts(b);
        float shrink = shrinkInterp.apply(b.fout());
        float height = this.height * ((1f - shrinkY) + shrinkY * shrink);
        float width = this.width * ((1f - shrinkX) + shrinkX * shrink);
        float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f) + rotationOffset;

        Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

        Draw.mixcol(mix, mix.a);

        if(backRegion.found()){
            Draw.color(backColor);
            Draw.rect(backRegion, b.x, b.y, width, height, offset);
        }


        Draw.color(frontColor);
        Draw.rect(frontRegion, b.x, b.y, width, height, offset);

        Draw.reset();
    }

    @Override
    public void hit(Bullet b) {
        super.hit(b);
    }

    @Override
    public void update(Bullet b){
        Vec2 face = Tmp.v2.trns(Angles.angle(b.x,b.y,b.originX,b.originY),1f);
        float scl = reverseAcc * Time.delta;
        float lastDst = Mathf.len(b.lastX-b.originX,b.lastY-b.originY);
        boolean isBack = Mathf.len(b.x-b.originX,b.y-b.originY) < lastDst;
        if(isBack && !b.collided.contains(-1)){
            for(int i=0;i<b.collided.size;i++){
                b.collided.set(i,-1);
            }
            //Debug.point(b.x,b.y);
            b.collided.add(-1);
        }
        Tmp.v3.trns(Angles.angle(b.originX,b.originY,b.aimX,b.aimY),b.type.lifetime * b.type.speed /4).add(b.originX,b.originY);

        b.vel.add(face.x * scl,face.y * scl).add(Tmp.v2.set(Tmp.v3.x-b.x,Tmp.v3.y-b.y).nor().scl(0.1f));
        super.update(b);
    }
}

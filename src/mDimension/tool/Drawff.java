package mDimension.tool;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.FloatSeq;
import arc.util.Time;
import arc.util.noise.Simplex;
import mindustry.graphics.Drawf;

public class Drawff {
    public static Rand rand = new Rand();

    public static FloatSeq floatSeq = new FloatSeq();
    public static Vec2 v = new Vec2() , v1 = new Vec2() , v2 = new Vec2();

    public static void prominence(long seed,float x,float y,int amount,float timeScl,float len,float width,float radius,float h){
        rand.setSeed(seed);
        float rad = (float) Math.sqrt(radius*radius-(width*width*0.25f));
        for(int i = 0;i<amount;i++){
            float length = Math.max(0,
                    Mathf.absin(Time.time+rand.range(1145f),timeScl,h)-h+1);
            length*=len * (Mathf.absin(Time.time+rand.range(325),timeScl*Mathf.PI*Mathf.E/4,0.8f)+0.6f);
            float angle = i*(360f/amount);
            v1.set(1,0).setAngle(angle).setLength(rad).add(x,y);
            float vx = v1.x,vy = v1.y;
            Drawf.tri(vx,vy,width,length,angle);
        }
    }

    public static void jellyCircle(long seed,float x,float y,float rad,float timeScl,int sides){
        floatSeq.clear();
        v.set(rad,0);
        rand.setSeed(seed);

        for(int i=0;i<sides;i++){
            float d=360f/sides*i;
            v.trns(d,
                    (cyclicPerlin(d,Time.time/timeScl,seed,0.2f)+1)
                    *rad
            );
            floatSeq.add(v.x +x,v.y +y);
        }

        Fill.poly(floatSeq);
    }

    public static float cyclicPerlin(float degrees, float t, long seed, float scale) {
        float Radians = degrees*Mathf.degreesToRadians;
        float x = Mathf.cos(Radians);
        float y = Mathf.sin(Radians);

        return Simplex.noise2d((int)seed,1.2,0,0.6,x+t,y+t) *scale;

    }

    public static void stick(float x,float y,float angle,float len){
        float dx = Mathf.cosDeg(angle)*len/2;
        float dy = Mathf.sinDeg(angle)*len/2;
        Lines.line(x-dx,y-dy,x+dx,y+dy);
    }

    public static void particleFlow(long seed,float fadingDst,float x,float y,float x1,float y1,int amount,float len,float spread,float timeScl){
        float totalLen = Mathf.len(x1-x,y1-y);
        if(totalLen<0.1f)return;
        float angle = Angles.angle(x,y,x1,y1);
        rand.setSeed(seed);
        v.trns(angle +90f,1);
        for(int i=0;i<amount;i++){
            float wRand = rand.range(0.5f);
            float p = (rand.random(totalLen) + Time.time/timeScl) % totalLen;
            float lenScl = fading(p/totalLen,(fadingDst/totalLen))*(-4*wRand*wRand+1);
            lenScl = Math.max(0,lenScl);
            float cx = x+(x1-x)*p/totalLen;
            float cy = y+(y1-y)*p/totalLen;
            Draw.alpha(lenScl);
            stick(cx+v.x*wRand*spread,cy+v.y*wRand*spread,angle,lenScl*len);
        }
    }

    public static void laserCap(float x,float y,float rad,float rotation,float stroke,int sides){
        laserCap(x,y,rad,rotation,stroke,0,sides);
    };

    public static void laserCap(float x,float y,float rad,float rotation,float stroke,float interval,int sides){
        float srk = stroke/2;
        float d = Mathf.sqrt(rad*rad-srk*srk);
        if(stroke>2*rad-0.1f || interval > d-0.1f){
            Fill.poly(x,y,sides,rad);
            return;
        }
        floatSeq.clear();

        float ang = (float)Math.asin(stroke/2)*Mathf.radiansToDegrees;
        float deltaRotate = (360-ang*2)/sides;
        v.set(interval,srk).rotate(rotation);
        floatSeq.add(v.x +x,v.y +y);
        v.set(d,srk).rotate(rotation);
        floatSeq.add(v.x +x,v.y +y);
        for(int i=0;i<sides;i++){
            v.trns(deltaRotate*i+ang+rotation,rad);
            floatSeq.add(v.x +x,v.y +y);
        }
        v.set(d,-srk).rotate(rotation);
        floatSeq.add(v.x +x,v.y +y);
        v.set(interval,-srk).rotate(rotation);
        floatSeq.add(v.x +x,v.y +y);

        Fill.poly(floatSeq);
    }

    public void polyPointLaser(float x,float y,float x1,float y1,float stroke,float capRad1,float capRad2){
        Lines.stroke(stroke+0.05f);
        Lines.line(x,y,x1,y1,false);
        laserCap(x,y,capRad1,Angles.angle(x,y,x1,y1),stroke,0,24);
        laserCap(x1,y1,capRad2,Angles.angle(x1,y1,x,y),stroke,0,24);
    }

    public static float fslopePow6(float x){
        float f = (2*x-1);
        return -f*f*f*f*f*f+1;
    }

    public static float fading(float x,float fadingDst){
        float h = -2f*Math.abs(x-0.5f)+1;
        h/=(2*fadingDst);
        return Mathf.clamp(h);
    }





}

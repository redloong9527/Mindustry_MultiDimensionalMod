package mDimension.entity.ability;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;

public class AccelerateAbility extends Ability {
    public float level = 1f;
    public float speed = 0.35f/60,Aspeed =0.1f;
    public float angle;
    public float dSpeed = 4f/60;
    public float target;
    public float tolerance = 50f;
    public Effect moveEffect;
    public float moveEffectCharge = 0.4f;
    public Effect maxEffect;
    public Color effectColor;
    public float arrowOffset = 0f;
    public float arrowScl = 1f;
    public int time = 0;
    @Override
    public void update(Unit unit){
        float speed2 = unit.vel().len2();
        if(speed2 > 0.1f){
            target = unit.vel().angle();
            time = 3;
        }else if(time>0){
            time -=1;
        }
        float sp,t;
        if(time<=0 || Math.abs(Angles.angleDist(angle,target)) > tolerance){
            sp = dSpeed;
            t = 0;
        }else {
            sp = speed;
            t = 1;
        }
        data = Mathf.approachDelta(data,t,sp);
        angle = Mathf.slerpDelta(angle,target,Aspeed);
        unit.speedMultiplier*= 1+Math.max(0,level * data);
        if(moveEffect!=null && data > 0.1f && Mathf.chanceDelta(data * moveEffectCharge)){
            moveEffect.at(unit.x,unit.y,effectColor);
            if(maxEffect!=null && data>0.99f){
                maxEffect.at(unit.x,unit.y,effectColor);
            }
        }

    }
    @Override
    public void draw(Unit unit) {
        if(data<0.01f)return;
        float scl = data;
        float z = Draw.z();
        Draw.z(Layer.blockUnder);
        Draw.color(unit.team.color,0.6f);
        Tmp.v1.trns(angle,unit.type.hitSize + arrowOffset) .add(unit.x,unit.y);
        Drawf.tri(Tmp.v1.x,Tmp.v1.y,6+6*scl * arrowScl,scl*2 * arrowScl,angle);
        Drawf.tri(Tmp.v1.x,Tmp.v1.y,2+2*scl * arrowScl,scl*2 * arrowScl,angle+180);
        Draw.reset();
        Draw.z(z);
        //Fonts.outline.draw("v:"+unit.vel().len2() + "\ndt:"+Time.delta,unit.x,unit.y,Color.white,0.2f,false, Align.center);
    }
}

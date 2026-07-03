package mDimension.entity.pattern;

import arc.util.Nullable;
import mindustry.entities.pattern.ShootSine;

public class ShootSwing extends ShootSine {
    public float startRotation = -30;
    public float endRotation = 30;

    @Override
    public void shoot(int totalShots, BulletHandler handler, @Nullable Runnable barrelIncrementer){
        if(shots <=1){
            handler.shoot(0,0,0,firstShotDelay + shotDelay);
            return;
        }
        float dr = (endRotation-startRotation)/(shots-1);
        for(int i = 0; i < shots; i++){
            handler.shoot(0, 0, startRotation+dr*i, firstShotDelay + shotDelay * i);
        }
    }
}

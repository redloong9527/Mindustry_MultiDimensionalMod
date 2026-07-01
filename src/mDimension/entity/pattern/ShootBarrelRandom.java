package mDimension.entity.pattern;

import arc.math.Mathf;
import arc.util.Nullable;
import mindustry.entities.pattern.ShootBarrel;

public class ShootBarrelRandom extends ShootBarrel {
    public static long visited = 0;
    public boolean noRepeat = true;
    @Override
    public void shoot(int totalShots, BulletHandler handler, @Nullable Runnable barrelIncrementer){
        visited =0;
        for(int i = 0; i < shots; i++){
            int index = getIndex(i);
            handler.shoot(barrels[index], barrels[index + 1], barrels[index + 2], firstShotDelay + shotDelay * i);
            if(barrelIncrementer != null) barrelIncrementer.run();
        }
    }

    int getIndex(int j){
        if(noRepeat){
            int index = Mathf.random(barrels.length / 3);
            for(int i=0;i<barrels.length / 3;i++){
                if((visited & 1L<<index) != 0){
                    index++;
                }else break;
            }
            visited |= 1L<<index;
            return index % (barrels.length / 3) *3;
        }else return Mathf.random(barrels.length / 3) * 3;
    }
}

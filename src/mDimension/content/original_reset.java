package mDimension.content;
import arc.graphics.Color;
import mDimension.entity.bullet.BoomerangBulletType;
import mindustry.content.*;
import mindustry.entities.bullet.LiquidBulletType;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LiquidTurret;

import static mDimension.content.md_blocks.modname;

public class original_reset {
    public static void load() {
        LiquidTurret tsunami = (LiquidTurret) Blocks.tsunami;
        tsunami.ammoTypes.put(
                md_liquids.dimension_fluid,new LiquidBulletType(md_liquids.dimension_fluid){{
                    lifetime = 50f;
                    speed = 6f;
                    knockback = 1.3f;
                    puddleSize = 8f;
                    orbSize = 4f;
                    drag = 0.001f;
                    ammoMultiplier = 0.4f;
                    statusDuration = 60f * 4f;
                    damage = 10f;
                    rangeChange = 95f;
                    statusDuration = 600f;
                    trailEffect = Fx.trailFade;
                    }
                }
        );

//        var duo = (ItemTurret)Blocks.duo;
//        duo.inaccuracy = 5f;
//        duo.ammoTypes.put(
//                Items.sand,new BoomerangBulletType(3,30,modname+"saw-disc-bullet"){{
//                    lifetime = 60f;
//                    width = 20;
//                    height = 20;
//                    hitSize = 8f;
//                    pierce = true;
//                    onlyBackFrag = true;
//                    fragOnDespawn = false;
//                    fragBullet = UnitTypes.quad.weapons.get(0).bullet;
//                    pierceCap = 2;
//                    trailInterval = 2f;
//                    trailEffect = md_Fx.circleLineTrail.wrap(trailColor.cpy().lerp(Color.white,0.4f),4.5f);
//                }}
//        );

    }
}

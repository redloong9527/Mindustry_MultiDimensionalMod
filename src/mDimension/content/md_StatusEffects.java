package mDimension.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import mDimension.world.blocks.md_complexStatusEffect;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;

public class md_StatusEffects {
    public static StatusEffect
            dimension_slip,cracking,move_out,embrittlement,explore,bless,coordination;
    public static void load(){
        coordination = new StatusEffect("coordination"){{
            damageMultiplier = 1.2f;
            reloadMultiplier = 1.2f;
            speedMultiplier = 1.2f;
        }};
        dimension_slip = new StatusEffect("dimension-slip"){{
            color = Color.valueOf("ffffff");
            healthMultiplier = 0.65f;
            damageMultiplier = 0.8f;
            speedMultiplier = 1.1f;
            damage = 4f;
            effect = md_Fx.dimension_vapor;
            effectChance = 0.03f;
        }};

        cracking = new md_complexStatusEffect("cracking"){{
            percentageShieldDamage = 0.3f/60f;
            damage = 100f/60f;
            color = Color.valueOf("ffffff");

            draw = e->{
                if(e.shield>0) {
                    float size = Math.min(1f,e.shield/(e.maxHealth*0.25f));
                    Draw.z(Layer.buildBeam);
                    Draw.color(Color.valueOf("ffe299"));
                    for (int i = 0; i < 4; i++) {
                        Drawf.tri(e.x, e.y, e.hitSize * 0.25f, e.hitSize * 1.3f*size, i * 90f + 45f);
                    }
                    Lines.stroke(e.hitSize / 8);
                    Lines.circle(e.x, e.y, e.hitSize*size);
                    Draw.reset();
                    if(Time.time%18f<Time.delta&&!Vars.state.isPaused()) {
                        md_Fx.polygonalStar(50f, 3,Color.valueOf("ffe299"), e.hitSize * 0.6f, e.hitSize * 0.15f,0)
                                .at(e.x + Mathf.range(e.bounds() / 3f), e.y + Mathf.range(e.bounds() / 3f),Mathf.range(120f));
                    }
                }
            };
        }};

        move_out = new md_complexStatusEffect("move-out"){{
            color = Color.valueOf("ffffff");
            percentageDamage = -0.02f/60f;
            armorMultiplier = 0.5f;
            act = e->{
                if(e.health<e.maxHealth){
                    e.health = e.maxHealth;
                }
            };
            draw = e->{
                Draw.z(Layer.buildBeam);
                Draw.color(e.team.color);
                Draw.rect(e.type.fullIcon,e.x,e.y,e.type.fullIcon.width*0.3f,e.type.fullIcon.height*0.3f,e.rotation-90f);
                Draw.reset();
            };
            init(() -> {
                opposite(dimension_slip);

                trans(dimension_slip,(unit,s,time)->{
                    md_Fx.regionFlash.at(unit.x,unit.y,unit.rotation-90f,unit.type.fullIcon);
                    s.time = 0;
                    s.effect = dimension_slip;
                });
            });
        }};

        embrittlement = new md_complexStatusEffect("embrittlement"){{

            armorMultiplier = 0.5f;
            armorAdditional = -5;

            healthMultiplier = 0.7f;
            transitionDamage = 50;
            init(()-> {
                affinity(StatusEffects.blasted, (unit, result, time) -> {
                    unit.damagePierce(transitionDamage);
                    md_Fx.polyWave(4,unit.bounds()*0.15f,0f,4f,25f,Color.white,0.7f).at(unit.x + Mathf.range(unit.bounds() / 3f), unit.y + Mathf.range(unit.bounds() / 3f));
                    result.set(embrittlement, Math.max(result.time, 600f));
                });
            });

        }};
        explore = new md_complexStatusEffect("explore"){{
            color = Color.valueOf("ffffff");
            intervalDamageTime = 60f;
            intervalDamagePierce = true;
            intervalDamage = 500f;
            percentageDamage = 0.1f/60f;
            percentageShieldDamage = 0.02f/60f;
            act = e->{
                if(Time.time%(intervalDamageTime/4)<Time.delta&&!Vars.state.isPaused()) {
                    md_Fx.Mulitpleslash(40f, 1, new Color(1f, 0.85f, 0.87f), e.hitSize * 2.5f, 4f, e.hitSize).at(e.x, e.y);
                }
            };
        }};

        bless = new md_complexStatusEffect("bless"){{
            color = Color.valueOf("ffffff");
            percentageDamage = -0.05f/60f;
            damage = -50/60f;
            damageMultiplier = 1.15f;
            healthMultiplier = 1.2f;
            speedMultiplier = 1.1f;
            reloadMultiplier = 1.1f;
            armorAdditional = 3;
            armorMultiplier = 1.15f;
            effectChance = 0.10f;
            act = e->{
                effect = md_Fx.polygonalStar(40f,2,Color.valueOf("EDFFE0"),0.8f*e.hitSize,5f,90f);
            };
        }};


    }
}

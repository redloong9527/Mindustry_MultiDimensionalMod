package mDimension.tool;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Align;
import mDimension.content.md_Fx;
import mindustry.entities.Effect;
import mindustry.gen.Posc;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;

public class Debug {
    public static void string(String s,float life,float x,float y){
        log(life).at(x,y,0, Color.white,s);
    }

    public static void string(String s, float life, Posc posc){
        log(life).at(posc.x(),posc.y(),0, Color.white,s);
    }

    public static void cry(float size ,float x,float y,Color color){
        md_Fx.polyWave(4,15f*size, Mathf.range(10f),2,20f, color,1f).at(x,y);
    }

    public static void cry(float size,Posc posc){
        cry(size,posc.x(),posc.y(),Pal.placing);
    }

    public static void cry(Posc posc){
        cry(1f,posc.x(),posc.y(),Pal.placing);
    }
    public static void cry(Posc posc,Color color){
        cry(1f,posc.x(),posc.y(),color);
    }
    public static void cry(float size,Posc posc,Color color){
        cry(size,posc.x(),posc.y(),color);
    }


    public static Effect log(float life){
        return new Effect(life,e->{
            if (e.data instanceof String s) {
                Fonts.outline.draw(s,e.x,e.y,e.color,0.3f,false, Align.center);
            }
        }).layer(200);
    }
}

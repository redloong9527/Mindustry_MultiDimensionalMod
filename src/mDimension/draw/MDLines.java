package mDimension.draw;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;

public class MDLines extends Lines {
    public static void line2(float x,float y,float x2,float y2){
        line2(Core.atlas.white(),x,y, Draw.getColorPacked(),x2,y2,(Draw.getColor().a(0)).toFloatBits(),false);
    }
    public static void line2(float x, float y, Color c, float x2, float y2, Color c2){
        line2(Core.atlas.white(), x, y, c.toFloatBits(), x2, y2, c2.toFloatBits(), false);
    }
    public static void line2(TextureRegion region, float x, float y, float color1, float x2, float y2, float color2, boolean cap){
        float hstroke = getStroke()/2f;
        float len = Mathf.len(x2 - x, y2 - y);
        float diffx = (x2 - x) / len * hstroke, diffy = (y2 - y) / len * hstroke;

        if(cap){
            Fill.quad(
                    region,

                    x - diffx - diffy,
                    y - diffy + diffx,
                    color1,

                    x - diffx + diffy,
                    y - diffy - diffx,
                    color1,

                    x2 + diffx + diffy,
                    y2 + diffy - diffx,
                    color2,

                    x2 + diffx - diffy,
                    y2 + diffy + diffx,
                    color2
            );
        }else{
            Fill.quad(
                    region,

                    x - diffy,
                    y + diffx,
                    color1,

                    x + diffy,
                    y - diffx,
                    color1,

                    x2 + diffy,
                    y2 - diffx,
                    color2,

                    x2 - diffy,
                    y2 + diffx,
                    color2

            );
        }

    }
}

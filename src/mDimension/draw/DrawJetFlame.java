package mDimension.draw;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.world.draw.DrawBlock;

public class DrawJetFlame extends DrawBlock {
    public float beginLen= 3f;
    public float len = 10f;
    public float lenScl = 3f;
    public float alphaScl = 5f;
    public float lenMul = 2f;
    public float alphaMul=0.15f;
    public float alpha = 0.85f;
    public Color colorOut = Color.valueOf("FFE6BF");
    public Color colorIn = Color.white;
    public float inMulti = 0.6f;
    public float stroke = 5.5f;

    public int amount =4;
    public float rotate = 45f;

    @Override
    public void draw(Building b) {
        if(b.warmup() < 0.01f)return;
        float x = b.x,y = b.y;
        Draw.blend(Blending.additive);
        Draw.alpha(Mathf.absin(alphaScl,alphaMul) + alpha);
        float realLen = (Mathf.absin(lenScl,lenMul) + len)* b.warmup();
        for(int i=0;i<amount;i++){
            float angle = i*(360f/amount) + rotate;
            Tmp.v1.trns(angle,beginLen);
            Tmp.v2.trns(angle,realLen);
            for(int o:Mathf.zeroOne){
                Lines.stroke(stroke * (o == 0?1f:inMulti));
                MDLines.line2(x+Tmp.v1.x,y+Tmp.v1.y,(o == 0?colorOut:colorIn),
                        x+Tmp.v2.x,y+Tmp.v2.y,Color.clear
                );
            }
        }
        Draw.blend();
        Draw.reset();
    }
}

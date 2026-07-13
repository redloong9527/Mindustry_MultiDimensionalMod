package mDimension.tool;
import arc.Core;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.Color;
import arc.util.*;
import mindustry.graphics.*;

public class Pix {

    public static TextureRegion makeOutline(TextureRegion region, Color outlineColor){
        // 从 Atlas 取出原始 Pixmap（region 必须是 AtlasRegion 才能这样取）
        PixmapRegion pix = Core.atlas.getPixmap(region);

        // 创建一张更大的图，留出描边边距（通常 1~2px）
        int border = 3;
        Pixmap out = new Pixmap(pix.width + border*2, pix.height + border*2);

        // 先把原图居中画上去
        out.draw(pix.pixmap, pix.x, pix.y, pix.width, pix.height, border, border);

        // 描边：遍历原图有效像素，在四周填充 outlineColor
        for(int x = 0; x < pix.width; x++){
            for(int y = 0; y < pix.height; y++){
                int px = pix.x + x;
                int py = pix.y + y;
                if(pix.pixmap.getA(px, py) > 128){ // 原图不透明
                    // 对四周 4 个方向填充
                    for(int dx = -border; dx <= border; dx++){
                        for(int dy = -border; dy <= border; dy++){
                            if(dx == 0 && dy == 0) continue;
                            int ox = border + x + dx;
                            int oy = border + y + dy;
                            // 只填充当前透明的地方，避免覆盖原图
                            if((dx*dx+dy*dy <= 9)&&out.getA(ox, oy) < 128){
                                out.setRaw(ox, oy, outlineColor.argb8888());
                            }
                        }
                    }
                }
            }
        }

        // 生成新纹理（注意：实际 Mod 中应通过 MultiPacker 入图集，而不是直接 new Texture）
        // 这里仅演示返回 TextureRegion 的思路
        Texture tex = new Texture(out);
        return new TextureRegion(tex, border, border, pix.width, pix.height);
    }
}

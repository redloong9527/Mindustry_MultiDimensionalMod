package mDimension.ui;

import arc.graphics.g2d.TextureAtlas;
import mDimension.MDimensionMod;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.ctype.ContentType.*;
import mindustry.ctype.UnlockableContent;
import mindustry.ui.Fonts;

import static mDimension.MDimensionMod.MODNAME;
//这好像没啥用
public class MD_CreateEmoji {
    public static int nextEmojiCode = 0xE000 + 2048;
    public static ContentType[] allType = {
            ContentType.item,ContentType.block,ContentType.liquid,ContentType.unit,ContentType.status,ContentType.error
    };

    public static void create(){
        for(var type:allType){
            for(var content : Vars.content.getBy(type)){
                if(content.minfo.mod == null
                || !content.minfo.mod.name.equals(MODNAME)
                || !(content instanceof UnlockableContent u)
                || (u.uiIcon == null || !u.uiIcon.found())
                )continue;
                int code = nextEmojiCode ++;
                String name = u.name;
                Fonts.registerIcon(u.name, u.uiIcon instanceof TextureAtlas.AtlasRegion atlas ? atlas.name : u.name, code, u.uiIcon);
            }
        }
    }
}

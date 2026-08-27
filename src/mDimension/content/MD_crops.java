package mDimension.content;

import arc.graphics.Color;
import mDimension.world.blocks.farming.Crop;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class MD_crops {
    public static Block crop;

    public static void load(){
        crop = new Crop("crop",MD_Items.seed1){{
            mapColor = Color.valueOf("917F50");
            outputItems = ItemStack.with(MD_Items.bauxite,5);
        }};
    }
}

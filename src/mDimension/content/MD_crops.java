package mDimension.content;

import arc.graphics.Color;
import mDimension.world.blocks.farming.Crop;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class MD_crops {
    public static Block xenogum_tree;

    public static void load(){
        xenogum_tree = new Crop("xenogum-tree",MD_Items.colloid_fruit){{
            mapColor = Color.valueOf("917F50");
            growthTime = 90*60f;
            outputItems = ItemStack.with(MD_Items.colloid_fruit,3);
            requirements = ItemStack.with(MD_Items.colloid_fruit,2);
        }};
    }
}

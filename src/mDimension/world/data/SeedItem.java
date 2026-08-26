package mDimension.world.data;

import arc.func.Prov;
import arc.graphics.Color;
import arc.struct.ObjectMap;
import mDimension.world.blocks.farming.Crop;
import mindustry.type.Item;
import mindustry.world.Block;

public class SeedItem extends Item {
    public static ObjectMap<Item,Block> map = new ObjectMap<>();
    public SeedItem(String name, Color color) {
        super(name, color);
    }
}

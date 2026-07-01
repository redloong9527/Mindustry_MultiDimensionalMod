package mDimension.world.blocks.drill;

import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.production.BurstDrill;

public class BurstDrill_Pro extends BurstDrill {
    public float dominantItemsMulti = 1f;
    public BurstDrill_Pro(String name){
        super(name);
    }

    @Override
    protected void countOre(Tile tile){
        returnItem = null;
        returnCount = 0;

        oreCount.clear();
        itemArray.clear();

        for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
            if(canMine(other)){
                oreCount.increment(getDrop(other), 0, 1);
            }
        }

        for(Item item : oreCount.keys()){
            itemArray.add(item);
        }

        itemArray.sort((item1, item2) -> {
            int type = Boolean.compare(!item1.lowPriority, !item2.lowPriority);
            if(type != 0) return type;
            int amounts = Integer.compare(oreCount.get(item1, 0), oreCount.get(item2, 0));
            if(amounts != 0) return amounts;
            return Integer.compare(item1.id, item2.id);
        });

        if(itemArray.size == 0){
            return;
        }

        returnItem = itemArray.peek();
        returnCount = Math.round(oreCount.get(itemArray.peek(), 0)*dominantItemsMulti);
    }

    public class BurstDrill_ProBuilding extends BurstDrillBuild{
    }
}

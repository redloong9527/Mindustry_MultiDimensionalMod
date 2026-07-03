package mDimension.entity.comp;

import annotations.Annotations;
import mindustry.gen.Entityc;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;


@Annotations.EntityDef({Entityc.class})
public abstract class CollectorEntityComp implements Entityc{
    public ItemStack itemStack;
    public LiquidStack liquidStack;

}

package mDimension.world.blocks.environment;

import arc.struct.Seq;
import mindustry.content.Liquids;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.environment.Floor;

public class CrustFloor extends Floor {
    public CrustFloor(String name){
        super(name);
    }

    public Stratum[] strata = new Stratum[]{};
    public Stratum[] with(Stratum... stratum){
        return stratum;
    };


    public static class Stratum{
        public static int itemCount;
        public static int liquidCount;
        public static final Seq<Stratum> temp = new Seq<>(false,2,Stratum.class);
        public ItemStack itemStack = new ItemStack();
        public LiquidStack liquidStack = new LiquidStack(Liquids.water,0);
        public int startLayer = 0;
        public int endLayer = 1;
        public int hardness = 0;

        public Stratum(){}
        public Stratum(int start,int end,ItemStack itemStack,LiquidStack liquidStack){
            this.endLayer = end;
            this.startLayer = start;
            this.liquidStack = liquidStack;
            this.itemStack = itemStack;
        }
        public Stratum(int start,int end,Item item,int itemAmount){
            this(start,end,new ItemStack(item,itemAmount),null);
        }
        public Stratum(int start, int end, Liquid liquid, float liquidAmount){
            this(start,end,null,new LiquidStack(liquid,liquidAmount));
        }
        public Stratum(int start, int end,Item item,int itemAmount, Liquid liquid, float liquidAmount){
            this(start,end,new ItemStack(item,itemAmount),new LiquidStack(liquid,liquidAmount));
        }


        public static Stratum[] find(int layer,Stratum[] strata){
            temp.clear();
            itemCount = liquidCount = 0;
            for(Stratum s:strata){
                if(layer<= s.endLayer && layer>=s.startLayer){
                    temp.add(s);
                    if(s.itemStack!=null && s.itemStack.amount>0){
                        itemCount++;
                    }
                    if(s.liquidStack!=null && s.liquidStack.amount>0){
                        liquidCount++;
                    }
                }
            }
            return temp.toArray(Stratum.class);
        }

        @Override
        public String toString() {
            return "Stratum[start:"+startLayer+",end:"+endLayer+"]item:["+itemStack+"]liquid:["+liquidStack+"]";
        }
    }
}

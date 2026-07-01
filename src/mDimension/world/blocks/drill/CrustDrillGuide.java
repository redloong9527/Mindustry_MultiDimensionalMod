package mDimension.world.blocks.drill;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.tool.md_Edge;
import mDimension.world.blocks.environment.CrustFloor;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.blocks.ItemSelection;

import static mindustry.Vars.content;


public class CrustDrillGuide extends CrustDrillBooster {
    public CrustDrillGuide(String name) {
        super(name);
        isGuide = true;
        configurable = true;
        config(Item.class, (CrustDrillGuideBuild b, Item u) -> {
            b.config = u;
        });
        config(Liquid.class, (CrustDrillGuideBuild b, Liquid u) -> {
            b.config = u;
        });
        configClear((CrustDrillGuideBuild b) -> b.config = null);
    }


    public class CrustDrillGuideBuild extends CrustDrillBoosterBuild {
        Seq<UnlockableContent> configs = new Seq<>();
        public int getGuide(){
            int res = -1;
            float count=0;
            if(md_Edge.getAllFacingBuild(this) instanceof CrustDrill.CrustDrillBuild boss){
                for(CrustFloor.Stratum s:boss.floor.strata){
                    float amount=0;
                    if((s.liquidStack!=null && s.liquidStack.liquid == config) || (s.itemStack!=null && s.itemStack.item == config)){
                        amount = Math.max(s.liquidStack!=null?s.liquidStack.amount:0,s.itemStack!=null?s.itemStack.amount:0);
                        if(amount > count){
                            res = s.endLayer;
                            count = amount;
                        }
                    }
                }
            }
            return res;
        }
        public UnlockableContent config;
        @Override
        public Object config() {
            return config;
        }

        @Override
        public void draw() {
            super.draw();
            Color color = Color.white;
            if(config instanceof Item i){
                color = i.color;
            }
            if(config instanceof Liquid i){
                color = i.color;
            }
            Draw.color(color);
            Draw.rect("cat",x,y,10,10);
        }

        @Override
        public void buildConfiguration(Table table) {
            Building b =  md_Edge.getAllFacingBuild(this);
            if (b == null) return;
            if (b instanceof CrustDrill.CrustDrillBuild boss) {
                configs.clear();

                for (int i = 0; i < boss.floor.strata.length; i++) {
                    var e = boss.floor.strata[i];
                    if (e.itemStack != null && e.itemStack.amount > 0) configs.addUnique(e.itemStack.item);
                    if (e.liquidStack != null && e.liquidStack.amount > 0) configs.addUnique(e.liquidStack.liquid);
                }
                configs.sort((a, c) -> {
                    boolean aL = a instanceof Liquid;
                    boolean bL = c instanceof Liquid;
                    if (aL != bL) {
                        return Boolean.compare(aL, bL);
                    }
                    return Integer.compare(a.id, c.id);
                });
                ItemSelection.buildTable(block, table, configs,
                        () -> config, this::configure, false
                );
            }

        }

        @Override
        public void write(Writes w) {
            super.write(w);
            w.s(
                    config == null?0:config instanceof Item?1:config instanceof Liquid?2:0
            );
            w.s(config == null?-1:config instanceof Item item? item.id:config instanceof Liquid liquid? liquid.id:-1);
        }

        @Override
        public void read(Reads r, byte v) {
            super.read(r, v);
            int s = r.s();
            int id = r.s();
            if (id !=-1) {
                if(s == 1 ){
                    config = content.item(id);
                }else if(s == 2){
                    config = content.liquid(id);
                }
            }
        }
    }
}

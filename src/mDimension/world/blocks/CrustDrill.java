package mDimension.world.blocks;

import arc.func.Floatf;
import arc.func.Func;
import arc.func.Intc;
import arc.func.Intf;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.IntSeq;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.world.Block;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

import static mindustry.Vars.world;

public class CrustDrill extends Block {
    public CrustDrill(String name){
        super(name);
        size = 4;
        update = true;
        solid = true;
        group = BlockGroup.drills;
        hasLiquids = true;
        hasItems = true;
        ambientSound = Sounds.loopDrill;
        ambientSoundVolume = 0.2f;
        //drills work in space I guess
        envEnabled |= Env.space;
        flags = EnumSet.of(BlockFlag.drill);
        configurable = true;
        config(UnlockableContent.class,(CrustDrillBuild b,UnlockableContent u)->{
            b.config = u;
        });
        configClear((CrustDrillBuild b)->b.config = null);
    }
    public float warmupSpeed = 0.012f;

    public float drillingSpeed = 1f/60f;
    public Floatf<Integer> increase= layer ->{
        return 1 + layer*layer*0.2f;
    };
    public int basicLayer = 1;
    public int maxLinks = 3;
    public float boostMulti = 0.5f;

    public class CrustDrillBuild extends Building{
        public UnlockableContent config;
        public int layer = 0;
        public float progress = 0,warmup;
        public float totalProgress=0;
        public IntSeq links = new IntSeq();

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        @Override
        public void updateTile() {
            if(efficiency >0){
                warmup = Mathf.approachDelta(warmup, 1, warmupSpeed);
                int maxLayer = getMaxLayer();
                if (layer<maxLayer) {
                    progress+= drillingSpeed * edelta() * (1+boostMulti * optionalEfficiency);
                    if(progress > increase.get(layer)){
                        progress = 0;
                        layer++;
                    };
                }
            }else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }
        }

        public int getMaxLayer(){
            int[] items = links.items;
            int res = basicLayer;
            for(int i=0;i<items.length;i++){
                int pos = items[i];
                Building other = world.build(pos);
                if(i<maxLinks-1 && other instanceof CrustDrillBooster.CrustDrillBoosterBuild boost && other.team == this.team && !other.dead){
                    res+=boost.boostLayers();
                }else{
                    links.removeValue(pos);
                }
            }

            return res;
        }
    }
}


package mDimension.world.blocks;

import arc.struct.EnumSet;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.world.Block;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

public class CrustDrillBooster extends Block {
    public int boostLayers = 2;
    public CrustDrillBooster(String name){
        super(name);
        size = 2;
        update = true;
        solid = true;
        group = BlockGroup.drills;
        rotate = true;
        rotateDraw = false;
        hasLiquids = true;
        hasItems = true;
        ambientSound = Sounds.loopDrill;
        ambientSoundVolume = 0.2f;
        //drills work in space I guess
        envEnabled |= Env.space;
        flags = EnumSet.of(BlockFlag.drill);
    }

    public class CrustDrillBoosterBuild extends Building{
        public int boostLayers(){
            return boostLayers;
        }
    }
}

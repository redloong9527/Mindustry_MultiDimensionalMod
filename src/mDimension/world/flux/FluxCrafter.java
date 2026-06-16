package mDimension.world.flux;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.consumers.ConsumeFlux;
import mDimension.consumers.modules.FluxModule;
import mindustry.world.blocks.production.GenericCrafter;

public class FluxCrafter extends GenericCrafter {
    public FluxCrafter(String name){
        super(name);
    }



    public class FluxCrafterBuild extends GenericCrafterBuild implements Flux{

        public FluxModule flux = new FluxModule(this);
        @Override
        public void craft(){
            super.craft();
        }

        @Override
        public FluxModule flux() {
            return flux;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            flux.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read,revision);
            flux.read(read);
        }
    }
}

package mDimension.world.flux;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.consumers.ConsumeFlux;
import mDimension.consumers.modules.FluxModule;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.blocks.production.GenericCrafter;

public class FluxCrafter extends GenericCrafter {
    public FluxCrafter(String name){
        super(name);
    }



    public class FluxCrafterBuild extends GenericCrafterBuild implements Flux{



        //region module
        public FluxModule flux = new FluxModule();
        public FluxModule flux(){return flux;};

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            var res = super.init(tile,team,shouldAdd,rotation);
            flux.init(this);
            return res;
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
        @Override
        public void onRemoved() {
            FluxGraphRemoved();
        }
        //endregion
    }
}

package mDimension.world.blocks.flux;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.consumers.modules.FluxModule;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class FluxTurret extends ItemTurret {
    public float consumeAmount = 10f;
    public FluxTurret(String name){
        super(name);
    }

    @Override
    public void init() {
        super.init();
    }

    public class FluxTurretBuild extends ItemTurretBuild implements Flux{
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

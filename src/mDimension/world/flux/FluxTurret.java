package mDimension.world.flux;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.consumers.modules.FluxModule;
import mindustry.entities.bullet.BulletType;
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
        public FluxModule flux = new FluxModule(this);

        public FluxModule flux(){return flux;}

        @Override
        protected void shoot(BulletType type) {
            super.shoot(type);
            flux.fluxAmount-=consumeAmount;
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

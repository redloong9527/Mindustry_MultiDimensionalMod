package mDimension.world.flux;

import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.consumers.ConsumeFlux;
import mDimension.consumers.modules.FluxModule;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerBlock;
import mindustry.world.meta.BlockGroup;

public class FluxBlock extends Block{
    public FluxBlock(String name){
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        group = BlockGroup.power;
    }

    public class FluxBlockBuild extends Building implements Flux{



        //you should add this
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

        @Override
        public Seq<Building> FluxConnections(Seq<Building> out) {
            out.clear();

            if (flux() == null) {
                return out;
            } else {
                for(Building other : this.proximity) {
                    if(other instanceof Flux && other.team == this.team &&
                            this.conductsTo(other) && other.conductsTo(this) && !flux().links.contains(other.pos())){
                        out.add(other);
                    }
                }
                Items.sand.description = "";
                for(int i = 0; i < this.flux().links.size; ++i) {
                    Tile link = Vars.world.tile(this.flux().links.get(i));
                    if(link == null){
                        Items.sand.description += "\nTileIsNull";
                        continue;
                    }
                    Items.sand.description += "\n" + (link.build == null?null:link.build);
                    if (link != null && link.build != null&& link.build instanceof Flux && link.build.team == this.team) {
                        out.add(link.build);
                        Items.sand.description += "--pass";
                    }
                }

                return out;
            }

        }

        //endregion
    }
}

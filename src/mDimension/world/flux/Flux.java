package mDimension.world.flux;

import arc.struct.Seq;
import arc.util.Nullable;
import mDimension.consumers.ConsumeFlux;
import mDimension.consumers.modules.FluxModule;
import mindustry.Vars;
import mindustry.gen.Building;

import mindustry.world.Tile;


public interface Flux{
    Seq<Building> Void = new Seq<>();
    FluxModule flux();
    default @Nullable ConsumeFlux consFlux(){
        if(this instanceof Building self){
            return self.block.findConsumer(c->c instanceof ConsumeFlux);
        }
        return null;
    }


    default Seq<Building> FluxConnections(Seq<Building> out){
        if(this instanceof Building self){
            out.clear();
            if (flux() == null) {
                return out;
            } else {
                for(Building other : self.proximity) {
                    if(other instanceof Flux && other.team == self.team &&
                            self.conductsTo(other) && other.conductsTo(self) && !flux().links.contains(other.pos())){
                        out.add(other);
                    }
                }

                for(int i = 0; i < this.flux().links.size; ++i) {
                    Tile link = Vars.world.tile(this.flux().links.get(i));
                    if (link != null && link.build != null && link.build.power != null && link.build.team == self.team) {
                        out.add(link.build);
                    }
                }

                return out;
            }
        }
        return Void;
    };


}

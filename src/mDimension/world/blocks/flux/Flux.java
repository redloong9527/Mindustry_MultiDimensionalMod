package mDimension.world.blocks.flux;

import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Nullable;
import mDimension.consumers.ConsumeFlux;
import mDimension.consumers.modules.FluxModule;
import mDimension.content.MD_Fx;
import mDimension.tool.Debug;
import mindustry.Vars;
import mindustry.gen.Building;

import mindustry.world.Tile;


public interface Flux{
    Seq<Building> Void = new Seq<>();
    FluxModule flux();
    default @Nullable ConsumeFlux consFlux(){
        if(this instanceof Building self){
            if(self.block == null)return null;
            return self.block.findConsumer(c->c instanceof ConsumeFlux);
        }
        return null;
    }

    default float outputAmount(){
        if(this instanceof Building self){
            return self.edelta() * consFlux().produceAmount;
        }
        return 0;
    }

    default float consumerAmount(){
        if(this instanceof Building self){
            return self.edelta() * consFlux().usage;
        }
        return 0;
    }
    default void cleanFlux(){
        flux().fluxAmount=0;
    }
    default void overload(){
        if(this instanceof Building b){
            b.damagePierce(b.block.health *0.25f +1f);
        }
    }


    default void FluxGraphRemoved() {
        if (flux() != null && this instanceof Building self) {
            MD_Fx.polyWave(4,20,0,2,20, Color.valueOf("fff090"),1f).at(self.x,self.y);
            flux().graph.remove(self);

            for(int i = 0; i < flux().links.size; ++i) {
                Tile other = Vars.world.tile(flux().links.get(i));
                if (other != null && other.build != null && other.build.power != null) {
                    other.build.power.links.removeValue(self.pos());
                }
            }

            flux().links.clear();
        }
    }


    default Seq<Building> FluxConnections(Seq<Building> out){
        out.clear();
        if(this instanceof Building self){
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
                    if (link != null && link.build != null&& link.build instanceof Flux && link.build.team == self.team) {
                        out.add(link.build);
                    }
                }

                return out;
            }
        }
        return out;
    };


}

package mDimension.world.blocks.flux;

import arc.struct.Seq;
import arc.util.Nullable;
import mDimension.consumers.modules.FluxModule;
import mindustry.gen.Building;

public class Fluxs {
    public static @Nullable FluxModule flux(Building b){
        if(b instanceof Flux f){
            return f.flux();
        }
        return null;
    }

    public static Seq<Building> FluxConnections(Building b,Seq<Building> out){
        var f = (Flux)b;
        return f.FluxConnections(out);
    }
}

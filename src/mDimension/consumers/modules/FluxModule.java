package mDimension.consumers.modules;

import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.consumers.ConsumeFlux;
import mDimension.world.flux.FluxGraph;
import mindustry.gen.Building;
import mindustry.world.modules.BlockModule;

/**复制 PowerModule*/
public class FluxModule extends BlockModule {
    public FluxModule(Building owner){
        graph = new FluxGraph();
        graph.init(owner);
        this.owner = owner;
    }

    public Building owner;

    /**
     * In case of unbuffered consumers, this is the percentage (1.0f = 100%) of the demanded power which can be supplied.
     * Blocks will work at a reduced efficiency if this is not equal to 1.0f.
     * In case of buffered consumers, this is the percentage of power stored in relation to the maximum capacity.
     */
    //public float status = 0.0f;
    // public float effectivity = 0f;
    public float fluxAmount = 0f;
    public boolean fusing = false;
    public FluxGraph graph ;
    public IntSeq links = new IntSeq();

    @Override
    public void write(Writes write){
        write.f(fluxAmount);
    }

    @Override
    public void read(Reads read){
        links.clear();
        fluxAmount = read.f();
        graph = new FluxGraph();
        if(Float.isNaN(fluxAmount) || Float.isInfinite(fluxAmount)) fluxAmount = 0f;
    }

    @Override
    public String toString() {
        return super.toString()+"\nfluxAmount:"+fluxAmount+"\n";
    }
}

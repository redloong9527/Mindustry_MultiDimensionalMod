package mDimension.consumers.modules;

import arc.struct.IntSeq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.world.blocks.flux.FluxGraph;
import mindustry.gen.Building;
import mindustry.world.modules.BlockModule;

/**复制 PowerModule*/
public class FluxModule extends BlockModule {
    public FluxModule(){
    }
    public void init(Building owner){
        FluxGraph temp = new FluxGraph();
        temp.init(owner);
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
    public boolean overload = false;
    public FluxGraph graph ;
    public IntSeq links = new IntSeq();

    @Override
    public void write(Writes write){
        write.f(fluxAmount);
        write.s(links.size);
        for(int i =0;i<links.size;i++){
            write.s(links.get(i));
        }
    }

    @Override
    public void read(Reads read){
        links.clear();
        fluxAmount = read.f();
        int link = read.s();
        for(int i=0;i<link;i++){
            links.add(read.s());
        }
        graph = new FluxGraph();
        if(Float.isNaN(fluxAmount) || Float.isInfinite(fluxAmount)) fluxAmount = 0f;
    }

    @Override
    public String toString() {
        return super.toString()+"\nfluxAmount:"+fluxAmount+"\n";
    }
}

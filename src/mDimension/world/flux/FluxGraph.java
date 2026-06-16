package mDimension.world.flux;

import arc.graphics.Color;
import arc.struct.IntSet;
import arc.struct.Queue;
import arc.struct.Seq;
import mDimension.consumers.ConsumeFlux;
import mDimension.consumers.modules.FluxModule;
import mDimension.world.data.MDEvents;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.gen.Building;

import static mDimension.world.flux.Fluxs.*;
import static mindustry.Vars.world;

public class FluxGraph {
    private int lastChange = -2;
    public static IntSet visited = new IntSet();
    public static Queue<Building> queue = new Queue<>();
    public static Seq<Building> outArray1 = new Seq<>();
    public boolean deprecate = false;
    public boolean init = false;
    public Seq<Building> all = new Seq<>();
    public Building[] lastAll;

    public String debugLog = "";

    {
        MDEvents.graphs.add(this);
    }

    public void init(Building b){
        init = true;
        add(b);
    }

    public void add(Building b){
        FluxModule flux = flux(b);
        if(flux == null)return;
        all.add(b);

        if(flux.graph != this)flux.graph.deprecate();
        flux.graph = this;
    }

    public void deprecate(){
        all.clear();
        MDEvents.graphs.remove(this);
        deprecate = true;
    }

    public Seq<Building> bfs(Building start,Seq<Building> out){
        out.clear();
        queue.clear();
        visited.clear();
        queue.addLast(start);
        out.add(start);
        visited.add(start.pos());
        while (!queue.isEmpty()){
            Building node = queue.removeFirst();
            for (Building c : FluxConnections(node, outArray1)) {
                FluxModule cflux = flux(c);
                if (c != null && visited.add(c.pos())&& cflux !=null) {

                    cflux.graph =this;
                    Fx.colorTrail.at(c.x,c.y,2, Color.valueOf("4040ff"));
                    out.add(c);
                    queue.addLast(c);
                }

            }

        }
        return out;
    }

    public void update() {
        if (lastChange != world.tileChanges) {
            lastChange = world.tileChanges;
            updateGraph();
        }


    }
    public void updateGraph(){
        debugLog = "size:"+all.size + " init:"+init+" deprecate:"+deprecate +"\nlastAll:\n"+all;
        if(!init)return;

        boolean none = true;
        for(Building b:all){
            if(b.dead || world.build(b.pos()) == null){
                all.remove(b);
            }else if(flux(b).graph == this){
                none = false;
            }else{
                all.remove(b);
            }
        }
        if(all.size == 0 || none){
            deprecate();
            return;
        }

        lastAll = all.toArray(Building.class);
        Building start = all.get(0);
        Fx.colorTrail.at(start.x,start.y,3.5f, Color.valueOf("40ff40"));

        all = bfs(start,all);
        debugLog += "\nAll:\n"+all;
        for (Building last : lastAll) {
            if (!all.contains(last)) {
                FluxModule lastFlux = flux(last);
                if (lastFlux != null) {
                    Fx.colorTrail.at(last.x,last.y,7, Color.valueOf("ff4040"));
                    lastFlux.graph =  new FluxGraph();
                    lastFlux.graph.init(last);
                }
            }
        }

    }

    public void saveLoad(){
        for(Building b:all){
            Building nowb = world.build(b.pos());
            FluxModule flux = flux(nowb);
            if(flux!=null){
                flux.graph = new FluxGraph();
                flux.graph.init(nowb);
            }
        }
        deprecate();
    }
    public String getDebugLog(){
        return debugLog;
    }

}

package mDimension.world.data;

import arc.Events;
import arc.struct.Seq;
import arc.util.Interval;
import mDimension.consumers.ConsumeBeam;
import mDimension.consumers.modules.ExtraModule;
import mDimension.world.flux.Flux;
import mDimension.world.flux.FluxGraph;
import mDimension.world.flux.Fluxs;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class MDEvents {
    public static Interval timer;
    public static Seq<Building> out = new Seq<>();
    public static Seq<Building> overload = new Seq<>();
    public static Seq<FluxGraph> graphs = new Seq<>();
    public static  void load(){
        timer = new Interval(3);
        Events.run(EventType.Trigger.update,()->{
            if(timer.get(0,5*60)){
                ConsumeBeam.free();
                for(ExtraModule<?> mods :ExtraModule.allModule){
                    mods.freeAllIf(b->b.dead);
                }
            }
            updateGraphs();

        });
//        Events.on(EventType.BlockDestroyEvent.class,e->{
//
//            Building b = e.tile.build;
//            if(b!=null){
//                var flux = Fluxs.flux(b);
//                if(flux!=null){
//                    if(flux.overload){
//                        ItemTurret t = (ItemTurret)(Blocks.scathe);
//                        t.ammoTypes.get(Items.carbide).spawnUnit.weapons.get(0).bullet.create(b,b.x,b.y,0);
//                    }
//                }
//            }
//        });

//        Events.on(EventType.BlockBuildBeginEvent.class,e->{
//            updateGraphs();
//        });
//        Events.on(EventType.BlockBuildEndEvent.class,e->{
//            updateGraphs();
//        });
//
//        Events.on(EventType.PayloadDropEvent.class,e->{
//            if(e.build!=null && ConsumeFlux.hasConsume(e.build)){
//                updateGraphs();
//            }
//        });
//
//        Events.on(EventType.PickupEvent.class,e->{
//            if(e.build!=null && ConsumeFlux.hasConsume(e.build)){
//                updateGraphs();
//            }
//        });
//
        Events.on(EventType.SaveLoadEvent.class,e->{
            updateGraphs(true);
        });




    }

    public static void updateGraphs(){
        updateGraphs(false);
    }
    public static void updateGraphs(boolean loadSave){
        overload.clear();
        if(!Vars.state.isPaused()) {
            int ind = 0;
            Items.coal.description = "";
            for (FluxGraph graph : graphs) {
                if (graph.deprecate) {
                    graphs.remove(graph);
                } else if (graph.init) {
                    //Items.coal.description += "\n\n\nindex:" + ind + " allSize:" + graphs.size + "\n" + graph.getDebugLog();
                    if(loadSave){
                        graph.saveLoad();
                        graph.updateGraph();
                    }else{
                        graph.update();
                    }
                }
                ind++;
            }

            for(int i=0;i<overload.size;i++){
                Building b = overload.get(i);
                if(b instanceof Flux f){
                    var cons = f.consFlux();
                    f.cleanFlux();
                    if(cons.canOverload){
                        f.overload();
                        f.flux().overload = true;
                    }
                }
            }
        }
    }
}

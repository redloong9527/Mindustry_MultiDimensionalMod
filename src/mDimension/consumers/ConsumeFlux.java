package mDimension.consumers;

import arc.Core;
import arc.func.Boolc;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.IntSeq;
import arc.struct.IntSet;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import mDimension.consumers.modules.ExtraModule;
import mDimension.consumers.modules.FluxModule;
import mDimension.tool.continuousRGB;
import mDimension.world.flux.FluxGraph;
import mDimension.world.flux.FluxNode;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.core.UI;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;

import java.rmi.server.UID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.world;
import static mDimension.world.flux.Fluxs.*;

public class ConsumeFlux extends Consume {
    //public static Seq<Building> outArray2 = new Seq<>();
    public static Queue<Building> queue = new Queue<>();
    //public static Seq<Building> outArray1 = new Seq<>();
    public static IntSet visited = new IntSet();
    //public static ExtraModule<FluxModule> fluxModMain = new ExtraModule<>();
    public static Interval timer = new Interval();

    /** The maximum amount of power which can be processed per tick. This might influence efficiency or load a buffer. */
    public float usage =0;

    public float produceAmount =0;
    /**最大的正常存储上限，超过的会以dissipationSpeed/tick的速度流失*/
    public float capacity =0;
    /**辐能承载力，超过capacity+bearingCapacity的话进入熔断状态，你可以重写overloadProcess()来更改熔断机制*/
    public float bearingCapacity =0;
    /**建筑拉取和保有的目标值*/
    public float retain = 0;
    public boolean isWillFusing = true;
    public boolean pullFlux = true;
    public boolean isNode = false;
    public boolean isbatter = false;
    public BiPredicate<FluxModule,ConsumeFlux> canPull = (mod,cons)-> {
        return cons.isbatter || mod.fluxAmount > cons.retain;
    };

    public boolean alwaysDissipation = false;
    public float  dissipationSpeed = 0.3f/60f;

    public Effect overloadEffect = new Effect(60f,e->{
        Draw.color(e.color);
        float fout = (Mathf.sqrt(e.fin())- e.fin())*4f;
        randLenVectors(e.id,3,0.5f,e.finpow() * 4f,(x,y)->{
            Lines.stroke(1f);
            float angle = Mathf.angle(x,y);
            Lines.lineAngle(e.x+x,e.y+y,angle, fout*3.2f,false);
        });
    });
    public float effectChangePerTile = 0.08f;

    public ConsumeFlux(){
    }

    @Override
    public void apply(Block block){
        //fluxModMain.master = block;
        if (retain==0)retain=usage*2+10;
        if(!isNode) {
            block.addBar("fluxAmount", (b) -> {
                FluxModule flux = flux(b);
                Color color = Color.valueOf("F090F0");
                return new Bar(
                        () -> {
                            return Core.bundle.format("bar.flux", UI.formatAmount((long)flux.fluxAmount),UI.formatAmount((long)capacity),UI.formatAmount((long)(capacity+bearingCapacity)));
                        },
                        () -> color,
                        () -> ((int) (flux.fluxAmount) / (capacity + bearingCapacity))
                );
            });
            block.addBar("fluxStats", (b) -> {
                FluxModule flux = flux(b);
                Color color = Color.valueOf("FFB19C");
                return new Bar(
                        () -> {
                            if(flux.fusing){
                                return Core.bundle.get("bar.fluxFusing");
                            }else if(flux.fluxAmount > capacity){
                                return Core.bundle.get("bar.fluxOverload");
                            }else{
                                return Core.bundle.get("bar.fluxNormal");
                            }
                        },
                        () -> color,
                        () -> {
                            if(flux.fusing){
                                return 1f;
                            }else if(flux.fluxAmount > capacity){
                                return 0.66f;
                            }else{
                                return 0.33f;
                            }
                        }
                );
            });
        }else{
            isWillFusing = false;
        }
    }

    public static ConsumeFlux getConsume(Building b){
        return b.block.findConsumer(c->c instanceof ConsumeFlux);
    };
    public static ConsumeFlux getConsume(Block b){
        return b.findConsumer(c->c instanceof ConsumeFlux);
    };
    public static boolean hasConsume(Building b){
        if(b == null)return false;
        return b.block.findConsumer(c->c instanceof ConsumeFlux)!=null;
    };
//    public static boolean hasConsume(Block b){
//        return b.findConsumer(c->c instanceof ConsumeFlux)!=null;
//    };
//    public static FluxModule flux(Building b){
//        if(b == null)return null;
//        ConsumeFlux con = getConsume(b);
//        if(con == null)return null;
//        FluxModule flux = con.getModule(b);
//
//        return flux == null?fluxModMain.register(b,FluxModule::new):flux;
//    }

//    public FluxModule getModule(Building b){
//        return fluxModMain.register(b,FluxModule::new);
//    }

    public float overloadProcess(FluxModule flux){
        return Math.min(1,(flux.fluxAmount-capacity) / (bearingCapacity));
    }

    public Color fluxColor(FluxModule flux){
        return continuousRGB.contRGB((Mathf.sin(60,0.07f) + 0.07f * overloadProcess(flux)) % 1f).lerp(Color.white,0.55f+Mathf.absin(60,0.1f));
    }



//    public boolean hasModule(Building b){
//        return fluxModMain.has(b);
//    }

    @Override
    public void update(Building b) {
        FluxModule flux =  flux(b);
        if(!flux.graph.init){
            flux.graph.init(b);
        }
        if(!isNode && (flux.graph.deprecate)){
            flux.graph = new FluxGraph();
            flux.graph.init(b);
        }
        IntSeq linksc = flux.links;
        linksc.each(pos->{
            Building link = world.tile(pos).build;
            if(link == null || link.dead || flux(link) == null){
                flux.links.removeValue(pos);
            }
        });


        if(isNode)return;
        flux.fusing = isWillFusing && overloadProcess(flux)>0.99f;
        if(flux.fluxAmount>capacity && Mathf.chanceDelta(overloadProcess(flux) * effectChangePerTile * b.block.size* b.block.size)){
            overloadEffect.at(b.x+Mathf.range(b.block.size/2f)*8f,b.y+Mathf.range(b.block.size/2f)*8f,fluxColor(flux));
        }

        if(alwaysDissipation||flux.fluxAmount>capacity+0.01f){
            float amount = dissipationSpeed * Time.delta;
            flux.fluxAmount -= Math.min(amount,flux.fluxAmount-capacity);
        }
        //拉取辐能
        if(pullFlux&& capacity> 0 && flux.fluxAmount<retain){
            for(int i=0;i<flux.graph.all.size;i++){
                var n = flux.graph.all.get(i);
                if(n == b)continue;
                FluxModule nflux =  flux(n);
                ConsumeFlux ncons = getConsume(n);
                if(canPull.test(nflux,ncons) && flux.fluxAmount<retain){
                    float amount =0;
                    if(!isbatter){
                        amount = Math.min(nflux.fluxAmount-ncons.retain,capacity-flux.fluxAmount);
                    }else{
                        amount = Math.min(nflux.fluxAmount,capacity-flux.fluxAmount);
                    }
                    flux.fluxAmount+=amount;
                    nflux.fluxAmount-=amount;
                    if(flux.fluxAmount>=retain)break;
                }
            }
        }
    }

    @Override
    public void trigger(Building b) {
        if(!shouldConsumeFlux(b))return;
        FluxModule flux =  flux(b);
        flux.fluxAmount+=produceAmount;
        flux.fluxAmount-=usage;
    }

    @Override
    public float efficiency(Building b) {
        if(usage<=0)return 1f;
        FluxModule flux = flux(b);
        return flux.fluxAmount > usage ? 1f:0f;
    }

    public boolean shouldConsumeFlux(Building b){
        return b.shouldConsume();
    }

    public Seq<Building> connectFlux(Building self,Seq<Building> out,FluxModule flux){
        out.clear();
        if(flux==null)return out;
        self.updateProximity();
        for(int i=0;i<self.proximity.size;i++) {
            var other = self.proximity.get(i);
            if (other != null && flux(other)!=null && other.team == self.team) {
                out.add(other);
            }
        }
        Items.silicon.description = ""+flux.links.size;
        flux.links.each(pos->{
            Building b = world.build(pos);
            if(b!=null&&!b.dead){
                out.addUnique(b);
            }
        });
        return out;
    }
//    public Seq<Building> connectFlux(Building self,Seq<Building> out){
//        return connectFlux(self,out,flux(self));
//    }
//    public  Seq<Building> FluxBfs(Building start,Seq<Building> out){
//        out.clear();
//        //FluxModule flux = flux(start);
//        queue.clear();
//        visited.clear();
//        queue.addLast(start);
//        visited.add(start.pos());
//        while (!queue.isEmpty()){
//            Building node = queue.removeFirst();
//            ConsumeFlux cons = ConsumeFlux.getConsume(node);
//            if(cons!=null) {
//                for (Building c : cons.connectFlux(node, outArray1)) {
//                    if (c != null && visited.add(c.pos())) {
//                        BfsAct(node,c);
//                        out.add(c);
//                        queue.addLast(c);
//                    }
//
//                }
//            }
//
//        }
//
//        return out;
//    };

    public void BfsAct(Building f,Building c){

    }
}

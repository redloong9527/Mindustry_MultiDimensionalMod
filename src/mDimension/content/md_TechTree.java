package mDimension.content;

import arc.struct.Seq;

import static mDimension.content.md_items.*;
import static mDimension.content.md_liquids.*;
import static mDimension.content.md_blocks.*;
import static mDimension.content.md_UnitTypes.*;
import static mDimension.content.md_SectorPresets.*;
import static mindustry.content.Items.*;
import static mindustry.content.Liquids.*;
import static mindustry.game.Objectives.*;

import mindustry.content.TechTree;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;
import mindustry.type.SectorPreset;


public class md_TechTree {
    private static TechTree.TechNode context = null;
    public static Seq<TechTree.TechNode> all = new Seq<>();
    public static Seq<TechTree.TechNode> roots = new Seq<>();



    public static void load(){
        md_Planets.depicilon.techTree = nodeRoot("depicilon",md_blocks.coreSteady,()->{
            node(md_blocks.al_alloy_smelting,Seq.with(new Objectives.SectorComplete(md_SectorPresets.starting_point)),()->{
               node(ammonia_chamber,Seq.with(new Objectives.OnSector(md_SectorPresets.crystallization_oil_rift)),()->{
                   node(polymer_compressor);
               });
            });
            node(md_blocks.light_duct,()->{
                node(md_blocks.armored_light_duct,()->{
                    node(md_blocks.stack_rail_conveyor);
                });
                node(md_blocks.multiway_unloader);
                node(md_blocks.light_duct_bridge);
                node(shunt_router,()->{
                    node(light_sorter,()->{
                        node(light_invertedSorter);
                    });
                    node(light_overflowGate,()->{
                        node(light_underflowGate);
                    });

                });
            });
            node(fluid_conduit,Seq.with(new Objectives.OnSector(md_SectorPresets.crystallization_oil_rift)),()->{
                node(directional_fluid_router,()->{
                    node(fluid_junction);
                });

                node(fluid_conduit_bridge,()->{
                    node(fluid_unloader);
                });

                node(siphon_pump);
            });
            node(small_impact_drill,()->{
                node(heavy_pulverizer,()->{
                    node(small_silicon_arc_furnace,()->{

                    });
                });
                node(beam_bore,()->{
                    node(ammonia_collector);
                });
            });
            node(crack,()->{
                node(fracture);
                node(ionize);
            });
            node(aluminium_wall,()->{
                node(aluminium_wall_large,()->{
                    node(al_alloy_wall,()->{
                        node(al_alloy_wall_large);
                    });
                });
            });
            node(magnetic_node,()->{
                node(graphite_combustion_chamber,()->{
                    node(composite_combustion);
                });
                node(internal_energy_pile);
            });



            nodeProduce(sand,()->{
                nodeProduce(bauxite,()->{
                    nodeProduce(aluminium,()->{
                        nodeProduce(silicon,()->{
                            nodeProduce(al_alloy,()->{
                                nodeProduce(polymer);
                            });
                        });
                    });
                });
                nodeProduce(titanium,()->{
                    nodeProduce(ti_alloy,()->{
                        nodeProduce(polymorphic_crystal);
                        nodeProduce(plasma);
                    });
                });
                nodeProduce(copper);
                nodeProduce(graphite);

                nodeProduce(ammonia,()->{
                    nodeProduce(crystallization_oil);
                    nodeProduce(hydrogen);
                    nodeProduce(nitrogen);
                });
            });
            node(infantry_factory,()->{
                node(anchor_radar);
                node(army_anchor_point_reconstructor);
                node(airborne_vessels_factory,()->{
                    node(airforce_anchor_point_reconstructor);
                });

                node(captive,ItemStack.with(),()->{
                    node(zircon);
                });
                node(shimmer,ItemStack.with(polymer,50,silicon,100),()->{
                    node(firefly);
                });
            });

            node(starting_point,()->{
                node(crystallization_oil_rift,Seq.with(
                        new SectorComplete(starting_point),
                        new Research(ammonia_collector)
                ));
            });





        });
    }

    public static void addToNext(UnlockableContent content,Runnable run){
        context = TechTree.all.find(t->t.content == content);
        run.run();
    }

    public static TechTree.TechNode nodeRoot(String name, UnlockableContent content, Runnable children){
        return nodeRoot(name, content, false, children);
    }

    public static TechTree.TechNode nodeRoot(String name, UnlockableContent content, boolean requireUnlock, Runnable children){
        var root = node(content, content.researchRequirements(), children);
        root.name = name;
        root.requiresUnlock = requireUnlock;
        roots.add(root);
        return root;
    }
    public static TechTree.TechNode node(UnlockableContent content,Seq<Objectives.Objective>objectives){
        return node(content, content.researchRequirements(),objectives,()->{});
    }
    public static TechTree.TechNode node(UnlockableContent content, Runnable children){
        return node(content, content.researchRequirements(), children);
    }

    public static TechTree.TechNode node(UnlockableContent content, ItemStack[] requirements, Runnable children){
        return node(content, requirements, null, children);
    }

    public static TechTree.TechNode node(UnlockableContent content, ItemStack[] requirements, Seq<Objectives.Objective> objectives, Runnable children){
        TechTree.TechNode node = new TechTree.TechNode(context, content, requirements);
        if(objectives != null){
            node.objectives.addAll(objectives);
        }

        //insert missing sector parent dependencies
        if(context != null && context.content instanceof SectorPreset preset && !node.objectives.contains(o -> o instanceof Objectives.SectorComplete sc && sc.preset == preset)){
            node.objectives.insert(0, new Objectives.SectorComplete(preset));
        }

        TechTree.TechNode prev = context;
        context = node;
        children.run();
        context = prev;

        return node;
    }

    public static TechTree.TechNode node(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children){
        return node(content, content.researchRequirements(), objectives, children);
    }

    public static TechTree.TechNode node(UnlockableContent block){
        return node(block, () -> {});
    }

    public static TechTree.TechNode nodeProduce(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children){
        return node(content, content.researchRequirements(), objectives.add(new Objectives.Produce(content)), children);
    }

    public static TechTree.TechNode nodeProduce(UnlockableContent content, Runnable children){
        return nodeProduce(content, new Seq<>(), children);
    }

    public static TechTree.TechNode nodeProduce(UnlockableContent content){
        return nodeProduce(content, new Seq<>(),()->{});
    }
}

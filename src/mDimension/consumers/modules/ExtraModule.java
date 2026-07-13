package mDimension.consumers.modules;

import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.Block;
import mindustry.world.modules.BlockModule;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ExtraModule<Module extends BlockModule> {
    public static Seq<ExtraModule<?>> allModule = new Seq<>();
    public Block master = null;
    public IntMap<Module> moduleMap= new IntMap<>();

    public ExtraModule(){
        allModule.add(this);
    }

    public boolean has(Building b){
        return master != null && b.block == master;
    }

    public Module get(Building b){
        return moduleMap.get(b.id);
    }


    public Module register(Building b, Supplier<Module> craft){
        if(b == null || craft == null)return null;
        if(moduleMap.get(b.id)!= null) return moduleMap.get(b.id);
        Module mod = craft.get();
        moduleMap.put(b.id,mod);
        return mod;
    }

    public void free(Building b){
        if(b!=null)moduleMap.remove(b.id);
    }
    public void freeAll(){
        moduleMap.clear();
    }
    public void freeAllIf(Predicate<Building> condition) {
        IntSeq ids = moduleMap.keys().toSeq();
        ids.each(id->{
            if(Groups.build.mappingEnabled()){
                Building b = Groups.build.getByID(id);
                if(b!= null && condition.test(b)){
                    moduleMap.remove(id);
                }
            }

        });
    }
}

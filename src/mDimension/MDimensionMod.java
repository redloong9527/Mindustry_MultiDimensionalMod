package mDimension;
import arc.Core;
import arc.Events;
import arc.util.Time;
import mDimension.core.MDRenderer;
import mDimension.core.MDShaders;
import mDimension.meta.md_Stat;
import mDimension.meta.md_StatUnit;
import mDimension.world.MDEvents;
import mindustry.game.EventType;
import mindustry.graphics.Shaders;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;

//物品导入
import mDimension.content.*;

public class MDimensionMod extends Mod {

    public MDimensionMod() {

        Events.on(EventType.ClientLoadEvent.class, e -> {
            Time.runTask(20f, () -> {
                BaseDialog welcome = new BaseDialog("Welcome to play Multidimensional");
                welcome.cont.add("A new journey Let's begin").colspan(2).row();
                welcome.cont.image(Core.atlas.find("mdimension-evil")).pad(10f);
                welcome.cont.image(Core.atlas.find("mdimension-neuro")).pad(20f);
                Time.runTask(200f, welcome::addCloseButton);
                welcome.show();
            });
        });


    }

    @Override
    public void init() {
        MDShaders.init();
        MDRenderer.init();
        MDEvents.init();
    }

    public void replaceRegion(String from,String to,boolean abbModName){
        Core.atlas.addRegion(from,Core.atlas.find(abbModName?"mdimension-"+to:to));
    }
    public void replaceRegion(String from,String to){
        replaceRegion(from,to,true);
    }

    @Override
    public void loadContent() {

        Shaders.init();
        super.loadContent();
        //载入

        md_beams.load();
        md_StatUnit.load();
        md_Stat.load();
        md_StatusEffects.load();
        md_items.load();
        md_liquids.load();
        md_environment.load();
        md_UnitTypes.load();
        md_blocks.load();
        original_reset.load();

        md_Loadouts.load();
        md_Planets.load();

        md_SectorPresets.load();
        md_TechTree.load();

    }

}
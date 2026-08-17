package mDimension.content;

import mindustry.type.SectorPreset;

public class MD_SectorPresets {
    public static SectorPreset starting_point,marginal_outpost,crystallization_oil_rift;
    public static void load(){
        starting_point = new SectorPreset("starting-point", MD_Planets.depicilon,0){{
            alwaysUnlocked = true;
            difficulty = 2;
            captureWave = 23;
            noLighting = true;
        }};

        marginal_outpost = new SectorPreset("marginal-outpost", MD_Planets.depicilon,35){{
            difficulty = 3;
        }};

        crystallization_oil_rift = new SectorPreset("crystallization-oil-rift", MD_Planets.depicilon,171){{
            difficulty = 4;
        }};
    }
}

package mDimension.content;

import arc.struct.Seq;
import mindustry.type.Sector;
import mindustry.type.SectorPreset;
import static arc.struct.Seq.*;
public class md_SectorPresets {
    public static SectorPreset starting_point,crystallization_oil_rift;
    public static void load(){
        starting_point = new SectorPreset("starting-point",md_Planets.depicilon,0){{
            alwaysUnlocked = true;
            difficulty = 2;
            captureWave = 23;
            noLighting = true;
        }};
        crystallization_oil_rift = new SectorPreset("crystallization-oil-rift",md_Planets.depicilon,171){{
            difficulty = 4;
        }};
    }
}

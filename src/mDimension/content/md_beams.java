package mDimension.content;

import arc.graphics.Color;
import mDimension.world.data.Beam;

public class md_beams {
    public static Beam
            near_infrared_ligth,ultraviolet_ligth,nihility_light;
    public static void load() {
        near_infrared_ligth = new Beam("near-infrared-laser", Color.valueOf("FF6E61").a(0.7f)) {{
            energyLevel = 3;
            lenght = 18;

        }};
        ultraviolet_ligth = new Beam("ultraviolet-light", Color.valueOf("E363FF").a(0.7f)) {{
            energyLevel = 5;
            lenght = 12;

        }};

        nihility_light = new Beam("nihility_light",Color.valueOf("fff080").a(0.7f)){{
            energyLevel = 9;
            lenght = 6;

        }};
    }

}

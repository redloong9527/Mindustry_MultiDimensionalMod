package mDimension.content;
import arc.graphics.Color;
import mDimension.world.blocks.ployCellLiquid;
import mindustry.content.StatusEffects;
import mindustry.type.*;

import static mindustry.content.Liquids.water;

public class MD_Liquids {
    public static Liquid
            helium,dimension_fluid , crystallization_oil , ammonia;
    public static void load(){

        helium = new Liquid("helium",new Color(0xffcfffff)){{
            gas = true;
            flammability = 0f;
            explosiveness = 0f;
            blockReactive = false;
            coolant = false;
            incinerable = false;
            boilPoint = -1f;
            hidden = false;
        }};

        ammonia = new Liquid("ammonia",new Color(0xFFE28AFF)){{
            gas = true;
            flammability = 0.2f;
            explosiveness = 0.1f;
            blockReactive = false;
            coolant = false;
            incinerable = true;
            boilPoint = -1f;
            hidden = false;
        }};

        dimension_fluid = new ployCellLiquid("dimension-fluid",new Color(0xFFD188ff),4,4){{
            puddleRad = 9.5f;
            cellRad = 4.2f;
            cellRange = 10;
            colorStep = 5;
            effect = MD_StatusEffects.dimension_slip;
            boilPoint = 2f;
            viscosity = 0.85f;
            temperature = 0.1f;
            heatCapacity = 1.8f;
            vaporEffect = MD_Fx.dimension_vapor;
            particleEffect = MD_Fx.dimension_vapor_small;
            colorFrom = Color.valueOf("FFB578");
            colorTo = Color.valueOf("FFE491");
            cells = 7;
        }};

        crystallization_oil = new ployCellLiquid("crystallization-oil",Color.valueOf("705F42"),-1,6){{
            puddleRad = 9f;
            puddleRange = 60f;
            effect = StatusEffects.tarred;
            boilPoint = 2f;
            viscosity = 0.25f;
            heatCapacity = 0.7f;
            temperature = 0.3f;
            explosiveness = 1.5f;
            flammability = 0.8f;
            coolant = false;
            colorFrom = Color.valueOf("70382A");
            colorTo = Color.valueOf("705B2A");
            canStayOn.add(water);
            cells = -1;
        }};
    }
}

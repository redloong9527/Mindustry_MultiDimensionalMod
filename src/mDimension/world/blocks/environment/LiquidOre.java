package mDimension.world.blocks.environment;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OverlayFloor;
import mindustry.world.blocks.environment.SteamVent;
import mindustry.world.blocks.liquid.LiquidBlock;

import static arc.graphics.g2d.Draw.alpha;
import static arc.graphics.g2d.Draw.color;
import static mDimension.content.md_Fx.rand;
import static mDimension.content.md_Fx.v;
import static mindustry.Vars.tilesize;

public class LiquidOre extends OverlayFloor {
    public Effect effect = new Effect(130, e -> {
        color(e.color, Pal.vent2, e.fin());

        alpha(e.fslope() * 0.6f);

        float length = 3f + e.finpow() * 10f;
        rand.setSeed(e.id);
        for (int i = 0; i < rand.random(2, 3); i++) {
            v.trns(rand.random(360f), rand.random(length));
            Fill.circle(e.x + v.x, e.y + v.y, rand.random(1f, 2.5f) + e.fslope() * 1.1f);
        }
    });
    public TextureRegion[] mids;
    public float LiquidTilePad = 10/4f;
    public float effectSpacing = 150;
    public Color effectColor = null;
    /**Multi in liquidMultiplier*/
    public LiquidOre(String name, Liquid ore){
        super(name);
        this.localizedName = ore.localizedName;
        this.liquidDrop = ore;
        this.mapColor.set(ore.color);
        this.useColor = true;
    }
    public void setup(Liquid ore){
        this.localizedName = ore.localizedName + (wallOre ? " " + Core.bundle.get("wallore") : "");
        this.liquidDrop = ore;
        this.mapColor.set(ore.color);
    }

    @Override
    public void load() {
        super.load();
        mids = new TextureRegion[variants];
        for(int i=0;i<variants;i++){
            mids[i] =Core.atlas.find(name + "-mid" + (i+1));
            variantRegions[i] = Core.atlas.find(name + (i+1));
        }
    }

    @Override
    public void init(){
        super.init();

        if(liquidDrop != null){
            setup(liquidDrop);
        }else{
            throw new IllegalArgumentException(name + " must have an item drop!");
        }
        if(effectColor == null){
            effectColor = liquidDrop.color.cpy();
            if(liquidDrop == Liquids.water){
                effectColor.set(0.98f,0.98f,1f);
            }else if(!liquidDrop.gas){
                effectColor.lerp(Color.white,0.5f);
            }
        }
    }


    @Override
    public void drawBase(Tile tile) {
        //Draw.rect(mids[variant(tile.x, tile.y)], tile.worldx(), tile.worldy());

        LiquidBlock.drawTiledFrames(2, tile.worldx(), tile.worldy(), LiquidTilePad,liquidDrop,1f);

        Draw.color(tile.floor().mapColor);
        Draw.rect(variantRegions[variant(tile.x, tile.y)], tile.worldx(), tile.worldy());
        Draw.color();
    }


    @Override
    public String getDisplayName(Tile tile){
        return liquidDrop.localizedName;
    }
    @Override
    public void createIcons(MultiPacker packer){
        for(int i = 0; i < variants; i++){
            //use name (e.g. "ore-copper1"), fallback to "copper1" as per the old naming system
            PixmapRegion shadow = packer.get(name + (i + 1));
            PixmapRegion mid = packer.get(name+"-mid" + (i + 1));
            Pixmap image = shadow.crop();


            for(int x = 0; x < image.width; x++){
                for(int y = 0; y < image.height; y++){
                    if(shadow.getA(x, y) == 0 && mid.getA(x, y) != 0){
                        image.setRaw(x, y, mid.getA(x, y));
                    }
                }
            }


            if(i == 0){
                packer.add(MultiPacker.PageType.main, "block-" + name + "-full", image);
            }

            image.dispose();
        }
    }

    @Override
    public void renderUpdate(UpdateRenderState state){
        if(state.tile != null && state.tile.block()== Blocks.air && (state.data += Time.delta) >= effectSpacing){
            effect.at(state.tile.x * tilesize - tilesize, state.tile.y * tilesize - tilesize,effectColor);
            state.data =Mathf.random(effectSpacing*0.2f);
        }
    }
}

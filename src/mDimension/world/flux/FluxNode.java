package mDimension.world.flux;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Tmp;
import arc.util.io.Writes;
import mDimension.consumers.ConsumeFlux;
import mDimension.consumers.modules.FluxModule;
import mindustry.content.Items;
import mindustry.core.UI;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.meta.Env;

import static arc.math.geom.Geometry.d8;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static mDimension.world.flux.Fluxs.*;

public class FluxNode extends FluxBlock {
    public int maxNodes = 10;
    public int range = 10;
    private Seq<Building> outArray = new Seq<>();
    private ConsumeFlux cons;
    private String str = "";

    public TextureRegion laser;
    public TextureRegion laserEnd;
    public FluxNode(String name){
        super(name);
        configurable = true;
        consumesPower = false;
        outputsPower = false;
        canOverdrive = false;
        swapDiagonalPlacement = true;
        schematicPriority = -10;
        drawDisabled = false;
        envEnabled |= Env.space;
        destructible = true;
        delayLandingConfig = true;

        update = true;
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("fluxAmount", ob -> {
            FluxNodeBuild b = (FluxNodeBuild)ob;
            Color color = Color.valueOf("F090F0");
            return new Bar(
                    () -> {
                        return Core.bundle.get("bar.fluxIcon")+UI.formatAmount((long)b.fluxAmount);
                    },
                    () -> color,
                    () -> ((int) (b.fluxAmount) / (b.cap))
            );
        });

        addBar("fluxStats", ob -> {
            FluxNodeBuild b = (FluxNodeBuild)ob;
            Color color = Color.valueOf("FFB19C");
            return new Bar(
                    () -> {
                        return Core.bundle.format("bar.fluxStats",b.overloads,b.fusings);
                    },
                    () -> color,
                    () -> (float) (b.fusings) / (b.builds)
            );
        });
    }

    @Override
    public void init() {
        super.init();
        laser = Core.atlas.find("power-beam");
        laserEnd = Core.atlas.find("power-beam-end");

        consume(cons = new ConsumeFlux(){{
            dissipationSpeed = 0;
            isNode = true;
        }});
    }

    public void drawLaser(float x1, float y1, float x2, float y2){
        float w =0.4f;
        float dst = size/2f*tilesize*1.2f;
        float angle = Angles.angle(x1,y1,x2,y2);

        Tmp.v1.trns(angle,dst);
        float sx = Tmp.v1.x,sy = Tmp.v1.y;
        Drawf.laser(laser, laserEnd,sx + x1,sy + y1,x2 - sx,y2 - sy, w);

    }


    public class FluxNodeBuild extends FluxBlockBuild{
        public Point2[] links = new Point2[]{new Point2(-1,-1),new Point2(-1,-1),new Point2(-1,-1),new Point2(-1,-1)};
        public Building[] linkBuild = new Building[4];
        public float fluxAmount = 0f;
        public float cap =0;
        public int overloads = 0,fusings = 0;
        public int builds=0;

        @Override
        public void updateTile() {
            super.updateTile();
            float max = 0;

            for(Point2 p: links){
                p.set(-1,-1);
            }
            for(int i=0;i<4;i++){
                switch (i){
                    case (0)-> Tmp.p1.set(1,1);
                    case (1)-> Tmp.p1.set(-1,1);
                    case (2)-> Tmp.p1.set(-1,-1);
                    case (3)-> Tmp.p1.set(1,-1);
                }
                for(int g =1;g<range;g++){
                    max = Math.max(max,g);
                    Tmp.v1.set(tile.x + Tmp.p1.x*g,tile.y + Tmp.p1.y*g).scl(8);
                    Tile tilec = world.tile(tile.x + Tmp.p1.x*g,tile.y + Tmp.p1.y*g);
                    Building other = tilec.build;
                    FluxModule oflux = Fluxs.flux(other);
                    if (oflux!=null){
                        oflux.links.addUnique(pos());
                        linkBuild[i] = other;
                        if(!(other instanceof FluxNodeBuild node) || node.id<this.id){
                            links[i].set(tile.x + Tmp.p1.x*g,tile.y + Tmp.p1.y*g);
                        }
                        break;
                    }
                }
            }

            float s1 = 0;
            float s2 = 0;
            int s3 = 0;
            int s4 = 0;
            int s5 = 0;
            for (int i=0;i<flux.graph.all.size;i++) {
                var other = flux.graph.all.get(i);
                s5++;
                FluxModule oflux = Fluxs.flux(other);
                ConsumeFlux ocons = ConsumeFlux.getConsume(other);
                if(oflux == null||ocons==null)continue;
                s1 += oflux.fluxAmount;
                s2 += ocons.capacity + ocons.bearingCapacity;
                s3 += oflux.fluxAmount > ocons.capacity ? 1 : 0;
                s4 += oflux.fusing ? 1 : 0;
            }
            fluxAmount = s1;
            cap = s2;
            overloads = s3;
            fusings = s4;
            builds = s5;


            updateClipRadius(20f + 8f*max);
        }

        @Override
        public Seq<Building> FluxConnections(Seq<Building> out) {

            out = super.FluxConnections(out);

            for(int i = 1;i<8;i+=2){
                var p = Geometry.d8(i);
                for(int g =1;g<range;g++){

                    Building other = world.build(tile.x + p.x*g,tile.y + p.y*g);

                    if (other instanceof Flux && other.team == team && !other.dead){
                        out.add(other);
                        Fluxs.flux(other).links.addUnique(pos());
                        flux.links.addUnique(other.pos());
                        break;
                    }
                }
            }

            return out;
        }

        @Override
        public void draw() {
            super.draw();
            for(Point2 p: links){
                if(p.x>0)drawLaser(x,y,p.x *tilesize,p.y*tilesize);
            }

        }

    }
}

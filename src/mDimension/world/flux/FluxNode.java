package mDimension.world.flux;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Tmp;
import mDimension.consumers.ConsumeFlux;
import mDimension.consumers.modules.FluxModule;
import mDimension.tool.Debug;
import mindustry.core.UI;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.meta.Env;

import java.util.Arrays;

import static arc.math.geom.Geometry.d8;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

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
            var f = (Flux)ob;
            return new Bar(
                    () -> {
                        return Core.bundle.get("bar.fluxIcon")+UI.formatAmount((long)f.flux().graph.getTotal());
                    },
                    () -> color,
                    () -> (f.flux().graph.getTotal()/f.flux().graph.getStorage())
            );
        });

    }

    @Override
    public void init() {
        super.init();
        laser = Core.atlas.find("power-beam");
        laserEnd = Core.atlas.find("power-beam-end");

        consume(cons = new ConsumeFlux(){{
            isNode = true;
        }});
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        for(int i = 0; i < 4; i ++) {
            var dir = Geometry.d8edge[i];
            int offset = size / 2;
            //find first block with power in range
            int w = (range + offset);

            Drawf.dashLine(Pal.placing,(x + dir.x*0.5f) * tilesize,(y + dir.y*0.5f) * tilesize,(x + w*dir.x)* tilesize,(y + w*dir.y)* tilesize);
        }

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
        public Building[] links = new Building[4];
        public Tile[] dests = new Tile[4];
        public int lastChange = -2;
        public Interval t = new Interval();

        @Override
        public void updateTile() {
                //TODO this block technically does not need to update every frame, perhaps put it in a special list.
            if(lastChange != world.tileChanges){
                lastChange = world.tileChanges;
                updateDirections();
            }
//            if(t.get(0,10)){
//                String s = "";
//                Seq<Building> seq = FluxConnections(tempBuilds);
//                for(int i=0;i<seq.size;i++){
//                    s+=seq.get(i) + "\n";
//                }
//                Debug.string(s,11,this);
//            }

            updateClipRadius(80f);
        }

        @Override
        public void pickedUp(){
            Arrays.fill(links, null);
            Arrays.fill(dests, null);
        }


        @Override
        public void draw() {
            super.draw();
            for(int i = 0; i < 4; i ++) {
                if(links[i] == null )continue;
                var dir = Geometry.d8edge[i];
                int offset = size / 2;
                //find first block with power in range
                for (int j = 1 + offset; j <= range + offset; j++) {
                    int tx = tile.x + j * dir.x,ty =  tile.y + j * dir.y;
                    var other = world.build(tx,ty);

                    if (other != null && other.isInsulated()) {
                        break;
                    }

                    if (other == links[i]){
                        drawLaser(this.x,this.y,tx*tilesize,ty*tilesize);
                    }
                }
            }
            if (flux.graph != null && flux.graph.init && !flux.graph.deprecate) {
                int h = flux.graph.hashCode();
                int r = h & 0xff0000;
                int g = h & 0x00ff00;
                int b = h & 0x0000ff;
                int rgba = r|g|b|0x000000ff;
                Draw.color(rgba);

                Fill.circle(x,y,3);
            }
            Draw.reset();
        }


        @Override
        public void overload() {
            //pass
        }


        public void updateDirections(){
            for(int i = 0; i < 4; i ++){
                var prev = links[i];
                var dir = Geometry.d8edge[i];
                links[i] = null;
                dests[i] = null;
                int offset = size/2;
                //find first block with power in range
                for(int j = 1 + offset; j <= range + offset; j++){
                    var other = world.build(tile.x + j * dir.x, tile.y + j * dir.y);

                    //hit insulated wall
                    if(other != null && other.isInsulated()){
                        break;
                    }

                    //power nodes do NOT play nice with beam nodes, do not touch them as that forcefully modifies their links
                    if(other instanceof Flux && other.team == this.team && !other.dead){
                        links[i] = other;
                        dests[i] = world.tile(tile.x + j * dir.x, tile.y + j * dir.y);
                        break;
                    }
                }

                var next = links[i];

                if(next != prev){
                    //unlinked, disconnect and reflow
                    if(prev != null && prev.isAdded()){
                        var f = (Flux)prev;
                        var pflux = f.flux();
                        pflux.links.removeValue(pos());
                        flux.links.removeValue(prev.pos());

                        FluxGraph newgraph = new FluxGraph();
                        //reflow from this point, covering all tiles on this side
                        newgraph.bfs(this);

                        if(pflux.graph != newgraph){
                            //reflow power for other end
                            FluxGraph og = new FluxGraph();
                            og.bfs(prev);
                        }
                    }

                    //linked to a new one, connect graphs
                    if(next != null){
                        var nf = (Flux)next;
                        flux.links.addUnique(next.pos());
                        nf.flux().links.addUnique(pos());

                        flux.graph.addGraph(nf.flux().graph);
                    }
                }
            }
        }

    }
}

package mDimension.core;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.*;
import mindustry.Vars;
import mindustry.game.EventType;

import static arc.Core.settings;

public class MDRenderer {
    public static MDRenderer renderer;
    private FrameBuffer buffer;

    public static Well well;
    protected MDRenderer(){
        if(!Vars.headless) {
            buffer = new FrameBuffer();
            well = new Well();
            well.init();
            Events.run(EventType.Trigger.draw, this::advancedDraw);
        }
    }
    public static void init(){
        if(renderer == null) renderer = new MDRenderer();
    }

    public void advancedDraw(){
        if(settings.getBool("pixelate") || !settings.getBool("bloom")){
            return;
        }
        Draw.draw(210.5f, () -> {
            Vars.renderer.bloom.capture();
        });

        Draw.draw(211.5f, () -> {
            Vars.renderer.bloom.render();
        });

        Draw.draw(211.6f, () -> {
            well.capture();
        });

        Draw.draw(212.5f, () -> {
            well.render();
        });
    }

    public static class Well{

        private Shader shader;
        private FrameBuffer buffer;

        private boolean capturing = false;

        public void init(){
            buffer = new FrameBuffer();
            shader = MDShaders.well;
        }
        public void capture(){
            if(!capturing){
                capturing = true;
                buffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
                buffer.begin(Color.clear);
            }
        }

        public void render(){
            if(capturing){
                capturing = false;
                buffer.end();
            }
            MDShaders.well.apply();
            buffer.blit(MDShaders.well);

            buffer.begin();
            Draw.rect();
            buffer.end();
        }
    }

}



//public class MainRenderer{
//    private final Seq<BlackHole> holes = new Seq<>();
//    private static MainRenderer renderer;
//
//    private FrameBuffer buffer;
//
//    private static final float[][] initFloat = new float[512][];
//    private static final Pool<BlackHole> holePool = Pools.get(BlackHole.class, BlackHole::new);
//
//    protected MainRenderer(){
//        if(!Vars.headless) {
//            MainShader.createShader();
//
//            buffer = new FrameBuffer();
//            Events.run(Trigger.draw, this::advancedDraw);
//        }
//    }
//
//    public static void init(){
//        if(renderer == null) renderer = new MainRenderer();
//        for(int i = 0; i < 512; i++){
//            initFloat[i] = new float[i * 4];
//        }
//    }
//
//    public static void addBlackHole(float x, float y, float inRadius, float outRadius, float alpha){
//        if(!Vars.headless) renderer.addHole(x, y, inRadius, outRadius, alpha);
//    }
//    public static void addBlackHole(float x, float y, float inRadius, float outRadius){
//        if(!Vars.headless) renderer.addHole(x, y, inRadius, outRadius, 1);
//    }
//
//    private void advancedDraw(){
//        if(settings.getBool("pixelate") || holes.size >= 512 || hasOtherContentMod) {
//            holes.clear();
//            return;
//        }
//
//        Draw.draw(Layer.background - 1, () -> {
//            buffer.resize(graphics.getWidth(), graphics.getHeight());
//            buffer.begin();
//        });
//
//        Draw.draw(Layer.max - 1, () -> {
//            buffer.end();
//
//            if(holes.size >= 512) {
//                for(int i = 0; i <= holes.size - 512; i++){
//                    holes.remove(i);
//                }
//            }
//            if(holes.size >= MainShader.MaxCont) MainShader.createShader();
//
//            float[] blackholes = initFloat[holes.size];
//
//            for(int i = 0; i < holes.size; i++){
//                var hole = holes.get(i);
//                blackholes[i * 4] = hole.x;
//                blackholes[i * 4 + 1] = hole.y;
//                blackholes[i * 4 + 2] = hole.inRadius;
//                blackholes[i * 4 + 3] = hole.outRadius;
//
//                Draw.color(Tmp.c2.set(Color.black).a(hole.alpha));
//                Fill.circle(hole.x, hole.y, hole.inRadius * 1.5f);
//                Draw.color();
//                //之前忘了
//                holePool.free(hole);
//            }
//            MainShader.holeShader.blackHoles = blackholes;
//            buffer.blit(MainShader.holeShader);
//
//            buffer.begin();
//            Draw.rect();
//            buffer.end();
//            holes.clear();
//        });
//    }
//
//    private void addHole(float x, float y, float inRadius, float outRadius, float alpha){
//        if(inRadius > outRadius || outRadius <= 0) return;
//
//        holes.add(holePool.obtain().set(x, y, inRadius, outRadius, alpha));
//    }
//
//    private static class BlackHole{
//        float x, y, inRadius, outRadius, alpha;
//
//        public BlackHole set(float x, float y, float inRadius, float outRadius, float alpha){
//            this.x = x;
//            this.y = y;
//            this.inRadius = inRadius;
//            this.outRadius = outRadius;
//            this.alpha = alpha;
//            return this;
//        }
//
//        public BlackHole(){
//
//        }
//    }
//}

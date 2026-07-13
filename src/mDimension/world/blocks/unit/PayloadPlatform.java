package mDimension.world.blocks.unit;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.tool.md_Edge;
import mDimension.tool.md_ItemSelection;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.io.TypeIO;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.payloads.*;

import static mindustry.Vars.*;

public class PayloadPlatform extends PayloadBlock {

    public PayloadPlatform(String name){
        super(name);
        configurable = true;
        rotate =true;
        rotateDraw = false;
        outputsPayload = true;
        acceptsPayload = true;
        commandable = true;
        config(Block.class,(PayloadPlatformBuild b,Block e)->{
            if(b.config.contains(e)) {
                b.config.remove(e);
            }else{
                b.config.addUnique(e);
            }
        });
        config(UnitType.class,(PayloadPlatformBuild b,UnitType e)->{
            if(b.config.contains(e)) {
                b.config.remove(e);
            }else{
                b.config.addUnique(e);
            }
        });
        config(Seq.class,(building,s)->{
            var b = (PayloadPlatformBuild)building;
            b.config.set(s);
        });
        configClear((PayloadPlatformBuild b)->{
            b.config.clear();
        });
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("craft",(PayloadPlatformBuild b)->{
            return new Bar(
                    ()-> Core.bundle.format("bar.progress")+":"+(int)(b.progress()*100)+"%",
                    ()-> Pal.bar,
                    b::progress
            );
        });
    }

    public class PayloadPlatformBuild extends PayloadBlockBuild<Payload>{
        public float warmup= 0;
        public float progress= 0;
        public UnlockableContent targetType;
        public Seq<UnlockableContent> config = new Seq<>();
        public boolean conflict = false;
        public Vec2 commandPos;
        public IntIntMap visited = new IntIntMap();
        public IntSeq links = new IntSeq();

        public Seq<UnlockableContent> configs = new Seq<>();

        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(warmup);
            w.f(progress);
            w.s(config.size);
            for(int i=0;i<config.size;i++){
                var content = config.get(i);
                w.s(content instanceof Block?0:1);
                w.s(content.id);
            }
            TypeIO.writeVecNullable(w,commandPos);

        }

        @Override
        public void read(Reads r, byte v) {
            super.read(r, v);
            warmup = r.f();
            progress = r.f();
            int size = r.s();
            for(int i=0;i<size;i++){
                int isUnit = r.s();
                int id = r.s();
                if(isUnit == 1){
                    config.add(content.unit(id));
                }else{
                    config.add(content.block(id));
                }
            }

            commandPos = TypeIO.readVecNullable(r);
            updateProximity();
        }

        @Override
        public Vec2 getCommandPosition(){
            return commandPos;
        }

        @Override
        public void onCommand(Vec2 target){
            commandPos = target;
        }


        @Override
        public Object config() {
            return config;
        }

        public float getSpeed(){
            float res=0;
            for(int i=0;i<links.size;i++){
                Building other = world.build(links.get(i));
                if(other instanceof PayloadPlatformConstructor.PayloadPlatformConstructorBuild c && c.team == this.team){
                    res+=c.getSpeed();
                }
            }
            return res;
        }

        public UnlockableContent findType(IntSeq seq,UnlockableContent type){
            if(conflict)return null;
            for(int i=0;i<links.size;i++){
                Building other = world.build(links.get(i));
                if(other instanceof PayloadPlatformConstructor.PayloadPlatformConstructorBuild c){
                    var res = c.upgradeType(type);
                    if(res!=null){
                        return res;
                    }
                }
            }
            return null;
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return this.payload==null && this.relativeTo(source) != this.rotation && payload.fits(this.block.size);
        }

        @Override
        public void updateProximity() {
            super.updateProximity();
            for(Building other:proximity){
                if(other instanceof PayloadPlatformConstructor.PayloadPlatformConstructorBuild c){
                    configs.sort((a,b)->{
                        if(a instanceof Block){
                            if(b instanceof Block){
                                return Integer.compare(a.id, b.id);
                            }else{
                                return -1;
                            }
                        }else{
                            if(b instanceof Block){
                                return 1;
                            }else{
                                return Integer.compare(a.id,b.id);
                            }
                        }
                    });
                    if(md_Edge.getAllFacingBuild(c) == this){
                        links.addUnique(c.pos());
                        c.boss = this;

                    }
                }

            }
            targetType = payload == null?null:findType(links,payload.content());
            checkConflicts();
        }

        void checkConflicts(){
            visited.clear();

            for(int i=0;i<links.size;i++){
                var other = world.build(links.get(i));
                if(other instanceof PayloadPlatformConstructor.PayloadPlatformConstructorBuild c){
                    for(UnlockableContent[]e:c.getUpgrades()){
                        if(visited.containsKey(packId(e[0])) ){
                            if(!visited.containsValue(packId(e[1])) ){
                                conflict = true;
                                return;
                            }
                        }
                        visited.put(packId(e[0]) , packId(e[1]));
                    }
                }
            }
            conflict = false;
        }

        public int packId(UnlockableContent e){
            return e.id+(e instanceof UnitType?10000:0);
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (payload !=null) {
                if(config.contains(payload.content())){
                    moveOutPayload();
                }else{
                    if(moveInPayload() && canUpgrade()){
                        float speed = getSpeed();
                        progress += speed;
                    }
                }
                if(progress>=100){
                    progress=0;
                    UnlockableContent target = findType(links,payload.content());
                    if(target!=null)upgrade(target);
                }
            }
            float warmupTarget = payload == null || !hasArrived() || targetType == null || efficiency <0.01f || conflict?0:1;
            warmup = Mathf.approachDelta(warmup,warmupTarget,1/25f);
        }

        void upgrade(UnlockableContent target){
            if(payload instanceof UnitPayload unitPayload){
                if(target instanceof UnitType type){
                    unitPayload.unit = type.create(unitPayload.unit.team);
                    if(unitPayload.unit.isCommandable()){
                        if(commandPos != null){
                            unitPayload.unit.command().commandPosition(commandPos);
                        }
                    }
                    updatePayload(null,this);
                }else if(target instanceof Block type){
                    payload = new BuildPayload(type,unitPayload.unit.team);
                    updatePayload(null,this);
                }
                targetType = findType(links,payload.content());

            }else if(payload instanceof BuildPayload buildPayload){
                if(target instanceof UnitType type){
                    payload = new UnitPayload(type.create(buildPayload.build.team));
                    var unitPayload = (UnitPayload)payload;
                    if(unitPayload.unit.isCommandable()){
                        if(commandPos != null){
                            unitPayload.unit.command().commandPosition(commandPos);
                        }
                    }
                    updatePayload(null,this);
                }else if(target instanceof Block type){
                    buildPayload.build = type.newBuilding().create(type,buildPayload.build.team);
                    updatePayload(null,this);
                }
                targetType = findType(links,payload.content());
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            updateProximity();
            configs.clear();
            for(int i=0;i<links.size;i++){
                var other =world.build(links.get(i));
                if(other instanceof PayloadPlatformConstructor.PayloadPlatformConstructorBuild c){
                    for(var e:c.getUpgrades()){
                        configs.addUnique(e[1]);
                    }
                }
            }
//            ItemSelection.buildTable(PayloadPlatform.this, table,
//                    configs,
//                    () -> (UnlockableContent)config(), this::configure,false, selectionRows, selectionColumns);
            md_ItemSelection.multiSelect(PayloadPlatform.this,table,configs,
                    config::contains,(u,isChecked)->{
                        configure(u);
                    },false,selectionRows, selectionColumns
                    );
        }

        @Override
        public void configure(Object value) {
            super.configure(value);
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            super.handlePayload(source, payload);
            targetType = findType(links,payload.content());
        }

        public float getBeamSize(){
            if(targetType instanceof UnitType u){
                return u.hitSize/2;
            }else if(targetType instanceof Block u){
                return u.size * 4;
            }
            return -1;
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);

            //draw input
            boolean fallback = true;
            for(int i = 0; i < 4; i++){
                if(blends(i) && i != rotation){
                    Draw.rect(inRegion, x, y, (i * 90) - 180);
                    fallback = false;
                }
            }
            if(fallback) Draw.rect(inRegion, x, y, rotation * 90);

            Draw.rect(outRegion, x, y, rotdeg());

            Draw.z(Layer.blockOver);
            drawPayload();

            Draw.z(Layer.blockBuilding + 1);
            Draw.rect(topRegion, x, y);
            if(targetType!=null&&warmup>0 && payload!=null && hasArrived() && !conflict)Draw.draw(Layer.blockBuilding,()->{
                Draw.color(Pal.accent, warmup);
                Shaders.blockbuild.progress = progress();
                Shaders.blockbuild.time = Time.time;
                Shaders.blockbuild.region = targetType.fullIcon;

                Draw.rect(targetType.fullIcon,x,y,payload.rotation() - (targetType instanceof UnitType?90:0));
                Draw.flush();
                Draw.color();
                Shaders.blockbuild.alpha = 1f;
            });


        }
        Building DEBUG_buinding;
        boolean DEBUG_canMove,
        DEBUG_canDump;
        float destD;
        @Override
        public void moveOutPayload() {
            if(payload == null) return;

            updatePayload();

            Vec2 dest = Tmp.v1.trns(rotdeg() + destD, size * tilesize/2f);

            payRotation = Angles.moveToward(payRotation, rotdeg(), payloadRotateSpeed * delta());
            payVector.approach(dest, payloadSpeed * delta());

            Building front = md_Edge.getFacingBuild(this);
            DEBUG_buinding = front;
            boolean canDump = front == null || !front.tile.solid();
            boolean canMove = front != null && (front.block.outputsPayload || front.block.acceptsPayload);
            DEBUG_canDump = canDump;
            DEBUG_canMove = canMove;
            if(canDump && !canMove){
                pushOutput(payload, 1f - (payVector.dst(dest) / (size * tilesize / 2f)));
                destD = Mathf.sin(0.5f,0.1f);
            }else{
                destD = 0;
            }
            if(payVector.within(dest, 0.001f)){
                payVector.clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);

                if(canMove){
                    if(movePayload(payload)){
                        payload = null;
                    }
                }else if(canDump){
                    dumpPayload();
                }
            }
        }

        public boolean movePayload(Payload todump) {
            var e = md_Edge.getFacingBuild(this);
            if (e != null && e.team == this.team && e.acceptPayload(this, todump)) {
                e.handlePayload(this, todump);
                return true;
            } else {
                return false;
            }
        }

        public boolean canUpgrade(){
            if(targetType instanceof Block b){
                if(b.size > this.block.size)return false;
            }else if(targetType instanceof UnitType u){
                if(u.hitSize > this.block.size * tilesize)return false;
            }
            return true;
        }

        @Override
        public float progress() {
            return progress / 100;
        }
    }
}

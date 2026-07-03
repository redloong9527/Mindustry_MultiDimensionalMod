package mDimension.world.blocks;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Align;
import arc.util.Eachable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.ui.Fonts;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.StackConveyor;

import static mindustry.Vars.*;
import static mindustry.Vars.itemSize;

public class MulitStackConveyor extends StackConveyor {
    public TextureRegion edgeRegion1,edgeRegion2;
    public MulitStackConveyor(String name){
        super(name);
    }
    public Item[] drawItems = new Item[4];

    @Override
    public void init() {
        super.init();
        edgeRegion1 = Core.atlas.find(name + "-edge1");
        edgeRegion2 = Core.atlas.find(name + "-edge2");
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        int[] bits = getTiling(plan, list);

        if(bits == null) return;

        TextureRegion region = regions[0];
        Draw.rect(region, plan.drawx(), plan.drawy(), plan.rotation * 90);

        for(int i = 0; i < 4; i++){
            if((bits[3] & (1 << i)) == 0){
                int rot = ((plan.rotation - i) +4)%4;
                Draw.rect(rot<=1?edgeRegion1:edgeRegion2, plan.drawx(), plan.drawy(), rot * 90);
            }
        }
    }
    public class MulitStackConveyorBuild extends StackConveyorBuild{

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            if(items.total() >= getMaximumAccepted(item)) return 0;
            return !this.acceptItem(this, item) || !this.block.hasItems || source != null && source.team() != this.team ? 0 : Math.min(this.getMaximumAccepted(item), amount);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(this == source) return items.total() < itemCapacity; //player threw items
            if(cooldown > recharge - 1f) return false; //still cooling down
            return !((state != stateLoad) //not a loading dock
                    //||  (items.any() && !items.has(item)) //incompatible items
                    ||  (items.total() >= block.itemCapacity) //filled to capacity
                    ||  (front()  == source));
        }

        @Override
        public void updateTile(){
            //the item still needs to be "reeled" in when disabled
            float eff = enabled ? (efficiency + baseEfficiency) : 1f;

            //reel in crater
            if(cooldown > 0f) cooldown = Mathf.clamp(cooldown - speed * eff * delta(), 0f, recharge);

            //indicates empty state
            if(link == -1) return;

            //crater needs to be centered
            if(cooldown > 0f) return;

            //get current item
            if(lastItem == null || !items.has(lastItem)){
                lastItem = items.first();
            }

            //do not continue if disabled, will still allow one to be reeled in to prevent visual stacking
            if(!enabled) return;
            //boolean lastAny = items.any();
            if(state == stateUnload){ //unload
                if(items.any()){
                    for(int i=0;i<content.items().size;i++){
                        Item dump = content.item(i);
                        if(!items.has(dump))continue;
                        while(!outputRouter ? moveForward(dump) : dump(dump)){
                            if(!outputRouter){
                                items.remove(dump, 1);
                            }

                            if(!items.has(dump)){
                                poofOut();
                                lastItem = null;
                                break;
                            }
                        }
                    }
//
//                    if(lastAny&& !items.any()){
//                        lastItem = null;
//                        poofOut();
//                    }
                }
            }else{ //transfer
                if(state != stateLoad || (items.total() >= block.itemCapacity)){
                    if(front() instanceof MulitStackConveyorBuild e && e.team == team && e.link == -1 && e.block.itemCapacity >= this.block.itemCapacity){
                        e.items.add(items);
                        e.lastItem = lastItem;
                        e.link = tile.pos();
                        //▲ to | from ▼
                        link = -1;
                        items.clear();

                        cooldown = recharge;
                        e.cooldown = 1;
                    }
                }
            }
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return block.itemCapacity - items.total();
        }

        @Override
        public void draw(){
            Draw.z(Layer.block - 0.2f);

            Draw.rect(regions[state], x, y, rotdeg());

            for(int i = 0; i < 4; i++){
                if((blendprox & (1 << i)) == 0){
                    int rot = ((rotation - i) +4)%4;
                    Draw.rect(rot<=1?edgeRegion1:edgeRegion2, x, y, rot * 90);
                }
            }

            //draw inputs
            if(state == stateLoad){
                for(int i = 0; i < 4; i++){
                    int dir = Mathf.mod(rotation - i, 4);
                    var near = nearby(dir);
                    if((blendprox & (1 << i)) != 0 && i != 0 && near != null && !near.block.squareSprite){
                        Draw.rect(sliced(regions[0], SliceMode.bottom), x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, (float)(dir*90));
                    }
                }
            }else if(state == stateUnload){ //front unload
                //TOOD hacky front check
                if((blendprox & (1)) != 0 && front() != null && !front().block.squareSprite){
                    Draw.rect(sliced(regions[0], SliceMode.top), x + Geometry.d4x(rotation) * tilesize*0.75f, y + Geometry.d4y(rotation) * tilesize*0.75f, rotation * 90f);
                }
            }

            Draw.z(Layer.block - 0.1f);

            Tile from = world.tile(link);

            //TODO do not draw for certain configurations?
            if(glowRegion.found() && power != null && power.status > 0f){
                Draw.z(Layer.blockAdditive);
                Draw.color(glowColor, glowAlpha * power.status);
                Draw.blend(Blending.additive);
                Draw.rect(state == stateLoad ? edgeGlowRegion : glowRegion, x, y, rotation * 90);
                Draw.blend();
                Draw.color();
                Draw.z(Layer.block - 0.1f);
            }

            if(link == -1 || from == null || lastItem == null) return;

            int fromRot = from.build == null ? rotation : from.build.rotation;

            //offset
            Tmp.v1.set(from.worldx(), from.worldy());
            Tmp.v2.set(x, y);
            Tmp.v1.interpolate(Tmp.v2, 1f - cooldown, Interp.linear);

            //rotation
            float a = (fromRot%4) * 90;
            float b = (rotation%4) * 90;
            if((fromRot%4) == 3 && (rotation%4) == 0) a = -1 * 90;
            if((fromRot%4) == 0 && (rotation%4) == 3) a =  4 * 90;

            if(glowRegion.found()){
                Draw.z(Layer.blockAdditive + 0.01f);
            }

            //stack
            drawStack(Tmp.v1.x,Tmp.v1.y,Mathf.lerp(a, b, Interp.smooth.apply(1f - Mathf.clamp(cooldown * 2, 0f, 1f))) ,Mathf.lerp(Math.min((float)items.total() / itemCapacity, 1), 1f, 0.4f));
//            Draw.rect(stackRegion, Tmp.v1.x, Tmp.v1.y, Mathf.lerp(a, b, Interp.smooth.apply(1f - Mathf.clamp(cooldown * 2, 0f, 1f))));
//
//            //item
//            float size = itemSize * Mathf.lerp(Math.min((float)items.total() / itemCapacity, 1), 1f, 0.4f);
//            Drawf.shadow(Tmp.v1.x, Tmp.v1.y, size * 1.2f);
//            Draw.rect(lastItem.fullIcon, Tmp.v1.x, Tmp.v1.y, size, size, 0);
        }

        public void drawStack(float x,float y,float rotate,float sizeProcess){
            Draw.rect(stackRegion,x,y,rotate);
            float size = sizeProcess *itemSize;
            Drawf.shadow(x,y,size*1.4f);
            int amount=0;
            for(int i = 0;i<content.items().size;i++){
                var pack = content.item(i);
                if(items.has(pack)){
                    drawItems[amount] = pack;
                    amount ++;
                    if(amount >=4)break;
                }
            }
            if(amount >1) {
                for (int i = 0; i < amount; i++) {
                    Tmp.v2.trns((360f / amount) * i + (amount-1) * 45, 1f + amount * 0.08f);
                    float sizef = size * Mathf.sqrt(1f/(amount*0.8f));
                    Draw.rect(drawItems[i].fullIcon, x +Tmp.v2.x,y+Tmp.v2.y,sizef,sizef,0);
                }
            }else{
                Draw.rect(drawItems[0].fullIcon, x,y,size,size,0);
            }

            Fonts.outline.draw(items.total() +"",x,y-1.2f, Color.white,0.2f,false, Align.center);
        }
    }
}

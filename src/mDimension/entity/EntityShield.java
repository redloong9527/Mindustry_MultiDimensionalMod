package mDimension.entity;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.math.geom.Position;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mDimension.content.MD_Fx;
import mindustry.entities.Effect;
import mindustry.entities.EntityGroup;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;


public class EntityShield implements Entityc , Drawc {
    public float x;
    public float y;
    public Team team = Team.derelict;
    public Color color;
    public boolean added = false;
    public boolean broken = false;
    public int sides =4 ;
    public float buildup = 0,
            realRadius=0f,
            hit,shieldHealth = 800f,
            radius = 20f,
            shieldRotation = 45f,
            wramup = 0,
            lifeTime = 5*60f;
    private float time;
    protected transient int index__all;
    protected transient int index__draw;
    public transient int id = EntityGroup.nextId();

    public Sound hitSound = Sounds.shieldHit;
    public float hitSoundVolume = 0.1f;
    public Effect absorbEffect;
    public EntityShield(Team team){
        this.team = team;
    }
    public EntityShield(){
    }

    public void create(float x,float y,float rotation){
        create(x,y,rotation,Team.derelict,Team.derelict.color);
    }
    public void create(float x,float y,float rotation,Color color){
        create(x,y,rotation,Team.derelict,color);
    }
    public void create(float x,float y,float rotation,Team team){
        create(x,y,rotation,team,team.color);
    }
    public void create(float x,float y,float rotation,Team team,Color color){
        this.x = x;
        this.y = y;
        this.team = team;
        this.color = color;
        this.shieldRotation = rotation;
        add();
    }
    @Override
    public void update() {
        if(lifeTime <= 0||time>=lifeTime){
            broken = true;
        }else {
            time+=Time.delta;
        }
        if(wramup<1f){
            wramup += 1f / 8f * Time.delta;
            wramup = wramup>1f?1f:wramup;
        }
        realRadius = radius*(1-(1-wramup)*(1-wramup))*Math.min(1f,2*(1-(time/lifeTime)));

        if(buildup>=shieldHealth){
            broken = true;
            hit = 1f;
        }
        if(broken){
            MD_Fx.shieldBreak.at(x,y,0,color,this);
            remove();
        }else{
            deflectBullets();
            if(hit > 0f){
                hit -= 1f / 5f * Time.delta;
            }
        }
    }
    public void deflectBullets(){

        if(realRadius > 0.01f && !broken){
            Groups.bullet.intersect(x - realRadius, y - realRadius, realRadius * 2f, realRadius * 2f, bullet -> {
                if(bullet.team != team && bullet.type.absorbable && !bullet.absorbed &&
                        Intersector.isInRegularPolygon(sides, x, y, realRadius, shieldRotation, bullet.x, bullet.y)){

                    bullet.absorb();
                    hitSound.at(bullet.x, bullet.y, 1f + Mathf.range(0.1f),hitSoundVolume);
                    if(absorbEffect!=null) {
                        absorbEffect.at(bullet);
                    }else if(bullet.type.hitEffect!=null){
                        bullet.type.hitEffect.at(bullet);
                    }else if(bullet.type.despawnEffect!=null){
                        bullet.type.despawnEffect.at(bullet);
                    }
                    hit = 1f;
                    buildup += bullet.type.shieldDamage(bullet);
                }
            });
        }
    }
    @Override
    public void add() {
        if(!added) {
            this.index__draw = Groups.draw.addIndex(this);
            this.index__all = Groups.all.addIndex(this);
            added = true;
        }

    }
    @Override
    public void remove() {
        Groups.draw.remove(this);
        Groups.all.remove(this);
    }


    @Override
    public float clipSize() {
        return 50f;
    }

    public void draw(){
        if(!broken) {
            Draw.color(color, Color.white, Mathf.clamp(hit));
            Draw.z(Layer.shields + 0.01f * hit);
            Fill.poly(x, y, sides, realRadius, shieldRotation);
            Draw.reset();
        }
    }


    @Override
    public <T extends Entityc> T self() {
        return null;
    }

    @Override
    public <T> T as() {
        return null;
    }

    @Override
    public boolean isAdded() {
        return added;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public boolean serialize() {
        return false;
    }

    @Override
    public int classId() {
        return 100;
    }

    @Override
    public int id() {
        return id ;
    }

    @Override
    public void afterRead() {

    }

    @Override
    public void afterReadAll() {

    }

    @Override
    public void beforeWrite() {

    }

    @Override
    public void id(int i) {

    }

    @Override
    public void read(Reads read) {
        time = read.f();
        lifeTime = read.f();
        buildup = read.f();
        x = read.f();
        y = read.f();
        sides = read.i();
        radius = read.f();
        wramup = read.f();

    }

    @Override
    public void write(Writes write) {
        write.f(time);
        write.f(lifeTime);
        write.f(buildup);
        write.f(x);
        write.f(y);
        write.i(sides);
        write.f(radius);
        write.f(wramup);
    }

    @Override
    public Floor floorOn() {
        return null;
    }

    @Override
    public Building buildOn() {
        return null;
    }

    @Override
    public boolean onSolid() {
        return false;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public int tileX() {
        return 0;
    }

    @Override
    public int tileY() {
        return 0;
    }

    @Override
    public Block blockOn() {
        return null;
    }

    @Override
    public Tile tileOn() {
        return null;
    }

    @Override
    public void set(Position position) {

    }

    @Override
    public void set(float v, float v1) {

    }

    @Override
    public void trns(Position position) {

    }

    @Override
    public void trns(float v, float v1) {

    }

    @Override
    public void x(float v) {

    }

    @Override
    public void y(float v) {

    }
}

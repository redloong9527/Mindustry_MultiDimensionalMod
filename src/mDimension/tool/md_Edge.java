package mDimension.tool;

import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Tile;

import static mindustry.Vars.world;

public class md_Edge {
    public static Vec2[] getFacingNearby(Building b){
        if(b.block.size<=1)return new Vec2[]{new Vec2(b.x,b.y)};
        int size = b.block.size;
        Vec2[] points = new Vec2[size];
        for(int i = 0;i<size;i++){
            points[i] = transpose(new Vec2(((size-1)/2f)*8f,((size-1)/2f)*8f-i*8),b.rotation);
        }
        for(int i = 0;i<size;i++){
            points[i].add(b.x,b.y);
        }
        return points;
    }
    public static Vec2[] getFacingNearby(Block block){
        if(block.size<=1)return new Vec2[]{new Vec2(0,0)};
        int size = block.size;
        Vec2[] points = new Vec2[size];
        for(int i = 0;i<size;i++){
            points[i] = new Vec2(((size-1)/2f)*8f,((size-1)/2f)*8f-i*8);
        }
        return points;
    }
    public static Vec2[] getFacingNearby(Building b,int r){
        if(b.block.size<=1)return new Vec2[]{new Vec2(b.x,b.y)};
        int size = b.block.size;
        Vec2[] points = new Vec2[size];
        for(int i = 0;i<size;i++){
            points[i] = transpose(new Vec2(((size-1)/2f)*8f,((size-1)/2f)*8f-i*8),r);
        }
        for(int i = 0;i<size;i++){
            points[i].add(b.x,b.y);
        }
        return points;
    }

    public static Vec2 transpose(Vec2 v, int r){
        if(r == 0)return v;
        float x = v.x;
        float y = v.y;
        switch (r){
            case(1)-> v.set(-1*y,x);
            case(2)-> v.set(-1*x,-1*y);
            case(3)-> v.set(y,-1*x);
        }
        return v;
    }

    public static Vec2 direction (int r){
        switch (r){
            case(1)-> {
                return new Vec2(0,1);
            }
            case(2)-> {
                return new Vec2(-1,0);
            }
            case(3)-> {
                return new Vec2(0,-1);
            }
        }
        return new Vec2(1,0);
    }

    public static Building alignedNearby(Building b,int r){
        boolean isFind = false;
        Building cache = null;
        for(var v:getFacingNearby(b,r)){
            Vec2 rotat = direction(b.rotation);
            v.add(rotat.x*8,rotat.y*8);
            Building onBuild = world.buildWorld(v.x,v.y);
            if(onBuild == null)return null;
            if(!isFind){cache = onBuild;isFind = true;continue;}
            if(cache != onBuild)return null;
            if(onBuild.block.size < b.block.size)return null;
        }
        return cache;
    }
    public static Vec2 LimitInSquare(Vec2 v,float sideLen){
        v.x = Mathf.clamp(v.x,-sideLen/2,sideLen/2);
        v.y = Mathf.clamp(v.y,-sideLen/2,sideLen/2);
        return v;
    }

    public static int[] isInDiagonal(Building b, Tile t){
        if(t == null || b == null)return new int[]{-1};
        if(
                Math.abs(
                        Math.abs(b.x-t.worldx())-Math.abs(b.y-t.worldy())
                )<0.01f
        ){
            if(t.worldx()>b.x && t.worldy()>b.y)return new int[]{2,3};
            if(t.worldx()<b.x && t.worldy()>b.y)return new int[]{0,3};
            if(t.worldx()>b.x && t.worldy()<b.y)return new int[]{1,2};
            if(t.worldx()<b.x && t.worldy()<b.y)return new int[]{0,1};
        }

        return new int[]{-1};
    }

    public static Tile[] getFacingTile(Building b){
        int size=b.block.size;
        int r = (b.rotation+1)%4;
        int dx = Geometry.d4x(r);
        int dy = Geometry.d4y(r);
        int l = size/2+1;

        int ox = f4[b.rotation].x*l;
        int oy = f4[b.rotation].y*l;
        if(b.block.size %2 == 0) {
            ox+=f4i[b.rotation].x;
            oy+=f4i[b.rotation].y;
        }
        var res = new Tile[size];
        for(int i=0;i<size;i++){
            int wx = b.tile.x + ox + dx*i;
            int wy = b.tile.y + oy + dy*i;
            res[i] = world.tile(wx,wy);
        }

        return res;
    }
    public static Building getAllFacingBuild(Building b){
        Building res = null;
        for(var t:getFacingTile(b)){
            Building other = t.build;
            if(other ==null)return null;
            if(res == null){
                res = other;
            }else if(res != other){
                return null;
            }
        }
        return res;
    }

    public static final Point2[] f4=new Point2[]{
            new Point2(1,-1),
            new Point2(1,1),
            new Point2(-1,1),
            new Point2(-1,-1)
    };

    ///c x | x x
    ///c x | x x
    ///----o----
    ///c x | x x
    ///c c | c c

    public static final Point2[] f4i=new Point2[]{
            new Point2(0,2),
            new Point2(-1,0),
            new Point2(1,-1),
            new Point2(2,1)
    };
}

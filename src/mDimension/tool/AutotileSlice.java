package mDimension.tool;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.world.Block;

public class AutotileSlice {
    public static final int[][] pos = new int[][]{
            {0,0},{1,0},{2,0},{3,0},{4,0},{5,0},{6,0},{7,0},{8,0},{9,0},{10,0},{11,0},
            {0,1},{1,1},{2,1},{3,1},{4,1},{5,1},{6,1},{7,1},{8,1},{9,1},{10,1},{11,1},
            {0,2},{1,2},{2,2},{3,2},{4,2},{5,2},{6,2},{7,2},{8,2},{9,2},{10,2},{11,2},
            {0,3},{1,3},{2,3},{3,3},{4,3},{5,3},{6,3},{7,3},{8,3},{9,3},{10,3}

    };

    public static TextureRegion[] sliceAndPack(Block b){
        return sliceAndPack(b.name+"-tiled",b.name);
    }
    public static TextureRegion[] sliceAndPack(String allRegion,String name){
        TextureRegion all = Core.atlas.find(allRegion);
        TextureRegion[] res = new TextureRegion[47];
        for(int i=0;i<47;i++){

            res[i] = slice(all,pos[i][0]*32+1,pos[i][1]*32+1,32,32);
        }
        return res;
    };

    public static TextureRegion slice(TextureRegion all,int x,int y,int w,int h){
        return new TextureRegion(all,x,y,w,h);
    }

}

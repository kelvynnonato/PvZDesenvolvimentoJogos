package io.github.PlantsVsZombies10;

import com.badlogic.gdx.math.Vector2;

public class FrontyardGrid {

    private final int cols;
    private final int rows;
    private final float tileW;
    private final float tileH;
    private final float offsetX;
    private final float offsetY;

    public FrontyardGrid(int cols, int rows, float tileW, float tileH, float offsetX, float offsetY) {
        this.cols = cols;
        this.rows = rows;
        this.tileW = tileW;
        this.tileH = tileH;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public int   getCols()    { return cols; }
    public int   getRows()    { return rows; }
    public float getTileW()   { return tileW; }
    public float getTileH()   { return tileH; }
    public float getOffsetX() { return offsetX; }
    public float getOffsetY() { return offsetY; }
    public float getTotalWidth(){return cols * tileW;}
    public float getTotalHeight(){return rows * tileH;}

    public boolean worldToTile(float worldX, float worldY, int[] outCol, int[] outRow) {
        int col = (int)((worldX-offsetX)/tileW);
        int row = (int)((worldY-offsetY)/tileH);

        if(col < 0 || col >= cols || row < 0 || row >= rows)
            return false;

        outCol[0] = col;
        outRow[0] = row;
        return true;
    }

    public Vector2 tileBottomLeft(int col, int row){
        return new Vector2(offsetX + col * tileW, offsetY + row * tileH);
    }

    public Vector2 snap(float worldX, float worldY){
        int[] col = new int[1];
        int[] row = new int[1];

        if(!worldToTile(worldX, worldY, col, row))
            return null;

        return tileBottomLeft(col[0], row[0]);
    }
}

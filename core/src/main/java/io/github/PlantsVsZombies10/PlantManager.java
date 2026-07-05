package io.github.PlantsVsZombies10;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class PlantManager {

    //Grid de plantas - [col][row], null = vazio
    private final Plant[][] grid;
    private final int cols, rows;

    private final FrontyardGrid frontyardGrid;
    private Texture peashooterTexture;
    private Sound plant;
    private AssetManager assetManager;

    public PlantManager(FrontyardGrid grid, AssetManager assetManager) {
        this.frontyardGrid = grid;
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.grid = new Plant[cols][rows];
        this.assetManager = assetManager;

        if(plant == null){
            plant = assetManager.get("sounds/affects/plant.ogg", Sound.class);
        }
    }

    public void setTextures(Texture peashooterTexture) {
        this.peashooterTexture = peashooterTexture;
    }

    public boolean plantar(int col, int row, SeedSlot slot){
        if(col < 0 || col >= cols || row < 0 || row >= rows){ return false;}
        if(grid[col][row] != null){ return false;} // tile ocupado

        Plant peashooter = new Plant(peashooterTexture, 0.2f, 300, 100, 1.5f);

        //Centraliza no tile
        float tileX = frontyardGrid.getOffsetX() + col * frontyardGrid.getTileW();
        float tileY = frontyardGrid.getOffsetY() + row * frontyardGrid.getTileH();
        peashooter.x = tileX + (frontyardGrid.getTileW()  - peashooter.getWidth())  / 2f;
        peashooter.y = tileY + (frontyardGrid.getTileH() - peashooter.getHeight()) / 2f;

        peashooter.tileCol = col;
        peashooter.tileRow = row;

        grid[col][row] = peashooter;

        plant.play();
        return true;
    }

    // Atualiza todas as plantas
    // Retorna array com posicoes de disparos
    public Array<float[]> update(float delta){
        Array<float[]> shots = new Array<>();

        for(int c = 0; c < cols; c++){
            for(int r = 0; r < rows; r++){
                Plant peashooter = grid[c][r];

                if(peashooter == null) continue;

                if(!peashooter.isAlive()){
                    grid[c][r] = null;
                }

                boolean shoot = peashooter.update(delta);
                if(shoot){
                    shots.add(new float[]{peashooter.getMuzzleX(), peashooter.getMuzzleY()});
                }
            }
        }

        return shots;
    }

    public void render(SpriteBatch batch) {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (grid[c][r] != null) grid[c][r].render(batch);
            }
        }
    }

    public boolean isTileOccupied(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return false;
        return grid[col][row] != null;
    }

    public Plant getPlant(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return null;
        return grid[col][row];
    }

    // Expõe todas as plantas vivas
    public Array<Plant> getAllPlants() {
        Array<Plant> list = new Array<>();
        for (int c = 0; c < cols; c++)
            for (int r = 0; r < rows; r++)
                if (grid[c][r] != null) list.add(grid[c][r]);
        return list;
    }
}

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
    private Texture sunflowerTexture;
    private Sound plant;
    private AssetManager assetManager;

    // Posições (x, y, quantidade) onde um sol de girassol nasceu neste frame.
    // O World é quem efetivamente cria o SunDrop visual a partir disso.
    private final Array<float[]> pendingSunSpawns = new Array<>();
    private static final float SUNFLOWER_SCALE = 0.12f; // ajuste aqui o tamanho do girassol
    private static final float SUNFLOWER_PRODUCTION_COOLDOWN = 20f; // segundos entre cada sol gerado
    private static final int   SUNFLOWER_SUN_AMOUNT = 25;

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

    public void setTextures(Texture peashooterTexture, Texture sunflowerTexture) {
        this.peashooterTexture = peashooterTexture;
        this.sunflowerTexture = sunflowerTexture;
    }

    // Fábrica de plantas - cria a instância correta de acordo com o tipo do slot
    private Plant criarPlanta(SeedSlot slot){
        switch (slot.type){
            case SUNFLOWER: {
                Plant sunflower = new Plant(sunflowerTexture, 0.12f, 300, slot.cost, 5f, Plant.PlantType.SUNFLOWER);
                sunflower.sunAmount = 25; // quanto sol ela gera a cada ciclo
                return sunflower;
            }
            case PEASHOOTER:
            default:
                return new Plant(peashooterTexture, 0.18f, 300, slot.cost, 1.5f, Plant.PlantType.PEASHOOTER);
        }
    }

    public boolean plantar(int col, int row, SeedSlot slot){
        if(col < 0 || col >= cols || row < 0 || row >= rows){ return false;}
        if(grid[col][row] != null){ return false;} // tile ocupado

        Plant novaPlanta = criarPlanta(slot);
        if(novaPlanta == null) return false;

        //Centraliza no tile
        float tileX = frontyardGrid.getOffsetX() + col * frontyardGrid.getTileW();
        float tileY = frontyardGrid.getOffsetY() + row * frontyardGrid.getTileH();
        novaPlanta.x = tileX + (frontyardGrid.getTileW()  - novaPlanta.getWidth())  / 2f;
        novaPlanta.y = tileY + (frontyardGrid.getTileH() - novaPlanta.getHeight()) / 2f;

        novaPlanta.tileCol = col;
        novaPlanta.tileRow = row;

        grid[col][row] = novaPlanta;

        plant.play();
        return true;
    }

    // Atualiza todas as plantas
    // Retorna array com posicoes de disparos
    public Array<float[]> update(float delta){
        Array<float[]> shots = new Array<>();
        pendingSunSpawns.clear();

        for(int c = 0; c < cols; c++){
            for(int r = 0; r < rows; r++){
                Plant planta = grid[c][r];

                if(planta == null) continue;

                if(!planta.isAlive()){
                    grid[c][r] = null;
                    continue;
                }

                boolean ciclo = planta.update(delta);
                if(!ciclo) continue;

                switch (planta.type){
                    case PEASHOOTER:
                        shots.add(new float[]{planta.getMuzzleX(), planta.getMuzzleY()});
                        break;
                    case SUNFLOWER:
                        float centerX = planta.x + planta.getWidth()  / 2f;
                        float centerY = planta.y + planta.getHeight() / 2f;
                        pendingSunSpawns.add(new float[]{centerX, centerY, planta.sunAmount});
                        break;
                }
            }
        }

        return shots;
    }

    // Sóis que nasceram no último update(). O chamador deve consumir isso a cada frame.
    public Array<float[]> getPendingSunSpawns(){
        return pendingSunSpawns;
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

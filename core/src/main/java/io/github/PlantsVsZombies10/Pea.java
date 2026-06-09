package io.github.PlantsVsZombies10;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Pea implements Pool.Poolable {
    public Vector2 position;
    public boolean alive;
    private static Texture pea;
    private static String PEA_PATH = "Bullets/pea-shooted.png";
    private static Texture icePea;
    private static String ICE_PEA_PATH = "Bullets/pea-shooted.png";
    private static Texture firePea;
    private static String FIRE_PEA_PATH = "Bullets/pea-shooted.png";
    private AssetManager assetManager;
    private Sound shoot;
    boolean ervilhasMontada = false;


    public Pea() {
        this.position = new Vector2();
        this.alive = false;
    }

    public void init(float posX, float posY, AssetManager assetManager) {
        position.set(posX, posY);
        alive = true;
        this.assetManager = assetManager;
        if(!ervilhasMontada){
            ervilhasMontada = true;
            pea = assetManager.get(PEA_PATH, Texture.class);
            icePea = assetManager.get(ICE_PEA_PATH, Texture.class);
            firePea = assetManager.get(FIRE_PEA_PATH, Texture.class);
            shoot = assetManager.get("sounds/affects/firepea.ogg", Sound.class);
        }
        shoot.play();
    }

    @Override
    public void reset() {
        position.set(0,0);
        alive = false;
    }

    public void update(float delta){
        position.add(600*delta, 0);

        if(position.x > Gdx.graphics.getWidth() || position.x < 0 ||
            position.y > Gdx.graphics.getHeight() || position.y < 0) {
            alive = false;
        }
    }

    public void render(SpriteBatch batch, float scale) {
        batch.draw(pea, position.x, position.y,
            pea.getWidth() * scale,
            pea.getHeight() * scale);
    }

    public float getCenterX(float scale) {
        return position.x + (pea.getWidth() * scale) / 2f;
    }

    public float getCenterY(float scale) {
        return position.y + (pea.getHeight() * scale) / 2f;
    }

    public float getRadius(float scale) {
        return Math.min(pea.getWidth(), pea.getHeight()) * scale / 2f;
    }
}


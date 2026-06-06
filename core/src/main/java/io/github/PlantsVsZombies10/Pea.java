package io.github.PlantsVsZombies10;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Pea implements Pool.Poolable {
    public Vector2 position;
    public boolean alive;

    public Pea() {
        this.position = new Vector2();
        this.alive = false;
    }

    public void init(float posX, float posY) {
        position.set(posX, posY);
        alive = true;
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

    public void render(SpriteBatch batch, Texture texture, float scale) {
        batch.draw(texture, position.x, position.y,
            texture.getWidth() * scale,
            texture.getHeight() * scale);
    }
}


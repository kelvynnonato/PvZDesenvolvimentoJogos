package io.github.PlantsVsZombies10;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Plant {
    private final Texture texture;
    private final float scale;

    public Plant(Texture texture, float scale) {
        this.texture = texture;
        this.scale = scale;
    }

    public float getWidth()  { return texture.getWidth()  * scale; }
    public float getHeight() { return texture.getHeight() * scale; }

    public void render(SpriteBatch batch, float x, float y) {
        batch.draw(texture, x, y, getWidth(), getHeight());
    }
}

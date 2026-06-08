package io.github.PlantsVsZombies10;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool;

public class Particle implements Pool.Poolable {
    public float x, y;
    public float velX, velY;
    public float alpha;
    public float size;
    public boolean alive;
    public Color cor;

    private static final float LIFETIME = 0.6f;
    private float timer = 0f;

    public void init(float x, float y, Color cor) {
        this.x = x;
        this.y = y;
        this.alpha = 1f;
        this.size = (float)(Math.random() * 8 + 4);
        this.alive = true;
        this.timer = 0f;
        this.cor = cor.cpy();

        float angle = (float)(Math.random() * Math.PI * 2);
        float speed = (float)(Math.random() * 150 + 50);
        this.velX = (float)(Math.cos(angle) * speed);
        this.velY = (float)(Math.sin(angle) * speed);
    }

    public void update(float delta) {
        timer += delta;
        if (timer >= LIFETIME) {
            alive = false;
            return;
        }

        x += velX * delta;
        y += velY * delta;
        velX *= 0.95f;
        velY *= 0.95f;
        alpha = 1f - (timer / LIFETIME);
    }

    @Override
    public void reset() {
        x = y = velX = velY = alpha = size = timer = 0f;
        alive = false;
        cor = null;
    }
}


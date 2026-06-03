package io.github.PlantsVsZombies10;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ParticleManager {
    private final Array<Particle> activeParticles = new Array<>();
    private final Pool<Particle> particlePool = new Pool<Particle>() {
        @Override
        protected Particle newObject() { return new Particle(); }
    };
    private final ShapeRenderer shapeRenderer;

    public ParticleManager() {
        shapeRenderer = new ShapeRenderer();
    }

    public void spawn(float x, float y, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            Particle p = particlePool.obtain();
            p.init(x, y);
            activeParticles.add(p);
        }
    }

    public void update(float delta) {
        for (int i = activeParticles.size - 1; i >= 0; i--) {
            Particle p = activeParticles.get(i);
            p.update(delta);
            if (!p.alive) {
                activeParticles.removeIndex(i);
                particlePool.free(p);
            }
        }
    }

    public void render(OrthographicCamera cam) {
        if (activeParticles.size == 0) return;

        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : activeParticles) {
            shapeRenderer.setColor(new Color(0.9f, 0.3f, 0.3f, p.alpha));
            shapeRenderer.circle(p.x, p.y, p.size);
        }
        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}


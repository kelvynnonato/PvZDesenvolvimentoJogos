package io.github.PlantsVsZombies10;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class SunDropManager {
    private final Array<SunDrop> activeSuns = new Array<>();
    private final Pool<SunDrop> sunPool = new Pool<SunDrop>() {
        @Override
        protected SunDrop newObject() { return new SunDrop(); }
    };

    private final ShapeRenderer shape = new ShapeRenderer();

    // Distância que o sol cai a partir de onde nasceu, até "pousar"
    private static final float QUEDA_ALTURA = 45f;

    // Variação aleatória na posição de nascimento, pra não sair sempre no mesmo pixel
    private static final float OFFSET_RANGE_X = 28f;
    private static final float OFFSET_RANGE_Y = 18f;

    public void spawn(float x, float y, int sunAmount) {
        float spawnX = x + MathUtils.random(-OFFSET_RANGE_X, OFFSET_RANGE_X);
        float spawnY = y + MathUtils.random(-OFFSET_RANGE_Y, OFFSET_RANGE_Y);

        SunDrop sun = sunPool.obtain();
        sun.init(spawnX, spawnY, spawnY - QUEDA_ALTURA, sunAmount);
        activeSuns.add(sun);
    }

    public void update(float delta) {
        for (int i = activeSuns.size - 1; i >= 0; i--) {
            SunDrop sun = activeSuns.get(i);
            sun.update(delta);
            if (!sun.alive) {
                activeSuns.removeIndex(i);
                sunPool.free(sun);
            }
        }
    }

    /**
     * Tenta coletar um sol na posição de mundo informada.
     * Retorna a quantidade de sol coletada (0 se não acertou nenhum).
     */
    public int tryCollect(float worldX, float worldY) {
        for (int i = 0; i < activeSuns.size; i++) {
            SunDrop sun = activeSuns.get(i);
            if (sun.isCollectable() && sun.isHit(worldX, worldY)) {
                int amount = sun.sunAmount;
                sun.collect();
                return amount;
            }
        }
        return 0;
    }

    public void render(OrthographicCamera cam) {
        if (activeSuns.size == 0) return;

        shape.setProjectionMatrix(cam.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (SunDrop sun : activeSuns) {
            float alpha = sun.getAlpha();

            // círculo externo (amarelo mais forte) + núcleo mais claro no centro,
            // pra dar uma sensação de brilho sem precisar de textura
            shape.setColor(1f, 0.82f, 0.05f, alpha);
            shape.circle(sun.x, sun.y, SunDrop.RADIUS);

            shape.setColor(1f, 0.95f, 0.55f, alpha);
            shape.circle(sun.x, sun.y, SunDrop.RADIUS * 0.55f);
        }
        shape.end();
    }

    public void dispose() {
        shape.dispose();
    }
}

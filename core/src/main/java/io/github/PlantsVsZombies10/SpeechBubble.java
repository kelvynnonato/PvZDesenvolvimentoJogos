package io.github.PlantsVsZombies10;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SpeechBubble {
    private static NinePatch patch;
    private static BitmapFont font;
    private static GlyphLayout layout;

    private float x, y;
    private float timer;
    public boolean alive;

    private static final float DURATION = 1f;
    private static final float PADDING  = 10f;
    private static final String TEXT    = "Miolos...";

    private static ShapeRenderer shapeRenderer;


    public SpeechBubble() {
        if (patch == null) {
            // pixmap do balão com NinePatch
            int size = 16;
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

            pixmap.setColor(Color.WHITE);
            pixmap.fillRectangle(0, 0, size, size);

            pixmap.setColor(Color.BLACK);
            pixmap.drawRectangle(0, 0, size, size);

            Texture texture = new Texture(pixmap);
            pixmap.dispose();

            // bordas
            patch = new NinePatch(texture, 2, 2, 2, 2);
        }

        if (font == null) {
            font = new BitmapFont();
            font.setColor(Color.BLACK);
        }

        if (layout == null) {
            layout = new GlyphLayout();
        }

        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer();
        }
    }

    public void init(float laneY) {
        layout.setText(font, TEXT);

        float bubbleW = layout.width  + PADDING * 2;
        float bubbleH = layout.height + PADDING * 2;

        this.x = Gdx.graphics.getWidth() - bubbleW - 20f;
        this.y = laneY;

        this.timer = 0f;
        this.alive = true;
    }

    public void update(float delta) {
        if (!alive) return;

        timer += delta;
        if (timer >= DURATION) {
            alive = false;
        }
    }

    public void render(SpriteBatch batch, OrthographicCamera cam) {
        if (!alive) return;

        float bubbleW = layout.width  + PADDING * 2;
        float bubbleH = layout.height + PADDING * 2;

        patch.draw(batch, x, y, bubbleW, bubbleH);

        font.draw(batch, TEXT,
            x + PADDING,
            y + PADDING + layout.height
        );

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);

        float pontaX = x + bubbleW + 15f;
        float meioY = y + bubbleH / 2f;
        float baseX  = x + bubbleW;

        shapeRenderer.triangle(
            baseX, meioY + 8f,  // canto superior da base
            baseX, meioY - 8f,  // canto inferior da base
            pontaX, meioY        // ponta
        );

        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.triangle(
            baseX, meioY + 8f,
            baseX, meioY - 8f,
            pontaX, meioY
        );
        shapeRenderer.end();

        // reabre o batch para o resto do jogo continuar desenhando
        shapeRenderer.setProjectionMatrix(cam.combined);
        batch.begin();
    }

    public void dispose() {
        if (font != null) font.dispose();
    }
}

package io.github.PlantsVsZombies10;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Renderiza o grid visualmente para calibração.
 * USE APENAS DURANTE DESENVOLVIMENTO — remova quando o grid estiver calibrado.
 *
 * Como usar:
 *   1. Adicione no Main: private GridDebugRenderer debugRenderer;
 *   2. Em onAssetsLoaded(): debugRenderer = new GridDebugRenderer();
 *   3. No render(), APÓS batch.end():
 *        debugRenderer.render(cam, grid);
 *   4. Ajuste as constantes GRID_* no Main até as linhas ficarem sobre a grama.
 *   5. Quando estiver bom, remova tudo relacionado ao GridDebugRenderer.
 */
public class GridDebugRenderer {

    private final ShapeRenderer shape;

    public GridDebugRenderer() {
        shape = new ShapeRenderer();
    }

    public void render(OrthographicCamera cam, FrontyardGrid grid) {
        shape.setProjectionMatrix(cam.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);

        // Borda externa do grid (amarelo)
        shape.setColor(Color.YELLOW);
        shape.rect(
            grid.getOffsetX(),
            grid.getOffsetY(),
            grid.getTotalWidth(),
            grid.getTotalHeight()
        );

        // Linhas internas dos tiles (ciano semi-transparente)
        shape.setColor(0f, 1f, 1f, 0.5f);
        for (int col = 0; col <= grid.getCols(); col++) {
            float x = grid.getOffsetX() + col * grid.getTileW();
            shape.line(x, grid.getOffsetY(),
                x, grid.getOffsetY() + grid.getTotalHeight());
        }
        for (int row = 0; row <= grid.getRows(); row++) {
            float y = grid.getOffsetY() + row * grid.getTileH();
            shape.line(grid.getOffsetX(), y,
                grid.getOffsetX() + grid.getTotalWidth(), y);
        }

        shape.end();
    }

    public void dispose() {
        shape.dispose();
    }
}


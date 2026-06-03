package io.github.PlantsVsZombies10;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class LoadingScreen {
    private final AssetManager assetManager;
    private final float minTime;
    private float elapsed = 0.0f;

    private final SpriteBatch batch;
    private final Texture background, logo, grama, terra, rolo;

    // timer interto para rotação do rolo - nunca zera, apenas acumula
    private float roloAngle = 0.0f;
    private final float roloSpeed = 200f; // graus por segundo

    private final TextureRegion gramaRegion;

    public LoadingScreen(AssetManager assetManager, float minTime){
        this.assetManager = assetManager;
        this.minTime = minTime;
        this.batch = new SpriteBatch();

        background = new Texture("Loading/FundoLoading.png");
        logo = new Texture("Loading/Logo.png");
        grama = new Texture("Loading/Grama.png");
        terra = new Texture("Loading/Terra.png");
        rolo = new Texture("Loading/RoloGrama.png");
        gramaRegion = new TextureRegion(grama, 0, 0, grama.getWidth(), grama.getHeight());

    }

    public boolean update(float delta){
        elapsed += delta;

        //atualizando a rotação do rolo - subtrai o intervale, nunca vai zerar
        roloAngle += delta * roloSpeed;
        if (roloAngle >= 360.0f){roloAngle -= 360.0f;}

        assetManager.update();
        return assetManager.isFinished() && elapsed >= minTime;
    }

    public void render() {
        float progress = assetManager.getProgress();
        float timeProgress = Math.min(elapsed / minTime, 1.0f);
        float finalProgress = Math.min(progress, timeProgress);

        //smoothstep para termos uma animação suave
        float smooth = finalProgress * finalProgress * (3.0f - 2.0f * finalProgress);

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        //fazendo as dimensões da bara
        float barWidth = screenW * 0.6f;
        float barX = (screenW - barWidth) / 2f;
        float barY = screenH * 0.1f;

        //dimensões da terra (fundo da barra)
        float terraH = barWidth * ((float) terra.getHeight() / terra.getWidth());

        //dimensões da grama (ela fica em cima da terra)
        float gramaH = barWidth * ((float) grama.getHeight() / grama.getWidth());
        float gramaY = barY + terraH - gramaH * 0.45f; // ficando um pouco sobreposta na terra

        //dimensões do rolo
        float roloSize = Math.max(terraH * 1.4f * (1.0f - smooth), 1.0f); // um pouco maior que a barra,
        float roloX = barX + (barWidth * smooth) - roloSize / 2.0f; // diminui conforme avança o progresso, limitado
        float roloY = (barY + terraH) - roloSize * 0.4f;

        batch.begin();

        //fundo do loading
        batch.draw(background, 0, 0, screenW, screenH);

        //logo centralizada no topo
        float logoW = screenW * 0.80f;
        float logoH = logoW * ((float) logo.getHeight() / logo.getWidth());
        batch.draw(logo, (screenW - logoW) / 2.0f, screenH * 0.77f, logoW, logoH);

        //terra (fundinho da barra)
        batch.draw(terra, barX, barY, barWidth, terraH);

        //grama que fica aparecendo da esquerda até onde o rolo está
        gramaRegion.setRegion(0, 0, (int)(grama.getWidth() * smooth), grama.getHeight());
        batch.draw(gramaRegion, barX - 5.0f, gramaY, barWidth * smooth, gramaH);

        //rolo girando na ponta da grama
        batch.draw(
            rolo,
            roloX, roloY,               //posição
            roloSize / 2f, roloSize / 2f,   // origem (centro para rotação)
            roloSize, roloSize,         // tamanho
            1.0f, 1.0f,          // escala
            roloAngle,                  //ângulo de rotação
            0, 0,                  //região da textura
            rolo.getWidth(), rolo.getHeight(),
            false, false           //flip
        );
        batch.end();
    }

    public void dispose(){
        batch.dispose();
        background.dispose();
        logo.dispose();
        grama.dispose();
        terra.dispose();
        rolo.dispose();
    }
}


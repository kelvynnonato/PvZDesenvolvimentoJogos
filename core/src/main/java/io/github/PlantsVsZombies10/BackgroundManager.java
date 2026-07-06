package io.github.PlantsVsZombies10;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static com.badlogic.gdx.math.MathUtils.clamp;

public class BackgroundManager {
    private final Texture[] backgrounds;
    private int currentBackground = 1;
    private float bgTimer = 0f;
    private float bgAlpha = 1f;
    private boolean fading = false;
    private static Music day, night;
    private static String SOUND_PATH_DAY = "sounds/music/04. Grasswalk.mp3";
    private static String SOUND_PATH_NIGHT = "sounds/music/Main Music 01.mp3";
    private boolean musicSwitched = false;

    private final float bgInterval;
    private final float fadeDuration;
    private final float offsetX;

    private AssetManager assetManager;

    public BackgroundManager(Texture[] backgrounds, float bgInterval, float offsetX, float fadeDuration, AssetManager assetManager) {
        this.backgrounds = backgrounds;
        this.bgInterval = bgInterval;
        this.offsetX = offsetX;
        this.fadeDuration = fadeDuration;
        this.assetManager = assetManager;

        if (day == null && night == null) {
            day = assetManager.get(SOUND_PATH_DAY, Music.class);
            night = assetManager.get(SOUND_PATH_NIGHT, Music.class);
        }

        night.setLooping(true);
        night.setVolume(2.0f);
        night.play();
    }

    public boolean isDay() {
        return currentBackground == 1;
    }

    public void update(float delta) {
        bgTimer += delta;

        if (bgTimer >= bgInterval && !fading) {
            fading = true;
            bgTimer =- bgTimer;
            bgAlpha = 1f;
            musicSwitched = false; // reseta o flag ao iniciar o fade
        }

        if (fading) {
            bgAlpha -= delta / fadeDuration;
            bgAlpha = clamp(bgAlpha, 0f, 1f);

            // troca a música no meio do fade (quando alpha chega em 0.4)
            if (!musicSwitched && bgAlpha <= 0.4f) {
                musicSwitched = true;
                updateMusic();
            }

            if (bgAlpha <= 0f) {
                fading = false;
                currentBackground = (currentBackground + 1) % backgrounds.length;
                bgAlpha = 1f;
            }
        }
    }

    private void updateMusic() {
        int next = (currentBackground + 1) % backgrounds.length;
        if (next == 1) {
            night.stop();
            day.setLooping(true);
            day.setVolume(2.0f);
            day.play();
        } else {
            day.stop();
            night.setLooping(true);
            night.setVolume(2.0f);
            night.play();
        }
    }

    public void render(SpriteBatch batch) {
        int nextBackground = (currentBackground + 1) % backgrounds.length;

        float smoothAlpha = bgAlpha * bgAlpha * (3f - 2f * bgAlpha);

        //fundo seguinte abaixo
        batch.setColor(1, 1, 1, 1f);
        batch.draw(backgrounds[nextBackground], offsetX, 0);

        //fundo atual por cima com o alpha reduzindo
        batch.setColor(1, 1, 1, smoothAlpha);
        batch.draw(backgrounds[currentBackground], offsetX, 0);

        //reseta a cor para não afetar os outros sprites
        batch.setColor(1, 1, 1, 1f);
    }
}

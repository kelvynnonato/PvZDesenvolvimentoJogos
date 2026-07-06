package io.github.PlantsVsZombies10;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Zombie implements Pool.Poolable{
    private AssetManager assetManager;
    public Vector2 position;
    public Vector2 speed;
    public boolean alive;
    private static Sound brainEatenSound;
    private static final String SOUND_PATH = "sounds/affects/SFX chompsoft.ogg";

    private boolean corpoMontado = false;
    private static TextureRegion cabecaZumbi;
    private static TextureRegion bocaZumbi;
    private static TextureRegion cabeloZumbi;

    private static final String SPRITESHEET_PATH = "Zombies/PC _ Computer - Plants vs. Zombies - Zombies - Regular Zombies.png";

    private float animTimer = 0f;
    private float animIntervalo = 1.0f; // velocidade da boca
    private float bocaOffsetY = 0f;
    private boolean bocaAberta = false;

    private boolean comendo = false; // ativado futuramente pelas plantas
    private float comerTimer = 0f;
    private final float comerIntervalo = 0.15f; // mais rápido que o normal
    public boolean comeuCerebro;

    private boolean animPhase = false; // false = fase 1, true = fase 2

    private static final float SPEED = 300f;

    public float danoTimer = 0f;

    public Zombie() {
        this.position = new Vector2();
        this.speed = new Vector2();
        this.alive = false;
    }

    public void init(float posX, float posY, float targetX, float targetY, AssetManager assetManager) {
        position.set(posX, posY);
        alive = true;
        this.assetManager = assetManager;
        comeuCerebro = false;

        float dx = targetX - posX;
        float dy = targetY - posY;
        float length = (float) Math.sqrt(dx*dx + dy*dy);

        speed.set((dx/length) * SPEED, (dy/length) * SPEED);

        if (brainEatenSound == null) {
            brainEatenSound = assetManager.get(SOUND_PATH, Sound.class);;
        }

        if (!corpoMontado) {
            corpoMontado = true;
            Texture spritesheet = assetManager.get(SPRITESHEET_PATH, Texture.class);

            cabecaZumbi = new TextureRegion(spritesheet, 1, 9, 53, 48);
            bocaZumbi   = new TextureRegion(spritesheet, 1, 86, 32, 15);
            cabeloZumbi   = new TextureRegion(spritesheet, 1, 120, 64, 31);
        }
    }


    @Override
    public void reset() {
        position.set(0,0);
        speed.set(0,0);
        alive = false;
        comeuCerebro = false;
        danoTimer -= danoTimer;
    }
    /**
     * Método chamado em cada frame (no método render)
     que atualiza a posição da bala.
     */
    public void update (float delta) {
        // se estiver fora da tela então para de atualizar

        animTimer += delta;
        if (animTimer >= animIntervalo) {
            animTimer -= animIntervalo;
            animPhase = !animPhase;

            if (!comendo) {
                // boca devagar
                animIntervalo = 1f;
                bocaAberta = animPhase;
                bocaOffsetY = bocaAberta ? -9f : -5f;
            } else {
                animIntervalo = 0.2f;
                bocaAberta = animPhase;
                bocaOffsetY = bocaAberta ? -9f : -5f;
            }
        }

        float scale = 1.5f;
        float cabecaW = 53 * scale;
        float cabecaH = 48 * scale;
        float cabecaX = position.x;
        float cabecaY = position.y + 20f;

        float bocaW = 32 * scale;
        float bocaH = 15 * scale;
        float bocaX = position.x + (cabecaW - bocaW) / 2f;
        float bocaY = position.y + 20f + bocaOffsetY * scale;
        if (isOutOfScreen()) {
            alive = false;
        } else {
            //posicao da bala
            position.add(speed.x * delta, speed.y * delta);
        }
    }

    public void render(SpriteBatch batch, float scale) {
        if (cabecaZumbi == null) return;

        float cabecaW = 53 * scale;
        float cabecaH = 48 * scale;
        float bocaW   = 32 * scale;
        float bocaH   = 15 * scale;
        float cabeloW = cabecaW * (64f / 53f);
        float cabeloH = cabecaH * (31f / 48f);

        // cabeça sobe
        batch.draw(cabecaZumbi, position.x, position.y + 20f, cabecaW, cabecaH);

        // cabelo acompanha a cabeça
        batch.draw(cabeloZumbi,
            position.x - (cabeloW - cabecaW) / 2f,
            position.y + 3f + cabecaH * 0.75f, // mesmo offset da cabeça + o do cabelo
            cabeloW, cabeloH
        );

        // boca acompanha também
        float bocaX = position.x + (cabecaW - bocaW) / 2f;
        float bocaY = position.y + 20f + bocaOffsetY * scale;
        batch.draw(bocaZumbi, bocaX, bocaY, bocaW, bocaH);
    }


    private boolean isOutOfScreen() {
        return position.x > Gdx.graphics.getWidth() + 500  ||
            position.x < -500 ||
            position.y > Gdx.graphics.getHeight() + 500 ||
            position.y < -500;
    }

    // hitbox retangular — mantida para a hitbox comum
    private boolean CollidingWithBrain(float brainX, float brainY, float brainW, float brainH) {
        return position.x < brainX + brainW && (position.x + 80) > brainX &&
            position.y < brainY + brainH && (position.y + 97) > brainY;
    }

    // hitbox circular — nova
    private boolean CollidingWithBrainCircle(float retX, float retY, float retW, float retH,
                                             float cirX, float cirY, float raio) {
        // ponto mais próximo do círculo dentro do retângulo
        float closestX = Math.max(retX, Math.min(cirX, retX + retW));
        float closestY = Math.max(retY, Math.min(cirY, retY + retH));

        float dx = cirX - closestX;
        float dy = cirY - closestY;

        return (float) Math.sqrt(dx * dx + dy * dy) <= raio;
    }

    public float getBocaX(float scale) {
        float cabecaW = 53 * scale;
        float bocaW   = 32 * scale;
        return position.x + (cabecaW - bocaW) / 2f;
    }

    public float getBocaY(float scale) {
        return position.y + 20f + bocaOffsetY * scale;
    }
    public float getBocaOffsetY(float scale) {
        return bocaOffsetY * scale;
    }

    public float getBocaW(float scale) { return 32 * scale; }
    public float getBocaH(float scale) { return 15 * scale; }

    public void zumbiComendo(){
        comendo = true;
    }

    public void zumbiNaoComendo(){
        comendo = false;
    }
}


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

    private static TextureRegion corpoZumbi;
    private static TextureRegion gravataZumbi;
    private static TextureRegion pescocoZumbi;

    private static TextureRegion bracoExtZumbi;
    private static TextureRegion danoBracoExtZumbi;
    private static TextureRegion antebracoExtZumbi;
    private static TextureRegion maoExtZumbi;

    private static TextureRegion bracoIntZumbi;
    private static TextureRegion antebracoIntZumbi;
    private static TextureRegion maoIntZumbi;

    private static TextureRegion pernaExtZumbi;
    private static TextureRegion canelaExtZumbi;
    private static TextureRegion peExtZumbi;

    private static TextureRegion pernaIntZumbi;
    private static TextureRegion canelaIntZumbi;
    private static TextureRegion peIntZumbi;

    private static final String SPRITESHEET_PATH = "Zombies/PC _ Computer - Plants vs. Zombies - Zombies - Regular Zombies.png";

    private float animTimer = 0f;
    private final float animIntervalo = 1.0f; // velocidade da boca
    private float bocaOffsetY = 0f;
    private boolean bocaAberta = false;

    private boolean comendo = false; // ativado futuramente pelas plantas
    private float comerTimer = 0f;
    private final float comerIntervalo = 0.15f; // mais rápido que o normal
    public boolean comeuCerebro;

    private float bracoOffsetX = 0f;
    private float pernaExtOffsetX = 0f;
    private float pernaIntOffsetX = 0f;
    private boolean animPhase = false; // false = fase 1, true = fase 2

    private static final float SPEED = 300f;

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

            corpoZumbi = new TextureRegion(spritesheet, 1, 162, 53, 63);
            gravataZumbi = new TextureRegion(spritesheet, 55, 162, 17, 30);
            pescocoZumbi = new TextureRegion(spritesheet, 73, 162, 20, 21);

            bracoExtZumbi = new TextureRegion(spritesheet, 1, 234, 17, 35);
            danoBracoExtZumbi = new TextureRegion(spritesheet, 19, 234, 17, 35);
            antebracoExtZumbi = new TextureRegion(spritesheet, 1, 270, 19, 28);
            maoExtZumbi = new TextureRegion(spritesheet, 1, 299, 25, 27);

            bracoIntZumbi = new TextureRegion(spritesheet, 55, 234, 15, 25);
            antebracoIntZumbi = new TextureRegion(spritesheet, 55, 260, 19, 24);
            maoIntZumbi = new TextureRegion(spritesheet, 55, 285, 20, 23);

            pernaExtZumbi = new TextureRegion(spritesheet, 1, 340, 21, 39);
            canelaExtZumbi = new TextureRegion(spritesheet, 23, 340, 24, 30);
            peExtZumbi = new TextureRegion(spritesheet, 48, 340, 42, 21);

            pernaIntZumbi = new TextureRegion(spritesheet, 91, 340, 15, 26) ;
            canelaIntZumbi = new TextureRegion(spritesheet, 107, 340, 32, 36);
            peIntZumbi = new TextureRegion(spritesheet, 140, 340, 27, 17);
        }
    }


    @Override
    public void reset() {
        position.set(0,0);
        speed.set(0,0);
        alive = false;
        comeuCerebro = false;
    }
    /**
     * Método chamado em cada frame (no método render)
     que atualiza a posição da bala.
     */
    public void update (float delta, float brainX, float brainY, float brainW, float brainH) {
        // se estiver fora da tela então para de atualizar

        animTimer += delta;
        if (animTimer >= animIntervalo) {
            animTimer -= animIntervalo;
            animPhase = !animPhase;

            if (!comendo) {
                // boca devagar
                bocaAberta = animPhase;
                bocaOffsetY = bocaAberta ? -9f : -5f;
            }
        }

        float brainCenterX = brainX + brainW / 2f;
        float brainCenterY = brainY + brainH / 2f;
        float radius = Math.min(brainW, brainH) / 2f;

        float scale = 1.5f;
        float cabecaW = 53 * scale;
        float cabecaH = 48 * scale;
        float cabecaX = position.x;
        float cabecaY = position.y + 20f;

        float bocaW = 32 * scale;
        float bocaH = 15 * scale;
        float bocaX = position.x + (cabecaW - bocaW) / 2f;
        float bocaY = position.y + 20f + bocaOffsetY * scale;

        boolean colisaoCabeca = CollidingWithBrainCircle(cabecaX, cabecaY, cabecaW, cabecaH, brainCenterX, brainCenterY, radius);
        boolean colisaoBoca   = CollidingWithBrainCircle(bocaX, bocaY, bocaW, bocaH, brainCenterX, brainCenterY, radius);

        if (isOutOfScreen()) {
            alive = false;
        } else if (colisaoCabeca || colisaoBoca) {
            alive = false;
            comeuCerebro = true;
            brainEatenSound.play(0.5f);
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

    public float getBocaW(float scale) { return 32 * scale; }
    public float getBocaH(float scale) { return 15 * scale; }
}


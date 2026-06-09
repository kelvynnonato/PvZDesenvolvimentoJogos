package io.github.PlantsVsZombies10;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class World {
    // array contendo as balas ativas.
    private final Array<Zombie> activeZombies = new Array<>();
    // pool de balas.
    private final Pool<Zombie> zombiePool = new Pool<Zombie>() {
        @Override
        protected Zombie newObject() {
            return new Zombie();
        }
    };

    private final Array<SpeechBubble> activeBubbles = new Array<>();
    private final Pool<SpeechBubble> bubblePool = new Pool<SpeechBubble>() {
        @Override
        protected SpeechBubble newObject() {
            return new SpeechBubble();
        }
    };

    private float shootTimer = 0f;
    public boolean justShot = false;
    final Array<Pea> activePeas = new Array<Pea>();
    private final Pool<Pea> peaPool = new Pool<Pea>() {
        @Override
        protected Pea newObject() {
            return new Pea();
        }
    };

    private FrontyardGrid frontyardGrid;

    private final ParticleManager particleManager = new ParticleManager();

    public void setGrid(FrontyardGrid frontyardGrid){
        this.frontyardGrid = frontyardGrid;
    }

    public Array<Zombie> getActiveZombies() {
        return activeZombies;
    }

    private Sound mordida;

    private AssetManager assetManager;

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        mordida = assetManager.get("sounds/affects/SFX chompsoft.ogg", Sound.class);
    }

    public void spawnZombie() {
        if(frontyardGrid == null) return;

        int row = MathUtils.random(0, frontyardGrid.getRows() - 1);

        float targetY = frontyardGrid.getOffsetY() + row*frontyardGrid.getTileH() + frontyardGrid.getTileH()/2f - 45f;

        float startX = Gdx.graphics.getWidth() + 250f, startY = targetY;

        float targetX = frontyardGrid.getOffsetX();

        Zombie zombie = zombiePool.obtain();
        zombie.init(startX, startY, targetX, targetY, assetManager);
        activeZombies.add(zombie);

        // spawna o balão na altura do zumbi
        SpeechBubble bubble = bubblePool.obtain();
        bubble.init(startY); // startY é a Y da lane
        activeBubbles.add(bubble);
    }

    private void spawnPea(float x, float y) {
        Pea pea = peaPool.obtain();
        pea.init(x, y, assetManager);
        activePeas.add(pea);
    }

    public void update(float delta, float peashooterX, float peashooterY, float peashooterW, float peashooterH) {
        float peashooterCenterX = peashooterX + peashooterW/2f;
        float peashooterCenterY = peashooterY + peashooterH/2f;
        float peashooterRadius = Math.min(peashooterW, peashooterH)/2f;

        float scale = 1f;
        float cabecaW = 53 * scale;
        float cabecaH = 48 * scale;
        float bocaW = 32 * scale;
        float bocaH = 15 * scale;

        //timer da ervilha
        shootTimer += delta;
        justShot = false;
        if(shootTimer >= 0.6f){
            shootTimer -= 0.6f;
            spawnPea(peashooterX + peashooterW, peashooterY + peashooterH / 2f + 10f);
            justShot = true;
        }

        for (int i = activeZombies.size - 1; i >= 0; i--) {
            Zombie zombie = activeZombies.get(i);
            zombie.update(delta);

            if (!zombie.alive) {
                activeZombies.removeIndex(i);
                zombiePool.free(zombie);
            }

            float cabecaX = zombie.position.x;
            float cabecaY = zombie.position.y + 20f;
            float bocaX = zombie.position.x + (cabecaW - bocaW) / 2f;
            float bocaY = zombie.getBocaOffsetY(scale);

            boolean acertouPeashooter = intersectaCirculo(cabecaX, cabecaY, cabecaW, cabecaH, peashooterCenterX, peashooterCenterY, peashooterRadius) ||
                intersectaCirculo(bocaX, bocaY, bocaW, bocaH, peashooterCenterX, peashooterCenterY, peashooterRadius);

            if (acertouPeashooter) {
                zombie.alive = false;
                zombie.comeuCerebro = true;
                particleManager.spawn(zombie.position.x + 40f, zombie.position.y + 48f, 12,
                    new com.badlogic.gdx.graphics.Color(0.2f, 0.8f, 0.2f, 1f));
                activeZombies.removeIndex(i);
                zombiePool.free(zombie);
                mordida.play();
            }
        }

        for(int i = activePeas.size - 1; i >= 0; i--) {
            Pea pea = activePeas.get(i);
            pea.update(delta);
            if (!pea.alive) {
                activePeas.removeIndex(i);
                peaPool.free(pea);
            }

            float peaCX = pea.getCenterX(0.15f);
            float peaCY = pea.getCenterY(0.15f);
            float peaR = pea.getRadius(0.15f);

            for (int j = activeZombies.size - 1; j >= 0; j--) {
                Zombie zombie = activeZombies.get(j);

                float cabecaX = zombie.position.x;
                float cabecaY = zombie.position.y + 20f;
                float bocaX = zombie.position.x + (cabecaW - bocaW) / 2f;
                float bocaY = zombie.getBocaY(scale);

                boolean acertou =
                    intersectaCirculo(cabecaX, cabecaY, cabecaW, cabecaH, peaCX, peaCY, peaR) ||
                        intersectaCirculo(bocaX, bocaY, bocaW, bocaH, peaCX, peaCY, peaR);

                if(acertou){
                    pea.alive = false;
                    zombie.alive = false;
                    particleManager.spawn(peaCX, peaCY, 10,
                        new com.badlogic.gdx.graphics.Color(0.2f, 0.8f, 0.2f, 1f));
                    activePeas.removeIndex(i);
                    peaPool.free(pea);
                    activeZombies.removeIndex(j);
                    zombiePool.free(zombie);
                    break;
                }
            }
        }

        for (int i = activeBubbles.size - 1; i >= 0; i--) {
            SpeechBubble bubble = activeBubbles.get(i);
            bubble.update(delta);
            if (!bubble.alive) {
                activeBubbles.removeIndex(i);
                bubblePool.free(bubble);
            }
        }
    }

    public Array<SpeechBubble> getActiveBubbles() {
        return activeBubbles;
    }

    public Array<Pea> getActivePeas() {
        return activePeas;
    }

    public void updateParticles(float delta) {
        particleManager.update(delta);
    }

    public void renderParticles(OrthographicCamera cam) {
        particleManager.render(cam);
    }

    private boolean intersectaCirculo(float retX, float retY, float retW, float retH,
                                      float cirX, float cirY, float raio) {
        float closestX = Math.max(retX, Math.min(cirX, retX + retW));
        float closestY = Math.max(retY, Math.min(cirY, retY + retH));
        float dx = cirX - closestX;
        float dy = cirY - closestY;
        return (float) Math.sqrt(dx * dx + dy * dy) <= raio;
    }

    public void dispose() {
        particleManager.dispose();
    }
}


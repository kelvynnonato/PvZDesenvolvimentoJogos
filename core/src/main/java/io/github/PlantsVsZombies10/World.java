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

    public boolean justShot = false;
    final Array<Pea> activePeas = new Array<Pea>();
    private final Pool<Pea> peaPool = new Pool<Pea>() {
        @Override
        protected Pea newObject() {
            return new Pea();
        }
    };

    private FrontyardGrid frontyardGrid;
    private PlantManager plantManager;
    private final ParticleManager particleManager = new ParticleManager();
    private final SunDropManager sunDropManager = new SunDropManager();
    private Sound mordida, morteZumbi;
    private AssetManager assetManager;

    private boolean zombieReachedHouse = false;

    public boolean hasZombieReachedHouse() { return zombieReachedHouse; }
    public void resetZombieReachedHouse()  { zombieReachedHouse = false; }

    public void setGrid(FrontyardGrid frontyardGrid){
        this.frontyardGrid = frontyardGrid;
    }

    public void setPlantManager(PlantManager plantManager){
        this.plantManager = plantManager;
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        mordida = assetManager.get("sounds/affects/SFX chompsoft.ogg", Sound.class);
        sunDropManager.setAssetManager(assetManager);
        morteZumbi = assetManager.get("sounds/affects/splat.ogg", Sound.class);
    }

    public Array<Zombie> getActiveZombies() {
        return activeZombies;
    }
    public Array<SpeechBubble> getActiveBubbles() {
        return activeBubbles;
    }
    public Array<Pea> getActivePeas() {
        return activePeas;
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

    public void spawnPea(float x, float y) {
        Pea pea = peaPool.obtain();
        pea.init(x, y, assetManager);
        activePeas.add(pea);
    }

    // Chamado quando um girassol termina o ciclo de produção - cria o sol visual e clicável
    public void spawnSunDrop(float x, float y, int sunAmount) {
        sunDropManager.spawn(x, y, sunAmount);
    }

    public void spawnSkySunDrop(float targetX, float targetY, int sunAmount) {
        sunDropManager.spawnFromSky(targetX, targetY, sunAmount);
    }

    public void updateSunDrops(float delta) {
        sunDropManager.update(delta);
    }

    public void renderSunDrops(OrthographicCamera cam) {
        sunDropManager.render(cam);
    }

    public boolean trySunClick(float worldX, float worldY, Sun sun) {
        int coletado = sunDropManager.tryCollect(worldX, worldY);
        if (coletado <= 0) return false;

        sun.add(coletado);
        particleManager.spawn(worldX, worldY, 8, new com.badlogic.gdx.graphics.Color(1f, 0.85f, 0.2f, 1f));
        return true;
    }

    public void update(float delta, float peashooterX, float peashooterY,
                       float peashooterW, float peashooterH) {

        float scale   = 1f;
        float cabecaW = 53 * scale;
        float cabecaH = 48 * scale;
        float bocaW   = 32 * scale;
        float bocaH   = 15 * scale;

        // Zumbis
        for (int i = activeZombies.size - 1; i >= 0; i--) {
            Zombie zombie = activeZombies.get(i);
            zombie.update(delta);
            if (!zombie.alive) {
                activeZombies.removeIndex(i);
                zombiePool.free(zombie);
                continue;
            }

            if(zombie.position.x <= -53){
                zombieReachedHouse = true;
            }

            // Colisão zumbi vs plantas
            boolean colidiu = false;
            if (plantManager != null) {
                for (Plant plant : plantManager.getAllPlants()) {
                    float plantCX = plant.x + plant.getWidth()  / 2f;
                    float plantCY = plant.y + plant.getHeight() / 2f;
                    float plantR  = Math.max(plant.getWidth(), plant.getHeight()) / 2f;

                    float cabecaX = zombie.position.x;
                    float cabecaY = zombie.position.y + 20f;
                    float bocaX   = zombie.position.x + (cabecaW - bocaW) / 2f;
                    float bocaY   = zombie.getBocaY(scale);
                    float nextX   = zombie.position.x + zombie.speed.x * delta;

                    boolean acertou =
                        intersectaCirculo(cabecaX, cabecaY, cabecaW, cabecaH, plantCX, plantCY, plantR) ||
                            intersectaCirculo(bocaX,   bocaY,   bocaW,   bocaH,   plantCX, plantCY, plantR) ||
                            intersectaCirculo(nextX,   cabecaY, cabecaW, cabecaH, plantCX, plantCY, plantR);

                    if (acertou) {
                        colidiu = true;
                        zombie.speed.set(0, 0);
                        zombie.zumbiComendo();

                        zombie.danoTimer += delta;
                        if (zombie.danoTimer >= 0.5f) {
                            zombie.danoTimer -= 0.5f;
                            plant.hp -= 34;
                            mordida.play();
                            particleManager.spawn(plantCX, plantCY, 6,
                                new com.badlogic.gdx.graphics.Color(0.2f, 0.8f, 0.2f, 1f));
                        }
                        break;
                    }
                }
            }

            if (!colidiu) {
                zombie.zumbiNaoComendo();
                zombie.danoTimer -= zombie.danoTimer;
                if (zombie.speed.isZero()) {
                    zombie.speed.set(-300f, 0);
                }
            }
        }

        // Ervilhas vs Zumbis
        for (int i = activePeas.size - 1; i >= 0; i--) {
            Pea pea = activePeas.get(i);
            pea.update(delta);

            if (!pea.alive) {
                activePeas.removeIndex(i);
                peaPool.free(pea);
                continue;
            }

            float peaCX = pea.getCenterX(0.15f);
            float peaCY = pea.getCenterY(0.15f);
            float peaR  = pea.getRadius(0.15f);

            for (int j = activeZombies.size - 1; j >= 0; j--) {
                Zombie zombie = activeZombies.get(j);

                float cabecaX = zombie.position.x;
                float cabecaY = zombie.position.y + 20f;
                float bocaX   = zombie.position.x + (cabecaW - bocaW) / 2f;
                float bocaY   = zombie.getBocaY(scale);

                boolean acertou =
                    intersectaCirculo(cabecaX, cabecaY, cabecaW, cabecaH, peaCX, peaCY, peaR) ||
                        intersectaCirculo(bocaX,   bocaY,   bocaW,   bocaH,   peaCX, peaCY, peaR);

                if (acertou) {
                    morteZumbi.play();
                    pea.alive    = false;
                    zombie.alive = false;
                    particleManager.spawn(peaCX, peaCY, 10,
                        new com.badlogic.gdx.graphics.Color(0.2f, 0.8f, 0.2f, 1f));
                    particleManager.spawn(peaCX, peaCY, 6,
                        new com.badlogic.gdx.graphics.Color(0.8f, 0.2f, 0.2f, 1f));
                    activePeas.removeIndex(i);
                    peaPool.free(pea);
                    activeZombies.removeIndex(j);
                    zombiePool.free(zombie);
                    break;
                }
            }
        }

        // Balões
        for (int i = activeBubbles.size - 1; i >= 0; i--) {
            SpeechBubble bubble = activeBubbles.get(i);
            bubble.update(delta);
            if (!bubble.alive) {
                activeBubbles.removeIndex(i);
                bubblePool.free(bubble);
            }
        }
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
        sunDropManager.dispose();
    }
}


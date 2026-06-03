package io.github.PlantsVsZombies10;

import com.badlogic.gdx.assets.AssetManager;
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

    private FrontyardGrid frontyardGrid;

    private final ParticleManager particleManager = new ParticleManager();

    public void setGrid(FrontyardGrid frontyardGrid){
        this.frontyardGrid = frontyardGrid;
    }

    public Array<Zombie> getActiveZombies() {
        return activeZombies;
    }

    public void spawnZombie(AssetManager assetManager) {
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
    public void update(float delta, float brainX, float brainY, float brainW, float brainH) {
        for (int i = activeZombies.size - 1; --i >= 0;) {
            Zombie item = activeZombies.get(i);
            item.update(delta, brainX, brainY, brainW, brainH);

            if (!item.alive) {
                if (item.comeuCerebro) {
                    particleManager.spawn(
                        item.position.x + 40f, // centro do zumbi
                        item.position.y + 48f,
                        12
                    );
                }
                activeZombies.removeIndex(i);
                zombiePool.free(item);
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

    public void updateParticles(float delta) {
        particleManager.update(delta);
    }

    public void renderParticles(OrthographicCamera cam) {
        particleManager.render(cam);
    }

    public void dispose() {
        particleManager.dispose();
    }
}


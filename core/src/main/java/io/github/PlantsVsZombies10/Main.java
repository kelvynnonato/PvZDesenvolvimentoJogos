package io.github.PlantsVsZombies10;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.lerp;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private AssetManager assetManager;
    private BackgroundManager backgroundManager;
    private ShapeRenderer shapeRenderer;

    private LoadingScreen loadingScreen;
    private boolean loading = true;
    private float loadingTime = 0.0f, loadingMin = 3.0f;
    private World world;
    private float spawnTimer = 0f;
    private float spawnIntervalo = 0.5f;
    private float minSpawnIntervalo = 0.5f;
    private float gameTime = 0f;
    private static final float ZOMBIE_START_DELAY = 10f; // segundos antes do 1º zumbi aparecer

    private Music menu;
    private Sound zombie1, zombie2, zombie3, zombie4, zombie5, zombie6;

    private OrthographicCamera cam;

    private FrontyardGrid frontyardGrid;
    private static final int   GRID_COLS    = 9;
    private static final int   GRID_ROWS    = 5;
    private static final float GRID_TILE_W  = 80f;
    private static final float GRID_TILE_H  = 97f;
    private static final float GRID_OFFSET_X = 60f;
    private static final float GRID_OFFSET_Y = 30f;

    private GridDebugRenderer gridDebugRenderer;
    private boolean modoDebug = false;

    private PlantManager plantManager;
    private SeedBar seedBar;
    private Sun sun;

    private float skySunTimer = 0f;
    private float skySunNextInterval = MathUtils.random(15f, 20f);

    private boolean gameOver = false;
    public float gameOverSoundTimer = 0f;
    private float gameOverTimer = 0f;
    private static final float GAME_OVER_DURATION = 5f;
    private Sound zombieEatingBrain, gameOverScream;
    private Texture ZombiesAteYourBrain;
    private BitmapFont gameOverFont;

    private Sound zombieWave;
    private boolean firstZombie = false;

    @Override
    public void create() {
        assetManager = new AssetManager();
        Gdx.input.setInputProcessor(new GameInputProcessor(this));

        assetManager.load("Plants/peashooterIngame.png", Texture.class);
        assetManager.load("Bullets/pea-shooted.png", Texture.class);
        assetManager.load("Plants/sunflowerIngame.png", Texture.class);
        assetManager.load("Bullets/sun PVZ.png", Texture.class);

        assetManager.load("Backgrounds/Background_Noite.jpg", Texture.class);
        assetManager.load("Backgrounds/Background.jpg", Texture.class);
        assetManager.load("Zombies/zumbi_125_200.png", Texture.class);
        assetManager.load("Zombies/PC _ Computer - Plants vs. Zombies - Zombies - Regular Zombies.png", Texture.class);

        assetManager.load("sounds/music/Main Music 01.mp3", Music.class);
        assetManager.load("sounds/music/04. Grasswalk.mp3", Music.class);
        menu = Gdx.audio.newMusic(Gdx.files.internal("sounds/music/Menu Music.mp3"));
        menu.setLooping(true);
        menu.setVolume(2.0f);
        menu.play();

        assetManager.load("sounds/affects/SFX chompsoft.ogg", Sound.class);
        assetManager.load("sounds/affects/firepea.ogg", Sound.class);

        assetManager.load("sounds/affects/Voices groan.ogg", Sound.class);
        assetManager.load("sounds/affects/Voices groan2.ogg", Sound.class);
        assetManager.load("sounds/affects/Voices groan3.ogg", Sound.class);
        assetManager.load("sounds/affects/Voices groan4.ogg", Sound.class);
        assetManager.load("sounds/affects/Voices groan5.ogg", Sound.class);
        assetManager.load("sounds/affects/Voices groan6.ogg", Sound.class);

        assetManager.load("sounds/affects/buzzer.ogg", Sound.class);
        assetManager.load("sounds/affects/points.ogg", Sound.class);

        assetManager.load("sounds/affects/siren.ogg", Sound.class);
        assetManager.load("sounds/affects/gulp.ogg", Sound.class);
        assetManager.load("sounds/affects/splat.ogg", Sound.class);

        assetManager.load("sounds/affects/plant.ogg", Sound.class);

        assetManager.load("sounds/affects/readysetplant.ogg", Sound.class);

        assetManager.load("sounds/misc/scream.ogg", Sound.class);
        assetManager.load("GameOver/ZombiesAteYourBrain.png", Texture.class);

        loadingScreen = new LoadingScreen(assetManager, loadingMin);

        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        cam = new OrthographicCamera(w, h);
        cam.position.set(cam.viewportWidth/2f, cam.viewportHeight/2f, 0);
    }

    @Override
    public void render() {
        if(loading){
            float deltaLoading = Gdx.graphics.getDeltaTime();

            if (loadingScreen.update(deltaLoading)) {
                loading = false;
                loadingScreen.dispose();
                onAssetsLoaded();
            } else {
                ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
                loadingScreen.render();
            }
            return;
        }

        float delta = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        if (!gameOver) {
            seedBar.update(delta);

            // Plantas atiram
            boolean isDay = backgroundManager.isDay();
            Array<float[]> shots = plantManager.update(delta, isDay);
            for (float[] shot : shots) {
                world.spawnPea(shot[0], shot[1]);
            }
            for (float[] sunSpawn : plantManager.getPendingSunSpawns()) {
                world.spawnSunDrop(sunSpawn[0], sunSpawn[1], (int) sunSpawn[2]);
            }
            if (isDay) {
                skySunTimer += delta;
                if (skySunTimer >= skySunNextInterval) {
                    skySunTimer -= skySunNextInterval;
                    skySunNextInterval = MathUtils.random(15f, 20f);
                    spawnSkySun();
                }
            }

            gameTime += delta;
            if (gameTime >= ZOMBIE_START_DELAY) {
                if(!firstZombie){
                    firstZombie = true;
                    zombieWave.play();
                }
                float ingameTime = gameTime - ZOMBIE_START_DELAY;
                spawnIntervalo = Math.max(minSpawnIntervalo, 8f - ingameTime * (7.5f / 180f));

                spawnTimer += delta;
                if(spawnTimer >= spawnIntervalo){
                    spawnTimer -= spawnIntervalo;
                    world.spawnZombie();
                    switch(MathUtils.random(5)){
                        case 0: zombie1.play(); break;
                        case 1: zombie2.play(); break;
                        case 2: zombie3.play(); break;
                        case 3: zombie4.play(); break;
                        case 4: zombie5.play(); break;
                        default: zombie6.play(); break;
                    }
                }
            }

            backgroundManager.update(delta);
            world.update(delta, 0, 0, 0, 0);
            world.updateParticles(delta);
            world.updateSunDrops(delta);

            // Verifica condição de Game Over
            if(world.hasZombieReachedHouse()){
                gameOver = true;
                zombieEatingBrain.play();
                zombieEatingBrain.play();
                gameOverScream.play();
            }
        } else {
            // Se já for Game Over, apenas atualiza o temporizador de reinício
            gameOverTimer += delta;
            if(gameOverTimer >= GAME_OVER_DURATION){
                gameOverTimer -= GAME_OVER_DURATION;
                resetGame();
                return; // Sai para evitar desenhar frame quebrado no reset
            }
        }

        cam.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        cam.zoom = 1f;
        cam.update();

        batch.begin();
        batch.setProjectionMatrix(cam.combined);
        backgroundManager.render(batch);
        plantManager.render(batch);
        for (Pea p : world.getActivePeas()) {
            p.render(batch, 0.15f);
        }
        for (Zombie z : world.getActiveZombies()) {
            z.render(batch, 1f);
        }
        for (SpeechBubble bubble : world.getActiveBubbles()) {
            bubble.render(batch, cam);
        }
        batch.end();

        world.renderParticles(cam);
        world.renderSunDrops(cam);
        seedBar.render(batch, sun);

        if (modoDebug) {
            shapeRenderer.setProjectionMatrix(cam.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            float scale = 1f;
            for (Zombie z : world.getActiveZombies()) {
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.rect(z.position.x, z.position.y + 20f, 53 * scale, 48 * scale);
                shapeRenderer.rect(z.getBocaX(scale), z.getBocaY(scale), z.getBocaW(scale), z.getBocaH(scale));
            }
            shapeRenderer.end();
            gridDebugRenderer.render(cam, frontyardGrid);
        }

        if (gameOver) {
            backgroundManager.stopBackgroundMusic();
            Matrix4 uiMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(uiMatrix);
            batch.begin();

            // Desenha a imagem centralizada na tela
            float imgX = (Gdx.graphics.getWidth() - ZombiesAteYourBrain.getWidth()) / 2f;
            float imgY = (Gdx.graphics.getHeight() - ZombiesAteYourBrain.getHeight()) / 2f;
            batch.draw(ZombiesAteYourBrain, imgX, imgY);

            gameOverFont.setColor(Color.WHITE);
            gameOverFont.getData().setScale(2f); // Ajustado escala para não ficar gigante
            gameOverFont.draw(batch, "Reiniciando...", Gdx.graphics.getWidth() / 2f - 60f, imgY - 10f);

            batch.end();
        }
    }

    @Override
    public void dispose() {
        if (menu != null) {
            menu.dispose();
        }
        assetManager.dispose();
        world.dispose();
        seedBar.dispose();
    }

    public void onTouch(int screenX, int screenY) {
        if (loading) return;

        // Um único unproject, usado tanto pra coletar sol quanto pra plantar
        Vector3 worldCoords = new Vector3(screenX, screenY, 0);
        cam.unproject(worldCoords);

        // Sol caído tem prioridade: se o clique acertou um, só coleta e não faz mais nada
        if (world.trySunClick(worldCoords.x, worldCoords.y, sun)) {
            return;
        }

        int slotClicked = seedBar.touchDown(screenX, screenY, sun);

        if(!seedBar.hasSelection()) return;

        int[] col = new int[1];
        int[] row = new int[1];
        if(!frontyardGrid.worldToTile(worldCoords.x, worldCoords.y, col, row)) return;

        if(plantManager.isTileOccupied(col[0], row[0])) return;

        SeedSlot slot = seedBar.getSlot(seedBar.getSelectedSlot());
        if(slot == null) return;
        if(!sun.canAfford(slot.cost)) return;

        boolean planted = plantManager.plantar(col[0], row[0], slot);
        if(planted){
            sun.spend(slot.cost);
            seedBar.onPlanted();
        }
    }

    private void spawnSkySun() {
        int col = MathUtils.random(0, frontyardGrid.getCols() - 1);
        int row = MathUtils.random(0, frontyardGrid.getRows() - 1);

        Vector2 tileBottomLeft = frontyardGrid.tileBottomLeft(col, row);
        float targetX = tileBottomLeft.x + frontyardGrid.getTileW() / 2f;
        float targetY = tileBottomLeft.y + frontyardGrid.getTileH() / 2f;

        world.spawnSkySunDrop(targetX, targetY, 25);
    }

    private void onAssetsLoaded(){
        menu.stop();
        menu.dispose();

        batch = new SpriteBatch();
        world = new World();
        world.setAssetManager(assetManager);
        Gdx.input.setInputProcessor(new GameInputProcessor(this));
        shapeRenderer = new ShapeRenderer();

        Texture[] bgs = {
            assetManager.get("Backgrounds/Background_Noite.jpg", Texture.class),
            assetManager.get("Backgrounds/Background.jpg", Texture.class)
        };
        backgroundManager = new BackgroundManager(bgs, 100f, -200, 5f, assetManager);

        zombie1 = assetManager.get("sounds/affects/Voices groan.ogg", Sound.class);
        zombie2 = assetManager.get("sounds/affects/Voices groan2.ogg", Sound.class);
        zombie3 = assetManager.get("sounds/affects/Voices groan3.ogg", Sound.class);
        zombie4 = assetManager.get("sounds/affects/Voices groan4.ogg", Sound.class);
        zombie5 = assetManager.get("sounds/affects/Voices groan5.ogg", Sound.class);
        zombie6 = assetManager.get("sounds/affects/Voices groan6.ogg", Sound.class);

        frontyardGrid = new FrontyardGrid(GRID_COLS, GRID_ROWS, GRID_TILE_W, GRID_TILE_H, GRID_OFFSET_X, GRID_OFFSET_Y);
        gridDebugRenderer = new GridDebugRenderer();
        world.setGrid(frontyardGrid);

        // Aqui para mudar quanto sois voce começa
        sun = new Sun(150);
        plantManager = new PlantManager(frontyardGrid, assetManager);
        plantManager.setTextures(
            assetManager.get("Plants/peashooterIngame.png", Texture.class),
            assetManager.get("Plants/sunflowerIngame.png", Texture.class)
        );
        world.setPlantManager(plantManager);

        seedBar = new SeedBar(assetManager);
        seedBar.setSlotTexture(0, assetManager.get("Plants/peashooterIngame.png", Texture.class));
        seedBar.setSlotTexture(1, assetManager.get("Plants/sunflowerIngame.png", Texture.class));

        zombieWave = assetManager.get("sounds/affects/siren.ogg", Sound.class);

        gameOverScream = assetManager.get("sounds/misc/scream.ogg", Sound.class);
        zombieEatingBrain = assetManager.get("sounds/affects/SFX chompsoft.ogg", Sound.class);
        ZombiesAteYourBrain = assetManager.get("GameOver/ZombiesAteYourBrain.png", Texture.class);

        gameOverFont = new BitmapFont();
        gameOverFont.getData().setScale(4f);
        gameOverFont.setColor(Color.RED);
    }

    public void setCameraMode(boolean followBrain) {
            cam.zoom = 1f;
    }

    public void toggleDebug(){
        modoDebug = !modoDebug;
    }

    private void resetGame(){
        gameOver = false;
        gameOverTimer = 0f;
        gameTime = 0f;
        spawnTimer = 0f;

        world.dispose();
        world = new World();
        world.setAssetManager(assetManager);
        world.setGrid(frontyardGrid);

        sun = new Sun(150);

        plantManager = new PlantManager(frontyardGrid, assetManager);
        plantManager.setTextures(
            assetManager.get("Plants/peashooterIngame.png", Texture.class),
            assetManager.get("Plants/sunflowerIngame.png", Texture.class)
        );
        world.setPlantManager(plantManager);
        seedBar.clearSelection();
        backgroundManager.startBackgroundMusic();
    }
}

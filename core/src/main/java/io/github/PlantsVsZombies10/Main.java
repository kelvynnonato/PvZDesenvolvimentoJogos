package io.github.PlantsVsZombies10;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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

    @Override
    public void create() {
        assetManager = new AssetManager();
        Gdx.input.setInputProcessor(new GameInputProcessor(this));

        assetManager.load("Plants/peashooter.png", Texture.class);
        assetManager.load("Bullets/pea-shooted.png", Texture.class);

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

        assetManager.load("sounds/affects/plant.ogg", Sound.class);

        assetManager.load("sounds/affects/readysetplant.ogg", Sound.class);

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

        seedBar.update(delta);

        // Plantas atiram
        Array<float[]> shots = plantManager.update(delta);
        for (float[] shot : shots) {
            world.spawnPea(shot[0], shot[1]);
        }

        spawnTimer += delta;
        if(spawnTimer >= spawnIntervalo){
            spawnTimer = -2f;
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

        backgroundManager.update(delta);
        world.update(delta, 0, 0, 0, 0);

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

        world.updateParticles(delta);
        world.renderParticles(cam);

        seedBar.render(batch, sun);

        if (modoDebug) {
            shapeRenderer.setProjectionMatrix(cam.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            float scale = 1f;

            for (Zombie z : world.getActiveZombies()) {
                // hitbox da cabeça
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.rect(z.position.x, z.position.y + 20f, 53 * scale, 48 * scale);
                shapeRenderer.rect(z.getBocaX(scale), z.getBocaY(scale), z.getBocaW(scale), z.getBocaH(scale));
            }
            shapeRenderer.end();
            gridDebugRenderer.render(cam,frontyardGrid);
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

        int slotClicked = seedBar.touchDown(screenX, screenY, sun);

        if(!seedBar.hasSelection()) return;

        Vector3 worldCoords = new Vector3(screenX, screenY, 0);
        cam.unproject(worldCoords);

        int[] col = new int[1];
        int[] row = new int[1];
        if(!frontyardGrid.worldToTile(worldCoords.x, worldCoords.y, col, row)) return;

        if(plantManager.isTileOccupied(col[0], row[0])) return;

        SeedSlot slot = seedBar.getSlot(seedBar.getSelectedSlot());
        if(slot == null) return;
        else if(!slot.isReady()){
            //fazer tocar som no futuro
        }
        if(!sun.canAfford(slot.cost)) return;

        boolean planted = plantManager.plantar(col[0], row[0], slot);
        if(planted){
            sun.spend(slot.cost);
            seedBar.onPlanted();
        }
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
        backgroundManager = new BackgroundManager(bgs, 10f, -200, 5f, assetManager);

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
        plantManager.setTextures(assetManager.get("Plants/peashooter.png", Texture.class));
        world.setPlantManager(plantManager);

        seedBar = new SeedBar(assetManager);
        seedBar.setSlotTexture(0, assetManager.get("Plants/peashooter.png", Texture.class));
    }

    public void setCameraMode(boolean followBrain) {
            cam.zoom = 1f;
    }

    public void toggleDebug(){
        modoDebug = !modoDebug;
    }
}

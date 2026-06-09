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

    private Plant peashooter;

    private LoadingScreen loadingScreen;
    private boolean loading = true;
    private float loadingTime = 0.0f, loadingMin = 3.0f;

    private float X = 0, XStep = 0, Xmin = 0, Xmax = 0;
    private float Y = 0, YStep = 0, Ymin = 0, Ymax = 0;

    enum EstadoClick { PARADO, NORMAL, RAPIDO }
    EstadoClick estado = EstadoClick.PARADO;
    long UltimoClick = 0;
    float intervalo = 0.2f;

    private World world;
    private Texture zombieTexture;
    private float spawnTimer = 0f;
    private float spawnIntervalo = 0.5f;

    private Music menu;
    private Sound zombie1, zombie2, zombie3, zombie4, zombie5, zombie6;

    private OrthographicCamera cam;
    private boolean cameraFollowBrain = true;

    private FrontyardGrid frontyardGrid;
    private static final int   GRID_COLS    = 9;
    private static final int   GRID_ROWS    = 5;
    private static final float GRID_TILE_W  = 80f;
    private static final float GRID_TILE_H  = 97f;
    private static final float GRID_OFFSET_X = 60f;
    private static final float GRID_OFFSET_Y = 30f;

    private GridDebugRenderer gridDebugRenderer;
    private boolean modoDebug = false;

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

        Xmax = Gdx.graphics.getWidth() - peashooter.getWidth();
        Ymax = Gdx.graphics.getHeight() - peashooter.getHeight();

        float speed = 0;
        switch (estado){
            case PARADO: speed = 0.0f; break;
            case NORMAL: speed = 3.0f; break;
            case RAPIDO: speed = 7.0f; break;
        }

        speed = speed * Gdx.graphics.getDeltaTime();
        XStep = lerp(XStep, X, speed);
        YStep = lerp(YStep, Y, speed);

        XStep = clamp(XStep, Xmin, Xmax);
        YStep = clamp(YStep, Ymin, Ymax);

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
        world.update(delta, XStep, YStep, peashooter.getWidth(), peashooter.getHeight());

        if (cameraFollowBrain) {
            cam.position.set(XStep + peashooter.getWidth()/2f, YStep + peashooter.getHeight()/2f, 0);

            float halfW = (cam.viewportWidth * cam.zoom)/2f;
            float halfH = (cam.viewportHeight * cam.zoom)/2f;

            cam.position.x = clamp(cam.position.x, halfW, Gdx.graphics.getWidth() - halfW);
            cam.position.y = clamp(cam.position.y, halfH, Gdx.graphics.getHeight() - halfH);
        } else {
            cam.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        }
        cam.update();

        batch.begin();
        batch.setProjectionMatrix(cam.combined);
        backgroundManager.render(batch);

        peashooter.render(batch, XStep, YStep);

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

        if (modoDebug) {
            shapeRenderer.setProjectionMatrix(cam.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            float scale = 1f;
            float cabecaW = 53 * scale;
            float cabecaH = 48 * scale;

            for (Zombie z : world.getActiveZombies()) {
                // hitbox da cabeça
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.rect(z.position.x, z.position.y + 20f, 53 * scale, 48 * scale);

                // hitbox da boca (sobe e desce)
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.rect(z.getBocaX(scale), z.getBocaY(scale), z.getBocaW(scale), z.getBocaH(scale));
            }

            // hitbox circular do cérebro
            shapeRenderer.setColor(Color.GREEN);
            float cx = XStep + peashooter.getWidth() / 2f;
            float cy = YStep + peashooter.getHeight() / 2f;
            float r  = Math.min(peashooter.getWidth(), peashooter.getHeight()) / 2f;
            shapeRenderer.circle(cx, cy, r);

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
    }

    public void onTouch(int screenX, int screenY) {
        if (loading) return;

        Vector3 worldCoords = new Vector3(screenX, screenY, 0);
        cam.unproject(worldCoords);

        Vector2 snapped = frontyardGrid.snap(worldCoords.x, worldCoords.y);

        if(snapped == null) return;

        X = snapped.x + (frontyardGrid.getTileW()  - peashooter.getWidth())  / 2f;
        X = clamp(X, Xmin, Xmax);
        Y = snapped.y + (frontyardGrid.getTileH() - peashooter.getHeight()) / 2f;
        Y = clamp(Y, Ymin, Ymax);

        long agora = TimeUtils.millis();
        float dif = (agora - UltimoClick) / 1000f;

        if (dif < intervalo) {
            estado = (estado == EstadoClick.RAPIDO) ? EstadoClick.PARADO : EstadoClick.RAPIDO;
        } else {
            if (estado == EstadoClick.PARADO) estado = EstadoClick.NORMAL;
            else if (estado == EstadoClick.RAPIDO) estado = EstadoClick.PARADO;
            else estado = EstadoClick.NORMAL;
        }

        UltimoClick = agora;
    }

    private void onAssetsLoaded(){
        menu.stop();
        menu.dispose();

        batch = new SpriteBatch();
        world = new World();
        world.setAssetManager(assetManager);
        Gdx.input.setInputProcessor(new GameInputProcessor(this));
        shapeRenderer = new ShapeRenderer();

        peashooter = new Plant(
            assetManager.get("Plants/peashooter.png", Texture.class),
            0.2f
        );

        Texture[] bgs = {
            assetManager.get("Backgrounds/Background_Noite.jpg", Texture.class),
            assetManager.get("Backgrounds/Background.jpg", Texture.class)
        };
        backgroundManager = new BackgroundManager(bgs, 10f, -200, 5f, assetManager);

        zombieTexture = assetManager.get("Zombies/zumbi_125_200.png", Texture.class);

        zombie1 = assetManager.get("sounds/affects/Voices groan.ogg", Sound.class);
        zombie2 = assetManager.get("sounds/affects/Voices groan2.ogg", Sound.class);
        zombie3 = assetManager.get("sounds/affects/Voices groan3.ogg", Sound.class);
        zombie4 = assetManager.get("sounds/affects/Voices groan4.ogg", Sound.class);
        zombie5 = assetManager.get("sounds/affects/Voices groan5.ogg", Sound.class);
        zombie6 = assetManager.get("sounds/affects/Voices groan6.ogg", Sound.class);

        frontyardGrid = new FrontyardGrid(GRID_COLS, GRID_ROWS, GRID_TILE_W, GRID_TILE_H, GRID_OFFSET_X, GRID_OFFSET_Y);
        gridDebugRenderer = new GridDebugRenderer();

        Vector2 firstTile = frontyardGrid.tileBottomLeft(0, 0);
        X = firstTile.x + (frontyardGrid.getTileW() - peashooter.getWidth()) / 2f;
        Y = firstTile.y + (frontyardGrid.getTileH() - peashooter.getHeight()) / 2f;
        XStep = X;
        YStep = Y;
        world.setGrid(frontyardGrid);
    }

    public void setCameraMode(boolean followBrain) {
        cameraFollowBrain = followBrain;
        if(followBrain){
            cam.zoom = 0.5f;
        } else {
            cam.zoom = 1f;
        }
    }

    public void toggleDebug(){
        modoDebug = !modoDebug;
    }
}

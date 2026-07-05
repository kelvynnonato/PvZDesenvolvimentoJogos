package io.github.PlantsVsZombies10;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class SeedBar {

    // Layout
    private static final float BAR_X = 10f;
    private static final float BAR_Y_FROM_TOP = 10f; // Distancia do topo da tela
    private static final float SLOT_W = 60f;
    private static final float SLOT_H = 80f;
    private static final float SLOT_PAD = 6f;
    private static final float SUN_BOX_W = 70f;
    private static final int MAX_SLOTS = 6;

    private final SeedSlot[] slots;
    private final Texture[] slotTextures; //null para slots vazios
    private int selectedSlot = -1; // -1 = nenhum selecionado

    private final ShapeRenderer shape;
    private final BitmapFont font;
    private AssetManager assetManager;
    private Sound cantBuyNow;

    public SeedBar(AssetManager assetManager){
        shape = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(0.85f);

        this.assetManager = assetManager;
        // Definindo os slots
        slots = new SeedSlot[MAX_SLOTS];
        slotTextures = new Texture[MAX_SLOTS];

        slots[0] = new SeedSlot("Peashooter", 100, 7.5f);

        if(cantBuyNow == null){
            cantBuyNow = assetManager.get("sounds/affects/buzzer.ogg", Sound.class);
        }
    }

    // Registra a textura de preview de um slot
    public void setSlotTexture(int index, Texture texture){
        if(index >= 0 && index < MAX_SLOTS) slotTextures[index] = texture;
    }

    public void update(float delta){
        for(SeedSlot slot : slots){
            if(slot != null) slot.update(delta);
        }
    }

    // Retorna o slot clicado ou -1 se não foi a barra
    public int touchDown(int screenX, int screenY, Sun sun) {
        float screenH = Gdx.graphics.getHeight();
        float barY = screenH - BAR_Y_FROM_TOP - SLOT_H;
        float startX = BAR_X + SUN_BOX_W + SLOT_PAD;

        for(int i = 0; i < MAX_SLOTS; i++){
            float sx = startX + i * (SLOT_W + SLOT_PAD);
            float sy = screenH - (barY + SLOT_H);

            if(screenX >= sx && screenX <= sx + SLOT_W &&
                screenY >= sy && screenY <= sy + SLOT_H){

                if(slots[i] == null) return i;

                if(selectedSlot == i){
                    selectedSlot = -1;
                } else if(!slots[i].isReady() || !sun.canAfford(slots[i].cost)){
                    cantBuyNow.play(); // sem sóis ou em cooldown
                } else {
                    selectedSlot = i;
                }
                return i;
            }
        }
        return -1; // clique fora da barra
    }

    // Chama quando uma planta é plantada com sucesso
    public void onPlanted(){
        if(selectedSlot >= 0 && slots[selectedSlot] != null){
            slots[selectedSlot].triggerCooldown();
        }
        selectedSlot = -1;
    }

    public boolean hasSelection(){return selectedSlot >= 0;}
    public int getSelectedSlot(){return selectedSlot;}
    public void clearSelection(){selectedSlot = -1;}

    public SeedSlot getSlot(int i){
        return (i >= 0 && i < MAX_SLOTS) ? slots[i] : null;
    }

    public void render(SpriteBatch batch, Sun sun){
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float barY = screenH - BAR_Y_FROM_TOP - SLOT_H;

        // Usa a camera de tela (sem projeção do mundo)
        Matrix4 uiMatrix = new Matrix4().setToOrtho2D(0, 0, screenW, screenH);

        // Fundo da barra
        shape.setProjectionMatrix(uiMatrix);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        float totalW = SUN_BOX_W + SLOT_W + MAX_SLOTS * (SLOT_W + SLOT_PAD);
        shape.setColor(0.15f, 0.12f, 0.05f, 1f);
        shape.rect(BAR_X - 4, barY - 4, totalW + 8, SLOT_H + 8);

        // Caixa de sóis
        shape.setColor(0.25f, 0.22f, 0.05f, 1f);
        shape.rect(BAR_X, barY, SUN_BOX_W, SLOT_H);

        // Slots
        float startX = BAR_X + SUN_BOX_W + SLOT_PAD;
        for (int i = 0; i < MAX_SLOTS; i++) {
            float sx = startX + i * (SLOT_W + SLOT_PAD);
            SeedSlot slot = slots[i];

            if (slot == null) {
                // slot vazio
                shape.setColor(0.2f, 0.2f, 0.2f, 1f);
                shape.rect(sx, barY, SLOT_W, SLOT_H);
            } else if (i == selectedSlot) {
                // selecionado — borda amarela
                shape.setColor(0.9f, 0.75f, 0.1f, 1f);
                shape.rect(sx - 3, barY - 3, SLOT_W + 6, SLOT_H + 6);
                shape.setColor(0.3f, 0.5f, 0.15f, 1f);
                shape.rect(sx, barY, SLOT_W, SLOT_H);
            } else {
                shape.setColor(0.3f, 0.5f, 0.15f, 1f);
                shape.rect(sx, barY, SLOT_W, SLOT_H);
            }

            // Overlay de cooldown (escurece de baixo para cima)
            if (slot != null && !slot.isReady()) {
                float progress = slot.getCooldownProgress(); // 0=cheio escuro, 1=pronto
                float darkH    = SLOT_H * (1f - progress);
                shape.setColor(0f, 0f, 0f, 0.6f);
                shape.rect(sx, barY, SLOT_W, darkH);
            }

            if (slot != null && slot.isReady() && !sun.canAfford(slot.cost)) {
                shape.setColor(0f, 0f, 0f, 0.45f);
                shape.rect(sx, barY, SLOT_W, SLOT_H);
            }
        }

        shape.end();

        // Textura dos slots e textos
        batch.setProjectionMatrix(uiMatrix);
        batch.begin();

        // Sois
        font.setColor(Color.YELLOW);
        font.draw(batch, "☀ " + sun.getSuns(),
            BAR_X + 5f, barY + SLOT_H / 2f + 8f);

        for (int i = 0; i < MAX_SLOTS; i++) {
            float sx   = startX + i * (SLOT_W + SLOT_PAD);
            SeedSlot slot = slots[i];

            if (slot == null) continue;

            // Preview da planta
            if (slotTextures[i] != null) {
                float texScale = Math.min(SLOT_W / slotTextures[i].getWidth(),
                    (SLOT_H - 16) / slotTextures[i].getHeight());
                float tw = slotTextures[i].getWidth()  * texScale;
                float th = slotTextures[i].getHeight() * texScale;
                batch.draw(slotTextures[i],
                    sx + (SLOT_W - tw) / 2f,
                    barY + 16 + (SLOT_H - 16 - th) / 2f,
                    tw, th);
            }

            // Custo
            font.setColor(slot.isReady() ? Color.YELLOW : Color.GRAY);
            font.draw(batch, String.valueOf(slot.cost),
                sx + 4f, barY + 14f);
        }

        batch.end();
    }

    public void dispose(){
        shape.dispose();
        font.dispose();
    }
}

package io.github.PlantsVsZombies10;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Plant {
    private final Texture texture;
    private final float scale;

    // Tile onde a planta ta plantada

    public int tileCol = -1;
    public int tileRow = -1;

    // Posicao em pixels
    public float x, y;

    // Stats da planta
    public float hp, maxHp;
    public int cost;

    // Tiro
    private float shootTimer = 0f;
    private final float shootCooldown;

    public Plant(Texture texture, float scale, int maxHp, int cost, float shootCooldown) {
        this.texture = texture;
        this.scale = scale;
        this.maxHp = maxHp;
        this.cost = cost;
        this.hp = maxHp;
        this.shootCooldown = shootCooldown;
    }

    public float getWidth()  { return texture.getWidth()  * scale; }
    public float getHeight() { return texture.getHeight() * scale; }

    // Atualiza o timer do tiro. Vai retornar true quando for hora de atirar
    public boolean update(float delta) {
        shootTimer += delta;
        if(shootTimer >= shootCooldown){
            shootTimer -= shootTimer;
            return true;
        }
        return false;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, getWidth(), getHeight());
    }

    //Posicao do tiro
    public float getMuzzleX(){return x + getWidth();}
    public float getMuzzleY(){return y + getHeight()/2f;}

    public boolean isAlive(){return hp > 0;}
}

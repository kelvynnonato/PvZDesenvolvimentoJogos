package io.github.PlantsVsZombies10;

import com.badlogic.gdx.utils.Pool;

/**
 * Representa um único sol "físico" no mundo: nasce em cima de um girassol (ou cai do céu,
 * se você quiser usar isso no futuro), cai até uma posição de descanso, fica parado
 * esperando o jogador clicar, e some sozinho depois de um tempo se ninguém coletar.
 */
public class SunDrop implements Pool.Poolable {

    private enum State { FALLING, IDLE, COLLECTED }

    public float x, y;
    private float startY, targetY;
    private State state;

    private float fallTimer;
    private float fallDuration; // tempo de queda - varia conforme quem gerou o sol
    private static final float DEFAULT_FALL_DURATION = 0.8f;

    private float idleTimer;
    private static final float IDLE_DURATION = 8f; // tempo parado antes de sumir sozinho
    private static final float FADE_DURATION = 1.5f; // começa a piscar/sumir faltando esse tempo

    public static final float RADIUS = 44f; // raio usado pro desenho e pra detectar o clique

    public int sunAmount;
    public boolean alive;

    public void init(float x, float startY, float targetY, int sunAmount) {
        init(x, startY, targetY, sunAmount, DEFAULT_FALL_DURATION);
    }

    public void init(float x, float startY, float targetY, int sunAmount, float fallDuration) {
        this.x = x;
        this.startY = startY;
        this.targetY = targetY;
        this.y = startY;
        this.sunAmount = sunAmount;
        this.fallDuration = fallDuration;
        this.state = State.FALLING;
        this.fallTimer = 0f;
        this.idleTimer = 0f;
        this.alive = true;
    }

    public void update(float delta) {
        switch (state) {
            case FALLING:
                fallTimer += delta;
                float t = Math.min(fallTimer / fallDuration, 1f);
                // ease-out: cai rápido e desacelera perto do chão
                float eased = 1f - (1f - t) * (1f - t);
                y = startY + (targetY - startY) * eased;
                if (t >= 1f) {
                    y = targetY;
                    state = State.IDLE;
                }
                break;

            case IDLE:
                idleTimer += delta;
                if (idleTimer >= IDLE_DURATION) {
                    alive = false; // ninguém clicou a tempo
                }
                break;

            case COLLECTED:
                alive = false;
                break;
        }
    }

    // Só pode ser coletado depois que termina de cair (igual ao jogo original)
    public boolean isCollectable() {
        return state == State.IDLE;
    }

    // Teste de clique em coordenadas de mundo
    public boolean isHit(float worldX, float worldY) {
        float dx = worldX - x;
        float dy = worldY - y;
        return (dx * dx + dy * dy) <= RADIUS * RADIUS;
    }

    public void collect() {
        state = State.COLLECTED;
    }

    // Alpha pra desenhar - pisca/esmaece nos últimos instantes antes de sumir sozinho
    public float getAlpha() {
        if (state != State.IDLE) return 1f;
        float remaining = IDLE_DURATION - idleTimer;
        if (remaining < FADE_DURATION) {
            return Math.max(remaining / FADE_DURATION, 0.15f);
        }
        return 1f;
    }

    @Override
    public void reset() {
        x = y = startY = targetY = fallTimer = idleTimer = 0f;
        fallDuration = DEFAULT_FALL_DURATION;
        sunAmount = 0;
        state = State.FALLING;
        alive = false;
    }
}

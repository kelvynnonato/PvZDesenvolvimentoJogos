package io.github.PlantsVsZombies10;

public class SeedSlot {
    public final String name;
    public final int cost;
    public final float cooldown;
    public final Plant.PlantType type;

    private float cooldownTimer = 0f;
    private boolean onCooldown = false;

    public SeedSlot(String name, int cost, float cooldown, Plant.PlantType type) {
        this.name = name;
        this.cost = cost;
        this.cooldown = cooldown;
        this.type = type;
    }

    public void update(float delta){
        if(onCooldown){
            cooldownTimer -= delta;
            if(cooldownTimer <= 0f){
                cooldownTimer -= cooldownTimer;
                onCooldown = false;
            }
        }
    }

    // Chamar quando a semente é usada para plantar
    public void triggerCooldown(){
        onCooldown = true;
        cooldownTimer = cooldown;
    }

    public boolean isReady(){return !onCooldown;}
    public float getCooldownTimer(){return cooldownTimer;}

    //Progresso de cooldown
    public float getCooldownProgress(){
        if(!onCooldown) return 1f;
        return 1 - (cooldownTimer / cooldown);
    }
}

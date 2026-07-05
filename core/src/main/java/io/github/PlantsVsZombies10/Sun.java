package io.github.PlantsVsZombies10;

public class Sun {
    private int suns;

    public Sun(int startingSuns){
        this.suns = startingSuns;
    }

    public int getSuns() {return suns;}

    public boolean canAfford(int cost) {return suns >= cost;}

    public void spend(int cost) { suns -= cost;}

    public void add(int amount) { suns += amount;}
}

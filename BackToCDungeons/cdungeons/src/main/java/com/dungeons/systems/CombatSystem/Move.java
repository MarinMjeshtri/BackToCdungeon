package com.dungeons.systems.CombatSystem;

public class Move {

    private String id;
    private String name;
    private int damage;
    private String description;
    private String statusEffect;
    private int duration;
    private double chance;

    // new fields
    private int hits = 1;             // how many times it hits
    private String hitStyle = "single"; // "single", "rapid", "heal", "clone"
    private int cooldown = 0;          // turns before usable again
    private String abilitySprite = ""; // path to ability image, set manually

    public Move() {}

    public Move(String id, String name, int damage, String description) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatusEffect() { return statusEffect; }
    public void setStatusEffect(String s) { this.statusEffect = s; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getChance() { return chance; }
    public void setChance(double chance) { this.chance = chance; }

    public int getHits() { return hits; }
    public void setHits(int hits) { this.hits = hits; }

    public String getHitStyle() { return hitStyle; }
    public void setHitStyle(String hitStyle) { this.hitStyle = hitStyle; }

    public int getCooldown() { return cooldown; }
    public void setCooldown(int cooldown) { this.cooldown = cooldown; }

    public String getAbilitySprite() { return abilitySprite; }
    public void setAbilitySprite(String abilitySprite) { this.abilitySprite = abilitySprite; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %d dmg x%d | %s", id, name, damage, hits, description);
    }
}
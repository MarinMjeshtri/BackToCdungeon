package com.dungeons.systems.CombatSystem;

/**
 * Represents a combat move (attack or ability) usable by either the player or a boss.
 * Values are loaded from stats.json.
 */
public class Move {

    private String id;
    private String name;
    private int damage;
    private String description;

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

    @Override
    public String toString() {
        return String.format("[%s] %s - %d dmg | %s", id, name, damage, description);
    }
}

package com.dungeons.systems.CombatSystem;

public class StatusEffect {

    public enum Type { DOT, SKIP, HALF_DMG }

    private Type type;
    private int turnsLeft;
    private int dotDamage = 12; // flat DOT tick damage

    public StatusEffect(Type type, int duration) {
        this.type = type;
        this.turnsLeft = duration;
    }

    public Type getType() { return type; }
    public int getTurnsLeft() { return turnsLeft; }
    public void tick() { turnsLeft = Math.max(0, turnsLeft - 1); }
    public boolean isExpired() { return turnsLeft <= 0; }
    public int getDotDamage() { return dotDamage; }

    public String getLabel() {
        switch (type) {
            case DOT:      return "🔥 -" + dotDamage + " dmg/turn (" + turnsLeft + " turns)";
            case SKIP:     return "⚡ STUNNED (" + turnsLeft + " turns)";
            case HALF_DMG: return "🌀 HALF DMG (" + turnsLeft + " turns)";
            default:       return "";
        }
    }
}
package com.dungeons.systems.CombatSystem;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BossLoader extends Combatant {

    private String id;
    private String title;
    private final Random rng = new Random();

    // mood sprites — set from Stats.json, never overwritten by ability sprites
    private String spriteNeutral  = "";
    private String spriteAngry    = "";
    private String spriteThinking = "";
    private String spriteDefeated = "";
    private String spriteCloned   = "";

    // ability sprite — only used during attack animation, never stored as mood
    private String currentAbilitySprite = "";

    private StatusEffect activeEffect = null;
    private double lastKnownPlayerHpPercent = 1.0;
    private boolean isCloned = false;

    public BossLoader() {}

    @Override
    public Move chooseMove() {
        if (moves == null || moves.isEmpty())
            throw new IllegalStateException("Boss has no moves: " + name);
        switch (id) {
            case "CassieYarn":  return cassieAI();
            case "FreakyRelah": return freakyRelahAI();
            case "JohnMKati":   return johnAI();
            default:            return randomDamagingMove();
        }
    }

    private Move cassieAI() {
        return randomDamagingMove();
    }

   private Move freakyRelahAI() {
    // below 40% player hp — go for max burst, no more DOT stalling
    if (lastKnownPlayerHpPercent < 0.4) {
        return moves.stream()
                .filter(m -> m.getDamage() > 0)
                .max((a, b) -> Integer.compare(
                        a.getDamage() * a.getHits(),
                        b.getDamage() * b.getHits()))
                .orElse(randomDamagingMove());
    }

    // above 60% player hp — mix DOT and normal attacks randomly
    // do NOT always prefer DOT, just include it in the pool
    if (lastKnownPlayerHpPercent > 0.6) {
        return randomDamagingMove();
    }

    // between 40-60% — prefer stun or halfDmg moves to control the fight
    List<Move> controlMoves = moves.stream()
            .filter(m -> "skip".equals(m.getStatusEffect()) ||
                         "halfDmg".equals(m.getStatusEffect()))
            .collect(Collectors.toList());
    if (!controlMoves.isEmpty())
        return controlMoves.get(rng.nextInt(controlMoves.size()));

    return randomDamagingMove();
}

    private Move johnAI() {
        if (getHpPercent() < 0.4 && !isCloned) {
            Move cloneMove = moves.stream()
                    .filter(m -> "clone".equals(m.getHitStyle()))
                    .findFirst().orElse(null);
            if (cloneMove != null) return cloneMove;
        }
        if (lastKnownPlayerHpPercent > 0.5) {
            List<Move> statusMoves = moves.stream()
                    .filter(m -> m.getStatusEffect() != null &&
                            !"clone".equals(m.getHitStyle()))
                    .collect(Collectors.toList());
            if (!statusMoves.isEmpty())
                return statusMoves.get(rng.nextInt(statusMoves.size()));
        }
        if (lastKnownPlayerHpPercent < 0.4) {
            Move ironFist = moves.stream()
                    .filter(m -> "Iron Fist".equals(m.getName()))
                    .findFirst().orElse(null);
            if (ironFist != null) return ironFist;
        }
        return randomDamagingMove();
    }

    private Move randomDamagingMove() {
        List<Move> valid = moves.stream()
                .filter(m -> m.getDamage() > 0 || "heal".equals(m.getHitStyle()))
                .collect(Collectors.toList());
        if (valid.isEmpty()) return moves.get(0);
        return valid.get(rng.nextInt(valid.size()));
    }

    // returns the correct mood sprite based on HP — never returns ability sprite
    public String getCurrentSprite() {
        if (currentHp <= 0)        return spriteDefeated.isEmpty() ? spriteNeutral : spriteDefeated;
        if (isCloned)              return spriteCloned.isEmpty()   ? spriteNeutral : spriteCloned;
        if (getHpPercent() <= 0.4) return spriteAngry.isEmpty()    ? spriteNeutral : spriteAngry;
        return spriteNeutral;
    }

    // only used during attack animation in the controller — separate from mood
    public String getCurrentAbilitySprite()          { return currentAbilitySprite; }
    public void setCurrentAbilitySprite(String path) { this.currentAbilitySprite = path; }

    // clears ability sprite after animation so it never bleeds into mood
    public void clearAbilitySprite() { this.currentAbilitySprite = ""; }

    public void applyClone() {
        isCloned = true;
        int restored = (int)(maxHp * getHpPercent());
        currentHp = Math.min(maxHp, currentHp + restored);
    }

    public void applyHeal(int amount) { heal(amount); }

    public void applyEffect(StatusEffect effect) { this.activeEffect = effect; }
    public StatusEffect getActiveEffect()        { return activeEffect; }

    public void tickEffect() {
        if (activeEffect != null) {
            activeEffect.tick();
            if (activeEffect.isExpired()) activeEffect = null;
        }
    }

    public boolean isStunned() {
        return activeEffect != null && activeEffect.getType() == StatusEffect.Type.SKIP;
    }

    public boolean isHalfDmg() {
        return activeEffect != null && activeEffect.getType() == StatusEffect.Type.HALF_DMG;
    }

    public void setLastKnownPlayerHpPercent(double v) { this.lastKnownPlayerHpPercent = v; }
    public boolean isCloned()                         { return isCloned; }

    public String getId()    { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getThinkingSprite()  { return spriteThinking; }
    public String getSpriteNeutral()   { return spriteNeutral; }
    public String getSpriteAngry()     { return spriteAngry; }
    public String getSpriteDefeated()  { return spriteDefeated; }
    public String getSpriteCloned()    { return spriteCloned; }

    public void setSpriteNeutral(String s)  { this.spriteNeutral  = s; }
    public void setSpriteAngry(String s)    { this.spriteAngry    = s; }
    public void setSpriteThinking(String s) { this.spriteThinking = s; }
    public void setSpriteDefeated(String s) { this.spriteDefeated = s; }
    public void setSpriteCloned(String s)   { this.spriteCloned   = s; }

    @Override
    public String toString() {
        return String.format("%s | HP: %d/%d | ATK: %d | DEF: %d",
                name, currentHp, maxHp, attack, defense);
    }
}
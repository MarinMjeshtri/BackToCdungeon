package com.dungeons.systems.CombatSystem;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BossLoader extends Combatant {

    private String id;
    private String title;
    private final Random rng = new Random();

    // sprite paths per mood — fill paths manually in Stats.json
    private String spriteNeutral  = "";
    private String spriteAngry    = "";
    private String spriteThinking = "";
    private String spriteDefeated = "";
    private String spriteCloned   = "";

    // current active ability sprite shown during attack
    private String currentAbilitySprite = "";

    private StatusEffect activeEffect = null;
    private double lastKnownPlayerHpPercent = 1.0;

    // clone state for JohnMKati
    private boolean isCloned = false;

    public BossLoader() {}

    // picks move based on which boss this is
    @Override
    public Move chooseMove() {
        if (moves == null || moves.isEmpty())
            throw new IllegalStateException("Boss has no moves: " + name);
        switch (id) {
            case "CassieYarn":   return cassieAI();
            case "FreakyRelah":  return freakyRelahAI();
            case "JohnMKati":    return johnAI();
            default:             return randomDamagingMove();
        }
    }

    // Boss 1 — fully random, no strategy
    private Move cassieAI() {
        return randomDamagingMove();
    }

    // Boss 2 — uses DOT when player is healthy, bursts when player is low
    private Move freakyRelahAI() {
        if (lastKnownPlayerHpPercent < 0.4) {
            // go for max damage discharge
            return moves.stream()
                    .max((a, b) -> Integer.compare(a.getDamage() * a.getHits(),
                                                   b.getDamage() * b.getHits()))
                    .orElse(randomDamagingMove());
        }
        // prefer DOT attacks while player is healthy
        List<Move> dotMoves = moves.stream()
                .filter(m -> "DOT".equals(m.getStatusEffect()))
                .collect(Collectors.toList());
        if (!dotMoves.isEmpty())
            return dotMoves.get(rng.nextInt(dotMoves.size()));
        return randomDamagingMove();
    }

    // Boss 3 — uses Twining when below 40% if not already cloned,
    // prefers status moves when player is healthy
    private Move johnAI() {
        // clone when low and not yet cloned
        if (getHpPercent() < 0.4 && !isCloned) {
            Move cloneMove = moves.stream()
                    .filter(m -> "clone".equals(m.getHitStyle()))
                    .findFirst().orElse(null);
            if (cloneMove != null) return cloneMove;
        }
        // use status moves when player is above 50%
        if (lastKnownPlayerHpPercent > 0.5) {
            List<Move> statusMoves = moves.stream()
                    .filter(m -> m.getStatusEffect() != null
                            && !"clone".equals(m.getHitStyle()))
                    .collect(Collectors.toList());
            if (!statusMoves.isEmpty())
                return statusMoves.get(rng.nextInt(statusMoves.size()));
        }
        // iron fist when player is low
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

    // returns sprite path based on current HP mood
    public String getCurrentSprite() {
        if (currentHp <= 0)          return spriteDefeated;
        if (isCloned)                return spriteCloned;
        if (getHpPercent() <= 0.4)   return spriteAngry;
        return spriteNeutral;
    }

    // clone mechanic — doubles current HP percentage
    public void applyClone() {
        isCloned = true;
        int restored = (int)(maxHp * getHpPercent());
        currentHp = Math.min(maxHp, currentHp + restored);
    }

    // heal mechanic for Repair Surge type moves
    public void applyHeal(int amount) {
        heal(amount);
    }

    public void applyEffect(StatusEffect effect) { this.activeEffect = effect; }
    public StatusEffect getActiveEffect() { return activeEffect; }

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
    public boolean isCloned() { return isCloned; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getThinkingSprite() { return spriteThinking; }
    public String getSpriteNeutral()  { return spriteNeutral; }
    public String getSpriteAngry()    { return spriteAngry; }
    public String getSpriteDefeated() { return spriteDefeated; }
    public String getSpriteCloned()   { return spriteCloned; }

    public void setSpriteNeutral(String s)   { this.spriteNeutral = s; }
    public void setSpriteAngry(String s)     { this.spriteAngry = s; }
    public void setSpriteThinking(String s)  { this.spriteThinking = s; }
    public void setSpriteDefeated(String s)  { this.spriteDefeated = s; }
    public void setSpriteCloned(String s)    { this.spriteCloned = s; }

    public String getCurrentAbilitySprite()             { return currentAbilitySprite; }
    public void setCurrentAbilitySprite(String path)    { this.currentAbilitySprite = path; }

    @Override
    public String toString() {
        return String.format("%s | HP: %d/%d | ATK: %d | DEF: %d",
                name, currentHp, maxHp, attack, defense);
    }
}
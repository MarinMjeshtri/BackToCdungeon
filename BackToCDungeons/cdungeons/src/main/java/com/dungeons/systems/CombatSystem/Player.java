package com.dungeons.systems.CombatSystem;

import java.util.List;

public class Player extends Combatant {

    private List<Item> items;
    private StatusEffect activeEffect = null;

    public Player() {}

    public int useItem(String itemId) {
        for (Item item : items) {
            if (item.getId().equals(itemId) && item.isAvailable()) {
                item.consume();
                return heal(item.getHealAmount());
            }
        }
        return -1;
    }
//debug
 //   public void setCurrentHp(int currentHp) {
   //     System.out.println("setCurrentHp called with: " + currentHp);
   //     this.currentHp = currentHp;
   // }

    //for the images
    public boolean hasItems() {
        return items != null && items.stream().anyMatch(Item::isAvailable);
    }
    private String spriteNeutral  = "";
    private String spriteDefeated = "";
    private String spriteAttack1  = "";
    private String spriteAttack2  = "";
    private String spriteAttack3  = "";
    private String spriteAttack4  = "";

    public String getSpriteNeutral()   { return spriteNeutral; }
    public String getSpriteDefeated()  { return spriteDefeated; }
    public String getSpriteAttack(int index) {
        switch (index) {
            case 0: return spriteAttack1;
            case 1: return spriteAttack2;
            case 2: return spriteAttack3;
            case 3: return spriteAttack4;
            default: return spriteNeutral;
        }
    }
    public void setSpriteNeutral(String s)  { this.spriteNeutral  = s; }
    public void setSpriteDefeated(String s) { this.spriteDefeated = s; }
    public void setSpriteAttack1(String s)  { this.spriteAttack1  = s; }
    public void setSpriteAttack2(String s)  { this.spriteAttack2  = s; }
    public void setSpriteAttack3(String s)  { this.spriteAttack3  = s; }
    public void setSpriteAttack4(String s)  { this.spriteAttack4  = s; }

    @Override
    public Move chooseMove() {
        throw new UnsupportedOperationException(
            "Player move selection is handled by CombatUI.");
    }

    // ── STATUS EFFECT ─────────────────────────────────────────────────

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

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}
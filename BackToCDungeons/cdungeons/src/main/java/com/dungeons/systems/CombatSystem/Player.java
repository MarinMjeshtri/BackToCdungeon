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

    public boolean hasItems() {
        return items != null && items.stream().anyMatch(Item::isAvailable);
    }

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
package com.dungeons.shopItemsManager;

import com.dungeons.systems.CombatSystem.PlayerProgress;

public class PlayerInventory {

    private static PlayerInventory instance = new PlayerInventory();
    public static PlayerInventory getInstance() { return instance; }
    private PlayerInventory() {}

    private Shop[] slots = new Shop[4]; // 4 item slots total

    public boolean addItem(Shop item) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = item;
                return true;
            }
        }
        return false;
    }

    public Shop getSlot(int index) { return slots[index]; }

    public boolean isFull() {
        for (Shop slot : slots) {
            if (slot == null) return false;
        }
        return true;
    }

    public void clearSlot(int index) { slots[index] = null; }

    // -----------------------------------------------------------------------
    // useItemOutsideCombat(int slotIndex)
    // Called when the player presses 1-4 on the game screen while walking.
    // Returns a result string for console output.
    //
    // HEAL items: restore a percentage of current HP.
    //   - smallHealthPotion: 10% of current HP (see shopItems.json "heal": 50 -- value unused, % used instead)
    //   - bigHealthPotion:   25% of current HP (see shopItems.json "heal": 120 -- value unused, % used instead)
    //   To change heal percentages, edit SMALL_HEAL_PERCENT and BIG_HEAL_PERCENT below.
    //
    // ATK items: cannot be used while walking. Prints a message to console only.
    //   ATK potions only work during combat (one turn effect).
    //
    // TO ADD A NEW POTION TYPE IN THE FUTURE:
    //   1. Add its key to shopItems.json with the right effects map
    //   2. Add a new else-if branch below checking item.effects.containsKey("yourNewEffect")
    //   3. Handle the effect logic the same way as heal or atk
    // -----------------------------------------------------------------------

    // Heal percentages for walking use. Change these to tune healing strength.
    // SMALL_HEAL_PERCENT = 0.10 means 10% of current HP restored.
    // BIG_HEAL_PERCENT   = 0.25 means 25% of current HP restored.
    private static final double SMALL_HEAL_PERCENT = 0.10;
    private static final double BIG_HEAL_PERCENT   = 0.25;

    // Threshold to decide if a potion is "small" or "big" based on JSON heal value.
    // If heal value in JSON is <= SMALL_HEAL_THRESHOLD, treat as small. Otherwise big.
    // smallHealthPotion has heal:50, bigHealthPotion has heal:120. Threshold of 100 splits them.
    private static final int SMALL_HEAL_THRESHOLD = 100;

    public String useItemOutsideCombat(int slotIndex) {
        Shop item = slots[slotIndex];

        if (item == null) {
            return "Slot " + (slotIndex + 1) + " is empty.";
        }

        PlayerProgress progress = PlayerProgress.getInstance();

        if (item.effects == null || item.effects.isEmpty()) {
            return "Item has no effects.";
        }

        // --- HEAL EFFECT ---
        if (item.effects.containsKey("heal")) {
            int currentHp  = progress.getCurrentHp();
            int maxHp      = progress.getScaledHp();

            // already at full HP, do not waste the item
            if (currentHp >= maxHp) {
                return "HP is already full. Item not used.";
            }

            double healValue = item.effects.get("heal");

            // decide percentage based on heal value in JSON
            // to change thresholds or add new tiers, edit here
            double percent = healValue <= SMALL_HEAL_THRESHOLD
                    ? SMALL_HEAL_PERCENT
                    : BIG_HEAL_PERCENT;

            int healAmount = (int)(currentHp * percent);
            int newHp      = Math.min(maxHp, currentHp + healAmount);

            progress.setCurrentHp(newHp);

            // consume item from slot
            clearSlot(slotIndex);

            return "Used " + item.displayName + ". Restored " + healAmount + " HP. HP: " + newHp + "/" + maxHp;
        }

        // --- ATK EFFECT (cannot be used while walking) ---
        if (item.effects.containsKey("atk")) {
            // ATK potions only work in combat. Print message, do not consume.
            System.out.println("[Item] " + item.displayName + " can only be used in combat.");
            return "Cannot use " + item.displayName + " outside of combat.";
        }

        // --- FUTURE EFFECT TYPES ---
        // Add new effect branches here following the same pattern:
        // if (item.effects.containsKey("yourEffect")) { ... clearSlot(slotIndex); return "..."; }

        return "Item effect not recognized.";
    }

    // -----------------------------------------------------------------------
    // useItemInCombat(int slotIndex, Player player)
    // Called from CombatController when the player clicks an item button in combat.
    // Returns the result as a CombatItemResult so the controller knows what happened.
    //
    // HEAL: restores percentage of current HP directly on the Player object.
    //       The engine then logs the HP change in the turn.
    //
    // ATK:  applies a temporary ATK multiplier for the current turn only.
    //       ATK_BOOST_MULTIPLIER controls how strong the boost is.
    //       After the turn ends, CombatController must restore original ATK (see comment there).
    //
    // TO ADD A NEW COMBAT ITEM TYPE IN THE FUTURE:
    //   1. Add to CombatItemResult enum a new type
    //   2. Add a new branch here handling the effect
    //   3. Handle the result in CombatController.handleItemUse()
    // -----------------------------------------------------------------------

    // ATK multiplier applied for one combat turn when an ATK potion is used.
    // 1.3 = 30% more damage for that turn. Change to tune strength.
    public static final double ATK_BOOST_MULTIPLIER = 1.3;

    public enum CombatItemResult {
        EMPTY,       // slot was empty
        HEALED,      // heal item used successfully
        ATK_BOOST,   // atk potion used, boost applied for this turn
        NO_EFFECT    // item had no recognized effect
    }

    public CombatItemResult useItemInCombat(int slotIndex,
                                            com.dungeons.systems.CombatSystem.Player combatPlayer) {

        Shop item = slots[slotIndex];
        if (item == null) return CombatItemResult.EMPTY;

        if (item.effects == null || item.effects.isEmpty()) return CombatItemResult.NO_EFFECT;

        // --- HEAL EFFECT IN COMBAT ---
        if (item.effects.containsKey("heal")) {
            int currentHp  = combatPlayer.getCurrentHp();
            int maxHp      = combatPlayer.getMaxHp();

            if (currentHp >= maxHp) {
                // already full, do not consume
                return CombatItemResult.NO_EFFECT;
            }

            double healValue = item.effects.get("heal");
            double percent   = healValue <= SMALL_HEAL_THRESHOLD
                    ? SMALL_HEAL_PERCENT
                    : BIG_HEAL_PERCENT;

            int healAmount = (int)(currentHp * percent);
            int newHp      = Math.min(maxHp, currentHp + healAmount);

            combatPlayer.setCurrentHp(newHp);

            // also update the persistent saved HP so it carries to the next fight
            PlayerProgress.getInstance().setCurrentHp(newHp);

            clearSlot(slotIndex);
            return CombatItemResult.HEALED;
        }

        // --- ATK EFFECT IN COMBAT ---
        if (item.effects.containsKey("atk")) {
            // boost is applied by multiplying current ATK by ATK_BOOST_MULTIPLIER
            // CombatController stores the original ATK and restores it after the turn
            // see CombatController.handleItemUse() for the restore logic
            int boostedAtk = (int)(combatPlayer.getAttack() * ATK_BOOST_MULTIPLIER);
            combatPlayer.setAttack(boostedAtk);

            clearSlot(slotIndex);
            return CombatItemResult.ATK_BOOST;
        }

        // --- FUTURE EFFECT TYPES ---
        // Add new combat effect branches here following the same pattern.

        return CombatItemResult.NO_EFFECT;
    }
}
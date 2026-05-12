package com.dungeons.systems.CombatSystem;

// RewardTable is a lookup table for XP and gold rewards.
// Every reward number in the game lives here in one place.
// Nothing is stored here - it is all constants and calculation methods.
// 'static' methods mean you call them directly: RewardTable.getBossXP("CassieYarn")
// without needing to create a RewardTable object first.
public class RewardTable {

    // -----------------------------------------------------------------------
    // BOSS REWARDS
    // Named bosses give fixed rewards. No level scaling - bosses are always
    // the same difficulty. Each boss gives more than the previous one.
    // To change a boss's reward, change the number on its line.
    // -----------------------------------------------------------------------

    // Returns XP rewarded for defeating a named boss.
    // 'default' handles any boss ID not listed - safe fallback of 100 XP.
    public static int getBossXP(String bossId) {
        switch (bossId) {
            case "CassieYarn":  return 25;  // first boss - 120 XP
            case "FreakyRelah": return 50;  // second boss - 200 XP
            case "JohnMKati":   return 150;  // final boss - 320 XP
            default:            return 100;  // unknown boss - safe fallback
        }
    }

    // Returns gold rewarded for defeating a named boss.
    public static int getBossGold(String bossId) {
        switch (bossId) {
            case "CassieYarn":  return 2;   // first boss - 30 gold
            case "FreakyRelah": return 3;   // second boss - 55 gold
            case "JohnMKati":   return 5;   // final boss - 90 gold
            default:            return 5;   // unknown boss - safe fallback
        }
    }


    // -----------------------------------------------------------------------
    // MOB REWARDS
    // Mobs scale their rewards with their level (which comes from which room they
    // were spawned in). Higher room = higher level = more XP and gold.
    //
    // XP equation:   XP_MOB_BASE + (mobLevel - 1) * XP_MOB_PER_LEVEL
    // Gold equation: GOLD_MOB_BASE + (mobLevel - 1) * GOLD_MOB_PER_LEVEL
    //
    // At level 1: the (level-1) term is 0, so result is just the base value.
    // Every level above 1 adds the per-level bonus on top.
    //
    // Results:
    //   Level 1 mob: XP=20, Gold=1
    //   Level 3 mob: XP=30, Gold=3
    //   Level 5 mob: XP=40, Gold=5
    //   Level 8 mob: XP=55, Gold=8
    //
    // To make mobs more rewarding: raise XP_MOB_PER_LEVEL or GOLD_MOB_PER_LEVEL.
    // To raise the floor (level 1 reward): raise XP_MOB_BASE or GOLD_MOB_BASE.
    // -----------------------------------------------------------------------

    private static final int XP_MOB_BASE       = 6; // XP for a level 1 mob
    private static final int XP_MOB_PER_LEVEL  = 2;  // extra XP added per level above 1

    private static final int GOLD_MOB_BASE      = 1;  // gold for a level 1 mob (base value is 1)
    private static final int GOLD_MOB_PER_LEVEL = 0;  // extra gold added per level above 1

    // Returns XP for a mob at the given level.
    // Math.max(1, mobLevel) is a safety guard - level can never go below 1.
    public static int getMobXP(int mobLevel) {
        int level = Math.max(1, mobLevel); // clamp to minimum level 1
        return XP_MOB_BASE + (level - 1) * XP_MOB_PER_LEVEL;
    }

    // Returns gold for a mob at the given level.
    public static int getMobGold(int mobLevel) {
        int level = Math.max(1, mobLevel); // clamp to minimum level 1
        return GOLD_MOB_BASE + (level - 1) * GOLD_MOB_PER_LEVEL;
    }
}
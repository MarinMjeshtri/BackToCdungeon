package com.dungeons.systems.CombatSystem;

// PlayerProgress is the XP, level, and gold tracker for the player.
// It is a SINGLETON - meaning there is only ever ONE of these objects alive in the whole game.
// Any file that needs to read or change the player's level calls PlayerProgress.getInstance()
// and gets back that same one shared object.
public class PlayerProgress {

    // --- SINGLETON SETUP ---
    // This line creates the one and only PlayerProgress object when the game starts.
    // It is stored in 'instance' which is static (shared, not per-object).
    private static PlayerProgress instance = new PlayerProgress();

    // Any file calls PlayerProgress.getInstance() to get the shared object.
    public static PlayerProgress getInstance() { return instance; }

    // reset() throws away the current object and creates a fresh one.
    // Only used in CombatEngineTest to start a clean test run.
    public static void reset() { instance = new PlayerProgress(); }


    // --- BASE STATS (level 1 values) ---
    // These must match the player's stats in Stats.json.
    // If you change the player's HP in Stats.json, change BASE_HP here too.
    // 'static final' means these are constants - one shared value, never changes at runtime.
    private static final int BASE_HP  = 200; // player HP at level 1 *THIS MUST MATCH VALE IN JSON* IT doesnt cause a logic issue just a visual. Sorry
    private static final int BASE_ATK = 0;  // player ATK at level 1
    private static final int BASE_DEF = 8;   // player DEF at level 1


    // --- STAT GROWTH PER LEVEL ---
    // How much each stat grows every time the player gains a level.
    // Raise these to make leveling feel more powerful. Lower to make it subtle.
    private static final int HP_PER_LEVEL  = 25; // +20 HP each level
    private static final int ATK_PER_LEVEL = 3;  // +2 ATK each level
    private static final int DEF_PER_LEVEL = 1;  // +1 DEF each level


    // --- XP CURVE CONSTANTS ---
    // XP required to reach the next level = XP_BASE * (current level ^ XP_EXPONENT)
    // XP_BASE = 80:  lower this to make leveling faster overall
    // XP_EXPONENT = 1.4:  higher values make late levels require much more XP than early ones
    //
    // Example thresholds:
    //   Level 1 -> 2:  80 * 1^1.4 =  80 XP
    //   Level 5 -> 6:  80 * 5^1.4 = 262 XP
    //   Level 10 -> 11: 80 * 10^1.4 = 506 XP
    private static final int    XP_BASE     = 1;
    private static final double XP_EXPONENT = 1.1;

    // Hard cap on level. Player cannot go above this no matter how much XP they gain.
    private static final int MAX_LEVEL = 30;


    // --- LIVE STATE (these change as the player plays) ---
    private int level = 1;  // current player level, starts at 1
    private int xp    = 0;  // current XP within this level, starts at 0
    private int gold  = 100;  // total gold collected, starts at 0
    private int currentHp = -1; //If its the first run, player spawns full hp
    private int maxHpBonusPercent = 0;
    private int attackDamageBonusPercent = 0;

    // Constructor is private - nobody can write 'new PlayerProgress()' from outside.
    // The only way to get this object is getInstance(). This enforces the singleton.
    private PlayerProgress() {}


    // --- addXP ---
    // Adds XP to the player and handles leveling up.
    // Returns true if the player leveled up at least once (so the UI can show a message).
    public boolean addXP(int amount) {
        if (level >= MAX_LEVEL) return false; // already max level, XP is ignored

        xp += amount; // add the earned XP

        boolean leveledUp = false;

        // Loop: keep leveling up as long as xp is enough and level is below cap.
        // The loop handles the rare case where a single reward pushes multiple levels.
        while (level < MAX_LEVEL && xp >= xpToNextLevel()) {
            xp -= xpToNextLevel();
            if (currentHp != -1) {
                int oldMax  = getScaledHp(); // max before level up
                int missing = oldMax - currentHp; // HP missing before level up
                level++;
                int newMax  = getScaledHp(); // max after level up
                currentHp = Math.max(1, newMax - missing); // preserve missing HP
            } else {
                level++;
            }
            leveledUp = true;
        }

        return leveledUp; // true if at least one level-up happened
    }


    // --- xpToNextLevel ---
    // Returns the XP needed to go from the current level to the next one.
    // Equation: XP_BASE * (level ^ XP_EXPONENT)
    // At max level, returns a huge number so the while loop in addXP never triggers.
    public int xpToNextLevel() {
        if (level >= MAX_LEVEL) return Integer.MAX_VALUE; // 2 billion - effectively unreachable
        return (int)(XP_BASE * Math.pow(level, XP_EXPONENT));
    }


    // --- addGold ---
    // Adds gold to the total. Gold never goes down.
    public void addGold(int amount) { gold += amount; }


    // --- SCALED STAT GETTERS ---
    // These calculate what the player's stats SHOULD be at the current level.
    // They are NOT automatically applied to the Player object.
    // The commented applyToPlayer() method below is what actually applies them.
    //
    // Formula: BASE + (level - 1) * GROWTH
    // At level 1: (1-1) * anything = 0, so result equals the base value.
    // At level 5: (5-1) * growth = 4 * growth added on top of base.
    //
    // Stat table:
    //   Level 1:  HP=420, ATK=25, DEF=8
    //   Level 5:  HP=500, ATK=33, DEF=12
    //   Level 10: HP=600, ATK=43, DEF=17
    //   Level 20: HP=800, ATK=63, DEF=27
    public int getScaledHp()  { return applyPercent(BASE_HP + (level - 1) * HP_PER_LEVEL, maxHpBonusPercent); }
    public int getScaledAtk() { return BASE_ATK + (level - 1) * ATK_PER_LEVEL; }
    public int getScaledDef() { return BASE_DEF + (level - 1) * DEF_PER_LEVEL; }

    private int applyPercent(int value, int percent) {
        return (int)Math.round(value * (1.0 + percent / 100.0));
    }

    public void addMaxHpPercentBonus(int percent) {
        int oldMax = getScaledHp();
        maxHpBonusPercent += percent;
        int newMax = getScaledHp();
        int gained = Math.max(0, newMax - oldMax);
        currentHp = currentHp == -1 ? newMax : currentHp + gained;
    }

    public void addAttackDamagePercentBonus(int percent) {
        attackDamageBonusPercent += percent;
    }

    public int getMaxHpBonusPercent() { return maxHpBonusPercent; }
    public int getAttackDamageBonusPercent() { return attackDamageBonusPercent; }


    // --- applyToPlayer ---
    // Applies the player's scaled level stats to the actual Player object used in combat.
    public void applyToPlayer(Player player) {
        if (StatsLoader.opMode) {
            player.setMaxHp(9999);
            player.setCurrentHp(9999);
            player.setAttack(999);
            player.setDefense(999);
            currentHp = 9999;
            System.out.println("OP mode active | HP: 9999 | ATK: 999 | DEF: 999");
            return;
        }

        int hp  = getScaledHp();
        int atk = getScaledAtk();
        int def = getScaledDef();

        player.setMaxHp(hp);
        player.setAttack(atk);
        player.setDefense(def);
        if (attackDamageBonusPercent > 0 && player.getMoves() != null) {
            for (Move move : player.getMoves()) {
                if (move.getDamage() > 0) {
                    move.setDamage(applyPercent(move.getDamage(), attackDamageBonusPercent));
                }
            }
        }
        System.out.println("Level: " + level + " | scaledHp: " + getScaledHp() + " | current: " + player.getCurrentHp()); //debug
    }
    private int oldMaxHp = -1; // tracks max HP before last level up
    public int getOldMaxHp()          { return oldMaxHp; }
    public void setOldMaxHp(int hp)   { this.oldMaxHp = hp; }

    // --- GETTERS ---
    // Read-only access to live state. There are no setters for level or XP on purpose -
    // only addXP() is allowed to change them, ensuring the XP curve is always applied correctly.
    public int getLevel()         { return level; }
    public int getXp()            { return xp; }
    public int getGold()          { return gold; }
    public int getXpToNextLevel() { return xpToNextLevel(); } // shortcut for UI display
    public int getCurrentHp()        { return currentHp; }
    public void setCurrentHp(int hp) { this.currentHp = hp; }


    // toString: used for console debug output. Example: "Level 3 | XP 45/163 | Gold 12"
    @Override
    public String toString() {
        return String.format("Level %d | XP %d/%d | Gold %d",
                level, xp, xpToNextLevel(), gold);
    }
}

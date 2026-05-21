package com.dungeons.systems.CombatSystem;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

// BossLoader represents any enemy in a fight - named bosses or mobs.
// It extends Combatant, meaning it automatically has HP, ATK, DEF, takeDamage(),
// heal(), and the move list without rewriting any of that code here.
// What BossLoader adds on top: AI move selection, sprites, status effects, mob level, rewards.
public class BossLoader extends Combatant {

    private String id;    // internal ID matching the key in Stats.json ("CassieYarn", "Mob1" etc)
    private String title; // display subtitle, set to "" by default (not used yet)

    // rng = random number generator. Used in AI methods to pick random moves.
    // 'final' means this object is created once and never replaced.
    private final Random rng = new Random();

    // mobLevel tracks which room level this enemy was spawned at.
    // For named bosses (CassieYarn, FreakyRelah, JohnMKati) this stays 1 and has no effect.
    // For Mob1-Mob5, StatsLoader sets this when creating the mob. It is used in reward calculation.
    private int mobLevel = 1;

    // --- MOOD SPRITES ---
    // These are file paths to images shown on the boss portrait during combat.
    // They are set from Stats.json by StatsLoader. The controller reads getCurrentSprite()
    // each turn to decide which one to display.
    private String spriteNeutral  = ""; // shown at full/high HP
    private String spriteAngry    = ""; // shown when HP drops below 40%
    private String spriteThinking = ""; // not currently used in logic
    private String spriteDefeated = ""; // shown when HP reaches 0
    private String spriteCloned   = ""; // shown when JohnMKati uses Twining (clone)

    // currentAbilitySprite is SEPARATE from mood sprites.
    // It holds the path to an attack animation image, set when the boss picks a move.
    // The controller shows it during the attack animation, then clears it.
    // It is never mixed with the mood sprites.
    private String currentAbilitySprite = "";

    // activeEffect holds the current status effect on this boss (DOT, stun, halfDmg).
    // null means no effect is active. Only one effect can be active at a time.
    private StatusEffect activeEffect = null;

    // lastKnownPlayerHpPercent: the AI reads this to decide how aggressive to be.
    // Updated every turn by CombatEngine before the boss picks its move.
    // Starts at 1.0 (100% player HP assumed at start of fight).
    private double lastKnownPlayerHpPercent = 1.0;

    // isCloned: true after JohnMKati uses the Twining move.
    // Affects sprite selection and prevents him from cloning twice (2x HP buff)
    private boolean isCloned = false;

    // Empty constructor needed because StatsLoader builds the object piece by piece
    // using setters after creation rather than passing everything in at once.
    public BossLoader() {}


    // -----------------------------------------------------------------------
    // chooseMove()
    // The engine calls this every turn when it is the boss's turn to act.
    // This is the "router" - it looks at the boss ID and sends control
    // to the correct AI method. Mobs and unknown IDs all use randomDamagingMove().
    // -----------------------------------------------------------------------
    @Override
    public Move chooseMove() {
        if (moves == null || moves.isEmpty())
            throw new IllegalStateException("Boss has no moves: " + name); // safety check

        switch (id) {
            case "CassieYarn":  return cassieAI();          // Cassie's own AI
            case "FreakyRelah": return freakyRelahAI();     // Relah's HP-phase AI
            case "JohnMKati":   return johnAI();            // John's clone + burst AI
            case "Mob1":        return randomDamagingMove(); // all mobs = random
            case "Mob2":        return randomDamagingMove();
            case "Mob3":        return randomDamagingMove();
            case "Mob4":        return randomDamagingMove();
            case "Mob5":        return randomDamagingMove();
            default:            return randomDamagingMove(); // fallback for any unknown ID
        }
    }


    // -----------------------------------------------------------------------
    // cassieAI()
    // Cassie currently uses random moves. Placeholder for future smarter logic.
    // To give Cassie phases or priorities, replace this with actual conditions.
    // -----------------------------------------------------------------------
    private Move cassieAI() {
        return randomDamagingMove();
    }


    // -----------------------------------------------------------------------
    // freakyRelahAI()
    // Three-phase AI based on the player's current HP percentage.
    //
    // Phase 1 (player HP > 60%): random attacks, no special priority
    // Phase 2 (player HP 40-60%): prefer stun or halfDmg moves to control the fight
    // Phase 3 (player HP < 40%): go for maximum burst damage to finish the player
    //
    // 'lastKnownPlayerHpPercent' is a value between 0.0 and 1.0.
    // 0.4 = 40% HP, 0.6 = 60% HP. Change these thresholds to shift when phases kick in.
    // -----------------------------------------------------------------------
    private Move freakyRelahAI() {

        // Phase 3: player is low - pick the move with highest total damage output
        // getDamage() * getHits() = total damage if all hits land (before defense)
        if (lastKnownPlayerHpPercent < 0.4) {
            return moves.stream()
                    .filter(m -> m.getDamage() > 0) // only consider damaging moves
                    .max((a, b) -> Integer.compare(
                            a.getDamage() * a.getHits(),
                            b.getDamage() * b.getHits())) // compare by total damage potential
                    .orElse(randomDamagingMove()); // fallback if no damaging move found
        }

        // Phase 1: player is healthy - just attack randomly, no special pressure
        if (lastKnownPlayerHpPercent > 0.6) {
            return randomDamagingMove();
        }

        // Phase 2: player is in the middle range - prefer control moves (stun or halfDmg)
        // stream().filter() builds a list of moves that have one of these two status effects
        List<Move> controlMoves = moves.stream()
                .filter(m -> "skip".equals(m.getStatusEffect()) ||    // "skip" = stun
                        "halfDmg".equals(m.getStatusEffect()))   // "halfDmg" = halve player damage
                .collect(Collectors.toList()); // collect results into a list

        if (!controlMoves.isEmpty())
            return controlMoves.get(rng.nextInt(controlMoves.size())); // pick one at random

        return randomDamagingMove(); // fallback if no control moves exist
    }


    // -----------------------------------------------------------------------
    // johnAI()
    // Three-condition AI for JohnMKati.
    //
    // Condition 1: John's own HP is below 40% AND he has not yet cloned himself
    //              -> use Twining (clone move) to double his effective HP
    // Condition 2: player HP above 50% -> prefer moves with status effects (pressure early)
    // Condition 3: player HP below 40% -> use Iron Fist specifically (80 dmg + 70% stun)
    // Fallback: random damaging move
    // -----------------------------------------------------------------------
    private Move johnAI() {

        // Condition 1: desperate clone - only triggers once (isCloned prevents reuse)
        if (getHpPercent() < 0.4 && !isCloned) {
            Move cloneMove = moves.stream()
                    .filter(m -> "clone".equals(m.getHitStyle())) // find the Twining move
                    .findFirst().orElse(null);
            if (cloneMove != null) return cloneMove;
        }

        // Condition 2: player is healthy - apply pressure with status effects
        if (lastKnownPlayerHpPercent > 0.5) {
            List<Move> statusMoves = moves.stream()
                    .filter(m -> m.getStatusEffect() != null &&      // must have an effect
                            !"clone".equals(m.getHitStyle()))        // exclude Twining
                    .collect(Collectors.toList());
            if (!statusMoves.isEmpty())
                return statusMoves.get(rng.nextInt(statusMoves.size()));
        }

        // Condition 3: player is low - use Iron Fist for the kill attempt
        if (lastKnownPlayerHpPercent < 0.4) {
            Move ironFist = moves.stream()
                    .filter(m -> "Iron Fist".equals(m.getName())) // find by exact name
                    .findFirst().orElse(null);
            if (ironFist != null) return ironFist;
        }

        return randomDamagingMove(); // default if no condition matched
    }


    // -----------------------------------------------------------------------
    // randomDamagingMove()
    // Picks a random move from the move list that either deals damage or is a heal.
    // Used by all mobs and as a fallback in boss AI methods.
    // 'stream().filter()' removes moves with 0 damage that are not heals (like clone).
    // 'rng.nextInt(size)' returns a random whole number from 0 to (size - 1).
    // -----------------------------------------------------------------------
    private Move randomDamagingMove() {
        List<Move> valid = moves.stream()
                .filter(m -> m.getDamage() > 0 || "heal".equals(m.getHitStyle()))
                .collect(Collectors.toList());
        if (valid.isEmpty()) return moves.get(0); // last resort: first move in list
        return valid.get(rng.nextInt(valid.size())); // pick randomly
    }


    // -----------------------------------------------------------------------
    // getCurrentSprite()
    // Returns the correct mood image path based on the boss's current state.
    // Called by CombatController every time the portrait needs updating.
    // Priority order: defeated > cloned > angry (low HP) > neutral
    // The 0.4 threshold (40% HP) controls when the angry sprite activates.
    // Change 0.4 to a higher number to make the angry sprite appear sooner.
    // -----------------------------------------------------------------------
    public String getCurrentSprite() {
        if (currentHp <= 0)        return spriteDefeated.isEmpty() ? spriteNeutral : spriteDefeated;
        if (isCloned)              return spriteCloned.isEmpty()   ? spriteNeutral : spriteCloned;
        if (getHpPercent() <= 0.4) return spriteAngry.isEmpty()    ? spriteNeutral : spriteAngry;
        return spriteNeutral; // default state
    }

    // Returns the ability sprite path (attack animation image).
    // The controller shows this during the attack animation frame.
    public String getCurrentAbilitySprite()          { return currentAbilitySprite; }
    public void setCurrentAbilitySprite(String path) { this.currentAbilitySprite = path; }

    // clearAbilitySprite: called by the controller after the animation finishes.
    // Prevents the attack image from bleeding into the mood portrait.
    public void clearAbilitySprite() { this.currentAbilitySprite = ""; }


    // -----------------------------------------------------------------------
    // applyClone()
    // Used by JohnMKati's Twining move. Sets isCloned to true and restores HP.
    // Current * 2
    // Math.min(maxHp, ...) ensures he never exceeds max HP.
    // -----------------------------------------------------------------------
    public void applyClone() {
        isCloned = true;
        currentHp = Math.min(maxHp, currentHp * 2); // add it, cap at maxHp
    }

    // applyHeal: used for moves with hitStyle="heal" (like Cassie's Repair Surge).
    // Calls the inherited heal() method from Combatant which caps at maxHp.
    public void applyHeal(int amount) { heal(amount); }


    // -----------------------------------------------------------------------
    // STATUS EFFECT METHODS
    // applyEffect: replaces whatever effect is currently active.
    //              Only one effect can be on the boss at a time.
    // tickEffect:  counts down the effect by one turn. Clears it if expired.
    // isStunned:   true if the active effect is a SKIP (stun) type.
    // isHalfDmg:   true if the active effect is HALF_DMG type.
    // -----------------------------------------------------------------------
    public void applyEffect(StatusEffect effect) { this.activeEffect = effect; }
    public StatusEffect getActiveEffect()        { return activeEffect; }

    public void tickEffect() {
        if (activeEffect != null) {
            activeEffect.tick();                               // count down one turn
            if (activeEffect.isExpired()) activeEffect = null; // remove when done
        }
    }

    public boolean isStunned() {
        return activeEffect != null && activeEffect.getType() == StatusEffect.Type.SKIP;
    }

    public boolean isHalfDmg() {
        return activeEffect != null && activeEffect.getType() == StatusEffect.Type.HALF_DMG;
    }

    // Called by CombatEngine each turn to keep the AI aware of player HP.
    // 'v' is a value between 0.0 and 1.0 (0.4 = player at 40% HP).
    public void setLastKnownPlayerHpPercent(double v) { this.lastKnownPlayerHpPercent = v; }

    public boolean isCloned() { return isCloned; }


    // -----------------------------------------------------------------------
    // MOB LEVEL
    // mobLevel is set by StatsLoader when spawning a mob with loadBossAtLevel().
    // Named bosses always have mobLevel=1. It is used only in reward calculation.
    // -----------------------------------------------------------------------
    public int getMobLevel()           { return mobLevel; }
    public void setMobLevel(int level) { this.mobLevel = level; }


    // -----------------------------------------------------------------------
    // REWARD HELPERS
    // These are called by CombatEngine.grantRewards() automatically when this enemy dies.
    // They route to RewardTable based on whether this is a mob or a named boss.
    // id.startsWith("Mob") checks if the ID begins with "Mob" (Mob1, Mob2, etc.)
    // -----------------------------------------------------------------------

    // Returns the XP this enemy gives when defeated.
    public int getXPReward() {
        if (id != null && id.startsWith("Mob")) {
            return RewardTable.getMobXP(mobLevel); // mob: scales with level
        }
        return RewardTable.getBossXP(id); // named boss: fixed value from RewardTable
    }

    // Returns the gold this enemy gives when defeated.
    public int getGoldReward() {
        if (id != null && id.startsWith("Mob")) {
            return RewardTable.getMobGold(mobLevel); // mob: scales with level
        }
        return RewardTable.getBossGold(id); // named boss: fixed value from RewardTable
    }


    // --- GETTERS AND SETTERS ---
    public String getId()    { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // Sprite getters (read by CombatController for portrait display)
    public String getThinkingSprite()  { return spriteThinking; }
    public String getSpriteNeutral()   { return spriteNeutral; }
    public String getSpriteAngry()     { return spriteAngry; }
    public String getSpriteDefeated()  { return spriteDefeated; }
    public String getSpriteCloned()    { return spriteCloned; }

    // Sprite setters (called by StatsLoader when loading from Stats.json)
    public void setSpriteNeutral(String s)  { this.spriteNeutral  = s; }
    public void setSpriteAngry(String s)    { this.spriteAngry    = s; }
    public void setSpriteThinking(String s) { this.spriteThinking = s; }
    public void setSpriteDefeated(String s) { this.spriteDefeated = s; }
    public void setSpriteCloned(String s)   { this.spriteCloned   = s; }

    // toString: debug-friendly one-line summary of this enemy's current state.
    // The Lv. shows mob level (always 1 for named bosses).
    @Override
    public String toString() {
        return String.format("%s (Lv.%d) | HP: %d/%d | ATK: %d | DEF: %d",
                name, mobLevel, currentHp, maxHp, attack, defense);
    }
}
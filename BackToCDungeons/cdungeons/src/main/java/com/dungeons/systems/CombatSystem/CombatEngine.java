package com.dungeons.systems.CombatSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// CombatEngine is the brain of every fight.
// It receives a Player and a BossLoader at the start, then processes one full turn
// at a time through processTurnByIndex(). Each call to that method = one complete round
// (player acts, then boss acts). The engine tracks all state: round number, cooldowns,
// guard/counter flags, damage dealt, and the final result.
// CombatController calls this and uses the returned TurnLog to update the UI.
public class CombatEngine {

    // The two fighters. Set once in the constructor and never replaced.
    // 'final' means these references cannot be reassigned after construction.
    private final Player player;
    private final BossLoader boss;

    // Random number generator. Used for guard/counter rolls, talk/insult rolls,
    // status effect proc rolls. One shared instance for the whole fight.
    private final Random rng = new Random();

    // Tracks which turn number we are on. Starts at 0, incremented at the top of each turn.
    private int roundNumber = 0;

    // Tracks the fight outcome. Starts as ONGOING.
    // Changes to PLAYER_WIN or PLAYER_LOSE when a fighter's HP hits 0.
    // Once it is no longer ONGOING, processTurnByIndex() refuses to run.
    private CombatResult result = CombatResult.ONGOING;

    // Full history of every turn. Each TurnLog is added after processTurnByIndex() finishes.
    // CombatController can read this to replay or review the fight.
    private final List<TurnLog> history = new ArrayList<>();

    // guardActive: true if the player pressed Guard this turn before the boss attacked.
    // guardCooldownLeft: turns remaining until Guard can be used again (starts at 0 = usable).
    // counterActive: true if the player pressed Counter this turn.
    private boolean guardActive       = false;
    private boolean counterActive     = false;

    // talkModifier: multiplier applied to boss incoming damage this turn.
    // 1.0 = normal damage. 0.5 = half damage (talk succeeded). 1.2 or 2.0 = more damage (talk failed).
    // Resets to 1.0 at the end of the player action phase every turn.
    private double  talkModifier      = 1.0;

    // Flags to prevent using both talk and guard/counter in the same turn, or stacking them.
    private boolean talkUsedThisTurn  = false;
    private boolean guardUsedThisTurn = false;

    // guardCooldownLeft: counts down each turn. Guard is only available when this is 0.
    // After a guard is used (successfully or not), this is set to 3 (blocks guard for 3 turns).
    private int guardCooldownLeft = 0;

    // move4CooldownLeft: counts down each turn. Move 4 (Overload Burst) is only usable when 0.
    // After use, this is set to move.getCooldown() + 1 (the +1 accounts for the current turn).
    private int move4CooldownLeft = 0;

    // Stores the individual damage values from the boss's last attack (one entry per hit).
    // Used by CombatController to display floating damage numbers for multi-hit moves.
    private List<Integer> lastBossHitList = new ArrayList<>();

    // Stores the hitStyle of the boss's last move ("single", "rapid", "heal", "clone").
    // Used by CombatController to decide which animation to play.
    private String lastBossMoveHitStyle = "single";


    // -----------------------------------------------------------------------
    // STATUS EFFECT CHANCE CAPS
    // These are the maximum chance any boss move can have to apply a status effect.
    // Even if Stats.json says a boss move has a 70% stun chance, the engine caps it here.
    // This prevents any fight feeling completely unfair due to constant effect application.
    //
    // Values are 0.0 to 1.0 (1.0 = 100% = always applies, 0.0 = 0% = never applies).
    // These caps ONLY affect boss moves. Player moves use the Stats.json chance as-is.
    //
    // Raise a cap to allow bosses to apply that effect more often (harder).
    // Lower a cap to make boss effects more rare (easier).
    // -----------------------------------------------------------------------
    private static final double BOSS_DOT_CHANCE_CAP     = 0.40; // boss DOT (burn) max 40%
    private static final double BOSS_STUN_CHANCE_CAP    = 0.35; // boss stun max 35%
    private static final double BOSS_HALFDMG_CHANCE_CAP = 0.40; // boss halfDmg max 40%


    // Constructor: receives the two fighters. Called by CombatController.startCombat().
    public CombatEngine(Player player, BossLoader boss) {
        this.player = player;
        this.boss   = boss;
    }


    // -----------------------------------------------------------------------
    // processTurnByIndex(int moveIndex, String itemId)
    //
    // This is the most important method in the system. One call = one full round.
    // 'moveIndex' is which ability button the player pressed (0=move1, 1=move2, 2=move3, 3=move4).
    // 'itemId' is the ID of the item to use (only relevant when moveIndex is -1).
    //
    // Full order of operations every turn:
    //   1. Guard that combat is still ongoing
    //   2. Increment round number, clear per-turn flags
    //   3. Tick player DOT (if active)
    //   4. Count down cooldowns
    //   5. Player acts (stunned skip / item use / move)
    //   6. Reset talk modifier
    //   7. Check if boss is dead -> grant rewards, return log
    //   8. Tick boss DOT (if active)
    //   9. Update boss AI awareness of player HP
    //  10. Boss acts (stunned skip / clone / heal / damage)
    //  11. Reset guard and counter flags
    //  12. Check if player is dead -> set PLAYER_LOSE
    //  13. Package TurnLog and return
    // -----------------------------------------------------------------------
    public TurnLog processTurnByIndex(int moveIndex, String itemId) {

        // Guard: if combat is already over, refuse to process another turn.
        if (result != CombatResult.ONGOING)
            throw new IllegalStateException("Combat is over: " + result);

        roundNumber++;           // count this as a new round
        lastBossHitList.clear(); // clear previous boss hit list
        talkUsedThisTurn  = false;
        guardUsedThisTurn = false;

        // Local variables to collect data for the TurnLog at the end.
        String playerMoveName    = null;
        int    playerDamageDealt = 0;
        String itemUsedName      = null;
        int    playerHpRestored  = 0;
        String bossMoveName      = null;
        int    bossDamageDealt   = 0;
        int    playerDotDamage   = 0;
        int    bossDotDamage     = 0;


        // --- STEP 3: Player DOT tick ---
        // If the player has a DOT (burn) effect active, deal its damage now at the start of the turn.
        // Then tick the effect (count it down by 1). If it expires, tickEffect() clears it.
        if (player.getActiveEffect() != null &&
                player.getActiveEffect().getType() == StatusEffect.Type.DOT) {
            playerDotDamage = player.getActiveEffect().getDotDamage(); // flat 12 damage (from StatusEffect)
            player.takeDamage(playerDotDamage); // deal DOT damage, goes through defense
            player.tickEffect();                // count down the DOT duration
        }


        // --- STEP 4: Count down cooldowns ---
        // Each cooldown decreases by 1 per turn. When it reaches 0, the ability/guard is usable again.
        if (guardCooldownLeft > 0) guardCooldownLeft--;
        if (move4CooldownLeft > 0) move4CooldownLeft--;


        // --- STEP 5: Player's action ---

        // Case A: player is stunned (SKIP effect active) - lose the turn
        boolean playerStunned = player.isStunned();
        if (playerStunned) {
            playerMoveName = "STUNNED"; // label used in TurnLog
            player.tickEffect();        // count down stun duration

            // Case B: player chose to use an item (moveIndex is -1, itemId is set)
        } else if (moveIndex == -1 && itemId != null) {
            Item item = findItem(itemId);
            if (item == null || !item.isAvailable()) {
                itemUsedName = itemId + " (unavailable)"; // item not found or out of uses
            } else {
                itemUsedName     = item.getName();
                playerHpRestored = player.useItem(itemId); // use item, returns HP restored
            }

            // Case C: player used a move (normal attack)
        } else {
            Move move = getMoveByIndex(moveIndex); // get the Move object for this index
            playerMoveName = move.getName();

            // Move 4 (index 3) has a cooldown. Set it when used.
            // +1 accounts for the current turn (cooldown starts counting next turn).
            if (moveIndex == 3) move4CooldownLeft = move.getCooldown() + 1;

            // DAMAGE CALCULATION:
            // raw = base move damage + player's ATK stat
            // Example: Overload Burst damage=130, player ATK=25 -> raw=155
            int raw = move.getDamage() + player.getAttack();

            // If player has halfDmg effect (from a boss move), cut damage in half this turn.
            // Then tick the effect to count it down.
            if (player.isHalfDmg()) { raw /= 2; player.tickEffect(); }

            // Split total damage evenly across the number of hits.
            // Each hit goes through boss.takeDamage() which applies boss defense.
            int perHit = raw / move.getHits();
            for (int h = 0; h < move.getHits(); h++) {
                playerDamageDealt += boss.takeDamage(perHit); // accumulate total damage dealt
            }

            // Try to apply the move's status effect to the boss.
            // 'false' = this is not a boss move, so no cap is applied to the chance.
            tryApplyEffect(move, boss, false);
        }

        // Reset talkModifier at end of player phase. If talk/insult was used this turn,
        // its effect applies to the boss action below, then is cleared for next turn.
        talkModifier = 1.0;


        // --- STEP 7: Check if boss is dead ---
        // If the boss was killed by the player's move, grant rewards and end the fight.
        // The boss does NOT get to act this turn.
        if (boss.isDefeated()) {
            result = CombatResult.PLAYER_WIN;
            grantRewards(); // add XP and gold to PlayerProgress singleton
            return finalizeTurn(playerMoveName, playerDamageDealt, itemUsedName,
                    playerHpRestored, null, 0, playerDotDamage, 0);
        }


        // --- STEP 8: Boss DOT tick ---
        // Same as player DOT but applied to the boss.
        if (boss.getActiveEffect() != null &&
                boss.getActiveEffect().getType() == StatusEffect.Type.DOT) {
            bossDotDamage = boss.getActiveEffect().getDotDamage();
            boss.takeDamage(bossDotDamage);
            boss.tickEffect();
        }


        // --- STEP 9: Update boss AI ---
        // Tell the boss what percentage HP the player is at right now.
        // The boss AI methods read this to decide which phase of their strategy to use.
        boss.setLastKnownPlayerHpPercent(player.getHpPercent());


        // --- STEP 10: Boss acts ---

        // Case A: boss is stunned - lose their turn
        if (boss.isStunned()) {
            bossMoveName = "STUNNED";
            boss.tickEffect(); // count down stun duration

            // Case B: boss picks and executes a move
        } else {
            Move bossMove        = boss.chooseMove();          // AI picks the move
            bossMoveName         = bossMove.getName();
            lastBossMoveHitStyle = bossMove.getHitStyle();     // store for controller animation
            boss.setCurrentAbilitySprite(bossMove.getAbilitySprite()); // store attack image

            if ("clone".equals(bossMove.getHitStyle())) {
                // Clone move: restore HP proportionally, set isCloned flag
                boss.applyClone();

            } else if ("heal".equals(bossMove.getHitStyle())) {
                // Heal move: restore a flat 80 HP
                // To change the heal amount, change the number here.
                int healAmt = 80;
                boss.applyHeal(healAmt);

            } else {
                // Normal damage move:

                // BOSS DAMAGE CALCULATION:
                // raw = move's base damage + boss's ATK stat
                int raw = (bossMove.getDamage() + boss.getAttack());

                // If boss has halfDmg effect (from player's Armor Break), cut damage in half.
                if (boss.isHalfDmg()) { raw /= 2; boss.tickEffect(); }

                // Apply talkModifier (from activateTalk or activateInsult this turn).
                // 1.0 = no change, 0.5 = half damage, 2.0 = double damage.
                raw = (int)(raw * talkModifier);

                // Apply guard or counter reduction:
                if (guardActive) {
                    // Guard: 55% chance to completely block the attack (0 damage through).
                    // On fail: damage goes through normally.
                    // Regardless of result: guard enters cooldown for 3 turns.
                    if (rng.nextDouble() < 0.55) {
                        bossDamageDealt = 0; // blocked
                    } else {
                        bossDamageDealt = dealBossDamage(bossMove, raw); // not blocked
                    }
                    guardActive = false;
                    guardCooldownLeft = 3; // guard locked for 3 turns after use

                } else if (counterActive) {
                    // Counter: 30% chance to block (lower than guard but no cooldown).
                    if (rng.nextDouble() < 0.30) {
                        bossDamageDealt = 0; // blocked
                    } else {
                        bossDamageDealt = dealBossDamage(bossMove, raw);
                    }
                    counterActive = false;

                } else {
                    // No guard or counter: damage goes through in full.
                    bossDamageDealt = dealBossDamage(bossMove, raw);
                }

                // Try to apply the boss move's status effect to the player.
                // 'true' = this IS a boss move, so the chance is capped by BOSS_*_CHANCE_CAP.
                tryApplyEffect(bossMove, player, true);
            }
        }

        // Reset guard and counter flags at end of boss phase.
        // Even if guard was not triggered (boss was stunned), clear it.
        guardActive   = false;
        counterActive = false;


        // --- STEP 12: Check if player is dead ---
        if (player.isDefeated()) result = CombatResult.PLAYER_LOSE;


        // --- STEP 13: Package and return TurnLog ---
        return finalizeTurn(playerMoveName, playerDamageDealt, itemUsedName,
                playerHpRestored, bossMoveName, bossDamageDealt,
                playerDotDamage, bossDotDamage);
    }


    // -----------------------------------------------------------------------
    // dealBossDamage(Move bossMove, int totalRaw)
    // Handles splitting the boss's total damage across multiple hits.
    // Each hit is dealt through player.takeDamage() which applies player defense.
    // Each individual hit value is stored in lastBossHitList for the controller to read.
    // Math.max(1, ...) ensures each hit deals at least 1 damage even if defense is very high.
    // -----------------------------------------------------------------------
    private int dealBossDamage(Move bossMove, int totalRaw) {
        int total  = 0;
        int perHit = Math.max(1, totalRaw / bossMove.getHits()); // divide damage by hit count
        for (int h = 0; h < bossMove.getHits(); h++) {
            int hit = player.takeDamage(perHit); // apply defense, deal damage
            lastBossHitList.add(hit);            // store this hit for the controller
            total += hit;                        // accumulate total
        }
        return total; // total actual damage dealt after all hits and defense
    }


    // -----------------------------------------------------------------------
    // activateGuard()
    // Called by CombatController when the player presses the Guard button.
    // Sets guardActive=true so the boss damage phase uses the 55% block chance.
    // Does nothing if guard was already used this turn or is still on cooldown.
    // guardUsedThisTurn prevents stacking guard and counter in the same turn.
    // -----------------------------------------------------------------------
    public void activateGuard() {
        if (guardUsedThisTurn || guardCooldownLeft > 0) return; // blocked: already used or on cooldown
        guardUsedThisTurn = true;
        guardActive = true;
    }


    // -----------------------------------------------------------------------
    // activateCounter()
    // Called by CombatController when the player uses counter (if implemented in UI).
    // Counter has a 30% block chance and no cooldown (less effective but always available).
    // -----------------------------------------------------------------------
    public void activateCounter() {
        if (guardUsedThisTurn) return; // cannot counter if guard was already activated
        guardUsedThisTurn = true;
        counterActive = true;
    }


    // -----------------------------------------------------------------------
    // activateTalk()
    // Called by CombatController when the player presses the Talk button.
    // 50% chance to calm the boss (halve incoming damage this turn).
    // 50% chance to annoy the boss (increase incoming damage by 20% this turn).
    // talkModifier is applied to boss raw damage in the boss action phase.
    // Returns a string describing what happened, shown in the turn log.
    // -----------------------------------------------------------------------
    public String activateTalk() {
        if (talkUsedThisTurn) return "You already tried talking this turn.";
        talkUsedThisTurn = true;
        if (rng.nextDouble() < 0.5) {         // 50% success chance
            talkModifier = 0.5;               // boss deals half damage this turn
            return "You talked to " + boss.getName() + " and calmed them. Half damage this turn.";
        } else {
            talkModifier = 1.2;               // boss deals 20% more damage this turn
            return "Talking failed. " + boss.getName() + " got annoyed. Damage +20% incoming.";
        }
    }


    // -----------------------------------------------------------------------
    // activateInsult()
    // Higher risk/reward version of talk.
    // 35% chance: boss gets sad, only 30% damage comes through.
    // 65% chance: boss gets furious, damage doubles.
    // -----------------------------------------------------------------------
    public String activateInsult() {
        if (talkUsedThisTurn) return "You already used talk this turn.";
        talkUsedThisTurn = true;
        if (rng.nextDouble() < 0.35) {        // 35% success chance (lower than talk)
            talkModifier = 0.3;               // boss deals only 30% damage this turn
            return "You insulted " + boss.getName() + " and they got sad. Only 30% damage incoming.";
        } else {
            talkModifier = 2.0;               // boss deals double damage this turn
            return "Bad idea. " + boss.getName() + " is furious. Damage doubled.";
        }
    }


    // -----------------------------------------------------------------------
    // tryApplyEffect(Move move, Object target, boolean isBossMove)
    // Attempts to apply a status effect from a move to a target (player or boss).
    //
    // Step 1: if the move has no status effect, do nothing and return.
    // Step 2: convert the string effect name ("DOT", "skip", "halfDmg") to the enum type.
    // Step 3: read the chance from the move (0.0 to 1.0).
    // Step 4: if isBossMove=true, cap the chance using the BOSS_*_CHANCE_CAP constants.
    //         Math.min(chance, cap) keeps it at or below the cap.
    //         Example: move chance=0.7, cap=0.35 -> actual chance used = 0.35
    //         Example: move chance=0.2, cap=0.35 -> actual chance used = 0.20 (already below cap)
    // Step 5: roll rng.nextDouble() (random 0.0 to 1.0). If roll > chance, effect does NOT apply.
    //         This means: if chance=0.35, the effect procs 35% of the time.
    // Step 6: if the roll passes, create a StatusEffect and apply it to the target.
    // -----------------------------------------------------------------------
    private void tryApplyEffect(Move move, Object target, boolean isBossMove) {
        if (move.getStatusEffect() == null) return; // this move has no status effect

        // Convert string to enum type
        StatusEffect.Type type;
        switch (move.getStatusEffect()) {
            case "DOT":     type = StatusEffect.Type.DOT;      break; // damage over time
            case "skip":    type = StatusEffect.Type.SKIP;     break; // stun (skip turn)
            case "halfDmg": type = StatusEffect.Type.HALF_DMG; break; // halve damage output
            default: return; // unknown effect string, do nothing
        }

        double chance = move.getChance(); // read chance from the Move (set from Stats.json)

        if (isBossMove) {
            // Cap the chance so boss effects are never overwhelming.
            // Math.min returns whichever is smaller: the move's chance or the cap.
            switch (type) {
                case DOT:      chance = Math.min(chance, BOSS_DOT_CHANCE_CAP);     break;
                case SKIP:     chance = Math.min(chance, BOSS_STUN_CHANCE_CAP);    break;
                case HALF_DMG: chance = Math.min(chance, BOSS_HALFDMG_CHANCE_CAP); break;
            }
        }

        // Roll: rng.nextDouble() gives a random number between 0.0 and 1.0.
        // If the random number is greater than chance, the effect does not proc. Return early.
        // If it is less than or equal to chance, the effect procs.
        if (rng.nextDouble() > chance) return;

        // Create the effect with its type and duration (from the move).
        StatusEffect effect = new StatusEffect(type, move.getDuration());

        // Apply to the correct target type.
        // 'instanceof' checks if the object is of that class at runtime.
        if (target instanceof Player)     ((Player) target).applyEffect(effect);
        if (target instanceof BossLoader) ((BossLoader) target).applyEffect(effect);
    }

    // Backward-compatible overload: called without isBossMove flag (defaults to false = player move).
    // Kept so any existing call sites that do not pass the flag still work.
    private void tryApplyEffect(Move move, Object target) {
        tryApplyEffect(move, target, false);
    }


    // -----------------------------------------------------------------------
    // grantRewards()
    // Called automatically when the boss is defeated, before returning the TurnLog.
    // Reads XP and gold from the defeated enemy via BossLoader.getXPReward() / getGoldReward().
    // Those methods route to RewardTable which holds all the actual numbers.
    // Passes the values to the PlayerProgress singleton which handles level-up logic.
    // Console output for now - wire into UI in CombatController when ready.
    // -----------------------------------------------------------------------
    private void grantRewards() {
        int xp   = boss.getXPReward();   // get XP from RewardTable (via BossLoader)
        int gold = boss.getGoldReward(); // get gold from RewardTable (via BossLoader)
        PlayerProgress progress = PlayerProgress.getInstance(); // get the shared singleton
        boolean leveledUp = progress.addXP(xp);   // add XP, returns true if level-up happened
        progress.addGold(gold);                    // add gold (no level-up logic needed)
        System.out.println("Rewards: +" + xp + " XP, +" + gold + " Gold");
        System.out.println("Progress: " + progress);
        if (leveledUp) {
            System.out.println("LEVEL UP! Now level " + progress.getLevel());
        }
    }


    // -----------------------------------------------------------------------
    // getMoveByIndex(int index)
    // Gets the player's Move at a given list position.
    // index 0 = first ability, 1 = second, 2 = third, 3 = fourth.
    // Throws a clear error if the index is out of range rather than crashing silently.
    // -----------------------------------------------------------------------
    private Move getMoveByIndex(int index) {
        List<Move> moves = player.getMoves();
        if (moves == null || moves.size() <= index)
            throw new IllegalStateException("No move at index " + index);
        return moves.get(index);
    }


    // -----------------------------------------------------------------------
    // findItem(String itemId)
    // Searches the player's item list for an item matching the given ID.
    // Returns null if not found. The engine checks isAvailable() before using it.
    // -----------------------------------------------------------------------
    private Item findItem(String itemId) {
        if (player.getItems() == null) return null;
        return player.getItems().stream()
                .filter(i -> i.getId().equals(itemId)) // match by ID string
                .findFirst()                           // return first match
                .orElse(null);                         // return null if none found
    }


    // -----------------------------------------------------------------------
    // finalizeTurn(...)
    // Packages everything that happened this turn into a TurnLog object.
    // Adds it to the history list, then returns it to CombatController.
    // The TurnLog is immutable (all fields final) - it is a read-only record.
    // -----------------------------------------------------------------------
    private TurnLog finalizeTurn(String playerMoveName, int playerDamageDealt,
                                 String itemUsed, int playerHpRestored,
                                 String bossMoveName, int bossDamageDealt,
                                 int playerDotDamage, int bossDotDamage) {
        TurnLog log = new TurnLog(roundNumber, PlayerAction.MOVE_1,
                playerMoveName, playerDamageDealt,
                itemUsed, playerHpRestored,
                bossMoveName, bossDamageDealt,
                player.getCurrentHp(), boss.getCurrentHp(), result);
        history.add(log); // add to history
        return log;       // return to CombatController
    }


    // --- GETTERS ---
    // Read by CombatController to update the UI, check cooldowns, decide animations, etc.

    // true if guard can be used right now (cooldown is at 0)
    public boolean isGuardAvailable()        { return guardCooldownLeft <= 0; }

    // true if Overload Burst (move 4) can be used right now
    public boolean isMove4Available()        { return move4CooldownLeft <= 0; }

    // list of individual hit damage values from the boss's last move (for floating number display)
    public List<Integer> getLastBossHitList(){ return lastBossHitList; }

    // "single", "rapid", "heal", or "clone" from the boss's last move (for animation selection)
    public String getLastBossMoveHitStyle()  { return lastBossMoveHitStyle; }

    // current fight outcome: ONGOING, PLAYER_WIN, or PLAYER_LOSE
    public CombatResult getResult()          { return result; }

    // which turn number we are on
    public int getRoundNumber()              { return roundNumber; }

    // the player object (so controller can read HP, effects, etc.)
    public Player getPlayer()               { return player; }

    // the boss object (so controller can read HP, sprite, effects, etc.)
    public BossLoader getBoss()             { return boss; }

    // full list of every TurnLog from this fight
    public List<TurnLog> getHistory()       { return history; }

    // shortcut: true if the fight is still going
    public boolean isOngoing()              { return result == CombatResult.ONGOING; }
}
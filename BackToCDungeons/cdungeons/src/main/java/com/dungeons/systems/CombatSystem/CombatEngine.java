package com.dungeons.systems.CombatSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CombatEngine {

    private final Player player;
    private final BossLoader boss;
    private final Random rng = new Random();

    private int roundNumber = 0;
    private CombatResult result = CombatResult.ONGOING;
    private final List<TurnLog> history = new ArrayList<>();

    private boolean guardActive       = false;
    private boolean counterActive     = false;
    private double  talkModifier      = 1.0;
    private boolean talkUsedThisTurn  = false;
    private boolean guardUsedThisTurn = false;

    private int guardCooldownLeft = 0;
    private int move4CooldownLeft = 0;

    private List<Integer> lastBossHitList = new ArrayList<>();
    private String lastBossMoveHitStyle   = "single";

    public CombatEngine(Player player, BossLoader boss) {
        this.player = player;
        this.boss   = boss;
    }

    public TurnLog processTurnByIndex(int moveIndex, String itemId) {
        if (result != CombatResult.ONGOING)
            throw new IllegalStateException("Combat is over: " + result);

        roundNumber++;
        lastBossHitList.clear();
        talkUsedThisTurn  = false;
        guardUsedThisTurn = false;

        String playerMoveName    = null;
        int    playerDamageDealt = 0;
        String itemUsedName      = null;
        int    playerHpRestored  = 0;
        String bossMoveName      = null;
        int    bossDamageDealt   = 0;
        int    playerDotDamage   = 0;
        int    bossDotDamage     = 0;

        // DOT tick on player
        if (player.getActiveEffect() != null &&
            player.getActiveEffect().getType() == StatusEffect.Type.DOT) {
            playerDotDamage = player.getActiveEffect().getDotDamage();
            player.takeDamage(playerDotDamage);
            player.tickEffect();
        }

        if (guardCooldownLeft > 0) guardCooldownLeft--;
        if (move4CooldownLeft > 0) move4CooldownLeft--;

        // player turn
        boolean playerStunned = player.isStunned();
        if (playerStunned) {
            playerMoveName = "STUNNED";
            player.tickEffect();
        } else if (moveIndex == -1 && itemId != null) {
            Item item = findItem(itemId);
            if (item == null || !item.isAvailable()) {
                itemUsedName = itemId + " (unavailable)";
            } else {
                itemUsedName     = item.getName();
                playerHpRestored = player.useItem(itemId);
            }
        } else {
            Move move = getMoveByIndex(moveIndex);
            playerMoveName = move.getName();

            if (moveIndex == 3) move4CooldownLeft = move.getCooldown() + 1;

            int raw = move.getDamage() + player.getAttack();
            if (player.isHalfDmg()) { raw /= 2; player.tickEffect(); }

            int perHit = raw / move.getHits();
            for (int h = 0; h < move.getHits(); h++) {
                playerDamageDealt += boss.takeDamage(perHit);
            }
            tryApplyEffect(move, boss);
        }

        talkModifier = 1.0;

        if (boss.isDefeated()) {
            result = CombatResult.PLAYER_WIN;
            return finalizeTurn(playerMoveName, playerDamageDealt, itemUsedName,
                    playerHpRestored, null, 0, playerDotDamage, 0);
        }

        // DOT tick on boss
        if (boss.getActiveEffect() != null &&
            boss.getActiveEffect().getType() == StatusEffect.Type.DOT) {
            bossDotDamage = boss.getActiveEffect().getDotDamage();
            boss.takeDamage(bossDotDamage);
            boss.tickEffect();
        }

        boss.setLastKnownPlayerHpPercent(player.getHpPercent());

        if (boss.isStunned()) {
            bossMoveName = "STUNNED";
            boss.tickEffect();
        } else {
            Move bossMove    = boss.chooseMove();
            bossMoveName     = bossMove.getName();
            lastBossMoveHitStyle = bossMove.getHitStyle();
            boss.setCurrentAbilitySprite(bossMove.getAbilitySprite());

            if ("clone".equals(bossMove.getHitStyle())) {
                boss.applyClone();
            } else if ("heal".equals(bossMove.getHitStyle())) {
                int healAmt = 80;
                boss.applyHeal(healAmt);
            } else {
                int raw = (bossMove.getDamage() + boss.getAttack());
                if (boss.isHalfDmg()) { raw /= 2; boss.tickEffect(); }

                double mod = talkModifier;
                raw = (int)(raw * mod);

                if (guardActive) {
                    if (rng.nextDouble() < 0.55) {
                        bossDamageDealt = 0;
                    } else {
                        bossDamageDealt = dealBossDamage(bossMove, raw);
                    }
                    guardActive = false;
                    guardCooldownLeft = 3;
                } else if (counterActive) {
                    if (rng.nextDouble() < 0.30) {
                        bossDamageDealt = 0;
                    } else {
                        bossDamageDealt = dealBossDamage(bossMove, raw);
                    }
                    counterActive = false;
                } else {
                    bossDamageDealt = dealBossDamage(bossMove, raw);
                }

                tryApplyEffect(bossMove, player);
            }
        }

        guardActive   = false;
        counterActive = false;

        if (player.isDefeated()) result = CombatResult.PLAYER_LOSE;

        return finalizeTurn(playerMoveName, playerDamageDealt, itemUsedName,
                playerHpRestored, bossMoveName, bossDamageDealt,
                playerDotDamage, bossDotDamage);
    }

    private int dealBossDamage(Move bossMove, int totalRaw) {
        int total  = 0;
        int perHit = Math.max(1, totalRaw / bossMove.getHits());
        for (int h = 0; h < bossMove.getHits(); h++) {
            int hit = player.takeDamage(perHit);
            lastBossHitList.add(hit);
            total += hit;
        }
        return total;
    }

    public void activateGuard() {
        if (guardUsedThisTurn || guardCooldownLeft > 0) return;
        guardUsedThisTurn = true;
        guardActive = true;
    }

    public void activateCounter() {
        if (guardUsedThisTurn) return;
        guardUsedThisTurn = true;
        counterActive = true;
    }

    public String activateTalk() {
        if (talkUsedThisTurn) return "You already tried talking this turn.";
        talkUsedThisTurn = true;
        if (rng.nextDouble() < 0.5) {
            talkModifier = 0.5;
            return "You talked to " + boss.getName() + " and calmed them. Half damage this turn.";
        } else {
            talkModifier = 1.2;
            return "Talking failed. " + boss.getName() + " got annoyed. Damage +20% incoming.";
        }
    }

    public String activateInsult() {
        if (talkUsedThisTurn) return "You already used talk this turn.";
        talkUsedThisTurn = true;
        if (rng.nextDouble() < 0.35) {
            talkModifier = 0.3;
            return "You insulted " + boss.getName() + " and they got sad. Only 30% damage incoming.";
        } else {
            talkModifier = 2.0;
            return "Bad idea. " + boss.getName() + " is furious. Damage doubled.";
        }
    }

    private void tryApplyEffect(Move move, Object target) {
        if (move.getStatusEffect() == null) return;
        if (rng.nextDouble() > move.getChance()) return;
        StatusEffect.Type type;
        switch (move.getStatusEffect()) {
            case "DOT":     type = StatusEffect.Type.DOT;      break;
            case "skip":    type = StatusEffect.Type.SKIP;     break;
            case "halfDmg": type = StatusEffect.Type.HALF_DMG; break;
            default: return;
        }
        StatusEffect effect = new StatusEffect(type, move.getDuration());
        if (target instanceof Player)     ((Player) target).applyEffect(effect);
        if (target instanceof BossLoader) ((BossLoader) target).applyEffect(effect);
    }

    private Move getMoveByIndex(int index) {
        List<Move> moves = player.getMoves();
        if (moves == null || moves.size() <= index)
            throw new IllegalStateException("No move at index " + index);
        return moves.get(index);
    }

    private Item findItem(String itemId) {
        if (player.getItems() == null) return null;
        return player.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst().orElse(null);
    }

    private TurnLog finalizeTurn(String playerMoveName, int playerDamageDealt,
                                  String itemUsed, int playerHpRestored,
                                  String bossMoveName, int bossDamageDealt,
                                  int playerDotDamage, int bossDotDamage) {
        TurnLog log = new TurnLog(roundNumber, PlayerAction.MOVE_1,
                playerMoveName, playerDamageDealt,
                itemUsed, playerHpRestored,
                bossMoveName, bossDamageDealt,
                player.getCurrentHp(), boss.getCurrentHp(), result);
        history.add(log);
        return log;
    }

    public boolean isGuardAvailable()       { return guardCooldownLeft <= 0; }
    public boolean isMove4Available()        { return move4CooldownLeft <= 0; }
    public List<Integer> getLastBossHitList(){ return lastBossHitList; }
    public String getLastBossMoveHitStyle()  { return lastBossMoveHitStyle; }
    public CombatResult getResult()          { return result; }
    public int getRoundNumber()              { return roundNumber; }
    public Player getPlayer()               { return player; }
    public BossLoader getBoss()             { return boss; }
    public List<TurnLog> getHistory()       { return history; }
    public boolean isOngoing()              { return result == CombatResult.ONGOING; }
}
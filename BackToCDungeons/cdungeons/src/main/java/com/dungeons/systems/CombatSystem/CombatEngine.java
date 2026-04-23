// Logjika e combatit. 
//* Bej balancim dhe rregullime ketu. Nuk funksionon per iden qe kemi, this is just a base */
package com.dungeons.systems.CombatSystem;

import java.util.ArrayList;
import java.util.List;


public class CombatEngine {

    private final Player player;
    private final BossLoader bossLoader;

    private int roundNumber = 0;
    private CombatResult result = CombatResult.ONGOING;

    private final List<TurnLog> history = new ArrayList<>();

   
    public CombatEngine(Player player, BossLoader bossLoader) {
        this.player = player;
        this.bossLoader = bossLoader;
    }

    /**
     * Processes one full round: player acts, then boss acts (if still alive).
     *
     * @param action  the action the player chose this turn
     * @param itemId  
     * @return a TurnLog describing everything that happened this round
     * @throws IllegalStateException if called after combat has already ended
     */
    public TurnLog processTurn(PlayerAction action, String itemId) {
        if (result != CombatResult.ONGOING) {
            throw new IllegalStateException("Combat is already over: " + result);
        }

        roundNumber++;

        
        String  playerMoveName    = null;
        int     playerDamageDealt = 0;
        String  itemUsedName      = null;
        int     playerHpRestored  = 0;

        String  bossMoveName      = null;
        int     bossDamageDealt   = 0;


        switch (action) {

            case MOVE_1: {
                Move move = getMoveByIndex(0);
                playerMoveName    = move.getName();
                playerDamageDealt = executeAttack(player, bossLoader, move);
                break;
            }

            case MOVE_2: {
                Move move = getMoveByIndex(1);
                playerMoveName    = move.getName();
                playerDamageDealt = executeAttack(player, bossLoader, move);
                break;
            }

            case UTILITY: {
                //WIP
                playerMoveName = "Utility (no effect)";
                break;
            }

            case ITEM: {
                if (itemId == null) {
                    throw new IllegalArgumentException("itemId must be provided when action is ITEM.");
                }
                Item item = findItem(itemId);
                if (item == null || !item.isAvailable()) {
                    // Treat as a wasted turn — item not found or exhausted
                    itemUsedName     = itemId + " (unavailable)";
                    playerHpRestored = 0;
                } else {
                    itemUsedName     = item.getName();
                    playerHpRestored = player.useItem(itemId);
                }
                break;
            }
        }

       //Win conditions
        if (bossLoader.isDefeated()) {
            result = CombatResult.PLAYER_WIN;
            TurnLog log = buildLog(roundNumber, action, playerMoveName, playerDamageDealt,
                    itemUsedName, playerHpRestored,
                    null, 0,
                    player.getCurrentHp(), bossLoader.getCurrentHp(), result);
            history.add(log);
            return log;
        }

       //Bos turn
        Move bossMove     = bossLoader.chooseMove();
        bossMoveName      = bossMove.getName();
        bossDamageDealt   = executeAttack(bossLoader, player, bossMove);

        //Win conditions
        if (player.isDefeated()) {
            result = CombatResult.PLAYER_LOSE;
        }

        //Logohet roundi
        TurnLog log = buildLog(roundNumber, action, playerMoveName, playerDamageDealt,
                itemUsedName, playerHpRestored,
                bossMoveName, bossDamageDealt,
                player.getCurrentHp(), bossLoader.getCurrentHp(), result);
        history.add(log);
        return log;
    }

    // The damage taken 

    /**
     * @return actual HP removed from defender
     */
    private int executeAttack(Combatant attacker, Combatant defender, Move move) {
        return defender.takeDamage(move.getDamage());
    }

 
    /**
     * Retrieves the player's move by list index (0 = move1, 1 = move2).
     */
    private Move getMoveByIndex(int index) {
        List<Move> moves = player.getMoves();
        if (moves == null || moves.size() <= index) {
            throw new IllegalStateException(
                "Player does not have a move at index " + index + ". Check stats.json.");
        }
        return moves.get(index);
    }

    /**
     * Finds an item in the player's inventory by id.
     * Returns null if not found.
     */
    private Item findItem(String itemId) {
        if (player.getItems() == null) return null;
        return player.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    private TurnLog buildLog(int round, PlayerAction action,
                              String playerMoveName, int playerDamageDealt,
                              String itemUsed, int playerHpRestored,
                              String bossMoveName, int bossDamageDealt,
                              int playerHpAfter, int bossHpAfter,
                              CombatResult result) {
        return new TurnLog(round, action, playerMoveName, playerDamageDealt,
                itemUsed, playerHpRestored,
                bossMoveName, bossDamageDealt,
                playerHpAfter, bossHpAfter, result);
    }

    public CombatResult getResult()   { return result; }
    public int getRoundNumber()        { return roundNumber; }
    public Player getPlayer()          { return player; }
    public BossLoader getBoss()              { return bossLoader; }
    public List<TurnLog> getHistory()  { return history; }
    public boolean isOngoing()         { return result == CombatResult.ONGOING; }
}

//Statusi per raund per cdo metod, attack etj.

package com.dungeons.systems.CombatSystem;


public class TurnLog {

    private final int roundNumber;

    //Players turn
    private final PlayerAction playerAction;
    private final String playerMoveName;    // null if action was ITEM
    private final int playerDamageDealt;    // 0 if action was ITEM
    private final String itemUsed;          // null if action was not ITEM
    private final int playerHpRestored;     // 0 if action was not ITEM

    //Boss turn
    private final String bossMoveName;
    private final int bossDamageDealt;

    //Log after this round
    private final int playerHpAfter;
    private final int bossHpAfter;
    private final CombatResult resultAfterRound;

    public TurnLog(
            int roundNumber,
            PlayerAction playerAction,
            String playerMoveName,
            int playerDamageDealt,
            String itemUsed,
            int playerHpRestored,
            String bossMoveName,
            int bossDamageDealt,
            int playerHpAfter,
            int bossHpAfter,
            CombatResult resultAfterRound) {

        this.roundNumber = roundNumber;
        this.playerAction = playerAction;
        this.playerMoveName = playerMoveName;
        this.playerDamageDealt = playerDamageDealt;
        this.itemUsed = itemUsed;
        this.playerHpRestored = playerHpRestored;
        this.bossMoveName = bossMoveName;
        this.bossDamageDealt = bossDamageDealt;
        this.playerHpAfter = playerHpAfter;
        this.bossHpAfter = bossHpAfter;
        this.resultAfterRound = resultAfterRound;
    }

   
    public int getRoundNumber() { return roundNumber; }
    public PlayerAction getPlayerAction() { return playerAction; }
    public String getPlayerMoveName() { return playerMoveName; }
    public int getPlayerDamageDealt() { return playerDamageDealt; }
    public String getItemUsed() { return itemUsed; }
    public int getPlayerHpRestored() { return playerHpRestored; }
    public String getBossMoveName() { return bossMoveName; }
    public int getBossDamageDealt() { return bossDamageDealt; }
    public int getPlayerHpAfter() { return playerHpAfter; }
    public int getBossHpAfter() { return bossHpAfter; }
    public CombatResult getResultAfterRound() { return resultAfterRound; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== Round %d ===\n", roundNumber));

        // Player action line
        switch (playerAction) {
            case MOVE_1:
            case MOVE_2:
                sb.append(String.format("  PLAYER used [%s] → dealt %d damage to boss\n",
                        playerMoveName, playerDamageDealt));
                break;
            case UTILITY:
                sb.append("  PLAYER used UTILITY (no effect yet)\n");
                break;
            case ITEM:
                sb.append(String.format("  PLAYER used [%s] → restored %d HP\n",
                        itemUsed, playerHpRestored));
                break;
        }

        // Boss always attacks (unless already defeated)
        if (resultAfterRound != CombatResult.PLAYER_WIN || bossDamageDealt > 0) {
            sb.append(String.format("  BOSS used [%s] → dealt %d damage to player\n",
                    bossMoveName != null ? bossMoveName : "—",
                    bossDamageDealt));
        } else {
            sb.append("  BOSS was defeated before it could act.\n");
        }

        sb.append(String.format("  → Player HP: %d | Boss HP: %d | Result: %s\n",
                playerHpAfter, bossHpAfter, resultAfterRound));

        return sb.toString();
    }
}

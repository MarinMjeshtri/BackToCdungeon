//Testimi per balanca etj

package com.dungeons.systems.CombatSystem; 


public class CombatEngineTest {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("  DUNGEON COMBAT — TEST");
        System.out.println("====================================\n");

        StatsLoader loader = new StatsLoader();

        // Change these names to test different matchups.
        // Must match exactly as written in stats.json.
        Player player = loader.loadPlayer("FrekiRelah");
        Boss   boss   = loader.loadBoss("CassieYarn");

        CombatEngine engine = new CombatEngine(player, boss);

        System.out.println("FIGHT: " + player.getName() + " vs " + boss.getName());
        System.out.printf("  %s — HP: %d | ATK: %d | DEF: %d%n",
                player.getName(), player.getMaxHp(), player.getAttack(), player.getDefense());
        System.out.printf("  %s — HP: %d | ATK: %d | DEF: %d%n",
                boss.getName(), boss.getMaxHp(), boss.getAttack(), boss.getDefense());
        System.out.println("--------------------------------------------\n");

        while (engine.isOngoing()) {
            PlayerAction action = (engine.getRoundNumber() % 2 == 0)
                    ? PlayerAction.MOVE_1
                    : PlayerAction.MOVE_2;

            TurnLog log = engine.processTurn(action, null);

            System.out.println("Round " + log.getRoundNumber());
            System.out.println("  Player used: " + log.getPlayerMoveName()
                    + " → " + log.getPlayerDamageDealt() + " damage dealt");

            if (log.getBossMoveName() != null) {
                System.out.println("  Boss used:   " + log.getBossMoveName()
                        + " → " + log.getBossDamageDealt() + " damage taken");
            } else {
                System.out.println("  Boss was defeated before acting!");
            }

            System.out.println("  " + player.getName() + " HP: " + log.getPlayerHpAfter()
                    + " | " + boss.getName() + " HP: " + log.getBossHpAfter());
            System.out.println();
        }

        System.out.println("============================================");
        if (engine.getResult() == CombatResult.PLAYER_WIN) {
            System.out.println("  RESULT: VICTORY — " + boss.getName() + " defeated!");
        } else {
            System.out.println("  RESULT: DEFEATED — " + player.getName() + " fell...");
        }
        System.out.println("  Total rounds: " + engine.getRoundNumber());
        System.out.println("============================================");
    }
}
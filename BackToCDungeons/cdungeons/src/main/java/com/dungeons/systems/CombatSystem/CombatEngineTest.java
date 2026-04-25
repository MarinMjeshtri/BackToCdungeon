package com.dungeons.systems.CombatSystem;

import com.dungeons.Controllers.CombatController;
import com.dungeons.screens.combatScreen;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CombatEngineTest extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        combatScreen screen = new combatScreen();
        CombatController control = screen.getLoader().getController();

        stage.setScene(new Scene(screen.getRoot(), 800, 600));
        stage.show(); // show BEFORE starting combat

        StatsLoader loader = new StatsLoader();
        Player player = loader.loadPlayer("FrekiRelah");
        BossLoader boss = loader.loadBoss("CassieYarn");

        // set initial UI state
        control.setStart(player.getName(), boss.getName(), boss.getMaxHp());

        CombatEngine engine = new CombatEngine(player, boss);

        // store max HP for percentage calculations
        int playerMaxHp = player.getMaxHp();
        int bossMaxHp   = boss.getMaxHp();

        // instead of while loop, use a Timeline that fires every 1 second
        // this lets JavaFX update the UI between each round
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {

            if (!engine.isOngoing()) {
                timeline.stop();

                // show result
                if (engine.getResult() == CombatResult.PLAYER_WIN) {
                    control.setTurnLog("VICTORY — " + boss.getName() + " defeated!");
                } else {
                    control.setTurnLog("DEFEATED — " + player.getName() + " fell...");
                }
                return;
            }

            // process one round
            PlayerAction action = (engine.getRoundNumber() % 2 == 0)
                    ? PlayerAction.MOVE_1
                    : PlayerAction.MOVE_2;

            TurnLog log = engine.processTurn(action, null);

            // update turn number
            control.setTurnNr(log.getRoundNumber());

            // update turn log text
            String logText = "Round " + log.getRoundNumber() + ": "
                    + player.getName() + " dealt " + log.getPlayerDamageDealt() + " dmg. ";
            if (log.getBossMoveName() != null) {
                logText += boss.getName() + " dealt " + log.getBossDamageDealt() + " dmg.";
            } else {
                logText += boss.getName() + " was defeated!";
            }
            control.setTurnLog(logText);

            // update HP bars as percentage of max HP
            control.updateBossHP(log.getBossHpAfter(), bossMaxHp);
            control.updatePlayerHP(log.getPlayerHpAfter(), playerMaxHp);
        }));

        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
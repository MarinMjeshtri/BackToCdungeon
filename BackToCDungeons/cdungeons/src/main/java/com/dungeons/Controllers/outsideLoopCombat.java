package com.dungeons.Controllers;

import com.dungeons.systems.CombatSystem.*;
import com.dungeons.screens.gameoverScreen;

import javafx.animation.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;


public class outsideLoopCombat {
    private Stage stage;

    gameoverScreen gameoverScreen;
    private CombatController combatController = new CombatController();
    private PauseTransition thinkingRevertTimer = null;
    private CombatEngine engine;
    private Player player;
    private BossLoader boss;


    private void beforeGameState(){

    }

    public void afterGameState(boolean playerWon) {
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            if (playerWon) {
                combatController.loadNextArea();
            } else {
                StackPane currentRoot = (StackPane) stage.getScene().getRoot();

                if (!currentRoot.getChildren().contains(gameoverScreen.getRoot())) {
                    currentRoot.getChildren().add(gameoverScreen.getRoot());
                }

                gameoverScreen.getRoot().setVisible(true);;
            }
        });
        delay.play();
    }
}

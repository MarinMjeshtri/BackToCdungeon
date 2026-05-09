package com.dungeons.Controllers;

import com.dungeons.systems.CombatSystem.*;

import javafx.fxml.FXML;
import com.dungeons.screens.GameScreen;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


public class uiOverlayController {
 @FXML Label totalHP;
 @FXML Label totalXP;
 @FXML Label currHP;
 @FXML Label currXP;
 @FXML Label totalGold;

@FXML Rectangle healthBar;
@FXML Rectangle xpBar;



    private PlayerProgress progress;

    public void setProgress(PlayerProgress progress) {
        this.progress = progress;
        // Update labels here instead of initialize()
        updateUI();
    }

    private void updateUI() {
        totalHP.setText(String.valueOf("TOT. HP "  + progress.getScaledHp()));
        currHP.setText(String.valueOf( "CURR. HP "  +progress.getCurrentHp()));
        totalXP.setText(String.valueOf("LEVEL "  +progress.getLevel()));
        currXP.setText(String.valueOf("CURR XP "  +progress.getXpToNextLevel()));
        totalGold.setText(String.valueOf(progress.getGold()));

        double ratio = (double) progress.getCurrentHp() / progress.getScaledHp();
        healthBar.setWidth(334 * ratio);

        double ratio1 = (double) progress.getXp() / progress.getXpToNextLevel();
        healthBar.setWidth(334 * ratio);
    }

}
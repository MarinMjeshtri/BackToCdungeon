package com.dungeons.Controllers;

import com.dungeons.shopItemsManager.PlayerInventory;
import com.dungeons.shopItemsManager.Shop;
import com.dungeons.systems.CombatSystem.*;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

@FXML Label slotItem1;
@FXML Label slotItem2;
@FXML Label slotItem3;
@FXML Label slotItem4;

@FXML ImageView slotImg1;
@FXML ImageView slotImg2;
@FXML ImageView slotImg3;
@FXML ImageView slotImg4;

    private PlayerProgress progress;

    public void setProgress(PlayerProgress progress) {
        this.progress = progress;
        // Update labels here instead of initialize()
        updateUI();
    }

    public void updateUI() {
        totalHP.setText(String.valueOf("TOT. HP "  + progress.getScaledHp()));
        currHP.setText(String.valueOf( "CURR. HP "  +progress.getCurrentHp()));
        totalXP.setText(String.valueOf("LEVEL "  +progress.getLevel()));
        currXP.setText(String.valueOf("CURR XP "  +progress.getXpToNextLevel()));
        totalGold.setText(String.valueOf(progress.getGold()));

        double ratio = (double) progress.getCurrentHp() / progress.getScaledHp();
        healthBar.setWidth(334 * ratio);

        double ratio1 = (double) progress.getXp() / progress.getXpToNextLevel();
        healthBar.setWidth(334 * ratio);

        PlayerInventory inventory = PlayerInventory.getInstance();
        Label[] slotLabels = {slotItem1, slotItem2, slotItem3, slotItem4};
        for (int i = 0; i < 4; i++) {
            Shop item = inventory.getSlot(i);
            slotLabels[i].setText(item != null ? item.displayName : "");
        }

        ImageView[] slotImages = {slotImg1, slotImg2, slotImg3, slotImg4};
        for (int i = 0; i < 4; i++) {
            Shop item = inventory.getSlot(i);
            if (item != null && item.image != null) {
                slotImages[i].setImage(new Image(getClass().getResourceAsStream(item.image)));
                slotLabels[i].setText("");  // optional: hide name if image shows
            } else {
                slotImages[i].setImage(null);
                slotLabels[i].setText("");
            }
        }
    }

}
package com.dungeons.Controllers;

import com.dungeons.screens.GameScreen;
import com.dungeons.shopItemsManager.PlayerInventory;
import com.dungeons.shopItemsManager.Shop;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.util.List;

public class BossRewardController {

    @FXML private StackPane rewardRoot;
    @FXML private ImageView itemImageOne;
    @FXML private ImageView itemImageTwo;
    @FXML private Label itemNameOne;
    @FXML private Label itemNameTwo;
    @FXML private Label itemDescOne;
    @FXML private Label itemDescTwo;
    @FXML private Label resultLabel;
    @FXML private Button claimButton;

    private List<Shop> rewards;
    private Runnable onClaim;
    private boolean claimed;

    public void setup(List<Shop> rewards, Runnable onClaim) {
        this.rewards = rewards;
        this.onClaim = onClaim;
        this.claimed = false;

        showItem(rewards.size() > 0 ? rewards.get(0) : null, itemImageOne, itemNameOne, itemDescOne);
        showItem(rewards.size() > 1 ? rewards.get(1) : null, itemImageTwo, itemNameTwo, itemDescTwo);
        resultLabel.setText("Two boss spoils are ready for your hotbar.");
        claimButton.setText("CLAIM");
    }

    private void showItem(Shop item, ImageView imageView, Label nameLabel, Label descLabel) {
        if (item == null) {
            nameLabel.setText("No item");
            descLabel.setText("The reward cache was empty.");
            imageView.setImage(null);
            return;
        }

        nameLabel.setText(item.displayName);
        descLabel.setText(item.desc);

        try (InputStream is = getClass().getResourceAsStream(item.image)) {
            if (is != null) {
                imageView.setImage(new Image(is));
            }
        } catch (Exception e) {
            imageView.setImage(null);
        }
    }

    @FXML
    private void claimRewards() {
        if (claimed) {
            closeAndContinue();
            return;
        }

        claimed = true;
        int added = 0;
        PlayerInventory inventory = PlayerInventory.getInstance();

        for (Shop reward : rewards) {
            if (inventory.addItem(reward)) {
                added++;
            }
        }

        GameScreen gameScreen = GameScreen.getInstance();
        if (gameScreen != null && gameScreen.getOverlayController() != null) {
            gameScreen.getOverlayController().updateUI();
        }

        if (added == rewards.size()) {
            closeAndContinue();
            return;
        }

        resultLabel.setText("Only " + added + " item(s) fit. Your hotbar is full.");
        claimButton.setText("CONTINUE");
    }

    private void closeAndContinue() {
        Parent parent = rewardRoot.getParent();
        if (parent instanceof Pane) {
            ((Pane) parent).getChildren().remove(rewardRoot);
        }

        if (onClaim != null) {
            onClaim.run();
        }
    }
}

package com.dungeons.Controllers;

import com.dungeons.screens.GameScreen;
import com.dungeons.shopItemsManager.PlayerInventory;
import com.dungeons.shopItemsManager.Shop;
import com.dungeons.shopItemsManager.ShopManager;
import com.dungeons.systems.CombatSystem.PlayerProgress;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UtilityRewardController {

    private static final int MAX_HP_BONUS_PERCENT = 15;
    private static final int ATTACK_DAMAGE_BONUS_PERCENT = 20;

    @FXML private Button healthChoice;
    @FXML private Button attackChoice;
    @FXML private Button itemsChoice;
    @FXML private ImageView itemImageOne;
    @FXML private ImageView itemImageTwo;

    private List<Shop> itemRewards = new ArrayList<>();
    private Runnable onChoice;
    private boolean chosen;

    public void initialize() {
        healthChoice.setText("MAX HP +" + MAX_HP_BONUS_PERCENT + "%");
        attackChoice.setText("ATTACK DAMAGE +" + ATTACK_DAMAGE_BONUS_PERCENT + "%");
        itemsChoice.setText("2 RANDOM ITEMS");
    }

    public void setup(Runnable onChoice) {
        this.onChoice = onChoice;
        this.itemRewards = generateRandomItems();
        showItemPreview(0, itemImageOne);
        showItemPreview(1, itemImageTwo);
    }

    @FXML
    private void chooseHealth() {
        if (chosen) return;
        chosen = true;
        PlayerProgress.getInstance().addMaxHpPercentBonus(MAX_HP_BONUS_PERCENT);
        refreshOverlay();
        close();
    }

    @FXML
    private void chooseAttack() {
        if (chosen) return;
        chosen = true;
        PlayerProgress.getInstance().addAttackDamagePercentBonus(ATTACK_DAMAGE_BONUS_PERCENT);
        refreshOverlay();
        close();
    }

    @FXML
    private void chooseItems() {
        if (chosen) return;
        chosen = true;
        PlayerInventory inventory = PlayerInventory.getInstance();
        for (Shop item : itemRewards) {
            inventory.addItem(item);
        }
        refreshOverlay();
        close();
    }

    private List<Shop> generateRandomItems() {
        ShopManager manager = new ShopManager();
        manager.load();

        List<Shop> items = new ArrayList<>(manager.getAllItems());
        Collections.shuffle(items);
        if (items.size() > 2) {
            return new ArrayList<>(items.subList(0, 2));
        }
        return items;
    }

    private void showItemPreview(int index, ImageView imageView) {
        if (itemRewards.size() <= index || itemRewards.get(index).image == null) {
            imageView.setImage(null);
            return;
        }

        try (InputStream is = getClass().getResourceAsStream(itemRewards.get(index).image)) {
            if (is != null) {
                imageView.setImage(new Image(is));
            }
        } catch (Exception e) {
            imageView.setImage(null);
        }
    }

    private void refreshOverlay() {
        GameScreen gameScreen = GameScreen.getInstance();
        if (gameScreen != null && gameScreen.getOverlayController() != null) {
            gameScreen.getOverlayController().updateUI();
        }
    }

    private void close() {
        if (onChoice != null) {
            onChoice.run();
        }
    }
}

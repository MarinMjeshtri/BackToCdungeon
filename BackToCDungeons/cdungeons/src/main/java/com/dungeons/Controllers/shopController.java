package com.dungeons.Controllers;

import com.dungeons.screens.GameScreen;
import com.dungeons.shopItemsManager.PlayerInventory;
import com.dungeons.shopItemsManager.Shop;
import com.dungeons.shopItemsManager.ShopManager;

import com.dungeons.systems.CombatSystem.PlayerProgress;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class shopController {

    private static final String CARD_STYLE = "-fx-background-color: #182539; -fx-border-color: #5c7aa5; -fx-border-width: 2; -fx-background-radius: 6; -fx-border-radius: 6;";
    private static final String SELECTED_CARD_STYLE = "-fx-background-color: #22345a; -fx-border-color: #7df3ff; -fx-border-width: 3; -fx-background-radius: 6; -fx-border-radius: 6; -fx-effect: dropshadow(gaussian, rgba(125,243,255,0.55), 16, 0.3, 0, 0);";

    // Row 1
    @FXML private Label item1, item2, item3;
    @FXML private Label price1, price2, price3;
    @FXML private ImageView img1, img2, img3;
    @FXML private AnchorPane card1, card2, card3;

    // Row 2
    @FXML private Label item4, item5, item6;
    @FXML private Label price4, price5, price6;
    @FXML private ImageView img4, img5, img6;
    @FXML private AnchorPane card4, card5, card6;

    // Description + purchase
    @FXML private Label descLabel;
    @FXML private Button purchaseBtn;
    @FXML private StackPane shopRoot;

    private ShopManager shopManager;
    private Shop selectedItem;
    private AnchorPane selectedCard;

    @FXML
    public void initialize() {
        try {
            shopManager = new ShopManager();
            shopManager.load();

            populateCard(card1, item1, price1, img1, "smallHealthPotion");
            populateCard(card2, item2, price2, img2, "bigHealthPotion");
            populateCard(card3, item3, price3, img3, "strPotion");
            populateCard(card4, item4, price4, img4, "shieldBattery");
            populateCard(card5, item5, price5, img5, "leechSerum");
            populateCard(card6, item6, price6, img6, "mirrorShard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateCard(AnchorPane card, Label nameLabel, Label priceLabel, ImageView imgView, String key) {
        card.setStyle(CARD_STYLE);
        card.setUserData(key);

        Shop item = shopManager.getItem(key);
        if (item == null) {
            nameLabel.setText("Missing Item");
            priceLabel.setText("-- G");
            card.setOpacity(0.45);
            return;
        }

        nameLabel.setText(item.displayName);
        priceLabel.setText(item.price + " G");


         Image img = new Image(getClass().getResourceAsStream(item.image));
         imgView.setImage(img);
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        AnchorPane clickedCard = (AnchorPane) event.getSource();
        String itemKey = (String) clickedCard.getUserData();

        if (itemKey == null || itemKey.isEmpty()) return;

        Shop item = shopManager.getItem(itemKey);
        if (item == null) return;

        // Deselect previous card
        if (selectedCard != null) {
            selectedCard.setStyle(CARD_STYLE);
        }

        // Select new card
        selectedItem = item;
        selectedCard = clickedCard;
        selectedCard.setStyle(SELECTED_CARD_STYLE);

        descLabel.setText(item.desc);
    }

    @FXML
    private void handlePurchase() {
        if (selectedItem == null) {
            descLabel.setText("Please select an item first.");
            return;
        }

        PlayerProgress progress = PlayerProgress.getInstance();
        PlayerInventory inventory = PlayerInventory.getInstance();

        if (progress.getGold() < selectedItem.price) {
            descLabel.setText("Not enough gold!");
            return;
        }

        if (inventory.isFull()) {
            descLabel.setText("Hotbar is full! Use an item before buying another.");
            return;
        }

        progress.addGold(-selectedItem.price);

        if (!inventory.addItem(selectedItem)) {
            progress.addGold(selectedItem.price);
            descLabel.setText("Hotbar is full! Use an item before buying another.");
            return;
        }

        if (GameScreen.getInstance() != null && GameScreen.getInstance().getOverlayController() != null) {
            GameScreen.getInstance().getOverlayController().updateUI();
        }

        descLabel.setText("Added " + selectedItem.displayName + " to your hotbar.");

        selectedItem = null;
        if (selectedCard != null) {
            selectedCard.setStyle(CARD_STYLE);
            selectedCard = null;
        }
    }

    @FXML
    private void handleExit() {
        if (shopRoot != null) {
            shopRoot.setVisible(false);
            if (shopRoot.getParent() != null) {
                shopRoot.getParent().setVisible(false);
            }
        }
    }
}

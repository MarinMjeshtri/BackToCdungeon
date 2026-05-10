package com.dungeons.Controllers;

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

public class shopController {

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Row 2
        // populateCard(card4, item4, price4, img4, "someItem");
        // populateCard(card5, item5, price5, img5, "someItem");
        // populateCard(card6, item6, price6, img6, "someItem");
    }

    private void populateCard(AnchorPane card, Label nameLabel, Label priceLabel, ImageView imgView, String key) {
        Shop item = shopManager.getItem(key);
        if (item == null) return;

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
            selectedCard.setStyle("");
        }

        // Select new card
        selectedItem = item;
        selectedCard = clickedCard;
        selectedCard.setStyle("-fx-border-color: #759cd5; -fx-border-width: 2;");

        descLabel.setText(item.desc);
    }

    @FXML
    private void handlePurchase() {
        if (selectedItem == null) {
            descLabel.setText("Please select an item first.");
            return;
        }

        PlayerProgress progress = PlayerProgress.getInstance();

        if (progress.getGold() < selectedItem.price) {
            descLabel.setText("Not enough gold!");
            return;
        }

        progress.addGold(-selectedItem.price);

        // TODO: add to inventory
        // For now just confirm
        descLabel.setText("Purchased: " + selectedItem.displayName + " for " + selectedItem.price + " G");

        selectedItem = null;
        if (selectedCard != null) {
            selectedCard.setStyle("");
            selectedCard = null;
        }
    }
}
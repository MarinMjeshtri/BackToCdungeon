package com.dungeons.Controllers;

import com.dungeons.theAlmagamation;
import com.dungeons.screens.startingScreen;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class tutorialController {

    @FXML private ScrollPane tutorialScroll;
    @FXML private ImageView sindiPortrait;

    @FXML
    public void initialize() {
        try {
            String path = "/sprites/DialougeSprites/SindiCharacterDialougeSprite-NBR.png";
            Image image = new Image(getClass().getResource(path).toExternalForm());
            sindiPortrait.setImage(image);
            System.out.println("Sindi portrait loaded successfully!");
        } catch (Exception e) {
            System.out.println("Could not load Sindi portrait: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        startingScreen screen = new startingScreen();
        OptionsNStartingController controller = screen.getLoader().getController();
        controller.setStage(theAlmagamation.getStage());
        theAlmagamation.switchTo(screen.getRoot());
    }
}

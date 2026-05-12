package com.dungeons.Controllers;

import com.dungeons.MusicandSoundsCode.GameMusicManager;
import com.dungeons.theAlmagamation;
import com.dungeons.screens.GameScreen;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class introController {

    @FXML private VBox     storyContainer;
    @FXML private Pane     fadeOverlay;
    @FXML private Pane     rootPane;
    @FXML private ImageView sindiPortrait;

    private static final double SCROLL_SECONDS = 50.0;
    private static final double FADE_SECONDS   = 2.5;

    private Timeline scrollTimeline;
    private boolean  launched = false;

    @FXML
    public void initialize() {

        // Load Cin de Moni portrait
        try {
            String path = "/sprites/DialougeSprites/SindiCharacterDialougeSprite-NBR.png";
            Image image = new Image(getClass().getResource(path).toExternalForm());
            sindiPortrait.setImage(image);
            System.out.println("Sindi portrait loaded in intro screen!");
        } catch (Exception e) {
            System.out.println("Could not load Sindi portrait: " + e.getMessage());
        }

        GameMusicManager.playIntro();

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                startScroll();
            }
        });
    }

    private void startScroll() {

        double endY = -(storyContainer.getPrefHeight() > 0
                ? storyContainer.getPrefHeight()
                : 3200);

        scrollTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(storyContainer.translateYProperty(), 0)),
                new KeyFrame(Duration.seconds(SCROLL_SECONDS),
                        new KeyValue(storyContainer.translateYProperty(), endY))
        );

        scrollTimeline.setOnFinished(e -> fadeToGame());
        scrollTimeline.play();
    }

    private void fadeToGame() {
        if (launched) return;
        launched = true;

        FadeTransition fade = new FadeTransition(Duration.seconds(FADE_SECONDS), fadeOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setOnFinished(e -> launchGame());
        fade.play();
    }

    @FXML
    private void handleSkip() {
        if (launched) return;
        if (scrollTimeline != null) scrollTimeline.stop();

        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), fadeOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setOnFinished(e -> launchGame());
        fade.play();
        launched = true;
    }

    // In introController.java

    private void launchGame() {
        try {
            // Get the stage once
            Stage stage = (Stage) rootPane.getScene().getWindow();

            GameScreen gameScreen = new GameScreen();
            gameScreen.setStage(stage);

            // Load the font (Only do this once globally if possible, but fine here)
            Font.loadFont(getClass().getResourceAsStream("/OpenType-TT/MarinVonGayNjega.ttf"), 10);

            // 3. THE CLEAN SWITCH
            // We call our fixed switchTo. No need to setFullScreen(true) again!
            theAlmagamation.switchTo(gameScreen.getRoot());

            // Start the game logic
            gameScreen.startLoop();
            GameMusicManager.playGameplay();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

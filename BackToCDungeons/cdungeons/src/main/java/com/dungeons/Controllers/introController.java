package com.dungeons.Controllers;

import com.dungeons.MusicandSoundsCode.GameMusicManager;
import com.dungeons.screens.GameScreen;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class introController {

    @FXML private VBox  storyContainer;
    @FXML private Pane  fadeOverlay;
    @FXML private Pane  rootPane;

    private static final double SCROLL_SECONDS = 50.0;

    private static final double FADE_SECONDS = 2.5;

    private Timeline scrollTimeline;
    private boolean  launched = false;

    @FXML
    public void initialize() {

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
                : 3200); // safe fallback

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
        // Short fast fade when skipping
        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), fadeOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setOnFinished(e -> launchGame());
        fade.play();
        launched = true;
    }

    private void launchGame() {
        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();

            GameScreen gameScreen = new GameScreen();
            gameScreen.setStage(stage);

            Font.loadFont(getClass().getResourceAsStream("/OpenType-TT/MarinVonGayNjega.ttf"), 10);

            Scene scene = new Scene(gameScreen.getRoot());
            scene.getStylesheets().add(
                    getClass().getResource("/sprites/style.css").toExternalForm()
            );

            stage.setScene(scene);
            gameScreen.startLoop();
            GameMusicManager.playGameplay();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

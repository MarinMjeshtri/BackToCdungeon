package com.dungeons;

import com.dungeons.screens.startingScreen;
import com.dungeons.Controllers.OptionsNStartingController;
import com.dungeons.MusicandSoundsCode.GameMusicManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class chatGptTesting extends Application {



    public enum ResolutionMode {
        NORMAL,
        FULLSCREEN
    }

    public static ResolutionMode currentMode = ResolutionMode.FULLSCREEN;

    private static Stage primaryStage;
    private static Scene mainScene;

    public void start(Stage stage) {
        primaryStage = stage;

        // 1. STABILIZE THE WINDOW
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        // This stops ESC from exiting fullscreen
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);

        Font.loadFont(getClass().getResourceAsStream("/OpenType-TT/REANO.ttf"), 10);
        stage.setTitle("LabDungeons 0.0.1");

        loadStartingScreen(stage);
        stage.show();
    }

    public static StackPane scaleToScreen(Parent root) {

        Screen primary = Screen.getPrimary();

        double screenW = primary.getBounds().getWidth();
        double screenH = primary.getBounds().getHeight();

        double scale = Math.min(
                screenW / 1280.0,
                screenH / 720.0
        );

        // fixed-size container for the game
        StackPane gameHolder = new StackPane(root);

        gameHolder.setPrefSize(1280, 720);
        gameHolder.setMinSize(1280, 720);
        gameHolder.setMaxSize(1280, 720);

        // scale the HOLDER instead of the root
        gameHolder.setScaleX(scale);
        gameHolder.setScaleY(scale);

        // fullscreen wrapper
        StackPane wrapper = new StackPane(gameHolder);

        wrapper.setStyle("-fx-background-color: black;");

        wrapper.setPrefSize(screenW, screenH);
        wrapper.setMinSize(screenW, screenH);
        wrapper.setMaxSize(screenW, screenH);

        return wrapper;
    }
    // ── STARTING SCREEN ────────────────────────────────────
    public static void loadStartingScreen(Stage stage) {
        try {
            startingScreen screen = new startingScreen();
            OptionsNStartingController controller = screen.getLoader().getController();
            controller.setStage(stage);

            Screen primary = Screen.getPrimary();
            double screenW = primary.getBounds().getWidth();
            double screenH = primary.getBounds().getHeight();

            StackPane wrapper = scaleToScreen(screen.getRoot());

            if (stage.getScene() == null) {
                // first launch — create scene
                mainScene = new Scene(wrapper, screenW, screenH);
                mainScene.setFill(javafx.scene.paint.Color.BLACK);
                mainScene.getStylesheets().add(
                        chatGptTesting.class
                                .getResource("/sprites/style.css")
                                .toExternalForm()
                );
                stage.setScene(mainScene);
            } else {
                // returning from another screen
                stage.getScene().setRoot(wrapper);
            }

            GameMusicManager.playOpening();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── SWITCH TO ANY SCREEN ───────────────────────────────
    // call from any controller to switch screens
    public static void switchTo(Parent newContent) {
        if (primaryStage != null && primaryStage.getScene() != null) {
            // Use your existing scaling logic
            StackPane wrapper = scaleToScreen(newContent);

            // 2. SMOOTH SWAP
            // Instead of replacing the root, we just change what's inside the current scene.
            // This prevents the "flash" of re-applying CSS and layouts.
            primaryStage.getScene().setRoot(wrapper);

            // Ensure focus goes to the new content so keys work immediately
            newContent.requestFocus();
        }
    }

    public static Stage getStage()   { return primaryStage; }
    public static Scene getScene()   { return mainScene;    }

    public static void main(String[] args) {
        launch(args);
    }
}
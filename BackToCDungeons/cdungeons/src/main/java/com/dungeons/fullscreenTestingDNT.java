package com.dungeons;

import com.dungeons.screens.GameScreen;
import com.dungeons.screens.startingScreen;
import com.dungeons.Controllers.OptionsNStartingController;
import com.dungeons.MusicandSoundsCode.GameMusicManager;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class fullscreenTestingDNT extends Application {

    @Override
    public void start(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/OpenType-TT/REANO.ttf"), 10);
        stage.setTitle("LabDungeons 0.0.1");
        loadStartingScreen(stage);
        stage.show();
    }

    public static void loadStartingScreen(Stage stage) {
        try {
            startingScreen screen = new startingScreen();
            OptionsNStartingController controller = screen.getLoader().getController();
            controller.setStage(stage);

            Scene scene = new Scene(screen.getRoot(), 1280, 720);
            scene.getStylesheets().add(
                    marinMainTesting.class
                            .getResource("/sprites/style.css")
                            .toExternalForm()
            );

            stage.setScene(scene);
            GameMusicManager.playOpening();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadGameScreen(Stage stage) {
        try {
            GameScreen gameScreen = new GameScreen();
            gameScreen.setStage(stage);
            Parent gameRoot = gameScreen.getRoot();

            // ── CHANGED: wrapper fills scene and provides black background ──
            StackPane wrapper = new StackPane(gameRoot);
            wrapper.setStyle("-fx-background-color: black;");

            Scene scene = new Scene(wrapper, 1280, 720);
            scene.getStylesheets().add(
                    marinMainTesting.class
                            .getResource("/sprites/style.css")
                            .toExternalForm()
            );

            // ── CHANGED: bind wrapper to always fill the scene ──
            wrapper.prefWidthProperty().bind(scene.widthProperty());
            wrapper.prefHeightProperty().bind(scene.heightProperty());

            // ── CHANGED: scale gameRoot on both axes to fill wrapper ──
            ChangeListener<Number> scaleListener = (obs, oldVal, newVal) -> {
                gameRoot.setScaleX(scene.getWidth()  / 1280.0);
                gameRoot.setScaleY(scene.getHeight() / 720.0);
            };

            scene.widthProperty().addListener(scaleListener);
            scene.heightProperty().addListener(scaleListener);

            stage.setScene(scene);
            gameScreen.startLoop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
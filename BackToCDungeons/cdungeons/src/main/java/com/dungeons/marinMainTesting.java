package com.dungeons;

import com.dungeons.screens.startingScreen;
import com.dungeons.Controllers.OptionsNStartingController;
import com.dungeons.MusicandSoundsCode.GameMusicManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class marinMainTesting extends Application {

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

            GameMusicManager.playEnding();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
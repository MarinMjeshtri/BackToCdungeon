package com.dungeons;

import com.dungeons.screens.startingScreen;
import com.dungeons.Controllers.OptionsNStartingController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class marinMainTesting extends Application {

    @Override
    public void start(Stage stage) {
        // load font once — not twice
        Font.loadFont(getClass().getResourceAsStream("/OpenType-TT/REANO.ttf"), 10);

        startingScreen screen = new startingScreen();

        OptionsNStartingController controller = screen.getLoader().getController();
        controller.setStage(stage);

        Scene scene = new Scene(screen.getRoot(), 800, 600);

        scene.getStylesheets().add(
                getClass().getResource("/sprites/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("LabDungeons 0.0.1");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
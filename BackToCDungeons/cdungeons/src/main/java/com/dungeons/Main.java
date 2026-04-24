package com.dungeons;

import com.dungeons.screens.startingScreen;
import com.dungeons.Controllers.OptionsNStartingController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        startingScreen screen = new startingScreen();

        OptionsNStartingController controller =
                screen.getLoader().getController();
        controller.setStage(stage);

        stage.setScene(new Scene(screen.getRoot(), 800, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
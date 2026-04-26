package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.dungeons.Controllers.PauseController;
import javafx.stage.Stage;

public class pauseScreen {

    private Parent root;
    private FXMLLoader loader;



    public pauseScreen(GameScreen gameScreen, Stage stage) throws IOException {
        try {
            loader = new FXMLLoader(
                    getClass().getResource("/screens/pauseScreen.fxml")
            );

            root = loader.load();

            // these two lines are what's missing!
            PauseController controller = loader.getController();
            controller.setGameScreen(gameScreen);
            controller.setStage(stage);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load pauseScreen.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}
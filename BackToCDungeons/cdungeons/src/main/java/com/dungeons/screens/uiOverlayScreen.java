package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.dungeons.Controllers.PauseController;
import javafx.stage.Stage;

public class uiOverlayScreen {

    private Parent root;
    private FXMLLoader loader;



    public uiOverlayScreen(GameScreen gameScreen, Stage stage) throws IOException {
        try {
            loader = new FXMLLoader(
                    getClass().getResource("/screens/gameUIscreen.fxml")
            );

            root = loader.load();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load gameUIscreen.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}
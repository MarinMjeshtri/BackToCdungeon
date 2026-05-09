package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class UIscreen {

    private Parent root;
    private FXMLLoader loader;

    public UIscreen(GameScreen gameScreen, Stage stage) {
        try {
            loader = new FXMLLoader(
                    getClass().getResource("/screens/gameOverlayUI.fxml")
            );

            root = loader.load();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load gameOverlayUI.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}
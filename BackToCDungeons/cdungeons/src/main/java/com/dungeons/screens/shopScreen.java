package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class shopScreen {

    private Parent root;
    private FXMLLoader loader;

    public shopScreen(GameScreen gameScreen, Stage stage) throws IOException {
        try {
            loader = new FXMLLoader(
                    getClass().getResource("/screens/shopScreen.fxml")
            );

            root = loader.load();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load shopScreen.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}
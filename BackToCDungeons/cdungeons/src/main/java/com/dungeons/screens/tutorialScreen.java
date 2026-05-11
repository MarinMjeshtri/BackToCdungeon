package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class tutorialScreen {

    private Parent root;
    private FXMLLoader loader;

    public tutorialScreen(GameScreen gameScreen, Stage stage) throws IOException {
        try {
            loader = new FXMLLoader(
                    getClass().getResource("/screens/tutorialScreen.fxml")
            );

            if (loader.getLocation() == null) {
                System.err.println("ERROR: tutorialScreen.fxml not found at /screens/tutorialScreen.fxml");
                throw new IOException("tutorialScreen.fxml not found");
            }

            root = loader.load();

        } catch (Exception e) {
            System.err.println("ERROR loading tutorialScreen.fxml: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load tutorialScreen.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}

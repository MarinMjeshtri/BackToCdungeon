package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class startingScreen {

    private Parent root;
    private FXMLLoader loader;

    public startingScreen() {
        try {
            loader = new FXMLLoader(
                    getClass().getResource("/screens/startingScreen.fxml")
            );

            root = loader.load();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load startingScreen.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}
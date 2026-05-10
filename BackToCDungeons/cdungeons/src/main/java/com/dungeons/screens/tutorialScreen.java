package com.dungeons.screens;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class tutorialScreen {

    private Parent root;
    private FXMLLoader loader;

    public tutorialScreen() throws IOException {
        loader = new FXMLLoader(
                getClass().getResource("/screens/tutorialScreen.fxml")
        );
        root = loader.load();
    }

    public Parent getRoot() {
        return root;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}

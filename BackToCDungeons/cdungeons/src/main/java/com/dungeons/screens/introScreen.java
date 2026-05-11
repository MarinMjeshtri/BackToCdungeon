package com.dungeons.screens;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class introScreen {

    private Parent root;
    private FXMLLoader loader;

    public introScreen() throws IOException {
        loader = new FXMLLoader(
                getClass().getResource("/screens/introScreen.fxml")
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

package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class DialoguesScreen {

    private Parent root;
    private FXMLLoader loader;

    public DialoguesScreen() throws IOException {
        try {
            loader = new FXMLLoader(
                    getClass().getResource("/screens/DialogueScreen.fxml")
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
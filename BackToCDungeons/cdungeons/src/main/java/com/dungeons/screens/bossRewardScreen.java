package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class bossRewardScreen {

    private Parent root;
    private FXMLLoader loader;

    public bossRewardScreen() throws IOException {
        try {
            loader = new FXMLLoader(getClass().getResource("/screens/bossRewardScreen.fxml"));
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bossRewardScreen.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}

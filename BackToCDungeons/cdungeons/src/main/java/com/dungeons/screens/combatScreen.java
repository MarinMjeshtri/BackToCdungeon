package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.dungeons.Controllers.CombatController;

public class combatScreen {

    private Parent root;
    private FXMLLoader loader;
    private CombatController controller;

    public combatScreen() throws IOException {
        try {
            loader = new FXMLLoader(
                    getClass().getResource("/screens/battleScreen.fxml")
            );

            root = loader.load();
            controller = loader.getController(); // 🔥 THIS is what you're missing

        } catch (IOException e) {
            throw new RuntimeException("Failed to load battleScreen.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public FXMLLoader getLoader() {
        return loader;
    }

    public CombatController getController() {
        return controller; // 🔥 expose controller
    }
}
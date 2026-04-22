
/*
THIS IS FINISHED AND ONLY USED FOR LOADING THE FXML FILES, IF YOU WANT TO ALTER THEM IN ANY WAY
CHECK THE FXML FILES THEMSELVES OR THE SPECIFIC METHODS! ~ Marini :D
 */
package com.dungeons.screens;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class startingScreen {

    private Parent root;
// Load the starting screen
    public startingScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/screens/startingScreen.fxml")
            );
            root = loader.load();
// Debugging reasons twins do not touch plz
        } catch (IOException e) {
            throw new RuntimeException("Failed to load startingScreen.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }
}
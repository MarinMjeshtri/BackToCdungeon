package com.dungeons;

import com.dungeons.Controllers.DialogueBoxController;
import com.dungeons.dialogueManager.DialogueManager;
import com.dungeons.screens.DialoguesScreen;
import com.dungeons.screens.startingScreen;
import com.dungeons.Controllers.OptionsNStartingController;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Load dialogue manager
        DialogueManager dialogueManager = new DialogueManager();
        dialogueManager.load();

        // Test dialogue box
        try {
            DialoguesScreen dialogueScreen = new DialoguesScreen();
            DialogueBoxController dController = dialogueScreen.getLoader().getController();
            dController.setDialogueManager(dialogueManager);
            dController.startDialogue("opening");

            stage.setScene(new Scene(dialogueScreen.getRoot(), 600, 400));
            stage.setTitle("Dialogue Test");
            stage.show();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
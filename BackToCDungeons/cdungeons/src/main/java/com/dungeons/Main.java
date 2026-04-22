package com.dungeons;

import com.dungeons.dialogueManager.DialogueManager;
import com.dungeons.screens.GameScreen;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        GameScreen game = new GameScreen();

        Scene scene = new Scene(game.getRoot(), 800, 600);
        stage.setTitle("Back to Dungeons");
        stage.setScene(scene);
        stage.show();

        game.startLoop();

        DialogueManager dialogueManager = new DialogueManager();
        dialogueManager.load();

//    // Test a dialogue
        dialogueManager.startDialogue("trader_shop");

        while (!dialogueManager.isFinished()) {
            System.out.println(dialogueManager.getNextLine());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com.dungeons;

import com.dungeons.dialogueManager.DialogueManager;
import com.dungeons.screens.GameScreen;
import com.dungeons.screens.startingScreen;
import com.dungeons.Controllers.OptionsNStartingController;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        startingScreen screen = new startingScreen();

        OptionsNStartingController controller =
                screen.getLoader().getController();

        controller.setStage(stage);

        stage.setScene(new Scene(screen.getRoot(), 800, 600));
        stage.show();

//        GameScreen game = new GameScreen();
//        Scene switchScreen = new Scene(game.getRoot(), 800, 600);
//        stage.setTitle("Back to Dungeons");
//        game.startLoop();

//    // Test a dialogue
//        dialogueManager.startDialogue("trader_shop");
//
//        while (!dialogueManager.isFinished()) {
//            System.out.println(dialogueManager.getNextLine());
//        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
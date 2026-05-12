package com.dungeons.Controllers;

import com.dungeons.MusicandSoundsCode.GameMusicManager;
import com.dungeons.theAlmagamation;
import com.dungeons.screens.GameScreen;
import com.dungeons.screens.startingScreen;
import com.dungeons.systems.CombatSystem.PlayerProgress;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class victoryQuestionController {

    @FXML
    private Pane gameOver;

    public void restart() throws IOException {
        // 1. Reset state before loading UI
        PlayerProgress.reset();

        Stage stage = (Stage) gameOver.getScene().getWindow();

        // 2. Initialize GameScreen
        GameScreen gameScreen = new GameScreen();
        gameScreen.setStage(stage);

        // 3. Setup Scene
        Scene scene = new Scene(gameScreen.getRoot());

        // 4. Style & Assets
        Font.loadFont(getClass().getResourceAsStream("/OpenType-TT/MarinVonGayNjega.ttf"), 10);
        scene.getStylesheets().add(
                getClass().getResource("/sprites/style.css").toExternalForm()
        );

        // 5. Switch Scene & ENFORCE FULLSCREEN
        stage.setScene(scene);

        // Critical: Re-enable fullscreen and hide the "Press ESC to exit" hint
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");

        // 6. Start game logic and music
        gameScreen.startLoop();
        GameMusicManager.playOpening();
    }

    @FXML
    public void getOut() {
        startingScreen screen = new startingScreen();
        OptionsNStartingController controller = screen.getLoader().getController();
        controller.setStage(theAlmagamation.getStage());
        theAlmagamation.switchTo(screen.getRoot());
    }


}
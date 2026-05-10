package com.dungeons.Controllers;

import com.dungeons.MusicandSoundsCode.GameMusicManager;
import com.dungeons.marinMainTesting;
import com.dungeons.screens.GameScreen;

import com.dungeons.systems.CombatSystem.PlayerProgress;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;


public class gameoverController {

    @FXML
    private Pane gameOver;

    public void restart() throws IOException {
        PlayerProgress.reset();
        Stage stage = (Stage) gameOver.getScene().getWindow();

        GameScreen gameScreen = new GameScreen();
        gameScreen.setStage(stage);
        Scene scene = new Scene(gameScreen.getRoot());

        Font.loadFont(getClass().getResourceAsStream("/OpenType-TT/MarinVonGayNjega.ttf"), 10);
        scene.getStylesheets().add(
                getClass().getResource("/sprites/style.css").toExternalForm()
        );

        stage.setScene(scene);
        gameScreen.startLoop();
        GameMusicManager.playOpening();
    }

    @FXML
    public void getOut() {
        Stage stage = (Stage) gameOver.getScene().getWindow();
        marinMainTesting.loadStartingScreen(stage);
    }

}
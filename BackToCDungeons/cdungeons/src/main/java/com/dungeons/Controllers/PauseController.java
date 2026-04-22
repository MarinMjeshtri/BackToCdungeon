package com.dungeons.Controllers;

import javafx.fxml.FXML;
import com.dungeons.screens.GameScreen;

public class PauseController {

    private GameScreen gameScreen;

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @FXML
    private void resume() {
        gameScreen.togglePause();
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    @FXML
    private void options() {
        // open options screen later
    }
}
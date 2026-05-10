package com.dungeons.Controllers;

import javafx.fxml.FXML;
import com.dungeons.screens.GameScreen;
import com.dungeons.screens.areYouSureScreen;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class PauseController {



    Stage stage;
    private GameScreen gameScreen;
    private areYouSureScreen uSureScreen;

    public void setStage(Stage stage) throws IOException {
        this.stage = stage;
        // now that we have the stage, create the overlay
        this.uSureScreen = new areYouSureScreen();
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @FXML
    private void resume() {
        gameScreen.togglePause();
    }



    @FXML
    private void exit() {
        Pane currentRoot = (Pane) stage.getScene().getRoot();

        //CHECK IF ITS LOADED OR NOT
        if (!currentRoot.getChildren().contains(uSureScreen.getRoot())) {
            currentRoot.getChildren().add(uSureScreen.getRoot());
        }

        // MAKE VISIBLE
        uSureScreen.getRoot().setVisible(true);
    }

    @FXML
    private void options() {
    //No function yet.
    }
}
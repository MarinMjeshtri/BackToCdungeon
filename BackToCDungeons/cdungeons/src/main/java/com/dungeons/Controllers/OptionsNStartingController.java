package com.dungeons.Controllers;

import com.dungeons.screens.GameScreen;
import com.dungeons.Main;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class OptionsNStartingController {
    private Stage stage;
    @FXML
    private void handleButton1() {

        GameScreen gameScreen = new GameScreen();
        Scene scene = new Scene(gameScreen.getRoot());

        stage.setScene(scene);
        gameScreen.startLoop();

    }

    @FXML
    private void handleButton2() {
        System.out.println("Options clicked");
    }

    @FXML
    private void handleButton3() {
        System.out.println("Play clicked");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}

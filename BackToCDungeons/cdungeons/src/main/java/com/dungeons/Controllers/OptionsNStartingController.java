package com.dungeons.Controllers;

import com.dungeons.screens.GameScreen;
import com.dungeons.Main;
import com.dungeons.screens.creditsScreen;
import com.dungeons.screens.areYouSureScreen;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;


public class OptionsNStartingController {
    private Stage stage;
    //OPEN THE GAME
    @FXML private Pane startingScreenPane;

    @FXML
    public void initialize() {
//use css, unless ur a lazy bum and want to just hard code it in (did i mention ur a bum if u do that) cough cough past me
    }
    @FXML
    private void handleButton1() throws IOException {

        GameScreen gameScreen = new GameScreen();
        gameScreen.setStage(stage);
        Scene scene = new Scene(gameScreen.getRoot());

        stage.setScene(scene);
        gameScreen.startLoop();

    }

    //OPEN THE CREDITS
    @FXML
    private void handleButton2() throws IOException {
        creditsScreen credits = new creditsScreen();
        Scene scene = new Scene(credits.getRoot());

        stage.setScene(scene);
    }

    //OPEN THE ARE U SURE OR WHATEVER I NAME IT
    @FXML
    private void handleButton3() throws IOException {
        areYouSureScreen uSure = new areYouSureScreen();

        Pane currentRoot = (Pane) stage.getScene().getRoot();
        currentRoot.getChildren().add(uSure.getRoot());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}

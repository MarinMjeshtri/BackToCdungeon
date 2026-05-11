package com.dungeons.Controllers;

import com.dungeons.chatGptTesting;
import com.dungeons.screens.*;
import com.dungeons.MusicandSoundsCode.GameMusicManager;

import com.dungeons.screens.scaryScreen;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;


public class OptionsNStartingController {
    private Stage stage;
    //OPEN THE GAME
    @FXML private Pane startingScreenPane;

    @FXML
    public void initialize() {
//use css, unless ur a lazy bum and want to just hard code it in (did i mention ur a bum if u do that) cough cough past me (i wil rape ur ass alive)
    }
    @FXML
    private void handleButton1() throws IOException {
        introScreen screen = new introScreen();
        chatGptTesting.switchTo(screen.getRoot());
    }

    //OPEN THE CREDITS
    @FXML
    private void handleButton2() throws IOException {
        creditsScreen credits = new creditsScreen(null, stage);
        chatGptTesting.switchTo(credits.getRoot());
    }

//    //OPEN THE ARE U SURE OR WHATEVER I NAME IT
//    @FXML
//    private void handleButton3() throws IOException {
//        scaryScreen scary = new scaryScreen(null,stage );
//        Scene scene = new Scene(scary.getRoot());
//
//        stage.setScene(scene);
//    }

    @FXML
    private void handleButton3() throws IOException {
        tutorialScreen tutorial = new tutorialScreen(null,stage );
        chatGptTesting.switchTo(tutorial.getRoot());
    }

    @FXML
    private void handleButton4(){
        System.exit(0);
    }

    @FXML
    private void handleButton5() throws IOException {
        optionsMenu options = new optionsMenu(null,stage );
        chatGptTesting.switchTo(options.getRoot());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}

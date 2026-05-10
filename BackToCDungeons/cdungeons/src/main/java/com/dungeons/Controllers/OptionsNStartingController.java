package com.dungeons.Controllers;

import com.dungeons.screens.GameScreen;
import com.dungeons.screens.creditsScreen;
import com.dungeons.screens.areYouSureScreen;
import com.dungeons.MusicandSoundsCode.GameMusicManager;
import com.dungeons.screens.scaryScreen;

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

    //OPEN THE CREDITS
    @FXML
    private void handleButton2() throws IOException {
        creditsScreen credits = new creditsScreen(null, stage);
        Scene scene = new Scene(credits.getRoot());

        stage.setScene(scene);
    }

    //OPEN THE ARE U SURE OR WHATEVER I NAME IT
    @FXML
    private void handleButton3() throws IOException {
        scaryScreen scary = new scaryScreen(null,stage );
        Scene scene = new Scene(scary.getRoot());

        stage.setScene(scene);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}

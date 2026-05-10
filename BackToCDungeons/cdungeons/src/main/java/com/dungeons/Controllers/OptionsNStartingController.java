package com.dungeons.Controllers;

import com.dungeons.screens.areYouSureScreen;
<<<<<<< HEAD
import com.dungeons.screens.creditsScreen;
import com.dungeons.screens.introScreen;
import com.dungeons.screens.tutorialScreen;
=======
import com.dungeons.MusicandSoundsCode.GameMusicManager;
>>>>>>> e134caa1f11d0b969d6b1e0b152282853c0a1574

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class OptionsNStartingController {

    private Stage stage;

    @FXML private Pane startingScreenPane;

    @FXML
    public void initialize() {
<<<<<<< HEAD
=======
//use css, unless ur a lazy bum and want to just hard code it in (did i mention ur a bum if u do that) cough cough past me (i wil rape ur ass alive)
>>>>>>> e134caa1f11d0b969d6b1e0b152282853c0a1574
    }

    @FXML
    private void handleButton1() throws IOException {
        introScreen intro = new introScreen();
        Font.loadFont(getClass().getResourceAsStream("/OpenType-TT/MarinVonGayNjega.ttf"), 10);
        Scene scene = new Scene(intro.getRoot());
        scene.getStylesheets().add(
                getClass().getResource("/sprites/style.css").toExternalForm()
        );
        stage.setScene(scene);
<<<<<<< HEAD
=======
        gameScreen.startLoop();
        GameMusicManager.playOpening();
>>>>>>> e134caa1f11d0b969d6b1e0b152282853c0a1574

    }

    @FXML
    private void handleTutorial() throws IOException {
        tutorialScreen tutorial = new tutorialScreen();
        Scene scene = new Scene(tutorial.getRoot());
        scene.getStylesheets().add(
                getClass().getResource("/sprites/style.css").toExternalForm()
        );
        stage.setScene(scene);
    }

    @FXML
    private void handleButton2() throws IOException {
        creditsScreen credits = new creditsScreen(null, stage);
        Scene scene = new Scene(credits.getRoot());
        stage.setScene(scene);
    }

    @FXML
    private void handleButton3() throws IOException {
<<<<<<< HEAD
        areYouSureScreen uSure = new areYouSureScreen();
        Pane currentRoot = (Pane) stage.getScene().getRoot();
        currentRoot.getChildren().add(uSure.getRoot());
=======
        System.exit(0);
>>>>>>> e134caa1f11d0b969d6b1e0b152282853c0a1574
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

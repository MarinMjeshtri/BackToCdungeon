package com.dungeons.Controllers;

import com.dungeons.screens.areYouSureScreen;
import com.dungeons.screens.creditsScreen;
import com.dungeons.screens.introScreen;
import com.dungeons.screens.tutorialScreen;

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
        areYouSureScreen uSure = new areYouSureScreen();
        Pane currentRoot = (Pane) stage.getScene().getRoot();
        currentRoot.getChildren().add(uSure.getRoot());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

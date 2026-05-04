package com.dungeons.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class creditsController {

    @FXML private ScrollPane creditScroll;

    @FXML
    public void initialize() {
        // autoscroll from top to bottom over 25 seconds
        Timeline autoScroll = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(creditScroll.vvalueProperty(), 0)),
                new KeyFrame(Duration.seconds(25),
                        new KeyValue(creditScroll.vvalueProperty(), 1))
        );
        autoScroll.setDelay(Duration.seconds(1));
        autoScroll.play();
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) creditScroll.getScene().getWindow();
        stage.getScene().setRoot(
                new com.dungeons.screens.startingScreen().getRoot()
        );
    }
}
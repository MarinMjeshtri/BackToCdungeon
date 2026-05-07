package com.dungeons.Controllers;

import com.dungeons.marinMainTesting;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class tutorialController {

    @FXML private ScrollPane tutorialScroll;

    @FXML
    private void handleBack() {
        Stage stage = (Stage) tutorialScroll.getScene().getWindow();
        marinMainTesting.loadStartingScreen(stage);
    }
}

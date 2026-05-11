package com.dungeons.Controllers;

import com.dungeons.chatGptTesting;
import com.dungeons.marinMainTesting;
import com.dungeons.screens.startingScreen;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class tutorialController {

    @FXML private ScrollPane tutorialScroll;

    @FXML
    private void handleBack() {
        startingScreen screen = new startingScreen();
        OptionsNStartingController controller = screen.getLoader().getController();
        controller.setStage(chatGptTesting.getStage());
        chatGptTesting.switchTo(screen.getRoot());
    }
}

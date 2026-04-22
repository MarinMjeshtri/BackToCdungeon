package com.dungeons.Controllers;

import javafx.fxml.FXML;

public class OptionsNStartingController {

    @FXML
    private void handleButton1() {
        System.exit(0);
    }

    @FXML
    private void handleButton2() {
        System.out.println("Options clicked");
    }

    @FXML
    private void handleButton3() {
        System.out.println("Play clicked");
    }

}

package com.dungeons.Controllers;

import com.dungeons.screens.areYouSureScreen;
import com.dungeons.screens.GameScreen;
import com.dungeons.Main;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;


public class quitController {
    @FXML Pane closePane;

    @FXML
    public void closeEntirely(){
        System.exit(0);


    }

    @FXML
    public void returnBack(){
        closePane.setVisible(false);
    }

}

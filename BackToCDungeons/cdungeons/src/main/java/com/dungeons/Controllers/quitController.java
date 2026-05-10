package com.dungeons.Controllers;

import com.dungeons.marinMainTesting;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class quitController {

    @FXML Pane closePane;
    @FXML
    AnchorPane anchorDecor;
    @FXML
    Button yesQuit;
    @FXML
    Button noQuit;

    @FXML
    public void initialize() {
        String url = getClass().getResource(
                "/sprites/DialougeSprites/MrBalls/willy2.jpg"
        ).toExternalForm();

        anchorDecor.setStyle(
                "-fx-background-image: url('" + url + "');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center;"
        );

        // Button 1 image
        String url2 = getClass().getResource(
                "/sprites/DialougeSprites/MrBalls/Sigma.jpg"
        ).toExternalForm();

        noQuit.setStyle(
                "-fx-background-image: url('" + url2 + "');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center;"
        );

        // Button 2 image
        String url3 = getClass().getResource(
                "/sprites/DialougeSprites/MrBalls/mrPenis.jpg"
        ).toExternalForm();

        yesQuit.setStyle(
                "-fx-background-image: url('" + url3 + "');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center;"
        );
    }

    @FXML
    public void closeEntirely(){
        Stage stage = (Stage) closePane.getScene().getWindow();
        marinMainTesting.loadStartingScreen(stage);
    }

    @FXML
    public void returnBack(){
        closePane.setVisible(false);
    }

}

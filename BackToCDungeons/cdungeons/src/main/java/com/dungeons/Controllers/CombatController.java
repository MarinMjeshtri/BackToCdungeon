package com.dungeons.Controllers;


import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class CombatController {
    @FXML private AnchorPane mainAnchor;
    @FXML private AnchorPane pressAttack;
    @FXML private AnchorPane pressDefense;
    @FXML private AnchorPane pressTalk;
    @FXML private AnchorPane pressItem;

    @FXML
    public void openAttack(){
        mainAnchor.setVisible(false);
        mainAnchor.setDisable(true);

        pressAttack.setVisible(true);
        pressAttack.setDisable(false);
    }

    @FXML
    public void openDefense(){
        mainAnchor.setVisible(false);
        mainAnchor.setDisable(true);

        pressDefense.setVisible(true);
        pressDefense.setDisable(false);
    }

    @FXML
    public void openItems(){
        mainAnchor.setVisible(false);
        mainAnchor.setDisable(true);

        pressItem.setVisible(true);
        pressItem.setDisable(false);
    }

    @FXML
    public void openTalk(){
        mainAnchor.setVisible(false);
        mainAnchor.setDisable(true);

        pressTalk.setVisible(true);
        pressTalk.setDisable(false);
    }

}

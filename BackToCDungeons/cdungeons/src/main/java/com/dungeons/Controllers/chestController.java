package com.dungeons.Controllers;

import com.dungeons.screens.shopScreen;
import com.dungeons.screens.GameScreen;
import com.dungeons.marinMainTesting;
import com.dungeons.systems.items.itemPicker;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javafx.scene.image.ImageView;
import java.io.IOException;

public class chestController {
    @FXML Label descItem;
    @FXML Label statsItem;
    @FXML Label itemName;
    @FXML ImageView img1;

    @FXML
    public void initialize(){
        itemPicker itempicker = new itemPicker();
        itempicker.load();
        descItem.setText(itempicker.getItem("brokenGlasses").desc);
        statsItem.setText(String.valueOf(itempicker.getItem("brokenGlasses").stats));
        itemName.setText(itempicker.getItem("brokenGlasses").name);

        Image img11 = new Image(getClass().getResourceAsStream(itempicker.getItem("brokenGlasses").image));
        img1.setImage(img11);

    }
}

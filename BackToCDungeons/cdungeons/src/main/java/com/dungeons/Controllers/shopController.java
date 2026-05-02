package com.dungeons.Controllers;

import com.dungeons.screens.shopScreen;
import com.dungeons.screens.GameScreen;
import com.dungeons.marinMainTesting;
import com.dungeons.systems.items.itemPicker;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.swing.text.html.ImageView;
import java.io.IOException;


public class shopController {

    //ITEMS we can create a randomizer later
    @FXML Label item1;
    @FXML Label item2;
    @FXML Label item3;
    //PRICE
    @FXML Label price1;
    @FXML Label price2;
    @FXML Label price3;
    //IMAGES
    @FXML ImageView img1;
    @FXML ImageView img2;
    @FXML ImageView img3;

    @FXML
    public void initialize(){
        itemPicker itempicker = new itemPicker();
        itempicker.load();
        item1.setText(itempicker.getItem("brokenGlasses").name);
        item2.setText(itempicker.getItem("clipper").name);
        item3.setText(itempicker.getItem("xSshirt").name);

        price1.setText(itempicker.getItem("brokenGlasses").price);
        price2.setText(itempicker.getItem("clipper").price);
        price3.setText(itempicker.getItem("xSshirt").price);
    }
}

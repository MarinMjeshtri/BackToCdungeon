package com.dungeons.Controllers;

import com.dungeons.screens.GameScreen;
import com.dungeons.world.Map;
import com.dungeons.world.MapManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class roomScreenController {
    MapManager mapManager;
    @FXML Label roomType;
    @FXML Label roomCounter;

    @FXML
    public void updateScreen() {
        if(
                mapManager.isCurrentMap("MobRoom1") ||
                mapManager.isCurrentMap("MobRoom2") ||
                mapManager.isCurrentMap("MobRoom3") ||
                mapManager.isCurrentMap("MobRoom4") ||
                mapManager.isCurrentMap("MobRoom5")
        ){
            roomType.setText("Mob Room");
        }
        else if(
                mapManager.isCurrentMap("BossRoomJoni") ||
                mapManager.isCurrentMap("RoomKledi") ||
                mapManager.isCurrentMap("k3jviBossroom")
        ){
            roomType.setText("Boss Room");
        }
        else if(
                mapManager.isCurrentMap("ShopRoom") ||
                mapManager.isCurrentMap("ChestRoom")
        ){
            roomType.setText("Utility Room");
        }
    }
}

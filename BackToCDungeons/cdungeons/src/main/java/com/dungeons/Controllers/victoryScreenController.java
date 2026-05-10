package com.dungeons.Controllers;

import com.dungeons.systems.CombatSystem.*;
import com.dungeons.world.Map;
import com.dungeons.world.MapManager;

import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;


public class victoryScreenController {

    MapManager mapManager;
    @FXML Label xp;
    @FXML Label gold;
    @FXML Label possibleItem;

    @FXML StackPane victoryPane;

    @FXML public void hideWindow(){
        victoryPane.setVisible(false);
    }

    @FXML public void initialize() {
        possibleItem.setVisible(false);
        // nothing else here — mapManager isn't set yet
    }

    public void setMapManager(MapManager mapManager) {
        this.mapManager = mapManager;
        setup();
    }

    private void setup() {


        if (mapManager.isCurrentMap("k3jviBossroom")) {
            xp.setText(String.valueOf(RewardTable.getBossXP("CassieYarn")));
            gold.setText(String.valueOf(RewardTable.getBossGold("CassieYarn")));
        } else if (mapManager.isCurrentMap("BossRoomJoni")) {
            xp.setText(String.valueOf(RewardTable.getBossXP("JohnMKati")));
            gold.setText(String.valueOf(RewardTable.getBossGold("JohnMKati")));
        } else if (mapManager.isCurrentMap("RoomKledi")) {
            xp.setText(String.valueOf(RewardTable.getBossXP("FreakyRelah")));
            gold.setText(String.valueOf(RewardTable.getBossGold("FreakyRelah")));
        } else {
        xp.setText(String.valueOf(RewardTable.getMobXP(Map.getRoomCounter())));
        gold.setText(String.valueOf(RewardTable.getMobGold(Map.getRoomCounter())));
        }
    }
}

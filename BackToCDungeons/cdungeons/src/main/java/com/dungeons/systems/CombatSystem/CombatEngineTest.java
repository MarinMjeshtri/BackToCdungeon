package com.dungeons.systems.CombatSystem;

import com.dungeons.Controllers.CombatController;
import com.dungeons.screens.combatScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CombatEngineTest extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        combatScreen screen = new combatScreen();
        CombatController controller = screen.getController();

        stage.setScene(new Scene(screen.getRoot(), 800, 600));
        stage.setTitle("BackToCDungeons — Combat Test");
        stage.show();

        // change boss ID here to test different bosses:
        // "CassieYarn", "FreakyRelah", "JohnMKati"
        controller.startCombat("FreakyRelah");

        System.out.println("Combat ready. Waiting for player input...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
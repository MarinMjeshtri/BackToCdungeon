package com.dungeons.systems.CombatSystem;

import com.dungeons.screens.combatScreen;
import com.dungeons.Controllers.CombatController;
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

        
      
        System.out.println("Combat screen loaded, pls");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com.dungeons.systems.CombatSystem;

import com.dungeons.Controllers.CombatController;
import com.dungeons.screens.combatScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class CombatEngineTest extends Application {


    // Which enemy to spawn.
    // Named bosses:  "CassieYarn", "FreakyRelah", "JohnMKati"
    // Mobs:          "Mob1", "Mob2", "Mob3", "Mob4", "Mob5" We stull dont have names
    private static final String TEST_ENEMY = "Mob1";

    // The level of the spawned enemy.
    // This ONLY affects Mob1-Mob5. Named bosses ignore this value completely.
    // Higher level = more HP, ATK, and DEF (see StatsLoader for the scaling equations).

    private static final int TEST_ENEMY_LEVEL = 3;

    // Simulates the player being at this level for this test run.
    // Set to 1 for a fresh player (exactly as the game starts).
    // Set higher to test mid-game or late-game scenarios.
    // NOTE: stat scaling for the player is currently commented out in PlayerProgress.
    // This constant still sets the XP progress correctly, but stats will not change
    // until applyToPlayer() is activated.
    private static final int TEST_PLAYER_LEVEL = 1;

    // -----------------------------------------------------------------------


    // -----------------------------------------------------------------------
    // start(Stage stage)
    // JavaFX calls this automatically when the application launches.
    // 'Stage' is the window. 'throws Exception' means JavaFX handles errors here.
    //
    // What it does:
    //   1. Resets PlayerProgress so each test starts clean (no leftover XP/gold/level)
    //   2. Simulates the player being at TEST_PLAYER_LEVEL by feeding XP
    //   3. Creates the combat screen and controller
    //   4. Opens the game window
    //   5. Starts combat with the configured enemy and level
    //   6. Prints debug info to the console
    // -----------------------------------------------------------------------
    @Override
    public void start(Stage stage) throws Exception {

        // Reset PlayerProgress so this test starts fresh.
        // Without this, XP from a previous test run could carry over.
        PlayerProgress.reset();

        // Simulate the player being at the target test level before combat starts.
        simulatePlayerLevel(TEST_PLAYER_LEVEL);

        // Build the combat screen. combatScreen creates the JavaFX layout and
        // CombatController wires the buttons and labels to combat logic.
        combatScreen screen = new combatScreen();
        CombatController controller = screen.getController();

        // Create the window with 800x600 resolution and a title that shows what is being tested.
        Scene scene = new Scene(screen.getRoot(), 800, 600);
        scene.getStylesheets().add(
                getClass().getResource("/sprites/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("BackToCDungeons -- Combat Test [" + TEST_ENEMY + " Lv." + TEST_ENEMY_LEVEL + "]");
        stage.show(); // make the window visible

        // Start combat using the level-aware method in CombatController.
        // For named bosses: level is ignored
        // For mobs: level determines scaled HP, ATK, DEF.
        controller.startCombatAtLevel(TEST_ENEMY, TEST_ENEMY_LEVEL);
    }


    // -----------------------------------------------------------------------
    // simulatePlayerLevel(int targetLevel)
    // Gives the PlayerProgress singleton exactly enough XP to reach targetLevel.
    // Only used for testing. In real gameplay, XP is earned naturally from fights.
    //
    // How it works:
    // The loop runs from level 1 up to (targetLevel - 1).
    // Each iteration: reads the XP needed for the current level, adds exactly that much.
    // addXP() then levels up the player and sets xp back to 0.
    // After the loop, the player is at targetLevel with 0 XP into that level.
    //
    // Example: targetLevel=3
    //   i=1: needs 80 XP to go 1->2. addXP(80) -> now level 2, xp=0.
    //   i=2: needs 119 XP to go 2->3. addXP(119) -> now level 3, xp=0.
    //   Loop ends. Player is at level 3.
    // -----------------------------------------------------------------------
    private void simulatePlayerLevel(int targetLevel) {
        PlayerProgress p = PlayerProgress.getInstance();
        for (int i = 1; i < targetLevel; i++) {
            // xpToNextLevel() returns the threshold for the CURRENT level before adding XP.
            // Adding exactly that amount causes one level-up per iteration.
            p.addXP(p.xpToNextLevel());
        }
    }


    // main() is the Java entry point. It just calls launch() which starts JavaFX
    // and eventually calls start(). 'args' passes any command-line arguments through.
    public static void main(String[] args) {
        launch(args);
    }
}
package com.dungeons.screens;

import com.dungeons.systems.Player;
import com.dungeons.screens.pauseScreen;

import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameScreen {
    private pauseScreen pauseScreen;
    // --- Screen setup ---
    private final Canvas canvas = new Canvas(800, 600);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    // --- Player ---
    private final Player player = new Player(150, 100);

    // --- Game loop ---
    private AnimationTimer loop;

    public Parent getRoot() {

        Pane root = new Pane(canvas);
        root.setPrefSize(800, 600);

        pauseScreen ps = new pauseScreen(this);
        root.getChildren().add(ps.getRoot());
        ps.getRoot().setVisible(false);


        this.pauseScreen = ps;

        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                togglePause();
            } else {
                player.keyPressed(e.getCode());
            }
        });
        canvas.setOnKeyReleased(e -> player.keyReleased(e.getCode()));

        return root;
    }


    // add this method so controller can call it too
    public void togglePause() {
        boolean nowPaused = !pauseScreen.getRoot().isVisible();
        pauseScreen.getRoot().setVisible(nowPaused);

        if (nowPaused) loop.stop();
        else           loop.start();
    }
    // Start the game loop — call this after stage.show()
    public void startLoop() {
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(); // move things
                render(); // draw things
            }
        };
        loop.start();
    }

    // Called every frame — update game state
    private void update() {
        player.update();
    }

    // Called every frame — draw everything
    private void render() {
        // Clear last frame
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setImageSmoothing(false);

        // Draw player
        player.render(gc);
    }
}
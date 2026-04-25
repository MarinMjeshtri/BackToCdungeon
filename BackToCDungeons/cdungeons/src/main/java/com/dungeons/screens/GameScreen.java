package com.dungeons.screens;

import com.dungeons.systems.Player;
import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class GameScreen {

    private pauseScreen pauseScreen;
    private Stage stage; // ← add this field

    private final Canvas canvas = new Canvas(800, 600);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final Player player = new Player(150, 100);
    private AnimationTimer loop;


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Parent getRoot() throws IOException {
        Pane root = new Pane(canvas);
        root.setPrefSize(800, 600);

        pauseScreen ps = new pauseScreen(this, stage); // ← pass stage
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

    public void togglePause() {
        boolean nowPaused = !pauseScreen.getRoot().isVisible();
        pauseScreen.getRoot().setVisible(nowPaused);

        if (nowPaused) loop.stop();
        else           loop.start();
    }

    public void startLoop() {
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        loop.start();
    }

    private void update() { player.update(); }

    private void render() {
        gc.setImageSmoothing(false);
        player.render(gc);
    }
}
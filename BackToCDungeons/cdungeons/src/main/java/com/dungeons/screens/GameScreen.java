package com.dungeons.screens;

import com.dungeons.systems.Player;
import com.dungeons.world.Map;
import com.dungeons.world.MapManager;
import com.dungeons.world.MapRenderer;
import com.dungeons.world.TilesetManager;

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
    private Stage stage;

    private final Canvas canvas = new Canvas(800, 600);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    // --- World ---
    private final TilesetManager tilesetManager = new TilesetManager();
    private MapManager mapManager;
    private MapRenderer mapRenderer;

    // --- Player ---
    private final Player player = new Player(150, 100);

    // --- Camera ---
    private double cameraX = 0;
    private double cameraY = 0;
    private static final int TILE_SIZE = 16;
    private static final int SCALE     = 3;

    // --- Game loop ---
    private AnimationTimer loop;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Parent getRoot() throws IOException {

        // load tilesets once
        tilesetManager.loadAll();

        // set up map manager with listeners
        mapManager = new MapManager(
                tilesetManager,

                // called when player walks into a transition zone
                (newMap, spawnX, spawnY) -> {
                    mapRenderer = new MapRenderer(newMap, tilesetManager);
                    player.setPosition(
                            spawnX * TILE_SIZE * SCALE,
                            spawnY * TILE_SIZE * SCALE
                    );
                    System.out.println("Map changed! Spawn: " + spawnX + ", " + spawnY);
                },

                // called when player steps on fight/shop/chest
                (type, tileX, tileY) -> {
                    System.out.println("Interact: " + type + " at " + tileX + "," + tileY);
                    // TODO: open combat/shop/chest screen based on type
                    switch (type) {
//                        case "fight" -> { /* open combat */ }
//                        case "shop"  -> { /* open shop   */ }
//                        case "chest" -> { /* open chest  */ }
                    }
                }
        );

        // load starting map
        mapManager.loadMap("k3jviBossroom");
        mapRenderer = new MapRenderer(mapManager.getCurrentMap(), tilesetManager);

        // set player spawn from map
        player.setPosition(
                mapManager.getCurrentMap().spawnX * TILE_SIZE * SCALE,
                mapManager.getCurrentMap().spawnY * TILE_SIZE * SCALE
        );

        // pause screen
        pauseScreen ps = new pauseScreen(this, stage);
        Pane root = new Pane(canvas);
        root.setPrefSize(800, 600);
        root.getChildren().add(ps.getRoot());
        ps.getRoot().setVisible(false);
        this.pauseScreen = ps;

        // input
        canvas.setFocusTraversable(true);
        canvas.requestFocus();
        canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) togglePause();
            else player.keyPressed(e.getCode());
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

    private void update() {
        player.update();
        updateCamera();

        // convert pixel position to tile coordinates
        int tileX = (int)(player.getX() / (TILE_SIZE * SCALE));
        int tileY = (int)(player.getY() / (TILE_SIZE * SCALE));
        mapManager.checkInteractions(tileX, tileY);
    }

    private void updateCamera() {
        Map map = mapManager.getCurrentMap();
        cameraX = player.getX() - canvas.getWidth()  / 2;
        cameraY = player.getY() - canvas.getHeight() / 2;

        double mapW = map.width  * TILE_SIZE * SCALE;
        double mapH = map.height * TILE_SIZE * SCALE;

        cameraX = Math.max(0, Math.min(cameraX, mapW - canvas.getWidth()));
        cameraY = Math.max(0, Math.min(cameraY, mapH - canvas.getHeight()));
    }

    private void render() {
        // clear
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setImageSmoothing(false);

        // apply camera
        gc.save();
        gc.translate(-cameraX, -cameraY);

        // draw map
        mapRenderer.render(gc);

        // draw player
        player.render(gc);

        gc.restore();
    }
}
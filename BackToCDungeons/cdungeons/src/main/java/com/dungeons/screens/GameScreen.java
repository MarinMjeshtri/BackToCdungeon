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

    private static final int TILE_SIZE = 16;
    private static final int SCALE = 2;

    private pauseScreen pauseScreen;
    private Stage stage;

    private final Canvas canvas = new Canvas(800, 600);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    private final TilesetManager tilesetManager = new TilesetManager();
    private MapManager mapManager;
    private MapRenderer mapRenderer;

    private final Player player = new Player(0, 0);

    private double cameraX = 0;
    private double cameraY = 0;

    private AnimationTimer loop;

    // Dialogue state
    private Object activeDialogue = null;   // DialogueBoxController — typed as Object until other team pushes
    private Object activeDialogueNode = null; // Parent node — typed as Object until other team pushes
    private int lastDialogueTileX = -1;
    private int lastDialogueTileY = -1;
    private Pane gameRoot;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Parent getRoot() throws IOException {

        tilesetManager.loadAll();

        mapManager = new MapManager(
                tilesetManager,

                (newMap, spawnX, spawnY) -> {
                    mapRenderer = new MapRenderer(newMap, tilesetManager);
                    player.setMap(newMap);
                    player.setPosition(
                            spawnX * TILE_SIZE * SCALE,
                            spawnY * TILE_SIZE * SCALE
                    );
                    System.out.println("Map changed! Spawn: " + spawnX + ", " + spawnY);
                },

                (type, tileX, tileY) -> {
                    System.out.println("Triggered: " + type + " at " + tileX + ", " + tileY);

                    if (type.equals("fight")) {
                        // TODO: character team hooks battle here
                        // When done they call: mapManager.markFightDone(tileX, tileY)
                    }

                    if (type.equals("shop")) {
                        // TODO: shop team hooks here
                    }

                    if (type.equals("chest")) {
                        // TODO: chest team hooks here
                    }

                    if (type.startsWith("dialogue:")) {
                        // Store tile coords so we can mark it done when dialogue finishes
                        lastDialogueTileX = tileX;
                        lastDialogueTileY = tileY;
                        loop.stop();

                        // TODO: uncomment this block when dialogue team pushes their code
                        // String dialogueId = type.split(":")[1];
                        // Platform.runLater(() -> {
                        //     try {
                        //         DialoguesScreen dialogueScreen = new DialoguesScreen();
                        //         DialogueBoxController dController = dialogueScreen.getLoader().getController();
                        //         dController.setDialogueManager(dialogueManager);
                        //         dController.startDialogue(dialogueId);
                        //         activeDialogue = dController;
                        //         activeDialogueNode = dialogueScreen.getRoot();
                        //         gameRoot.getChildren().add(activeDialogueNode);
                        //     } catch (Exception ex) {
                        //         ex.printStackTrace();
                        //     }
                        // });

                        // Temporary: just print and resume immediately for testing
                        System.out.println("Dialogue triggered: " + type.split(":")[1]);
                        mapManager.markDialogueDone(tileX, tileY);
                        loop.start();
                    }
                }
        );

        mapManager.loadMap("MobRoom1");
        Map currentMap = mapManager.getCurrentMap();
        mapRenderer = new MapRenderer(currentMap, tilesetManager);
        player.setMap(currentMap);
        player.setPosition(
                currentMap.spawnX * TILE_SIZE * SCALE,
                currentMap.spawnY * TILE_SIZE * SCALE
        );

        gameRoot = new Pane(canvas);
        gameRoot.setPrefSize(800, 600);

        pauseScreen ps = new pauseScreen(this, stage);
        gameRoot.getChildren().add(ps.getRoot());
        ps.getRoot().setVisible(false);
        this.pauseScreen = ps;

        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) togglePause();
            else player.keyPressed(e.getCode());
        });
        canvas.setOnKeyReleased(e -> player.keyReleased(e.getCode()));

        return gameRoot;
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
        mapManager.checkInteractions(player.getTileX(), player.getTileY());

        // When dialogue team pushes their code, replace the block below
        // if (activeDialogue != null && activeDialogue.isDialogueFinished()) {
        //     gameRoot.getChildren().remove(activeDialogueNode);
        //     mapManager.markDialogueDone(lastDialogueTileX, lastDialogueTileY);
        //     activeDialogue = null;
        //     activeDialogueNode = null;
        //     loop.start();
        // }
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
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setImageSmoothing(false);

        gc.save();
        gc.translate(-cameraX, -cameraY);

        mapRenderer.render(gc);
        player.render(gc);

        gc.restore();
    }
}
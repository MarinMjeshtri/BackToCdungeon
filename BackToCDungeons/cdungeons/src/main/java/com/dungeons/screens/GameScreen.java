package com.dungeons.screens;

import com.dungeons.Controllers.CombatController;
import com.dungeons.Controllers.DialogueBoxController;
import com.dungeons.dialogueManager.DialogueManager;
import com.dungeons.systems.Player;
import com.dungeons.world.*;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
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

    private DialogueManager dialogueManager;
    private DialogueBoxController activeDialogue = null;
    private Parent activeDialogueNode = null;
    private boolean dialoguePlayed = false;

    private pauseScreen pauseScreen;
    private Pane gameRoot; // ← fixed name
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

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Parent getRoot() throws IOException {

        tilesetManager.loadAll();

        dialogueManager = new DialogueManager();
        dialogueManager.load();

        mapManager = new MapManager(
                tilesetManager,

                (newMap, spawnX, spawnY) -> {
                    dialoguePlayed = false;
                    mapRenderer = new MapRenderer(newMap, tilesetManager);
                    player.setMap(newMap);
                    player.setPosition(
                            spawnX * TILE_SIZE * SCALE,
                            spawnY * TILE_SIZE * SCALE
                    );
                    System.out.println("Map changed! Spawn: " + spawnX + ", " + spawnY);
                },

                // SINDI SINDI SINDI JON JON JON JON JON JON JON JON JON JON JON this is where u place most of the dialogue :D ~yours truly
                (type, tileX, tileY) -> {
                    System.out.println("Triggered: " + type + " at " + tileX + ", " + tileY);

//                    if (type.equals("fight")) {
//                        loop.stop();
//                        Platform.runLater(() -> {
//                            try {
//                                combatScreen combat = new combatScreen();
//                                CombatController control = combat.getLoader().getController();
//                                control.setGameScreen(this);
//                                control.setStage(stage);
//                                stage.getScene().setRoot(combat.getRoot());
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
//                        });
//                    }
                    if (type.startsWith("dialogue:")) {
                        if(dialoguePlayed) { return;}
                        dialoguePlayed = true;
                        System.out.println("Dialogue triggered: " + type); // add this
                        String dialogueId = type.split(":")[1];
                        loop.stop();
                        Platform.runLater(() -> {
                            try {
                                DialoguesScreen dialogueScreen = new DialoguesScreen();
                                DialogueBoxController dController = dialogueScreen.getLoader().getController();
                                dController.setDialogueManager(dialogueManager);
                                dController.startDialogue(dialogueId);
                                dController.setGameScreen(this);
                                activeDialogue = dController;
                                activeDialogueNode = dialogueScreen.getRoot();
                                gameRoot.getChildren().add(activeDialogueNode);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }
                    if (type.equals("shop")) {
                        // TODO: shop team hooks here
                    }
                    if (type.equals("chest")) {
                        // TODO: chest team hooks here
                    }
                }
        );

        mapManager.loadMap("k3jviBossroom");

        System.out.println("Interact zones loaded:");
        for (InteractZone zone : mapManager.getCurrentMap().interactZones) {
            System.out.println(" - " + zone.type + " at " + zone.x + ", " + zone.y);
        }

        Map currentMap = mapManager.getCurrentMap();
        mapRenderer = new MapRenderer(currentMap, tilesetManager);
        player.setMap(currentMap);
        player.setPosition(
                currentMap.spawnX * TILE_SIZE * SCALE,
                currentMap.spawnY * TILE_SIZE * SCALE
        );



        pauseScreen ps = new pauseScreen(this, stage);
        Pane root = new Pane(canvas);
        root.setPrefSize(800, 600);
        root.getChildren().add(ps.getRoot());
        ps.getRoot().setVisible(false);
        this.pauseScreen = ps;
        this.gameRoot = root; // ← stores the root correctly now

        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) togglePause();
            else player.keyPressed(e.getCode());
        });
        canvas.setOnKeyReleased(e -> player.keyReleased(e.getCode()));

        return root;
    }

    public void returnFromCombat() {
        stage.getScene().setRoot(gameRoot);
        canvas.requestFocus();
        startLoop();
    }

    public void togglePause() {
        boolean nowPaused = !pauseScreen.getRoot().isVisible();
        pauseScreen.getRoot().setVisible(nowPaused);
        if (nowPaused) loop.stop();
        else loop.start();
    }

    public void startLoop() {
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    update();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                render();
            }
        };
        loop.start();
    }

    private void update() throws Exception {
        player.update();
        updateCamera();
        mapManager.checkInteractions(player.getTileX(), player.getTileY());

        if (activeDialogue != null && activeDialogue.isDialogueFinished()) {
            Platform.runLater(() -> {
                gameRoot.getChildren().remove(activeDialogueNode);
                activeDialogue = null;
                activeDialogueNode = null;
                canvas.requestFocus();
                loop.start();

            });
        }
    }

    private void updateCamera() {
        Map map = mapManager.getCurrentMap();
        cameraX = player.getX() - canvas.getWidth() / 2;
        cameraY = player.getY() - canvas.getHeight() / 2;

        double mapW = map.width * TILE_SIZE * SCALE;
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

    public void resumeFromDialogue() {
        Platform.runLater(() -> {
            gameRoot.getChildren().remove(activeDialogueNode);
            activeDialogue = null;
            activeDialogueNode = null;
            canvas.requestFocus();
            loop.start();
        });
    }
}
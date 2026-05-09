package com.dungeons.screens;

//COMBAT
import com.dungeons.Controllers.CombatController;

// DIALOGUE
import com.dungeons.Controllers.DialogueBoxController;
import com.dungeons.MusicandSoundsCode.GameMusicManager;
import com.dungeons.dialogueManager.DialogueManager;

//MAP
import com.dungeons.systems.Player;
import com.dungeons.world.Map;
import com.dungeons.world.MapManager;
import com.dungeons.world.MapRenderer;
import com.dungeons.world.TilesetManager;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class GameScreen {

    private static final int TILE_SIZE = 16;
    private static final int SCALE = 3;

    // ── PANES ──────────────────────────────────────────────
    private Pane gamePane;       // canvas lives here — the actual game
    private Pane uiOverlayA;
    private Pane uiPane;         // dialogue, credits, combat overlays
    private Pane secondUIPane;   // shop and chest overlays
    private Pane combatPane;
    private Pane escapePane;     // pause screen only
    private StackPane gameRoot;  // master root — stacks all four panes

    // ── SCREENS ────────────────────────────────────────────
    private shopScreen shopScreen;
    private creditsScreen creditsScreen;
    private itemPickupScreen itemPickupScreen;
    private pauseScreen pauseScreen;
    private UIscreen uiSkreen;
    private Stage stage;
    private static GameScreen instance;


    private Parent shopNode;
    private Parent chestNode;

    // ── CANVAS ─────────────────────────────────────────────
    private final Canvas canvas = new Canvas(1280, 720);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    // ── MAP ────────────────────────────────────────────────
    private final TilesetManager tilesetManager = new TilesetManager();
    private MapManager mapManager;
    private MapRenderer mapRenderer;

    // ── PLAYER ─────────────────────────────────────────────
    private final Player player = new Player(0, 0);
    private final DialogueManager dialogueManager = new DialogueManager();

    // ── CAMERA ─────────────────────────────────────────────
    private double cameraX = 0;
    private double cameraY = 0;

    // ── STATE ──────────────────────────────────────────────
    private int fightTileX;
    private int fightTileY;
    private AnimationTimer loop;
    private boolean interactionLocked = false;

    // ── DIALOGUE ───────────────────────────────────────────
    private DialogueBoxController activeDialogue = null;
    private Parent activeDialogueNode = null;
    private int lastDialogueTileX = -1;
    private int lastDialogueTileY = -1;

    public GameScreen() {
        instance = this;
    }

    public static GameScreen getInstance() {
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Parent getRoot() throws IOException {

        tilesetManager.loadAll();
        dialogueManager.load();

        // ── BUILD PANES ────────────────────────────────────
        // I plan on adding an inventory pane too!
        gamePane     = new Pane(canvas);           // GAME
        uiPane       = new Pane();                 // DIALOUGE CREDITS AND OVERALL UI
        uiOverlayA   = new Pane();
        secondUIPane = new Pane();                 // SHOP AND CHEST
        combatPane   = new Pane();                 // FOR LOADING COMBAT
        escapePane   = new Pane();                 // PAUSE

        for (Pane p : new Pane[]{gamePane,uiOverlayA, uiPane, secondUIPane, combatPane, escapePane}) {
            p.setPrefSize(1280, 720);
            p.setPickOnBounds(false); // transparent panes don't block mouse
        }

        // UI OVERLAY HEALTH N STUFF IDK
        UIscreen uIscreen = new UIscreen(this, stage);
        uiPane.getChildren().add(uIscreen.getRoot());
        uIscreen.getRoot().setVisible(true);
        this.uiSkreen = uIscreen;

        // ── PAUSE SCREEN → escapePane ──────────────────────
        pauseScreen ps = new pauseScreen(this, stage);
        escapePane.getChildren().add(ps.getRoot());
        ps.getRoot().setVisible(false);
        this.pauseScreen = ps;

        // ── STACK ALL PANES ────────────────────────────────
        // order = bottom to top: game → ui → secondUI → escape
        gameRoot = new StackPane(gamePane, uiPane, secondUIPane, escapePane);
        gameRoot.setPrefSize(1280, 720);


        // ── MAP MANAGER ────────────────────────────────────
        mapManager = new MapManager(
                tilesetManager,

                // map changed
                (newMap, spawnX, spawnY) -> {
                    mapRenderer = new MapRenderer(newMap, tilesetManager);
                    player.setMap(newMap);
                    player.setPosition(
                            spawnX * TILE_SIZE * SCALE,
                            spawnY * TILE_SIZE * SCALE
                    );
                    System.out.println("Map changed! Spawn: " + spawnX + ", " + spawnY);
                },

                // interact trigger
                (type, tileX, tileY) -> {
                    System.out.println("Triggered: " + type + " at " + tileX + ", " + tileY);

                    // ── FIGHT → switches scene to combat ──
                    if (type.equals("fight")) {
                        fightTileX = tileX;
                        fightTileY = tileY;
                        interactionLocked = true;
                        loop.stop();
                        Platform.runLater(() -> {
                            try {
                                combatScreen combat = new combatScreen();
                                CombatController control = combat.getLoader().getController();
                                String bossId = resolveBossFromCurrentMap();
                                stage.getScene().setRoot(combat.getRoot());
                                control.startCombat(bossId);

                                if (mapManager.isCurrentMap("BossRoomJoni")) {
                                    GameMusicManager.playFinalBoss();
                                } else {
                                    GameMusicManager.playCombat();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }

                    // ── SHOP → secondUIPane ───────────────
                    if (type.equals("shop")) {
                        shopScreen shop = new shopScreen(this, stage);
                        shopNode = shop.getRoot();
                        secondUIPane.getChildren().add(shopNode);
                        secondUIPane.setVisible(true);
                        this.shopScreen = shop;

                    }

                    // ── CHEST → secondUIPane ──────────────

                    if (type.equals("chest")) {
                        itemPickupScreen chest = new itemPickupScreen(this, stage);
                        chestNode = chest.getRoot();
                        secondUIPane.getChildren().add(chestNode);
                        secondUIPane.setVisible(true);
                        this.itemPickupScreen = chest;
                    }


                    //Put the callers for chest and shop outside to stop repeating code dattebayo
                    // in getRoot() — set ONCE, outside the interact listener
                    // ── CHANGED: moved E key logic here, removed duplicate from interact lambda ──
                    canvas.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ESCAPE) {
                            togglePause();
                        } else if (e.getCode() == KeyCode.E) {
                            if (shopNode != null && !shopNode.isVisible() && mapManager.isCurrentMap("ShopRoom")) {
                                shopNode.setVisible(true);
                                secondUIPane.setVisible(true);
                            } else if (chestNode != null && !chestNode.isVisible() && mapManager.isCurrentMap("ChestRoom")) {
                                chestNode.setVisible(true);
                                secondUIPane.setVisible(true);
                            } else if (shopNode != null && shopNode.isVisible()) {
                                shopNode.setVisible(false);
                                secondUIPane.setVisible(false);
                            } else if (chestNode != null && chestNode.isVisible()) {
                                chestNode.setVisible(false);
                                secondUIPane.setVisible(false);
                            }
                        } else {
                            player.keyPressed(e.getCode());
                        }
                    });
                    canvas.setOnKeyReleased(e -> player.keyReleased(e.getCode()));

                    // ── CREDITS → uiPane ──────────────────
                    if (type.equals("credits")) {
                        creditsScreen creditsscreen = new creditsScreen(this, stage);
                        Parent credits = creditsscreen.getRoot();
                        uiPane.getChildren().add(credits);
                        this.creditsScreen = creditsscreen;
                        GameMusicManager.playEnding();
                    }

                    // ── DIALOGUE → uiPane ─────────────────
                    if (type.startsWith("dialogue:")) {
                        lastDialogueTileX = tileX;
                        lastDialogueTileY = tileY;
                        interactionLocked = true;
                        loop.stop();

                        String dialogueId = type.split(":")[1];
                        Platform.runLater(() -> {
                            try {
                                DialoguesScreen dialogueScreen = new DialoguesScreen();
                                DialogueBoxController dController = dialogueScreen.getLoader().getController();
                                dController.setDialogueManager(dialogueManager);
                                dController.setOnFinished(() -> {
                                    uiPane.getChildren().remove(activeDialogueNode);
                                    mapManager.markDialogueDone(lastDialogueTileX, lastDialogueTileY);
                                    activeDialogue = null;
                                    activeDialogueNode = null;
                                    interactionLocked = false;
                                    canvas.requestFocus();
                                    loop.start();
                                });
                                dController.startDialogue(dialogueId);
                                activeDialogue = dController;
                                activeDialogueNode = dialogueScreen.getRoot();
                                uiPane.getChildren().add(activeDialogueNode);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }
                }
        );

        // ── LOAD STARTING MAP ──────────────────────────────
        mapManager.loadMap("MobRoom1");
        Map currentMap = mapManager.getCurrentMap();
        mapRenderer = new MapRenderer(currentMap, tilesetManager);
        player.setMap(currentMap);
        player.setPosition(
                currentMap.spawnX * TILE_SIZE * SCALE,
                currentMap.spawnY * TILE_SIZE * SCALE
        );

        // ── INPUT ──────────────────────────────────────────
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) togglePause();
            else player.keyPressed(e.getCode());
        });
        canvas.setOnKeyReleased(e -> player.keyReleased(e.getCode()));

        return gameRoot;
    }
    
    public void returnFromCombat() {
        mapManager.markFightDone(fightTileX, fightTileY);
        interactionLocked = false;
        stage.getScene().setRoot(gameRoot);
        player.clearInput();
        canvas.requestFocus();
        GameMusicManager.playGameplay();
        startLoop();
    }

    public void returnFromCombatWithMap(String nextMapName) {
        mapManager.loadMap(nextMapName);
        mapManager.markFightDone(fightTileX, fightTileY);
        interactionLocked = false;
        stage.getScene().setRoot(gameRoot);
        player.clearInput();
        canvas.requestFocus();
        GameMusicManager.playGameplay();
        startLoop();
    }

    // ── PAUSE ──────────────────────────────────────────────

    public void togglePause() {
        boolean nowPaused = !pauseScreen.getRoot().isVisible();
        pauseScreen.getRoot().setVisible(nowPaused);

        // escapePane absorbs input when paused
        escapePane.setPickOnBounds(nowPaused);

        if (nowPaused) loop.stop();
        else {
            loop.start();
            canvas.requestFocus();
        }
    }

    // ── GAME LOOP ──────────────────────────────────────────

    public void startLoop() {
        if (loop != null) loop.stop();
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
        if (!interactionLocked) {
            mapManager.checkInteractions(player.getTileX(), player.getTileY());
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

    // ── BOSS RESOLVER ──────────────────────────────────────

    private String resolveBossFromCurrentMap() {
        String name = mapManager.getCurrentMap().getMapName();
        if (name == null) return "CassieYarn";
        switch (name) {
            case "k3jviBossroom": return "CassieYarn";
            case "RoomKledi":     return "FreakyRelah";
            case "BossRoomJoni":  return "JohnMKati";
            case "MobRoom1":      return "Mob1";
            case "MobRoom2":      return "Mob2";
            case "MobRoom3":      return "Mob3";
            case "MobRoom4":      return "Mob4";
            case "MobRoom5":      return "Mob5";
            default:              return "CassieYarn";
        }
    }
}
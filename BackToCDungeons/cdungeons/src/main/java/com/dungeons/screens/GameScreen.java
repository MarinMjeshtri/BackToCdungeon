package com.dungeons.screens;

//COMBAT
import com.dungeons.Controllers.*;

//Inventory for 1234
import com.dungeons.shopItemsManager.PlayerInventory;

// DIALOGUE
import com.dungeons.MusicandSoundsCode.GameMusicManager;
import com.dungeons.animations.premadeAnimation;
import com.dungeons.dialogueManager.DialogueManager;

//MAP
import com.dungeons.systems.CombatSystem.PlayerProgress;
import com.dungeons.systems.Player;
import com.dungeons.world.Map;
import com.dungeons.world.MapManager;
import com.dungeons.world.MapRenderer;
import com.dungeons.world.TilesetManager;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
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
    private Pane gamePane;
    private Pane uiOverlayPane;
    private Pane uiPane;
    private Pane secondUIPane;
    private Pane combatPane;
    private Pane escapePane;
    private Pane actualCombatPane;
    private StackPane gameRoot;
    private roomTransitionScreen transitionScreen;

    // ── SCREENS ────────────────────────────────────────────
    private shopScreen shopScreen;
    private creditsScreen creditsScreen;
    private itemPickupScreen itemPickupScreen;
    private pauseScreen pauseScreen;
    private Stage stage;
    private static GameScreen instance;
    private gameoverScreen gameoverScreen;
    private uiOverlayScreen uiOverlaySkreen;
    private uiOverlayController overlayController;
    private victoryScreen victoryScreen;
    private boolean triggerEyesActive = false;


    private Parent shopNode;
    private Parent chestNode;
    private Parent scaryNode;
    private Parent combatNode;

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


    // Called when player presses 1-4 while walking.
    // Heal items restore HP. ATK items print a message and are not consumed.
    // After use, HUD overlay is updated to reflect the empty slot.
    private void useWalkingItem(int slotIndex) {
        String result = PlayerInventory.getInstance().useItemOutsideCombat(slotIndex);
        System.out.println("[Item] " + result);
        // update HUD so the slot goes blank if item was consumed
        if (overlayController != null) overlayController.updateUI();
    }

    public GameScreen() {
        instance = this;
    }

    public static GameScreen getInstance() {
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    //Getter for the overlay so that I can update the inventory
    public uiOverlayController getOverlayController() {
        return overlayController;
    }

    public Parent getRoot() throws IOException {

        tilesetManager.loadAll();
        dialogueManager.load();
        Map.generateChain();
        dialogueManager.load();

        // ── BUILD PANES ────────────────────────────────────
        gamePane     = new Pane(canvas);
        uiOverlayPane = new Pane();
        uiPane       = new Pane();
        secondUIPane = new Pane();
        combatPane   = new Pane();
        escapePane   = new Pane();
        actualCombatPane = new Pane();

        for (Pane p : new Pane[]{gamePane,combatPane, uiPane, secondUIPane, escapePane,actualCombatPane}) {
            p.setPrefSize(1280, 720);
            p.setPickOnBounds(false);
        }

        // ── PAUSE SCREEN → escapePane ──────────────────────
        pauseScreen ps = new pauseScreen(this, stage);
        escapePane.getChildren().add(ps.getRoot());
        ps.getRoot().setVisible(false);
        this.pauseScreen = ps;

        // OVERLAY
        uiOverlayScreen ovalay = new uiOverlayScreen(this, stage);
        uiOverlayPane.getChildren().add(ovalay.getRoot());
        ovalay.getRoot().setVisible(true);
        this.uiOverlaySkreen = ovalay;

        uiOverlayController ctrl = uiOverlaySkreen.getLoader().getController();
        ctrl.setProgress(PlayerProgress.getInstance());
        overlayController = ctrl; // add this



        // TRANSITION
        roomTransitionScreen transaction = new roomTransitionScreen(this, stage);
        escapePane.getChildren().add(transaction.getRoot());
        transaction.getRoot().setVisible(false);
        transaction.getRoot().setPickOnBounds(false);
        this.transitionScreen = transaction;

        // ── STACK ALL PANES ────────────────────────────────
        gameRoot = new StackPane(gamePane,combatPane,uiOverlayPane, uiPane, secondUIPane,actualCombatPane, escapePane);
        gameRoot.setPrefSize(1280, 720);

        // ── MAP MANAGER ────────────────────────────────────
        mapManager = new MapManager(
                tilesetManager,

                // map changed

                (newMap, spawnX, spawnY) -> {
                    roomScreenController controller = transitionScreen.getLoader().getController();
                    controller.setMapManager(mapManager);
                    controller.updateScreen();
                    premadeAnimation.showFor(transitionScreen.getRoot(), 1);

                    mapRenderer = new MapRenderer(newMap, tilesetManager);
                    player.setMap(newMap);
                    player.setPosition(
                            spawnX * TILE_SIZE * SCALE - Player.HITBOX_OFFSET_X,
                            spawnY * TILE_SIZE * SCALE - Player.HITBOX_OFFSET_Y
                    );
                    System.out.println("Map changed! Spawn: " + spawnX + ", " + spawnY);
                },

                // interact trigger
                (type, tileX, tileY) -> {
                    System.out.println("Triggered: " + type + " at " + tileX + ", " + tileY);



                    if (type.equals("triggerEyes")) {

                        if (triggerEyesActive) return;

                        triggerEyesActive = true;

                        handleTriggerEyes();
                    }

                    // ── FIGHT ─────────────────────────────
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

                                // ← add to combatPane instead of replacing scene
                                combatNode = combat.getRoot();
                                combatPane.getChildren().clear();
                                actualCombatPane.getChildren().add(combatNode);
                                actualCombatPane.setVisible(true);

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

                    // ── SHOP ──────────────────────────────
                    if (type.equals("shop")) {
                        shopScreen shop = new shopScreen(this, stage);
                        shopNode = shop.getRoot();
                        secondUIPane.getChildren().add(shopNode);
                        secondUIPane.setVisible(true);
                        this.shopScreen = shop;
                    }

                    // ── CHEST ─────────────────────────────
                    if (type.equals("chest")) {
                        itemPickupScreen chest = new itemPickupScreen(this, stage);
                        chestNode = chest.getRoot();
                        secondUIPane.getChildren().add(chestNode);
                        secondUIPane.setVisible(true);
                        this.itemPickupScreen = chest;
                    }


                    // ── CREDITS ───────────────────────────
                    if (type.equals("credits")) {
                        creditsScreen creditsscreen = new creditsScreen(this, stage);
                        Parent credits = creditsscreen.getRoot();
                        uiPane.getChildren().add(credits);
                        this.creditsScreen = creditsscreen;
                        GameMusicManager.playEnding();
                    }

                    // ── DIALOGUE ──────────────────────────
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

        // ── INPUT ──────────────────────────────────────────
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                togglePause();

            } else if (e.getCode() == KeyCode.DIGIT1) { useWalkingItem(0); }
                else if (e.getCode() == KeyCode.DIGIT2) { useWalkingItem(1); }
                else if (e.getCode() == KeyCode.DIGIT3) { useWalkingItem(2); }
                else if (e.getCode() == KeyCode.DIGIT4) { useWalkingItem(3);

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

        // ── LOAD STARTING MAP ──────────────────────────────
        mapManager.loadMap(Map.getStartRoom());
        Map currentMap = mapManager.getCurrentMap();
        mapRenderer = new MapRenderer(currentMap, tilesetManager);
        player.setMap(currentMap);
        player.setPosition(
                currentMap.spawnX * TILE_SIZE * SCALE - Player.HITBOX_OFFSET_X,
                currentMap.spawnY * TILE_SIZE * SCALE - Player.HITBOX_OFFSET_Y
        );

        roomScreenController controller = transitionScreen.getLoader().getController();
        premadeAnimation.showFor(transitionScreen.getRoot(), 1);


        return gameRoot;
    }

    // ── COMBAT RETURN ──────────────────────────────────────

    public void returnFromCombat() {
        uiOverlayController ctrl = uiOverlaySkreen.getLoader().getController();
        ctrl.setProgress(PlayerProgress.getInstance());
        mapManager.markFightDone(fightTileX, fightTileY);
        interactionLocked = false;

        // clear actualCombatPane not combatPane
        actualCombatPane.getChildren().clear();
        actualCombatPane.setVisible(false);

        player.clearInput();
        canvas.requestFocus();
        startLoop();
    }

    public void returnFromCombatWithMap(String nextMapName) {
        mapManager.loadMap(nextMapName);
        mapManager.markFightDone(fightTileX, fightTileY);
        interactionLocked = false;

        // same fix here
        actualCombatPane.getChildren().clear();
        actualCombatPane.setVisible(false);

        player.clearInput();
        canvas.requestFocus();
        startLoop();
    }

    // ── PAUSE ──────────────────────────────────────────────

    public void togglePause() {
        boolean nowPaused = !pauseScreen.getRoot().isVisible();
        pauseScreen.getRoot().setVisible(nowPaused);
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

    public void showGameOver() {
        try {
            gameoverScreen gameOver = new gameoverScreen();
            Parent gameOverNode = gameOver.getRoot();

            // add to escapePane on top of everything
            escapePane.getChildren().add(gameOverNode);
            escapePane.setVisible(true);
            escapePane.setPickOnBounds(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // CORRECT
    public void showVictoryScreen() {
        try {
            victoryScreen victory = new victoryScreen(this, stage);
            victoryScreenController controller = victory.getLoader().getController();
            controller.setMapManager(mapManager);
            Parent victoryNode = victory.getRoot();

            // add to uiPane
            uiPane.getChildren().add(victoryNode);
            uiPane.setVisible(true);
            uiPane.setPickOnBounds(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            case "MobRoom2":      return "Mob1";
            case "MobRoom3":      return "Mob1";
            case "MobRoom4":      return "Mob1";
            case "MobRoom5":      return "Mob1";
            default:              return "CassieYarn";
            //crasy okay
        }
    }

    private void handleTriggerEyes() {
        try {
            scaryScreen scary = new scaryScreen(this, stage);
            scaryNode = scary.getRoot();
            combatPane.getChildren().add(scaryNode);

            // second canvas just for player, sits on top of scaryScreen
            Canvas playerOverlay = new Canvas(1280, 720);
            playerOverlay.setMouseTransparent(true);
            combatPane.getChildren().add(playerOverlay);
            GraphicsContext pgc = playerOverlay.getGraphicsContext2D();

            new AnimationTimer() {
                public void handle(long now) {
                    pgc.clearRect(0, 0, 1280, 720);
                    pgc.save();
                    pgc.translate(-cameraX, -cameraY);
                    player.render(pgc);
                    pgc.restore();
                }
            }.start();

            combatPane.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
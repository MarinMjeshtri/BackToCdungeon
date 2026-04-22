package com.dungeons.screens;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dungeons.systems.Player;
import com.dungeons.systems.SpriteSheet;
import com.dungeons.world.CollisionMap;
import com.dungeons.world.TiledMapLoader;
import com.dungeons.world.TilesetInfo;

import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameScreen {

    // --- Screen setup ---
    private final Canvas canvas = new Canvas(800, 600);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    // --- Map ---
    // Load all layers from the tmx file once at startup
    private final Map<String, int[][]> layers = TiledMapLoader.loadAllLayers("/maps/k3jviBossroom.tmx");
    private final List<TilesetInfo> tilesetInfos = TiledMapLoader.loadTilesets("/maps/k3jviBossroom.tmx");
    private final Map<String, SpriteSheet> tilesets = new HashMap<>();

    // Which layers to draw (in order, bottom to top)
    private static final String[] RENDER_LAYERS = { "Floor", "FloorPlus", "Decorations" };

    // --- Tile sizing ---
    private static final int    TILE_SIZE = 16;   // px per tile in the source image
    private static final double SCALE     = 1.5;  // how much to scale up on screen

    // --- Map dimensions (in tiles) ---
    private static final int MAP_COLS = 30;
    private static final int MAP_ROWS = 20;

    // --- Collision ---
    // Builds a true/false grid from any layer with "COLLISION" in its name
    private final CollisionMap collisionMap = new CollisionMap(layers);

    // --- Player ---
    private final Player player = new Player(150, 100);

    // --- Camera ---
    // Offset so the view follows the player around the map
    private double cameraX = 0;
    private double cameraY = 0;

    // --- Game loop ---
    private AnimationTimer loop;

    public Parent getRoot() {

        // Load each tileset PNG into memory
        for (TilesetInfo info : tilesetInfos) {
            try {
                tilesets.put(info.imagePath,
                        new SpriteSheet("/sprites/" + info.imagePath, TILE_SIZE));
            } catch (Exception e) {
                System.out.println("Could not load tileset: " + info.imagePath);
            }
        }

        // Give the player the collision map so it can check before moving
        player.setCollisionMap(collisionMap);

        // Add canvas to a Pane and return it as the scene root
        Pane root = new Pane(canvas);
        root.setPrefSize(800, 600);

        // Keyboard input
        canvas.setFocusTraversable(true);
        canvas.requestFocus();
        canvas.setOnKeyPressed(e  -> player.keyPressed(e.getCode()));
        canvas.setOnKeyReleased(e -> player.keyReleased(e.getCode()));

        return root;
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
        updateCamera();
    }

    // Keep the camera centered on the player, clamped to map edges
    private void updateCamera() {
        cameraX = player.getX() - canvas.getWidth()  / 2;
        cameraY = player.getY() - canvas.getHeight() / 2;

        double mapW = MAP_COLS * TILE_SIZE * SCALE;
        double mapH = MAP_ROWS * TILE_SIZE * SCALE;

        cameraX = Math.max(0, Math.min(cameraX, mapW - canvas.getWidth()));
        cameraY = Math.max(0, Math.min(cameraY, mapH - canvas.getHeight()));
    }

    // Called every frame — draw everything
    private void render() {
        // Clear last frame
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setImageSmoothing(false); // keep pixel art crisp

        // Shift everything by camera offset so the world scrolls
        gc.save();
        gc.translate(-cameraX, -cameraY);

        // Draw map layers in order
        for (String layerName : RENDER_LAYERS) {
            int[][] layer = layers.get(layerName);
            if (layer != null) drawLayer(layer);
        }

        // Draw player on top of the map
        player.render(gc);

        gc.restore(); // reset camera offset (HUD would go after this)
    }

    // Draw one tile layer
    private void drawLayer(int[][] layer) {
        for (int row = 0; row < layer.length; row++) {
            for (int col = 0; col < layer[row].length; col++) {
                int gid = layer[row][col];
                if (gid <= 0) continue; // 0 = empty tile, skip

                // Find which tileset this tile belongs to
                TilesetInfo info = getTilesetForGid(gid);
                if (info == null) continue;

                SpriteSheet sheet = tilesets.get(info.imagePath);
                if (sheet == null) continue;

                // Convert global tile ID to local row/col within that tileset
                int localId = gid - info.firstGid;
                int tileCol = localId % info.columns;
                int tileRow = localId / info.columns;

                sheet.draw(gc, tileCol, tileRow,
                        col * TILE_SIZE * SCALE,  // screen X
                        row * TILE_SIZE * SCALE,  // screen Y
                        SCALE);
            }
        }
    }

    // Find which tileset owns a given global tile ID
    // Tilesets are sorted by firstGid so the last one with firstGid <= gid wins
    private TilesetInfo getTilesetForGid(int gid) {
        TilesetInfo result = null;
        for (TilesetInfo info : tilesetInfos) {
            if (info.firstGid <= gid) result = info;
        }
        return result;
    }
}
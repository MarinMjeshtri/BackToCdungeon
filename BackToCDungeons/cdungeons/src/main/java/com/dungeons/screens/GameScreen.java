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

    private final Canvas canvas = new Canvas(800, 600);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    private final Map<String, int[][]> layers =
        TiledMapLoader.loadAllLayers("/maps/testroom.tmx");
    private final List<TilesetInfo> tilesetInfos =
        TiledMapLoader.loadTilesets("/maps/testroom.tmx");

    private final CollisionMap collisionMap = new CollisionMap(layers);
    private final Player player = new Player(100, 100);

    private final Map<String, SpriteSheet> tilesets = new HashMap<>();

    private static final int    TILE_SIZE = 16;
    private static final double SCALE     = 1.5;
    private static final int    MAP_COLS  = 30;
    private static final int    MAP_ROWS  = 20;

    private static final String[] RENDER_LAYERS = {
        "Floor",
        "FloorPlus",
        "Decorations"
    };

    private double cameraX = 0;
    private double cameraY = 0;
    private AnimationTimer loop;

    public Parent getRoot() {
        // load each tileset PNG
        for (TilesetInfo info : tilesetInfos) {
            String path = "/sprites/" + info.imagePath;
            try {
                tilesets.put(info.imagePath, new SpriteSheet(path, TILE_SIZE));
                System.out.println("Loaded tileset image: " + info.imagePath);
            } catch (Exception e) {
                System.out.println("Warning: could not load tileset: " + path);
            }
        }

        player.setCollisionMap(collisionMap);

        Pane root = new Pane();
        root.setPrefSize(800, 600);
        root.getChildren().add(canvas);

        canvas.setFocusTraversable(true);
        canvas.requestFocus();
        canvas.setOnKeyPressed(e  -> player.keyPressed(e.getCode()));
        canvas.setOnKeyReleased(e -> player.keyReleased(e.getCode()));

        return root;
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
    }

    private void updateCamera() {
        cameraX = player.getX() - canvas.getWidth()  / 2;
        cameraY = player.getY() - canvas.getHeight() / 2;

        double mapPixelWidth  = MAP_COLS * TILE_SIZE * SCALE;
        double mapPixelHeight = MAP_ROWS * TILE_SIZE * SCALE;

        cameraX = Math.max(0, Math.min(cameraX, mapPixelWidth  - canvas.getWidth()));
        cameraY = Math.max(0, Math.min(cameraY, mapPixelHeight - canvas.getHeight()));
    }

    private void render() {
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setImageSmoothing(false);

        gc.save();
        gc.translate(-cameraX, -cameraY);

        for (String layerName : RENDER_LAYERS) {
            int[][] layer = layers.get(layerName);
            if (layer == null) {
                System.out.println("Warning: layer not found: " + layerName);
                continue;
            }
            drawLayer(layer);
        }

        player.render(gc);
        gc.restore();
    }

    private void drawLayer(int[][] layer) {
        for (int row = 0; row < layer.length; row++) {
            for (int col = 0; col < layer[row].length; col++) {
                int gid = layer[row][col];
                if (gid <= 0) continue;

                TilesetInfo info = getTilesetForGid(gid);
                if (info == null) continue;

                SpriteSheet sheet = tilesets.get(info.imagePath);
                if (sheet == null) continue;

                int localId  = gid - info.firstGid;
                int tileCol  = localId % info.columns;
                int tileRow  = localId / info.columns;

                sheet.draw(
                    gc,
                    tileCol, tileRow,
                    col * TILE_SIZE * SCALE,
                    row * TILE_SIZE * SCALE,
                    SCALE
                );
            }
        }
    }

    private TilesetInfo getTilesetForGid(int gid) {
        TilesetInfo result = null;
        for (TilesetInfo info : tilesetInfos) {
            if (info.firstGid <= gid) {
                result = info;
            }
        }
        return result;
    }
}
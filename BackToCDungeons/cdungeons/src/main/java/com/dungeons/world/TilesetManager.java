package com.dungeons.world;

import javafx.scene.image.*;
import java.util.HashMap;

public class TilesetManager {

    private final HashMap<String, Image[]> tilesets = new HashMap<>();

    final int TILE_SIZE = 16;

    String path = "C:\\Users\\User\\BackToCdungeon\\BackToCDungeons\\cdungeons\\src\\main\\resources\\tiles\\";

    public void loadAll() {
        load("tilesFloor.png", "floor");
        load("tilesWalls.png", "walls");
        load("tilesStuff.png", "stuff");
        load("spriteSheet_tiledLiquids_16x16.png", "liquids");
        load("drCassieYarnSprite.png", "cassie");
        load("drFrekiRelahSprite.png", "freki");
    }

    private void load(String file, String key) {
        Image img = new Image(
                getClass().getResourceAsStream("/tiles/" + file));

        int cols = (int)(img.getWidth() / TILE_SIZE);
        int rows = (int)(img.getHeight() / TILE_SIZE);
        int tileCount = cols * rows;

        Image[] tiles = new Image[tileCount];

        for (int i = 0; i < tileCount; i++) {
            int x = (i % cols) * TILE_SIZE;
            int y = (i / cols) * TILE_SIZE;

            tiles[i] = new WritableImage(
                    img.getPixelReader(),
                    x, y,
                    TILE_SIZE,
                    TILE_SIZE
            );
        }

        tilesets.put(key, tiles);
    }

    public Image get(String tileset, int localId) {
        Image[] t = tilesets.get(tileset);
        if (t == null || localId < 0 || localId >= t.length) return null;
        return t[localId];
    }
}
package com.dungeons.world;

import javafx.scene.canvas.GraphicsContext;

public class MapRenderer {

    private static final int TILE_SIZE = 16;
    private static final int SCALE = 2; // was 2, must match Player and GameScreen

    private final Map map;
    private final TilesetManager tiles;

    public MapRenderer(Map map, TilesetManager tiles) {
        this.map = map;
        this.tiles = tiles;
    }

    public void render(GraphicsContext gc) {
        for (String layerName : map.layers.keySet()) {
            int[] layer = map.layers.get(layerName);
            drawLayer(gc, layer);
        }
    }

    private void drawLayer(GraphicsContext gc, int[] layer) {
        for (int y = 0; y < map.height; y++) {
            for (int x = 0; x < map.width; x++) {

                int raw = layer[y * map.width + x];
                Object[] resolved = map.resolveTile(raw);
                if (resolved == null) continue;

                String tilesetKey = (String) resolved[0];
                int localId       = (int)    resolved[1];

                javafx.scene.image.Image tile = tiles.get(tilesetKey, localId);
                if (tile == null) continue;

                gc.drawImage(
                        tile,
                        x * TILE_SIZE * SCALE,
                        y * TILE_SIZE * SCALE,
                        TILE_SIZE * SCALE,
                        TILE_SIZE * SCALE
                );
            }
        }
    }
}
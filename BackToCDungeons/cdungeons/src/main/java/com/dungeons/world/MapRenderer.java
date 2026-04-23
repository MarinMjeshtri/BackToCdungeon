package com.dungeons.world;

import javafx.scene.canvas.GraphicsContext;

public class MapRenderer {

    private final Map map;
    private final TilesetManager tiles;

    public MapRenderer(Map map, TilesetManager tiles) {
        this.map = map;
        this.tiles = tiles;
    }

    public void render(GraphicsContext gc) {

        for (String layerName : map.layers.keySet()) {

            int[] layer = map.layers.get(layerName);

            String tileset = getTileset(layerName);

            drawLayer(gc, layer, tileset);
        }
    }

    private void drawLayer(GraphicsContext gc, int[] layer, String tileset) {

        for (int y = 0; y < map.height; y++) {
            for (int x = 0; x < map.width; x++) {

                int id = layer[y * map.width + x];

                if (id == 0) continue;

                gc.drawImage(
                        tiles.get(tileset, id),
                        x * 16,
                        y * 16
                );
            }
        }
    }

    private String getTileset(String layerName) {

        layerName = layerName.toLowerCase();

        if (layerName.contains("floor")) return "floor";
        if (layerName.contains("wall") || layerName.contains("sides")) return "walls";
        if (layerName.contains("stuff") || layerName.contains("decoration")) return "stuff";
        if (layerName.contains("liquid")) return "liquids";

        return "floor";
    }
}
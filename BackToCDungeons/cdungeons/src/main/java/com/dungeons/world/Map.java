package com.dungeons.world;

import com.google.gson.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Map {

    public int width, height;

    public HashMap<String, int[]> layers = new HashMap<>();

    public ArrayList<int[]> collisionLayers = new ArrayList<>();

    String path = "C:\\Users\\User\\BackToCdungeon\\BackToCDungeons\\cdungeons\\src\\main\\resources\\maps\\";

    public void load(String mapName) {

        try {
            JsonObject json = JsonParser.parseReader(
                    new FileReader(path + mapName + ".json")
            ).getAsJsonObject();

            width = json.get("width").getAsInt();
            height = json.get("height").getAsInt();

            JsonArray jsonLayers = json.getAsJsonArray("layers");

            for (JsonElement el : jsonLayers) {

                JsonObject layer = el.getAsJsonObject();

                String name = layer.get("name").getAsString();
                JsonArray data = layer.getAsJsonArray("data");

                int[] arr = new int[data.size()];

                for (int i = 0; i < data.size(); i++) {
                    arr[i] = data.get(i).getAsInt();
                }

                layers.put(name, arr);

                if (name.toLowerCase().contains("collision")) {
                    collisionLayers.add(arr);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSolid(int x, int y) {

        if (x < 0 || y < 0 || x >= width || y >= height)
            return true;

        for (int[] layer : collisionLayers) {
            if (layer[y * width + x] != 0)
                return true;
        }

        return false;
    }
}
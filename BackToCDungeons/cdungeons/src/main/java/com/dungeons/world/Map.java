package com.dungeons.world;

import com.google.gson.*;
import java.io.FileReader;
import java.util.*;

public class Map {

    public int width, height;

    public LinkedHashMap<String, int[]> layers = new LinkedHashMap<>();
    public ArrayList<int[]> collisionLayers = new ArrayList<>();
    public TreeMap<Integer, String> tilesetRanges = new TreeMap<>();

    String path = "C:\\Users\\User\\BackToCdungeon\\BackToCDungeons\\cdungeons\\src\\main\\resources\\maps\\";

    public void load(String mapName) {
        try {
            JsonObject json = JsonParser.parseReader(
                    new FileReader(path + mapName + ".json")
            ).getAsJsonObject();

            width  = json.get("width").getAsInt();
            height = json.get("height").getAsInt();

            JsonArray tilesets = json.getAsJsonArray("tilesets");
            for (JsonElement el : tilesets) {
                JsonObject ts = el.getAsJsonObject();
                int firstgid = ts.get("firstgid").getAsInt();
                String source = ts.get("source").getAsString();
                String key = resolveTilesetKey(source);
                tilesetRanges.put(firstgid, key);
            }

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

    public Object[] resolveTile(int rawGid) {
        final int FLIP_MASK = 0x1FFFFFFF;
        int gid = rawGid & FLIP_MASK;
        if (gid <= 0) return null;

        Integer firstgid = tilesetRanges.floorKey(gid);
        if (firstgid == null) return null;

        String tilesetKey = tilesetRanges.get(firstgid);
        int localId = gid - firstgid;

        return new Object[]{ tilesetKey, localId };
    }

    private String resolveTilesetKey(String source) {
        source = source.toLowerCase();
        if (source.contains("floor"))   return "floor";
        if (source.contains("wall"))    return "walls";
        if (source.contains("stuff"))   return "stuff";
        if (source.contains("liquid"))  return "liquids";
        return "floor";
    }

    public boolean isSolid(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return true;
        for (int[] layer : collisionLayers) {
            if (layer[y * width + x] != 0) return true;
        }
        return false;
    }
}
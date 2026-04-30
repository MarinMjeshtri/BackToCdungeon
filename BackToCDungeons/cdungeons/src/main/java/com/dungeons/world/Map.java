package com.dungeons.world;

import com.google.gson.*;
import java.util.*;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Map {

    public int width, height;

    public LinkedHashMap<String, int[]> layers = new LinkedHashMap<>();
    public ArrayList<int[]> collisionLayers = new ArrayList<>();
    public TreeMap<Integer, String> tilesetRanges = new TreeMap<>();

    public ArrayList<TransitionZone> transitions = new ArrayList<>();
    public ArrayList<InteractZone> interactZones = new ArrayList<>();

    public int spawnX = 0, spawnY = 0;

    private static final HashMap<String, String> MAP_TRANSITION_CHAIN = new HashMap<>();
    static {
        MAP_TRANSITION_CHAIN.put("MobRoom1",      "MobRoom2");
        MAP_TRANSITION_CHAIN.put("MobRoom2",      "k3jviBossroom");
        MAP_TRANSITION_CHAIN.put("k3jviBossroom", "MobRoom3");
        MAP_TRANSITION_CHAIN.put("MobRoom3",      "MobRoom4");
        MAP_TRANSITION_CHAIN.put("MobRoom4",      "RoomKledi");
        MAP_TRANSITION_CHAIN.put("RoomKledi",     "MobRoom5");
        MAP_TRANSITION_CHAIN.put("MobRoom5",      "BossRoomJoni");
        MAP_TRANSITION_CHAIN.put("ShopRoom",      "RoomKledi");
        MAP_TRANSITION_CHAIN.put("ChestRoom",     "RoomKledi");
        MAP_TRANSITION_CHAIN.put("BossRoomJoni",  null);
    }

    private String currentMapName;

    public void load(String mapName) {
        this.currentMapName = mapName;

        try {
            InputStream is = Map.class.getResourceAsStream("/maps/" + mapName + ".json");

            if (is == null) {
                System.out.println("Map not found: /maps/" + mapName + ".json");
                return;
            }

            JsonObject json = JsonParser.parseReader(
                    new InputStreamReader(is)
            ).getAsJsonObject();

            width  = json.get("width").getAsInt();
            height = json.get("height").getAsInt();

            JsonArray tilesets = json.getAsJsonArray("tilesets");
            for (JsonElement el : tilesets) {
                JsonObject ts = el.getAsJsonObject();
                int firstgid = ts.get("firstgid").getAsInt();
                String source = ts.get("source").getAsString();
                tilesetRanges.put(firstgid, resolveTilesetKey(source));
            }

            JsonArray jsonLayers = json.getAsJsonArray("layers");
            for (JsonElement el : jsonLayers) {
                JsonObject layer = el.getAsJsonObject();
                String type = layer.has("type") ? layer.get("type").getAsString() : "";
                String name = layer.get("name").getAsString();

                if (type.equals("tilelayer")) {
                    loadTileLayer(layer, name);
                } else if (type.equals("objectgroup")) {
                    loadObjectLayer(layer, name);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTileLayer(JsonObject layer, String name) {
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

    private void loadObjectLayer(JsonObject layer, String name) {
        String nameLower = name.toLowerCase();
        System.out.println("Loading object layer: '" + name + "' -> lowercase: '" + nameLower + "'");
        JsonArray objects = layer.getAsJsonArray("objects");



        for (JsonElement objEl : objects) {
            JsonObject obj = objEl.getAsJsonObject();
            int tileX = (int)(obj.get("x").getAsFloat() / 16);
            int tileY = (int)(obj.get("y").getAsFloat() / 16);
            int rectW = (int)Math.ceil(obj.get("width").getAsFloat() / 16);
            int rectH = (int)Math.ceil(obj.get("height").getAsFloat() / 16);

            if (nameLower.equals("spawnpoint")) {
                spawnX = tileX;
                spawnY = tileY;

            } else if (nameLower.equals("transition")) {
                String target = MAP_TRANSITION_CHAIN.get(currentMapName);
                if (target != null) {
                    for (int ty = tileY; ty < tileY + rectH; ty++) {
                        for (int tx = tileX; tx < tileX + rectW; tx++) {
                            transitions.add(new TransitionZone(tx, ty, target, -1, -1));
                        }
                    }
                }

            } else if (nameLower.equals("transitionshoproom")) {
                for (int ty = tileY; ty < tileY + rectH; ty++) {
                    for (int tx = tileX; tx < tileX + rectW; tx++) {
                        transitions.add(new TransitionZone(tx, ty, "ShopRoom", -1, -1));
                    }
                }

            } else if (nameLower.equals("transitionchestroom")) {
                for (int ty = tileY; ty < tileY + rectH; ty++) {
                    for (int tx = tileX; tx < tileX + rectW; tx++) {
                        transitions.add(new TransitionZone(tx, ty, "ChestRoom", -1, -1));
                    }
                }

            } else if (nameLower.equals("fight")) {
                for (int ty = tileY; ty < tileY + rectH; ty++) {
                    for (int tx = tileX; tx < tileX + rectW; tx++) {
                        interactZones.add(new InteractZone(tx, ty, "fight"));
                    }
                }

            } else if (nameLower.equals("shop")) {
                for (int ty = tileY; ty < tileY + rectH; ty++) {
                    for (int tx = tileX; tx < tileX + rectW; tx++) {
                        interactZones.add(new InteractZone(tx, ty, "shop"));
                    }
                }

            } else if (nameLower.equals("chest")) {
                for (int ty = tileY; ty < tileY + rectH; ty++) {
                    for (int tx = tileX; tx < tileX + rectW; tx++) {
                        interactZones.add(new InteractZone(tx, ty, "chest"));
                    }
                }

            } else if (nameLower.equals("cassie_encounter")
                    || nameLower.equals("freki_encounter")
                    || nameLower.equals("merchant_enter")
                    || nameLower.equals("johnmkati_lab_reveal")) {
                for (int ty = tileY; ty < tileY + rectH; ty++) {
                    for (int tx = tileX; tx < tileX + rectW; tx++) {
                        // Use original name (not lowercased) to match dialogue JSON keys
                        interactZones.add(new InteractZone(tx, ty, "dialogue:" + nameLower));
                    }
                }
            }
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
        if (source.contains("floor"))        return "floor";
        if (source.contains("wall"))         return "walls";
        if (source.contains("stuff"))        return "stuff";
        if (source.contains("liquid"))       return "liquids";
        if (source.contains("drcassieyarn")) return "cassie";
        if (source.contains("drfrekirelah")) return "freki";
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

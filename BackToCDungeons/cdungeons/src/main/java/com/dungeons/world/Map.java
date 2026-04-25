package com.dungeons.world;

import com.google.gson.*;
import java.io.FileReader;
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

    String path = "C:\\Users\\User\\BackToCdungeon\\BackToCDungeons\\cdungeons\\src\\main\\resources\\maps\\";

    // Maps each object layer name to what target map it transitions to
    private static final HashMap<String, String> TRANSITION_TARGETS = new HashMap<>();
    static {
        TRANSITION_TARGETS.put("transition_mobroom1",     "MobRoom1");
        TRANSITION_TARGETS.put("transition_mobroom2",     "MobRoom2");
        TRANSITION_TARGETS.put("transition_k3jvibossroom","k3jviBossroom");
        TRANSITION_TARGETS.put("transition_mobroom3",     "MobRoom3");
        TRANSITION_TARGETS.put("transition_mobroom4",     "MobRoom4");
        TRANSITION_TARGETS.put("transition_roomkledi",    "RoomKledi");
        TRANSITION_TARGETS.put("transition_mobroom5",     "MobRoom5");
        TRANSITION_TARGETS.put("transition_bossroomjoni", "BossRoomJoni");
        TRANSITION_TARGETS.put("transitionshop",          "ShopRoom");
        TRANSITION_TARGETS.put("transitionchest",         "ChestRoom");
        TRANSITION_TARGETS.put("transition",              null); // resolved per map at runtime
    }

    // Which map each map's "Transition" layer leads to
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
        MAP_TRANSITION_CHAIN.put("BossRoomJoni",  null); // end of game
    }

    private String currentMapName;

    public void load(String mapName) {
        this.currentMapName = mapName;

        try {
            // load from classpath — no hardcoded path needed
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

            // Load tilesets
            JsonArray tilesets = json.getAsJsonArray("tilesets");
            for (JsonElement el : tilesets) {
                JsonObject ts = el.getAsJsonObject();
                int firstgid = ts.get("firstgid").getAsInt();
                String source = ts.get("source").getAsString();
                tilesetRanges.put(firstgid, resolveTilesetKey(source));
            }

            // Load layers
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
        JsonArray objects = layer.getAsJsonArray("objects");

        for (JsonElement objEl : objects) {
            JsonObject obj = objEl.getAsJsonObject();
            int tileX = (int)(obj.get("x").getAsFloat() / 16);
            int tileY = (int)(obj.get("y").getAsFloat() / 16);

            if (nameLower.equals("spawnpoint")) {
                spawnX = tileX;
                spawnY = tileY;

            } else if (nameLower.equals("transition")) {
                String target = MAP_TRANSITION_CHAIN.get(currentMapName);
                if (target != null) {
                    // Spawn at spawnpoint of target — resolved when target loads
                    transitions.add(new TransitionZone(tileX, tileY, target, -1, -1));
                }
                // If target is null (BossRoomJoni) we just don't add it — does nothing

            } else if (nameLower.equals("transitionshop")) {
                transitions.add(new TransitionZone(tileX, tileY, "ShopRoom", -1, -1));

            } else if (nameLower.equals("transitionchest")) {
                transitions.add(new TransitionZone(tileX, tileY, "ChestRoom", -1, -1));

            } else if (nameLower.equals("fight")) {
                interactZones.add(new InteractZone(tileX, tileY, "fight"));

            } else if (nameLower.equals("shop")) {
                interactZones.add(new InteractZone(tileX, tileY, "shop"));

            } else if (nameLower.equals("chest")) {
                interactZones.add(new InteractZone(tileX, tileY, "chest"));
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
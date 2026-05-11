package com.dungeons.world;

import com.google.gson.*;
import java.util.*;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Map {

    public int width, height;

    public LinkedHashMap<String, int[]> layers         = new LinkedHashMap<>();
    public ArrayList<int[]>            collisionLayers = new ArrayList<>();
    public TreeMap<Integer, String>    tilesetRanges   = new TreeMap<>();

    public ArrayList<TransitionZone> transitions  = new ArrayList<>();
    public ArrayList<InteractZone>   interactZones = new ArrayList<>();

    public int spawnX = 0, spawnY = 0;

    // persists across all object layers so every Tiled object gets a unique id
    private int nextObjId = 0;

    // ── CHAIN ──────────────────────────────────────────────
    private static final List<String> SEQUENCE = new ArrayList<>();
    private static int currentSequenceIndex = 0;
    private static String previousMap = null;
    private static int roomCounter = 1;

    public static int getRoomCounter() { return roomCounter; }

    private static final List<String> RANDOM_POOL = Arrays.asList(
            "MobRoom1", "MobRoom2", "MobRoom3", "MobRoom4", "MobRoom5"
            //"HealingRoom"
    );

    private static final int SPAWN_AFTER_CHEST_X = 15;
    private static final int SPAWN_AFTER_CHEST_Y = 6;
    private static final int SPAWN_AFTER_SHOP_X  = 15;
    private static final int SPAWN_AFTER_SHOP_Y  = 18;

    public String currentMapName;
    private static String startRoom = "MobRoom1";

    // ── GENERATE CHAIN ─────────────────────────────────────
    public static void generateChain() {
        SEQUENCE.clear();
        currentSequenceIndex = 0;
        previousMap = null;
        roomCounter = 1;

        List<String> block1 = randomBlock(4, null);
        SEQUENCE.addAll(block1);
        SEQUENCE.add("k3jviBossroom");
        SEQUENCE.add("ShopRoom");

        List<String> block2 = randomBlock(3, block1.get(block1.size() - 1));
        SEQUENCE.addAll(block2);
        SEQUENCE.add("RoomKledi");
        SEQUENCE.add("ChestRoom");

        List<String> block3 = randomBlock(2, block2.get(block2.size() - 1));
        SEQUENCE.addAll(block3);
        SEQUENCE.add("ShopRoom");
        SEQUENCE.add("BossRoomJoni");
        SEQUENCE.add("FinalRoom");

        startRoom = SEQUENCE.get(0);

        System.out.println("Generated map chain:");
        for (int i = 0; i < SEQUENCE.size(); i++) {
            System.out.println("  Room " + (i + 1) + ": " + SEQUENCE.get(i));
        }
        System.out.println("Full sequence: " + SEQUENCE);
    }

    // ── NEXT MAP RESOLVER ──────────────────────────────────
    public static String getNextMap(String currentMap) {
        if (currentMap.equals("ShopRoom") || currentMap.equals("ChestRoom")) {
            int peekIdx = currentSequenceIndex + 1;
            if (peekIdx < SEQUENCE.size()) return SEQUENCE.get(peekIdx);
            return null;
        }
        currentSequenceIndex++;
        roomCounter++;
        if (currentSequenceIndex < SEQUENCE.size())
            return SEQUENCE.get(currentSequenceIndex);
        return null;
    }

    // ── RECORD VISIT ───────────────────────────────────────
    public static void recordVisit(String mapName) {
        if (!mapName.equals("ShopRoom") && !mapName.equals("ChestRoom")) {
            previousMap = mapName;
        }
    }

    // ── RANDOM BLOCK ───────────────────────────────────────
    private static List<String> randomBlock(int count, String firstPrevious) {
        Random rng = new Random();
        List<String> result = new ArrayList<>();
        String last = firstPrevious;

        for (int i = 0; i < count; i++) {
            List<String> available = new ArrayList<>(RANDOM_POOL);
            if (last != null) available.remove(last);
            String pick = available.get(rng.nextInt(available.size()));
            result.add(pick);
            last = pick;
        }
        return result;
    }

    // ── LOAD ───────────────────────────────────────────────
    public void load(String mapName) {
        this.currentMapName = mapName;

        nextObjId = 0;
        layers.clear();
        collisionLayers.clear();
        tilesetRanges.clear();
        transitions.clear();
        interactZones.clear();
        spawnX = 0;
        spawnY = 0;

        try {
            InputStream is = Map.class.getResourceAsStream("/maps/" + mapName + ".json");
            if (is == null) {
                System.out.println("Map not found: /maps/" + mapName + ".json");
                return;
            }

            JsonObject json = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();

            width  = json.get("width").getAsInt();
            height = json.get("height").getAsInt();

            // ← ONLY THIS BLOCK CHANGED
            JsonArray tilesets = json.getAsJsonArray("tilesets");
            for (JsonElement el : tilesets) {
                JsonObject ts = el.getAsJsonObject();
                int firstgid = ts.get("firstgid").getAsInt();

                if (!ts.has("source")) {
                    if (ts.has("name")) {
                        tilesetRanges.put(firstgid, resolveTilesetKey(
                                ts.get("name").getAsString()
                        ));
                    }
                    continue;
                }

                String source = ts.get("source").getAsString();
                tilesetRanges.put(firstgid, resolveTilesetKey(source));
            }
            // ← EVERYTHING BELOW IS UNTOUCHED

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

    // ── TILE LAYER ─────────────────────────────────────────
    private void loadTileLayer(JsonObject layer, String name) {
        JsonArray data = layer.getAsJsonArray("data");
        int[] arr = new int[data.size()];
        for (int i = 0; i < data.size(); i++) arr[i] = data.get(i).getAsInt();
        layers.put(name, arr);
        if (name.toLowerCase().contains("collision")) collisionLayers.add(arr);
    }

    // ── OBJECT LAYER ───────────────────────────────────────
    private void loadObjectLayer(JsonObject layer, String name) {
        String nameLower = name.toLowerCase();
        JsonArray objects = layer.getAsJsonArray("objects");

        System.out.println("Loading object layer: '" + name + "' with " + objects.size() + " objects");

        for (JsonElement objEl : objects) {
            JsonObject obj = objEl.getAsJsonObject();
            int tileX = (int)(obj.get("x").getAsFloat() / 16);
            int tileY = (int)(obj.get("y").getAsFloat() / 16);
            int rectW = (int)Math.ceil(obj.get("width").getAsFloat()  / 16);
            int rectH = (int)Math.ceil(obj.get("height").getAsFloat() / 16);

            System.out.println("  Object '" + name + "' at tile (" + tileX + ", " + tileY +
                    ") size (" + rectW + "x" + rectH + ")");

            if (nameLower.equals("spawnpoint")) {
                spawnX = tileX;
                spawnY = tileY;
                System.out.println("Spawn: " + spawnX + ", " + spawnY);

            } else if (nameLower.equals("transition")) {
                String target = getNextMap(currentMapName);
                if (target != null) {
                    for (int ty = tileY; ty < tileY + rectH; ty++)
                        for (int tx = tileX; tx < tileX + rectW; tx++)
                            transitions.add(new TransitionZone(tx, ty, target, -1, -1));
                }

            } else if (nameLower.equals("transitionbackfromchest")) {
                String target = getNextMap(currentMapName);
                if (target != null) {
                    for (int ty = tileY; ty < tileY + rectH; ty++)
                        for (int tx = tileX; tx < tileX + rectW; tx++)
                            transitions.add(new TransitionZone(tx, ty, target, SPAWN_AFTER_CHEST_X, SPAWN_AFTER_CHEST_Y));
                }

            } else if (nameLower.equals("transitionbackfromshop")) {
                String target = getNextMap(currentMapName);
                if (target != null) {
                    for (int ty = tileY; ty < tileY + rectH; ty++)
                        for (int tx = tileX; tx < tileX + rectW; tx++)
                            transitions.add(new TransitionZone(tx, ty, target, SPAWN_AFTER_SHOP_X, SPAWN_AFTER_SHOP_Y));
                }
            } else if (nameLower.equals("triggerend")) {
                for (int ty = tileY; ty < tileY + rectH; ty++) {
                    for (int tx = tileX; tx < tileX + rectW; tx++) {
                        interactZones.add(new InteractZone(tx, ty, "triggerEnd", nextObjId));
                    }
                }
            } else if (nameLower.equals("transitionshoproom")) {
                // ... continue other checks else if (nameLower.equals("transitionchestroom")) {
                for (int ty = tileY; ty < tileY + rectH; ty++)
                    for (int tx = tileX; tx < tileX + rectW; tx++)
                        transitions.add(new TransitionZone(tx, ty, "ChestRoom", -1, -1));

            } else if (nameLower.equals("fight")) {
                for (int ty = tileY; ty < tileY + rectH; ty++)
                    for (int tx = tileX; tx < tileX + rectW; tx++)
                        interactZones.add(new InteractZone(tx, ty, "fight", nextObjId));

            } else if (nameLower.equals("shop")) {
                for (int ty = tileY; ty < tileY + rectH; ty++)
                    for (int tx = tileX; tx < tileX + rectW; tx++)
                        interactZones.add(new InteractZone(tx, ty, "shop", nextObjId));

            } else if (nameLower.equals("chest")) {
                for (int ty = tileY; ty < tileY + rectH; ty++)
                    for (int tx = tileX; tx < tileX + rectW; tx++)
                        interactZones.add(new InteractZone(tx, ty, "chest", nextObjId));

            } else if (nameLower.equals("triggereyes")) {
            for (int ty = tileY; ty < tileY + rectH; ty++) {
                for (int tx = tileX; tx < tileX + rectW; tx++) {
                    interactZones.add(new InteractZone(tx, ty, "triggerEyes", nextObjId));
                }
            }
        } else if (nameLower.equals("cassie_encounter")
                    || nameLower.equals("freki_encounter")
                    || nameLower.equals("merchant_enter")
                    || nameLower.equals("johnmkati_lab_reveal")) {
                for (int ty = tileY; ty < tileY + rectH; ty++)
                    for (int tx = tileX; tx < tileX + rectW; tx++)
                        interactZones.add(new InteractZone(tx, ty, "dialogue:" + name, nextObjId));

            }


            nextObjId++;
        }
    }

    // ── TILE RESOLVER ──────────────────────────────────────
    public Object[] resolveTile(int rawGid) {
        final int FLIP_MASK = 0x0FFFFFFF;
        int gid = rawGid & FLIP_MASK;
        if (gid <= 0) return null;

        Integer firstgid = tilesetRanges.floorKey(gid);
        if (firstgid == null) return null;

        return new Object[]{ tilesetRanges.get(firstgid), gid - firstgid };
    }

    private String resolveTilesetKey(String source) {
        source = source.toLowerCase();
        if (source.contains("floor"))        return "floor";
        if (source.contains("wall"))         return "walls";
        if (source.contains("stuff"))        return "stuff";
        if (source.contains("liquid"))       return "liquids";
        if (source.contains("drcassieyarn")) return "cassie";
        if (source.contains("drfrekirelah")) return "freki";
        if (source.contains("north"))        return "north";
        if (source.contains("southwest"))    return "south-west";
        if (source.contains("west"))         return "west";
        if (source.contains("south"))        return "south";
        if (source.contains("blackTile"))    return "blackTile";
        if (source.contains("joni"))         return "joni";
        return "floor";
    }

    // ── COLLISION ──────────────────────────────────────────
    public void clearLayer(String layerName) {
        int[] layer = layers.get(layerName);
        if (layer != null) Arrays.fill(layer, 0);
    }

    public boolean isSolid(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return true;
        for (int[] layer : collisionLayers) {
            if (layer[y * width + x] != 0) return true;
        }
        return false;
    }

    // ── GETTERS ────────────────────────────────────────────
    public static String getStartRoom() {
        currentSequenceIndex = 0;
        roomCounter = 1;
        return startRoom;
    }

    public String getMapName() { return currentMapName; }
}
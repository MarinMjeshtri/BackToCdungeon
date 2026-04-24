package com.dungeons.world;

public class MapManager {

    private Map currentMap;
    private final TilesetManager tilesets;

    private MapChangeListener mapChangeListener;
    private InteractListener interactListener;

    public interface MapChangeListener {
        void onMapChanged(Map newMap, int spawnX, int spawnY);
    }

    public interface InteractListener {
        // Called when player steps on a fight/shop/chest tile
        // type = "fight", "shop", "chest"
        void onInteract(String type, int tileX, int tileY);
    }

    public MapManager(TilesetManager tilesets,
                      MapChangeListener mapChangeListener,
                      InteractListener interactListener) {
        this.tilesets = tilesets;
        this.mapChangeListener = mapChangeListener;
        this.interactListener = interactListener;
    }

    public void loadMap(String mapName) {
        Map map = new Map();
        map.load(mapName);
        currentMap = map;
    }

    public Map getCurrentMap() {
        return currentMap;
    }

    // Call this every frame with character's tile position
    public void checkInteractions(int charTileX, int charTileY) {
        checkTransitions(charTileX, charTileY);
        checkInteractZones(charTileX, charTileY);
    }

    private void checkTransitions(int charTileX, int charTileY) {
        for (TransitionZone zone : currentMap.transitions) {
            if (zone.x == charTileX && zone.y == charTileY) {
                Map newMap = new Map();
                newMap.load(zone.targetMap);
                currentMap = newMap;

                if (mapChangeListener != null) {
                    // Use the spawnpoint defined in the target map
                    mapChangeListener.onMapChanged(newMap, newMap.spawnX, newMap.spawnY);
                }
                return;
            }
        }
    }

    private void checkInteractZones(int charTileX, int charTileY) {
        for (InteractZone zone : currentMap.interactZones) {
            if (zone.triggered) continue;
            if (zone.x == charTileX && zone.y == charTileY) {
                if (interactListener != null) {
                    interactListener.onInteract(zone.type, zone.x, zone.y);
                }
                // For shop and chest, mark immediately as triggered
                // For fight, character team calls markFightDone() when battle ends
                if (!zone.type.equals("fight")) {
                    zone.triggered = true;
                }
                return;
            }
        }
    }

    // Character team calls this when a fight is over at a given tile
    public void markFightDone(int tileX, int tileY) {
        for (InteractZone zone : currentMap.interactZones) {
            if (zone.type.equals("fight") && zone.x == tileX && zone.y == tileY) {
                zone.triggered = true;
                return;
            }
        }
    }
}
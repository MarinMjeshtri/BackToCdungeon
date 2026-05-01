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
                String triggeredType = zone.type;
                // Mark ALL zones of this type as triggered at once
                for (InteractZone z : currentMap.interactZones) {
                    if (z.type.equals(triggeredType)) {
                        z.triggered = true;
                    }
                }
                if (interactListener != null) {
                    interactListener.onInteract(zone.type, zone.x, zone.y);
                }
                return;
            }
        }
    }

    public void markFightDone(int tileX, int tileY) {
        for (InteractZone zone : currentMap.interactZones) {
            if (zone.type.equals("fight")) {
                zone.triggered = true;
            }
        }
    }

    public void markDialogueDone(int tileX, int tileY) {
        for (InteractZone zone : currentMap.interactZones) {
            if (zone.type.startsWith("dialogue:")) {
                zone.triggered = true;
            }
        }
    }
}
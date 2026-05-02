package com.dungeons.systems.CombatSystem;

//Bej importet per maps here Kled + TMX reader


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Reads TMX map trigger layers and maps them to boss IDs.
// In your map controller, call checkTrigger(playerTileX, playerTileY) on every player move.
// Returns the boss ID string if the player is standing in a trigger zone, null if not.
//
// To start a fight from your map controller:
//   String bossId = trigger.checkTrigger(x, y);
//   if (bossId != null) {
//       combatController.startCombat(bossId);
//       App.setRoot("combatScreen");
//   }
public class CombatTrigger {

    // layer name in Tiled -> boss ID in Stats.json
    private static final java.util.Map<String, String> LAYER_TO_BOSS = new HashMap<>();
    static {
        LAYER_TO_BOSS.put("FightCassie", "CassieYarn");
        LAYER_TO_BOSS.put("FightFreki",  "FreakyRelah");
        LAYER_TO_BOSS.put("FightJohn",   "JohnMKati");
    }

    private final java.util.Map<String, List<int[]>> triggerZones = new HashMap<>();
    private int tileWidth  = 32;
    private int tileHeight = 32;

    public CombatTrigger(String tmxPath) {
        loadFromTmx(tmxPath);
    }

    private void loadFromTmx(String tmxPath) {
        try {
            TMXMapReader reader = new TMXMapReader();
            Map map = reader.readMap(
                    getClass().getResource(tmxPath).toString());

            tileWidth  = map.getTileWidth();
            tileHeight = map.getTileHeight();

            for (MapLayer layer : map.getLayers()) {
                String layerName = layer.getName(); //Layername like FightCassie as u told me
                if (!LAYER_TO_BOSS.containsKey(layerName)) continue;
                if (!(layer instanceof ObjectGroup)) continue;

                ObjectGroup group = (ObjectGroup) layer;
                List<int[]> zones = new ArrayList<>();

                for (MapObject obj : group.getObjects()) {
                    int tx = (int)(obj.getX() / tileWidth);
                    int ty = (int)(obj.getY() / tileHeight);
                    int tw = (int)(obj.getWidth()  / tileWidth);
                    int th = (int)(obj.getHeight() / tileHeight);
                    zones.add(new int[]{tx, ty, tw, th});
                }

                triggerZones.put(layerName, zones);
            }

        } catch (Exception e) {
            System.out.println("CombatTrigger: failed to load TMX — " + e.getMessage());
        }
    }

    // call this every time the player moves on the map
    public String checkTrigger(int playerTileX, int playerTileY) {
        for (java.util.Map.Entry<String, List<int[]>> entry : triggerZones.entrySet()) {
            for (int[] zone : entry.getValue()) {
                if (playerTileX >= zone[0] && playerTileX < zone[0] + zone[2] &&
                    playerTileY >= zone[1] && playerTileY < zone[1] + zone[3]) {
                    return LAYER_TO_BOSS.get(entry.getKey());
                }
            }
        }
        return null;
    }
}
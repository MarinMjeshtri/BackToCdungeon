package com.dungeons.systems.items;

import com.google.gson.*;
import java.io.*;
import java.util.Map;

public class itemPicker {

    private Map<String, Items> loadedItems;

    public void load() {
        Gson gson = new Gson();

        InputStream is = getClass().getResourceAsStream("/Items/Items.json");

        if (is == null) {
            System.out.println("File not found!");
            return;
        }

        InputStreamReader reader = new InputStreamReader(is);
        itemData registry = gson.fromJson(reader, itemData.class);
        loadedItems = registry.items;

        // Assign the image path using the item's key (ID)
        for (Map.Entry<String, Items> entry : loadedItems.entrySet()) {
            String itemId = entry.getKey();
            Items item = entry.getValue();
            item.image = "/sprites/itemSprites/" + itemId + ".png";
        }

        // Debug
        for (Map.Entry<String, Items> entry : loadedItems.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue().image);
        }
    }

    public Items getItem(String id) {
        return loadedItems.get(id);
    }

    public Map<String, Items> getAllItems() {
        return loadedItems;
    }
}
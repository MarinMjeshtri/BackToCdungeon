package com.dungeons.shopItemsManager;


import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ShopManager {

    private ShopData shopData;

    public void load(){
        Gson gson = new Gson();
        InputStream is = getClass().getResourceAsStream("/ShopItems/shopItems.json");
        Reader reader = new InputStreamReader(is);
        shopData = gson.fromJson(reader, ShopData.class);
    }

    public Shop getItem(String key) {
        return shopData.shopItems.get(key);
    }


}

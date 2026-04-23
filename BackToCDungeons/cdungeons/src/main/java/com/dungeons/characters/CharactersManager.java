package com.dungeons.characters;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class CharactersManager {
    private Map<String, Characters> characters;
    public void load(){
        Gson gson = new Gson();

        InputStream is = getClass().getResourceAsStream("/CharacterStats/Stats.json");

        System.out.println("Character found!");

        InputStreamReader reader = new InputStreamReader(is);
        CharacterData data = gson.fromJson(reader, CharacterData.class);
        characters = data.characters;

        System.out.println("Characters loaded!: " + characters.size());

    }
    public Characters getCharacter(String id) {
        return characters.get(id);
    }

    public boolean hasCharacter(String id) {
        return characters.containsKey(id);
    }
}

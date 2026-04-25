package com.dungeons.systems.CombatSystem; 

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StatsLoader {

    private static final String STATS_FILE = "/CharacterStats/Stats.json";
    private String raw;

    public StatsLoader() {
        try (InputStream is = StatsLoader.class.getResourceAsStream(STATS_FILE)) {
            if (is == null) throw new RuntimeException("Stats.json not found at: " + STATS_FILE);
            raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Stats.json: " + e.getMessage());
        }
    }

    
    public Player loadPlayer(String characterName) {
        String block = extractCharacterBlock(characterName);

        String statsBlock = extractBlock(block, "\"stats\"");
        Player player = new Player();
        player.setName(characterName);
        player.setMaxHp(extractInt(statsBlock, "hp"));
        player.setCurrentHp(extractInt(statsBlock, "hp"));
        player.setAttack(extractInt(statsBlock, "atk"));
        player.setDefense(extractInt(statsBlock, "def"));
        player.setMoves(parseAbilities(block));
        player.setItems(new ArrayList<>());
        return player;
    }

    public BossLoader loadBoss(String characterName) {
        String block = extractCharacterBlock(characterName);

        String statsBlock = extractBlock(block, "\"stats\"");
        BossLoader bossLoader = new BossLoader();
        bossLoader.setId(characterName);
        bossLoader.setName(characterName);
        bossLoader.setTitle("");
        bossLoader.setMaxHp(extractInt(statsBlock, "hp"));
        bossLoader.setCurrentHp(extractInt(statsBlock, "hp"));
        bossLoader.setAttack(extractInt(statsBlock, "atk"));
        bossLoader.setDefense(extractInt(statsBlock, "def"));
        bossLoader.setMoves(parseAbilities(block));
        return bossLoader;
    }

    // ---------------------------------------------------------------
    // Parsers
    // ---------------------------------------------------------------

    private List<Move> parseAbilities(String characterBlock) {
        List<Move> moves = new ArrayList<>();
        String abilitiesArray = extractArray(characterBlock, "abilities");
        if (abilitiesArray.equals("[]")) return moves; // mobs with no abilities

        List<String> objects = splitObjects(abilitiesArray);
        int index = 1;
        for (String obj : objects) {
            String name   = extractString(obj, "name");
            String desc   = extractString(obj, "desc");
            String effectsBlock = extractBlock(obj, "\"effects\"");
            int damage = extractInt(effectsBlock, "damage"); // 0 if not present
            moves.add(new Move("move" + index, name, damage, desc));
            index++;
        }
        return moves;
    }

    // ---------------------------------------------------------------
    // JSON helpers
    // ---------------------------------------------------------------

    private String extractCharacterBlock(String name) {
        String key = "\"" + name + "\"";
        int keyIdx = raw.indexOf(key);
        if (keyIdx == -1) throw new IllegalArgumentException("Character not found in Stats.json: " + name);
        int brace = raw.indexOf("{", keyIdx);
        return extractBalanced(raw, brace, '{', '}');
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return "";
        int colon  = json.indexOf(":", keyIdx);
        int quote1 = json.indexOf("\"", colon + 1);
        int quote2 = json.indexOf("\"", quote1 + 1);
        return json.substring(quote1 + 1, quote2);
    }

    private int extractInt(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return 0;
        int colon = json.indexOf(":", keyIdx);
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n' || json.charAt(start) == '\r')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (start == end) return 0;
        return Integer.parseInt(json.substring(start, end).trim());
    }

    private String extractArray(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return "[]";
        int bracket = json.indexOf("[", keyIdx);
        int nextBrace = json.indexOf("{", keyIdx);
        // make sure the [ comes before any next block opener
        if (bracket == -1 || (nextBrace != -1 && nextBrace < bracket)) return "[]";
        return extractBalanced(json, bracket, '[', ']');
    }

    private String extractBlock(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx == -1) return "{}";
        int brace = json.indexOf("{", keyIdx);
        return extractBalanced(json, brace, '{', '}');
    }

    private List<String> splitObjects(String array) {
        List<String> objects = new ArrayList<>();
        int i = 0;
        while (i < array.length()) {
            if (array.charAt(i) == '{') {
                String obj = extractBalanced(array, i, '{', '}');
                objects.add(obj);
                i += obj.length();
            } else {
                i++;
            }
        }
        return objects;
    }

    private String extractBalanced(String json, int startIdx, char open, char close) {
        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = startIdx; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == open) depth++;
            else if (c == close) depth--;
            sb.append(c);
            if (depth == 0) break;
        }
        return sb.toString();
    }
}
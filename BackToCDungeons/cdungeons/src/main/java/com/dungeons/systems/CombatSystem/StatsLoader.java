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
        BossLoader boss = new BossLoader();
        boss.setId(characterName);
        boss.setName(characterName);
        boss.setTitle("");
        boss.setMaxHp(extractInt(statsBlock, "hp"));
        boss.setCurrentHp(extractInt(statsBlock, "hp"));
        boss.setAttack(extractInt(statsBlock, "atk"));
        boss.setDefense(extractInt(statsBlock, "def"));
        boss.setMoves(parseAbilities(block));

        String spritesBlock = extractBlock(block, "\"sprites\"");
        if (!spritesBlock.equals("{}")) {
            boss.setSpriteNeutral(extractString(spritesBlock, "neutral"));
            boss.setSpriteAngry(extractString(spritesBlock, "angry"));
            boss.setSpriteThinking(extractString(spritesBlock, "thinking"));
            boss.setSpriteDefeated(extractString(spritesBlock, "defeated"));
            boss.setSpriteCloned(extractString(spritesBlock, "cloned"));
        }

        return boss;
    }

    private List<Move> parseAbilities(String characterBlock) {
        List<Move> moves = new ArrayList<>();
        String abilitiesArray = extractArray(characterBlock, "abilities");
        if (abilitiesArray.equals("[]")) return moves;

        List<String> objects = splitObjects(abilitiesArray);
        int index = 1;
        for (String obj : objects) {
            String name  = extractString(obj, "name");
            String desc  = extractString(obj, "desc");
            String effectsBlock = extractBlock(obj, "\"effects\"");
            int damage   = extractInt(effectsBlock, "damage");
            int hits     = extractInt(effectsBlock, "hits");
            if (hits <= 0) hits = 1;

            Move move = new Move("move" + index, name, damage, desc);

            String statusEffect = extractString(obj, "statusEffect");
            if (!statusEffect.isEmpty() && !statusEffect.equals("null")) {
                move.setStatusEffect(statusEffect);
            }
            move.setDuration(extractInt(obj, "duration"));
            move.setChance(extractDouble(obj, "chance"));
            move.setHits(hits);
            move.setHitStyle(extractString(obj, "hitStyle"));
            move.setCooldown(extractInt(obj, "cooldown"));
            move.setAbilitySprite(extractString(obj, "abilitySprite"));

            moves.add(move);
            index++;
        }
        return moves;
    }

    private String extractCharacterBlock(String name) {
        String key = "\"" + name + "\"";
        int keyIdx = raw.indexOf(key);
        if (keyIdx == -1) throw new IllegalArgumentException("Character not found: " + name);
        int brace = raw.indexOf("{", keyIdx);
        return extractBalanced(raw, brace, '{', '}');
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return "";
        int colon  = json.indexOf(":", keyIdx);
        int quote1 = json.indexOf("\"", colon + 1);
        if (quote1 == -1) return "";
        int quote2 = json.indexOf("\"", quote1 + 1);
        return json.substring(quote1 + 1, quote2);
    }

    private int extractInt(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return 0;
        int colon = json.indexOf(":", keyIdx);
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' ||
               json.charAt(start) == '\n' || json.charAt(start) == '\r')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) ||
               json.charAt(end) == '-')) end++;
        if (start == end) return 0;
        return Integer.parseInt(json.substring(start, end).trim());
    }

    private double extractDouble(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return 0.0;
        int colon = json.indexOf(":", keyIdx);
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' ||
               json.charAt(start) == '\n')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) ||
               json.charAt(end) == '.' || json.charAt(end) == '-')) end++;
        if (start == end) return 0.0;
        try { return Double.parseDouble(json.substring(start, end).trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private String extractArray(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return "[]";
        int bracket = json.indexOf("[", keyIdx);
        int nextBrace = json.indexOf("{", keyIdx);
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
            } else { i++; }
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
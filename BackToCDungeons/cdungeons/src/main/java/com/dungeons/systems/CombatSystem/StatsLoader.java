package com.dungeons.systems.CombatSystem;

import com.dungeons.world.Map;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// StatsLoader reads Stats.json and builds Player and BossLoader objects from it.
// It is called at the start of every combat by CombatController.
// It is a manual JSON parser - it does not use any external library.
// It reads the entire file as a plain text string and searches it using the
// private helper methods at the bottom.
public class StatsLoader {

    // Path to Stats.json inside the project's resources folder.
    // The leading "/" means it searches from the root of the resources directory.
    private static final String STATS_FILE = "/CharacterStats/Stats.json";

    // 'raw' holds the entire Stats.json file as one big string after the constructor runs.
    // All parsing methods operate on this string.
    private String raw;

    // -----------------------------------------------------------------------
    // CONSTRUCTOR
    // Runs once when 'new StatsLoader()' is called.
    // Opens Stats.json, reads every byte, stores it as a UTF-8 string in 'raw'.
    // If the file is not found or cannot be read, it crashes immediately with
    // a clear error message rather than silently failing later.
    // 'try-with-resources' (the 'try (InputStream is = ...)' syntax) automatically
    // closes the file after reading, even if an error occurs.
    // -----------------------------------------------------------------------
    public StatsLoader() {
        try (InputStream is = StatsLoader.class.getResourceAsStream(STATS_FILE)) {
            if (is == null) throw new RuntimeException("Stats.json not found at: " + STATS_FILE);
            raw = new String(is.readAllBytes(), StandardCharsets.UTF_8); // read all bytes, decode as UTF-8
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Stats.json: " + e.getMessage());
        }
    }


    // -----------------------------------------------------------------------
    // loadPlayer(String characterName)
    // Reads the character block for 'characterName' from Stats.json and builds
    // a Player object. Called with "Player" in CombatController.
    // Items are set to an empty list because the item system is not wired up yet.
    // -----------------------------------------------------------------------
    public Player loadPlayer(String characterName) {
        String block      = extractCharacterBlock(characterName); // find the whole character section
        String statsBlock = extractBlock(block, "\"stats\"");     // find the stats sub-section

        Player player = new Player();
        player.setName(characterName);
        player.setMaxHp(extractInt(statsBlock, "hp"));       // read hp value from stats block
        //player.setCurrentHp(extractInt(statsBlock, "hp"));   // current HP = max HP at start
        int savedHp = PlayerProgress.getInstance().getCurrentHp();
        player.setCurrentHp(savedHp != -1 ? savedHp : extractInt(statsBlock, "hp"));

        player.setAttack(extractInt(statsBlock, "atk"));     // read atk value
        player.setDefense(extractInt(statsBlock, "def"));    // read def value
        player.setMoves(parseAbilities(block));              // parse all abilities
        player.setItems(new ArrayList<>());                  // empty list - items not yet implemented
        return player;
    }


    // -----------------------------------------------------------------------
    // loadBoss(String characterName)
    // Shortcut for loading a named boss at level 1 (no scaling).
    // Named bosses should always use this, not loadBossAtLevel.
    // Internally just calls loadBossAtLevel with level=1.
    // -----------------------------------------------------------------------
    public BossLoader loadBoss(String characterName) {
        return loadBossAtLevel(characterName, 1);
    }


    // -----------------------------------------------------------------------
    // loadBossAtLevel(String characterName, int level)
    // The main enemy loading method. Handles both named bosses and mobs.
    // For named bosses: loads base stats exactly, no scaling (isMob=false).
    // For Mob1-Mob5: scales HP, ATK, DEF based on the level parameter.
    //
    // HOW ROOM DETECTION WILL WORK (future placeholder):
    // When room detection is implemented, replace the hardcoded level in
    // CombatEngineTest (or wherever the fight is started) with:
    //   int roomLevel = RoomManager.getCurrentRoom().getDifficultyLevel();
    //   loader.loadBossAtLevel("Mob2", roomLevel);
    //
    // MOB STAT SCALING EQUATIONS:
    //   finalHp  = baseHp  + (level - 1) * MOB_HP_PER_LEVEL
    //   finalAtk = baseAtk + (level - 1) * MOB_ATK_PER_LEVEL
    //   finalDef = baseDef + (level - 1) * MOB_DEF_PER_LEVEL
    //
    // At level 1: (1-1) = 0, so all stats equal the base values from Stats.json.
    // At level 5: (5-1) = 4 extra tiers of growth applied.
    //
    // Mob1 examples:
    //   Level 1: HP=100, ATK=12, DEF=5  (exactly Stats.json values)
    //   Level 3: HP=130, ATK=18, DEF=7
    //   Level 8: HP=205, ATK=33, DEF=12
    // -----------------------------------------------------------------------

    // Stat growth per level for mobs. Raise to make higher-room mobs tougher.
    private static final int MOB_HP_PER_LEVEL  = 15; // +15 HP per level
    private static final int MOB_ATK_PER_LEVEL = 3;  // +3 ATK per level
    private static final int MOB_DEF_PER_LEVEL = 1;  // +1 DEF per level

    public BossLoader loadBossAtLevel(String characterName, int level) {
        level = Map.getRoomCounter();
        String block      = extractCharacterBlock(characterName);
        String statsBlock = extractBlock(block, "\"stats\"");

        BossLoader boss = new BossLoader();
        boss.setId(characterName);
        boss.setName(characterName);
        boss.setTitle(""); // title is empty by default

        // Read the base stat values from Stats.json
        int baseHp  = extractInt(statsBlock, "hp");
        int baseAtk = extractInt(statsBlock, "atk");
        int baseDef = extractInt(statsBlock, "def");

        // 'isMob' is true if the name starts with "Mob" (Mob1, Mob2, Mob3, Mob4, Mob5)
        // Named bosses are never scaled regardless of the level parameter.
        boolean isMob = characterName.startsWith("Mob");
        int lvl = Math.max(1, level); // safety: level can never go below 1w

        // Ternary operator: 'condition ? value_if_true : value_if_false'
        // If isMob is true, apply the scaling formula. If false, use the base value unchanged.
        int finalHp  = isMob ? baseHp  + (lvl - 1) * MOB_HP_PER_LEVEL  : baseHp;
        int finalAtk = isMob ? baseAtk + (lvl - 1) * MOB_ATK_PER_LEVEL : baseAtk;
        int finalDef = isMob ? baseDef + (lvl - 1) * MOB_DEF_PER_LEVEL : baseDef;

        boss.setMaxHp(finalHp);
        boss.setCurrentHp(finalHp);    // starts fight at full HP
        boss.setAttack(finalAtk);
        boss.setDefense(finalDef);
        boss.setMobLevel(lvl);         // store the level so reward calculation can use it
        boss.setMoves(parseAbilities(block)); // parse abilities the same way for all enemies

        // Load sprites from the "sprites" block in Stats.json
        String spritesBlock = extractBlock(block, "\"sprites\"");
        if (!spritesBlock.equals("{}")) { // if sprites block exists and is not empty
            boss.setSpriteNeutral(extractString(spritesBlock, "neutral"));
            boss.setSpriteAngry(extractString(spritesBlock, "angry"));
            boss.setSpriteThinking(extractString(spritesBlock, "thinking"));
            boss.setSpriteDefeated(extractString(spritesBlock, "defeated"));
        }

        return boss;
    }


    // -----------------------------------------------------------------------
    // parseAbilities(String characterBlock)
    // Reads the "abilities" array from a character's JSON block and creates
    // a List of Move objects. Called for both the player and every enemy.
    // Each JSON object inside "abilities" becomes one Move.
    // -----------------------------------------------------------------------
    private List<Move> parseAbilities(String characterBlock) {
        List<Move> moves = new ArrayList<>();
        String abilitiesArray = extractArray(characterBlock, "abilities");
        if (abilitiesArray.equals("[]")) return moves; // no abilities found, return empty list

        List<String> objects = splitObjects(abilitiesArray); // split the array into individual objects
        int index = 1;
        for (String obj : objects) {
            String name         = extractString(obj, "name");
            String desc         = extractString(obj, "desc");
            String effectsBlock = extractBlock(obj, "\"effects\"");
            int damage          = extractInt(effectsBlock, "damage");
            int hits            = extractInt(effectsBlock, "hits");
            if (hits <= 0) hits = 1; // default to 1 hit if not specified in JSON

            // Build the core Move object with ID, name, damage, description
            Move move = new Move("move" + index, name, damage, desc);

            // Read optional fields - statusEffect, duration, chance, hits, hitStyle, cooldown, sprite
            String statusEffect = extractString(obj, "statusEffect");
            if (!statusEffect.isEmpty() && !statusEffect.equals("null")) {
                move.setStatusEffect(statusEffect); // "DOT", "skip", or "halfDmg"
            }
            move.setDuration(extractInt(obj, "duration"));      // how many turns effect lasts
            move.setChance(extractDouble(obj, "chance"));       // 0.0 to 1.0 proc chance
            move.setHits(hits);                                 // number of separate hits
            move.setHitStyle(extractString(obj, "hitStyle"));   // "single", "rapid", "heal"
            move.setCooldown(extractInt(obj, "cooldown"));      // turns before re-use
            move.setAbilitySprite(extractString(obj, "abilitySprite")); // attack animation image path

            moves.add(move);
            index++;
        }
        return moves;
    }


    // -----------------------------------------------------------------------
    // PRIVATE PARSER HELPERS
    // These all operate on the 'raw' string or a sub-string of it.
    // You do not call these directly and do not need to change them
    // unless the structure of Stats.json changes significantly.
    // -----------------------------------------------------------------------

    // Finds the full JSON block for one character (everything between its outer { and }).
    // Throws a clear error if the character name is not found in the file.
    private String extractCharacterBlock(String name) {
        String key = "\"" + name + "\"";          // wrap in quotes to match JSON key format
        int keyIdx = raw.indexOf(key);             // find where this key appears in the file
        if (keyIdx == -1) throw new IllegalArgumentException("Character not found: " + name);
        int brace = raw.indexOf("{", keyIdx);      // find the opening brace after the key
        return extractBalanced(raw, brace, '{', '}'); // read from { to its matching }
    }

    // Finds and returns the value of a string field, e.g. "hitStyle": "rapid" --< returns "rapid"
    private String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return ""; // key not found, return empty string
        int colon  = json.indexOf(":", keyIdx);
        int quote1 = json.indexOf("\"", colon + 1); // find opening quote of value
        if (quote1 == -1) return "";
        int quote2 = json.indexOf("\"", quote1 + 1); // find closing quote
        return json.substring(quote1 + 1, quote2);   // return text between the quotes
    }

    // Finds and returns the value of an integer field, e.g. "hp": 420 -> returns 420
    // Skips past any whitespace and newlines between the colon and the number.
    private int extractInt(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return 0; // key not found, return 0 as default
        int colon = json.indexOf(":", keyIdx);
        int start = colon + 1;
        // skip whitespace
        while (start < json.length() && (json.charAt(start) == ' ' ||
                json.charAt(start) == '\n' || json.charAt(start) == '\r')) start++;
        int end = start;
        // read digits (and minus sign for negative numbers)
        while (end < json.length() && (Character.isDigit(json.charAt(end)) ||
                json.charAt(end) == '-')) end++;
        if (start == end) return 0; // nothing to parse, return 0
        return Integer.parseInt(json.substring(start, end).trim());
    }

    // Finds and returns a decimal value, e.g. "chance": 0.35 -> returns 0.35
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

    // Finds and returns a JSON array by key, e.g. the entire "abilities": [...] block
    private String extractArray(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return "[]";
        int bracket   = json.indexOf("[", keyIdx);
        int nextBrace = json.indexOf("{", keyIdx);
        if (bracket == -1 || (nextBrace != -1 && nextBrace < bracket)) return "[]";
        return extractBalanced(json, bracket, '[', ']');
    }

    // Finds and returns a JSON object block by key, e.g. the "stats": { ... } section
    private String extractBlock(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx == -1) return "{}";
        int brace = json.indexOf("{", keyIdx);
        return extractBalanced(json, brace, '{', '}');
    }

    // Splits a JSON array string into individual object strings.
    // Input: '[{"name":"Quick Strike",...}, {"name":"Shock Jab",...}]'
    // Output: ['{"name":"Quick Strike",...}', '{"name":"Shock Jab",...}']
    private List<String> splitObjects(String array) {
        List<String> objects = new ArrayList<>();
        int i = 0;
        while (i < array.length()) {
            if (array.charAt(i) == '{') {
                String obj = extractBalanced(array, i, '{', '}'); // read one full object
                objects.add(obj);
                i += obj.length(); // move past this object
            } else { i++; }
        }
        return objects;
    }

    // The core parser method. Reads from startIdx through the text until the
    // open/close characters (like { and }) are balanced (depth returns to 0).
    // This correctly handles nested objects because depth tracks how deep we are.
    private String extractBalanced(String json, int startIdx, char open, char close) {
        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = startIdx; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == open)  depth++; // going deeper into nesting
            else if (c == close) depth--; // coming back out
            sb.append(c);
            if (depth == 0) break; // we have closed the original opening bracket, done
        }
        return sb.toString();
    }
}
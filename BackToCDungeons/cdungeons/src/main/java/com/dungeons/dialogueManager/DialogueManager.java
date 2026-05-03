package com.dungeons.dialogueManager;

import com.google.gson.*;
import java.io.*;
import java.util.Map;

public class DialogueManager {
    private Map<String, Dialogue> dialogues;
    private String[] currentLines;
    private int currentIndex;
    private Dialogue currentDialogue;

    public void load() {
        Gson gson = new Gson();

        //I ben load file te dialogjeve.
        InputStream is = getClass().getResourceAsStream("/Dialogues/dialogue.json");;

        if (is == null) {
            System.out.println("File not found!");
            return;
        } else {
            System.out.println("File found!");
        }

        InputStreamReader reader = new InputStreamReader(is);


        //I ben parse JSON ne Java Objects qe te mund ti lexoi
        DialogueData data = gson.fromJson(reader, DialogueData.class);
        dialogues = data.dialogues;

    }

    public void startDialogue(String id) {
        currentDialogue = dialogues.get(id);
        currentLines = currentDialogue.lines;
        currentIndex = 0;
    }

    public String getCurrentCharacter() {
        return currentDialogue.character;
    }

    public String getSprite() {
        return currentDialogue.sprite;
    }

    public String getNextLine() {
        return currentLines[currentIndex++];
    }

    public boolean isFinished() {
        return currentIndex >= currentLines.length;
    }
}

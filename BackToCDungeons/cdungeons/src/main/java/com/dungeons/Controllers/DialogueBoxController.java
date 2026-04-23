package com.dungeons.Controllers;

import com.dungeons.dialogueManager.DialogueManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

public class DialogueBoxController {
    @FXML
    private Label dialogueText;

    private DialogueManager dialogueManager;

    @FXML
    public void initialize() {
        dialogueText.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                        nextLine();
                    }
                });
            }
        });
    }

    public void setDialogueManager(DialogueManager dialogueManager) {
        this.dialogueManager = dialogueManager;
    }

    public void startDialogue(String id) {
        dialogueManager.startDialogue(id);
        dialogueText.setText(dialogueManager.getNextLine());
    }

    public void nextLine() {
        if (!dialogueManager.isFinished()) {
            dialogueText.setText(dialogueManager.getNextLine());
        } else {
            dialogueText.getScene().getRoot().setVisible(false);
        }
    }
}
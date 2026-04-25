package com.dungeons.Controllers;

import com.dungeons.dialogueManager.DialogueManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.io.InputStream;


public class DialogueBoxController {
    @FXML
    private Label dialogueText;
    @FXML
    private Label characterName;

    @FXML
    private ImageView character1;

    private DialogueManager dialogueManager;

    @FXML
    public void initialize() {

        try {
            java.net.URL url = getClass().getResource("/sprites");
            System.out.println("Sprites folder URL: " + url);

            java.net.URL url2 = getClass().getResource("/");
            System.out.println("Root URL: " + url2);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        dialogueText.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                        nextLine();
                    }
                });

                newScene.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
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
        characterName.setText(dialogueManager.getCurrentCharacter());
        dialogueText.setText(dialogueManager.getNextLine());

        setSprite(character1, dialogueManager.getSprite());
    }
    private void setSprite(ImageView view, String spriteName) {
        if (spriteName != null) {
            String path = "/sprites/DialougeSprites/" + spriteName;
            System.out.println("Trying to load sprite from: " + path);

            try {
                Image image = new Image(getClass().getResource(path).toExternalForm());
                view.setImage(image);
                System.out.println("Sprite loaded successfully!");
            } catch (Exception e) {
                System.out.println("Sprite not found: " + path);
                System.out.println("Error: " + e.getMessage());
                view.setImage(null);
            }
        } else {
            view.setImage(null);
        }
    }

    public void nextLine() {
        if (!dialogueManager.isFinished()) {
            dialogueText.setText(dialogueManager.getNextLine());
        } else {
            dialogueText.getScene().getRoot().setVisible(false);
        }
    }
}
package com.dungeons.Controllers;

import com.dungeons.dialogueManager.DialogueManager;
import com.dungeons.screens.GameScreen;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;

import java.io.InputStream;


public class DialogueBoxController {
    @FXML
    private Label dialogueText;
    @FXML
    private Label characterName;

    @FXML
    private ImageView character1;

    @FXML
    private Pane dialogueRoot;

    private DialogueManager dialogueManager;

    private boolean finished = false;

    public boolean isDialogueFinished() {
        return finished;
    }


    private GameScreen gameScreen;

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

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
                newScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                        nextLine();
                    }
                });

                newScene.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
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
            finished = true;
            dialogueRoot.setVisible(false);
            if (gameScreen != null) {
                gameScreen.resumeFromDialogue();
            }
        }
    }
}
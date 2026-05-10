package com.dungeons.Controllers;

import com.dungeons.MusicandSoundsCode.GameMusicManager;
import com.dungeons.screens.GameScreen;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * introController — Back to CDungeons
 *
 * Shows a dark cinematic intro screen where Cin.de Moni (the narrator,
 * played by Sindi's sprite) tells the opening story of the game
 * line by line — exactly like the in-game dialogue box.
 *
 * Player advances lines with SPACE, ENTER, or a click.
 * SKIP button skips the entire intro immediately.
 * After the last line the screen fades to black and the game launches.
 */
public class introController {

    // ── FXML NODES ────────────────────────────────────────────────────
    @FXML private Pane      rootPane;
    @FXML private Pane      fadeOverlay;
    @FXML private ImageView narratorPortrait;   // large portrait top-left
    @FXML private ImageView dialoguePortrait;   // small portrait in dialogue box
    @FXML private Label     characterNameLabel;
    @FXML private Label     dialogueText;
    @FXML private Label     continueHint;
    @FXML private Label     titleLabel;
    @FXML private Label     subtitleLabel;
    @FXML private AnchorPane portraitBox;
    @FXML private AnchorPane textBox;

    // ── NARRATOR SPRITE ───────────────────────────────────────────────
    private static final String SPRITE_PATH =
            "/sprites/DialougeSprites/SindiCharacterDialougeSprite-NBR.png";

    // ── OPENING STORY LINES (told by Cin.de Moni) ────────────────────
    // These match the "opening" dialogue in dialogue.json
    private static final String[] STORY_LINES = {
        "Hello. My name is Cin.de Moni. I will be your narrator today. Try not to die.",
        "For many years, one of the most important laboratories in the world stood at the heart of human discovery. Scientists called it the crown jewel of modern research. The press called it a miracle. Everyone else called it... the Lab.",
        "Until the day everyone now calls... The Explosion.",
        "No one knows what truly happened. The official reports were sealed within hours. Witnesses disappeared. Files were wiped.",
        "What is known is this: not a single person who entered that laboratory ever came back out.",
        "The world moved on. People forgot. Or pretended to. The kind of forgetting that feels deliberate.",
        "You are living an ordinary life. A quiet one. A job you do not love. A flat you do not hate. Tuesday afternoons that feel exactly like Monday mornings.",
        "Until one day, by complete accident, your hand brushes against a strange object. Small. Cold. Humming faintly, like it has been waiting.",
        "And in an instant — memories flood in. Memories that are not yours. A corridor. White walls. Screaming. A door sealing shut.",
        "Or... are they yours?",
        "The dungeon below the city holds the answers. It always has. Guarded by those who were there that night. Those who survived. Those who remember.",
        "And at the very bottom, in the ruins of the laboratory itself, something waits. Something that knows your name.",
        "Something that is, in a way you cannot yet explain...",
        "you.",
        "The truth is waiting in the dungeon.",
        "Whether you are ready for it... is another question entirely.",
        "Good luck. You are going to need it. I will be watching."
    };

    // ── STATE ─────────────────────────────────────────────────────────
    private int     currentLine = 0;
    private boolean launched    = false;

    // ── INITIALIZE ────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Start the emotional music
        GameMusicManager.playIntro();

        // Hide the dialogue box until the scene is ready
        if (portraitBox != null) portraitBox.setVisible(false);
        if (textBox     != null) textBox.setVisible(false);
        if (continueHint != null) continueHint.setVisible(false);

        // Wait for scene to attach, then load sprites and show first line
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupInput(newScene);
                loadSprites();
                showTitleThenStart();
            }
        });
    }

    /** Load Cin.de Moni's portrait into both image views. */
    private void loadSprites() {
        try {
            Image sprite = new Image(getClass().getResource(SPRITE_PATH).toExternalForm());
            if (narratorPortrait != null) narratorPortrait.setImage(sprite);
            if (dialoguePortrait != null) dialoguePortrait.setImage(sprite);
        } catch (Exception e) {
            System.out.println("[introController] Narrator sprite not found: " + SPRITE_PATH);
        }
    }

    /** Show title for 2 seconds then slide into the first dialogue line. */
    private void showTitleThenStart() {
        Timeline delay = new Timeline(
                new KeyFrame(Duration.seconds(2.0), e -> showFirstLine())
        );
        delay.play();
    }

    /** Fade in the dialogue box and show the first story line. */
    private void showFirstLine() {
        if (titleLabel   != null) titleLabel.setVisible(false);
        if (subtitleLabel != null) subtitleLabel.setVisible(false);

        if (portraitBox  != null) { portraitBox.setVisible(true);  portraitBox.setOpacity(0); }
        if (textBox      != null) { textBox.setVisible(true);       textBox.setOpacity(0); }
        if (continueHint != null) { continueHint.setVisible(true);  continueHint.setOpacity(0); }

        // Fade in the dialogue box
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), rootPane);
        fadeIn.setFromValue(1.0);
        fadeIn.setToValue(1.0); // rootPane stays visible

        if (portraitBox != null) {
            FadeTransition f1 = new FadeTransition(Duration.millis(600), portraitBox);
            f1.setFromValue(0); f1.setToValue(1); f1.play();
        }
        if (textBox != null) {
            FadeTransition f2 = new FadeTransition(Duration.millis(600), textBox);
            f2.setFromValue(0); f2.setToValue(1); f2.play();
        }
        if (continueHint != null) {
            FadeTransition f3 = new FadeTransition(Duration.millis(600), continueHint);
            f3.setFromValue(0); f3.setToValue(1); f3.play();
        }

        showLine(0);
    }

    /** Display a specific line by index. */
    private void showLine(int index) {
        if (index >= STORY_LINES.length) {
            onStoryFinished();
            return;
        }
        currentLine = index;
        if (dialogueText != null) {
            dialogueText.setText(STORY_LINES[index]);
        }
    }

    /** Called on SPACE / ENTER / CLICK — advance to the next line. */
    private void nextLine() {
        if (launched) return;
        int next = currentLine + 1;
        if (next >= STORY_LINES.length) {
            onStoryFinished();
        } else {
            showLine(next);
        }
    }

    /** All lines shown — fade to black and launch the game. */
    private void onStoryFinished() {
        if (launched) return;
        launched = true;
        fadeToGame(2.5);
    }

    // ── INPUT ─────────────────────────────────────────────────────────

    private void setupInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE ||
                event.getCode() == KeyCode.ENTER) {
                nextLine();
            }
        });
        scene.setOnMouseClicked(event -> nextLine());
    }

    // ── SKIP BUTTON ───────────────────────────────────────────────────

    @FXML
    private void handleSkip() {
        if (launched) return;
        launched = true;
        fadeToGame(0.6);
    }

    // ── FADE TO BLACK AND LAUNCH ──────────────────────────────────────

    private void fadeToGame(double seconds) {
        FadeTransition fade = new FadeTransition(Duration.seconds(seconds), fadeOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setOnFinished(e -> launchGame());
        fade.play();
    }

    private void launchGame() {
        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();

            GameScreen gameScreen = new GameScreen();
            gameScreen.setStage(stage);

            Font.loadFont(getClass().getResourceAsStream("/OpenType-TT/MarinVonGayNjega.ttf"), 10);

            Scene scene = new Scene(gameScreen.getRoot());
            scene.getStylesheets().add(
                    getClass().getResource("/sprites/style.css").toExternalForm()
            );

            stage.setScene(scene);
            gameScreen.startLoop();
            GameMusicManager.playGameplay();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

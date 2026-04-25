package com.dungeons.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

public class CombatController {

    // --- Panes ---
    @FXML private AnchorPane mainAnchor;
    @FXML private AnchorPane pressAttack;
    @FXML private AnchorPane pressDefense;
    @FXML private AnchorPane pressTalk;
    @FXML private AnchorPane pressItem;

    // --- Labels ---
    @FXML private Label bossName;
    @FXML private Label bossHPnumber;
    @FXML private Label playername;
    @FXML private Label turnNumber;
    @FXML private TextArea turnInformation;

    //  bars of soap and health
    @FXML private Rectangle bossHP;
    @FXML private Rectangle playerHP;

    // --- Images n shi --- no you treat me like shi
    @FXML private ImageView playercharacterSprite;
    @FXML private ImageView enemycharacterSprite;

    private static final double BOSS_BAR_MAX   = 435.0;
    private static final double PLAYER_BAR_MAX = 355.0;

    // called once at the start to set names and HP
    public void setStart(String playerNamee, String bossNamee, int bossMaxHp) {
        playername.setText(playerNamee);
        bossName.setText(bossNamee);
        bossHPnumber.setText(bossMaxHp + " / " + bossMaxHp);

        Image playerImg = new Image(getClass().getResourceAsStream("/sprites/DialougeSprites/KejviCharacterDialougeSprite-NBR.png"));
        Image enemyIMG = new Image(getClass().getResourceAsStream("/sprites/DialougeSprites/SindiCharacterDialougeSprite-NBR.png"));
        //SPRITE LOADING BHAAHAHAHAHAHAHA
        playercharacterSprite.setImage(playerImg);
        enemycharacterSprite.setImage(enemyIMG);

    }

    // update boss HP bar — takes current HP and max HP
    public void updateBossHP(int currentHp, int maxHp) {
        double percent = (double) currentHp / maxHp;
        double newWidth = BOSS_BAR_MAX * percent;
        bossHP.setWidth(newWidth); // setWidth not prefWidth — Rectangle uses setWidth
        bossHPnumber.setText(currentHp + " / " + maxHp);
    }

    // update player HP bar
    public void updatePlayerHP(int currentHp, int maxHp) {
        double percent = (double) currentHp / maxHp;
        double newWidth = PLAYER_BAR_MAX * percent;
        playerHP.setWidth(newWidth);
    }

    // update turn number label
    public void setTurnNr(int n) {
        turnNumber.setText("Turn: " + n);
    }
    // update the turn log text
    public void setTurnLog(String text) {
        turnInformation.appendText(text + "\n");
    }

    // --- Pane switching ---
    @FXML
    public void openAttack() {
        mainAnchor.setVisible(false);
        mainAnchor.setDisable(true);
        pressAttack.setVisible(true);
        pressAttack.setDisable(false);
    }

    @FXML
    public void openDefense() {
        mainAnchor.setVisible(false);
        mainAnchor.setDisable(true);
        pressDefense.setVisible(true);
        pressDefense.setDisable(false);
    }

    @FXML
    public void openItems() {
        mainAnchor.setVisible(false);
        mainAnchor.setDisable(true);
        pressItem.setVisible(true);
        pressItem.setDisable(false);
    }

    @FXML
    public void openTalk() {
        mainAnchor.setVisible(false);
        mainAnchor.setDisable(true);
        pressTalk.setVisible(true);
        pressTalk.setDisable(false);
    }

    // go back to main anchor from any sub-pane
    @FXML
    public void goBack() {
        pressAttack.setVisible(false);  pressAttack.setDisable(true);
        pressDefense.setVisible(false); pressDefense.setDisable(true);
        pressItem.setVisible(false);    pressItem.setDisable(true);
        pressTalk.setVisible(false);    pressTalk.setDisable(true);

        mainAnchor.setVisible(true);
        mainAnchor.setDisable(false);
    }
}
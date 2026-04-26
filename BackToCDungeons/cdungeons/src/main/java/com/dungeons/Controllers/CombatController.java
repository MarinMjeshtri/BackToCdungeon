package com.dungeons.Controllers;
//who tf used chatgpt on my beloved combat script and why is it terminal :sob:
//I have to practically scarp everything but cant very nice

// Necessary imports for loading gameloop
import com.dungeons.screens.GameScreen;

// Necessary imports for Combat
import com.dungeons.systems.CombatSystem.BossLoader;
import com.dungeons.systems.CombatSystem.CombatEngine;
import com.dungeons.systems.CombatSystem.CombatResult;
import com.dungeons.systems.CombatSystem.Move;
import com.dungeons.systems.CombatSystem.Player;
import com.dungeons.systems.CombatSystem.PlayerAction;
import com.dungeons.systems.CombatSystem.StatsLoader;
import com.dungeons.systems.CombatSystem.TurnLog;

// Necessary imports for JAVAFX
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.List;

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

    // --- HP Bars ---
    @FXML private Rectangle bossHP;
    @FXML private Rectangle playerHP;

    // --- Sprites ---
    @FXML private ImageView playercharacterSprite;
    @FXML private ImageView enemycharacterSprite;

    private static final double BOSS_BAR_MAX   = 435.0;
    private static final double PLAYER_BAR_MAX = 355.0;

    // --- Combat state ---
    private CombatEngine engine;
    private Player player;
    private BossLoader boss;
    private int playerMaxHp;
    private int bossMaxHp;

    GameScreen gameScreen;
    private Stage stage;

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // call this from wherever combat ends in your existing code
    public void returnToGame() {
        gameScreen.returnFromCombat();
    }



    // Called by JavaFX after FXML loads to load stats and wire the buttons (attack, guard, item, talk to their correspoding submenus)

    @FXML
    public void initialize() {
        // Load player and boss from Stats.json
        StatsLoader loader = new StatsLoader();
        player = loader.loadPlayer("Player"); //IDK tf to put here so i just left it as before for testing. Then ill make a loop or something, idk how it will be implemented yet
        boss   = loader.loadBoss("JohnMKati");

        playerMaxHp = player.getMaxHp();
        bossMaxHp   = boss.getMaxHp();

        engine = new CombatEngine(player, boss);

        // Set initial UI state
        setStart(player.getName(), boss.getName(), bossMaxHp);

        // Wire up ability buttons inside pressAttack pane
        wireAbilityButtons();

        // Wire up placeholder buttons like talk etc.
        wirePlaceholderButtons();
    }


    // Wire ability buttons to player moves/actions depedning on what they chose
    private void wireAbilityButtons() {
    List<Move> moves = player.getMoves();
    List<Button> abilityButtons = pressAttack.getChildren()
            .stream()
            .filter(n -> n instanceof Button && !((Button) n).getText().equals("GO BACK"))
            .map(n -> (Button) n)
            .collect(java.util.stream.Collectors.toList());

    for (int i = 0; i < abilityButtons.size(); i++) {
        Button btn = abilityButtons.get(i);
        if (i < moves.size()) {
            Move move = moves.get(i);
            btn.setText(move.getName());
            final int index = i;
            btn.setOnAction(e -> handlePlayerAttack(index));
        } else {
            btn.setText("-");
            btn.setDisable(true);
        }
    }
}



    // Wire buttons that dont have stuff yet (items, guard, talk and their submenus)
    private void wirePlaceholderButtons() {
    List<Button> itemButtons = pressItem.getChildren()
            .stream()
            .filter(n -> n instanceof Button && !((Button) n).getText().equals("GO BACK"))
            .map(n -> (Button) n)
            .collect(java.util.stream.Collectors.toList());


    //Items when used, made it so it prints into the log frame.
    String[] itemLabels = {"item1", "item2", "item3", "item4"};
    for (int i = 0; i < itemButtons.size(); i++) {
        String label = itemLabels[i];
        itemButtons.get(i).setOnAction(e -> {
            log("🧪 Used " + label);
            goBack();
        });
    }
   //Defense ---
    pressDefense.getChildren().stream()
            .filter(n -> n instanceof Button && !((Button) n).getText().equals("GO BACK"))
            .map(n -> (Button) n)
            .forEach(btn -> btn.setOnAction(e -> {
                log("🛡️ " + btn.getText());
                goBack();
            }));
    //Talk
    pressTalk.getChildren().stream()
            .filter(n -> n instanceof Button && !((Button) n).getText().equals("GO BACK"))
            .map(n -> (Button) n)
            .forEach(btn -> btn.setOnAction(e -> {
                log("💬 " + btn.getText());
                goBack();
            }));


        // Guard buttons inside pressDefense
    pressDefense.getChildren().stream()
        .filter(n -> n instanceof Button && !((Button) n).getText().equals("GO BACK"))
        .map(n -> (Button) n)
        .forEach(btn -> btn.setOnAction(e -> {
            log("🛡️ " + btn.getText());
            goBack();
        }));

    // Talk buttons inside pressTalk
    pressTalk.getChildren().stream()
        .filter(n -> n instanceof Button && !((Button) n).getText().equals("GO BACK"))
        .map(n -> (Button) n)
        .forEach(btn -> btn.setOnAction(e -> {
            log("💬 " + btn.getText());
            goBack();
        }));
    }

    // Handle player picking an attack move from the UI
    private void handlePlayerAttack(int moveIndex) {
        if (!engine.isOngoing()) return;

        PlayerAction action = (moveIndex == 0) ? PlayerAction.MOVE_1 : PlayerAction.MOVE_2;
        TurnLog turnLog = engine.processTurn(action, null);

        updateAfterTurn(turnLog);
        goBack();
    }


    // Update UI after every turn
    private void updateAfterTurn(TurnLog turnLog) {
        // Update turn number
        setTurnNr(turnLog.getRoundNumber());

        // Update HP bars
        updateBossHP(turnLog.getBossHpAfter(), bossMaxHp);
        updatePlayerHP(turnLog.getPlayerHpAfter(), playerMaxHp);

        // Buil pretty log message :D (Cant add colors with textarea so sad) Feel free to make anychanges btw like remove the emojies or anything. Pokkie helped
        StringBuilder sb = new StringBuilder();
        sb.append("~~~~~ Round ").append(turnLog.getRoundNumber()).append(" ~~~~~\n");

        // Player action
        if (turnLog.getPlayerMoveName() != null) {
            sb.append("").append(player.getName())
              .append(" used [").append(turnLog.getPlayerMoveName()).append("]")
              .append(" --> dealt ").append(turnLog.getPlayerDamageDealt()).append(" damage\n");
        }

        // Boss response
        if (turnLog.getBossMoveName() != null) {
            sb.append(" ").append(boss.getName())
              .append(" used [").append(turnLog.getBossMoveName()).append("]")
              .append(" --> dealt ").append(turnLog.getBossDamageDealt()).append(" damage\n");
        } else {
            sb.append(" ").append(boss.getName()).append(" was defeated before acting!\n"); //I wanted to do this skill issue but im scared i would forgget it.
        }

        // HP summary
        sb.append(" Your HP: ").append(turnLog.getPlayerHpAfter())
          .append("      ||  Boss HP: ").append(turnLog.getBossHpAfter()).append("\n");

        // Result
        if (turnLog.getResultAfterRound() == CombatResult.PLAYER_WIN) {
            disableAllActions();
            returnToGame(); // ← add this
        } else if (turnLog.getResultAfterRound() == CombatResult.PLAYER_LOSE) {
            sb.append("💀 DEFEATED... ").append(player.getName()).append(" has fallen...\n");
            disableAllActions();
            returnToGame(); // ← add this
        }

        log(sb.toString());
    }

    // Disable all action buttons when combat ends. Here u can do the next screen i think too.

    private void disableAllActions() {
        mainAnchor.setDisable(true);
    }

    // Helpers
    private void log(String text) {
        turnInformation.appendText(text + "\n");
    }

    public void setStart(String playerNamee, String bossNamee, int bossMaxHp) {
        playername.setText(playerNamee);
        bossName.setText(bossNamee);
        bossHPnumber.setText(bossMaxHp + " / " + bossMaxHp);

        try {
            Image playerImg = new Image(getClass().getResourceAsStream(
                "/sprites/DialougeSprites/KejviCharacterDialougeSprite-NBR.png"));
            Image enemyImg  = new Image(getClass().getResourceAsStream(
                "/sprites/DialougeSprites/SindiCharacterDialougeSprite-NBR.png"));
            playercharacterSprite.setImage(playerImg);
            enemycharacterSprite.setImage(enemyImg);
        } catch (Exception e) {
            System.out.println(" Sprites not found, skipping.");
        }
    }

    public void updateBossHP(int currentHp, int maxHp) {
        double percent  = (double) currentHp / maxHp;
        bossHP.setWidth(BOSS_BAR_MAX * percent);
        bossHPnumber.setText(currentHp + " / " + maxHp);
    }

    public void updatePlayerHP(int currentHp, int maxHp) {
        double percent = (double) currentHp / maxHp;
        playerHP.setWidth(PLAYER_BAR_MAX * percent);
    }

    public void setTurnNr(int n) {
        turnNumber.setText("Turn: " + n);
    }

    public void setTurnLog(String text) { log(text); }

    // --- Pane switching ---
    @FXML public void openAttack() {
        mainAnchor.setVisible(false);   mainAnchor.setDisable(true);
        pressAttack.setVisible(true);   pressAttack.setDisable(false);
    }

    @FXML public void openDefense() {
        mainAnchor.setVisible(false);   mainAnchor.setDisable(true);
        pressDefense.setVisible(true);  pressDefense.setDisable(false);
    }

    @FXML public void openItems() {
        mainAnchor.setVisible(false);   mainAnchor.setDisable(true);
        pressItem.setVisible(true);     pressItem.setDisable(false);
    }

    @FXML public void openTalk() {
        mainAnchor.setVisible(false);   mainAnchor.setDisable(true);
        pressTalk.setVisible(true);     pressTalk.setDisable(false);
    }

    @FXML public void goBack() {
        pressAttack.setVisible(false);  pressAttack.setDisable(true);
        pressDefense.setVisible(false); pressDefense.setDisable(true);
        pressItem.setVisible(false);    pressItem.setDisable(true);
        pressTalk.setVisible(false);    pressTalk.setDisable(true);
        mainAnchor.setVisible(true);    mainAnchor.setDisable(false);
    }
}
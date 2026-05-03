package com.dungeons.Controllers;

import com.dungeons.systems.CombatSystem.*;
import com.dungeons.screens.GameScreen;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class CombatController {

    @FXML private AnchorPane mainAnchor;
    @FXML private AnchorPane pressAttack;
    @FXML private AnchorPane pressDefense;
    @FXML private AnchorPane pressTalk;
    @FXML private AnchorPane pressItem;

    @FXML private Label bossName;
    @FXML private Label bossHPnumber;
    @FXML private Label playername;
    @FXML private Label turnNumber;
    @FXML private TextArea turnInformation;

    @FXML private Rectangle bossHP;
    @FXML private Rectangle playerHP;

    @FXML private ImageView playercharacterSprite;
    @FXML private ImageView enemycharacterSprite;

    private static final double BOSS_BAR_MAX   = 435.0;
    private static final double PLAYER_BAR_MAX = 355.0;

    private CombatEngine engine;
    private Player player;
    private BossLoader boss;
    private int playerMaxHp;
    private int bossMaxHp;

    private Label playerStatusLabel;
    private Label bossStatusLabel;
    private Label bossIntentLabel;
    private Label playerHpLabel;

    private List<Button> abilityButtons;
    private Button guardBtn;
    private Button counterBtn;

    private boolean guardUsedThisTurn = false;

    // stored so we can cancel it before ability sprite loads
    private PauseTransition thinkingRevertTimer = null;

    // maps boss ID to the next map to load after they are defeated
    private static final java.util.Map<String, String> BOSS_NEXT_MAP = new java.util.HashMap<>();
    static {
        BOSS_NEXT_MAP.put("CassieYarn",  "MobRoom3");
        BOSS_NEXT_MAP.put("FreakyRelah", "MobRoom5");
        // JohnMKati end — handled separately below
    }

    @FXML
    public void initialize() {
        startCombat("CassieYarn");
    }

    public void startCombat(String bossId) {
        StatsLoader loader = new StatsLoader();
        player = loader.loadPlayer("Player");
        boss   = loader.loadBoss(bossId);

        playerMaxHp = player.getMaxHp();
        bossMaxHp   = boss.getMaxHp();

        engine = new CombatEngine(player, boss);

        hideBlueOval();
        setStart(player.getName(), boss.getName(), bossMaxHp);
        injectStatusLabels();
        wireAbilityButtons();
        wirePlaceholderButtons();
        updateCooldownUI();

        turnInformation.setText("");
        log("Combat started. Choose your action.");
    }

    private void hideBlueOval() {
        AnchorPane enemyPane = (AnchorPane) enemycharacterSprite.getParent();
        enemyPane.getChildren().stream()
                .filter(n -> n instanceof Ellipse)
                .forEach(n -> n.setVisible(false));
    }

    private void injectStatusLabels() {
        AnchorPane bossHpPane   = (AnchorPane) bossHP.getParent();
        AnchorPane playerHpPane = (AnchorPane) playerHP.getParent();
        AnchorPane enemyPane    = (AnchorPane) enemycharacterSprite.getParent();
        

        // clear old injected labels before adding new ones
        if (bossStatusLabel != null) bossHpPane.getChildren().remove(bossStatusLabel);
        if (playerStatusLabel != null) playerHpPane.getChildren().remove(playerStatusLabel);
        if (playerHpLabel     != null) playerHpPane.getChildren().remove(playerHpLabel);        
        enemyPane.getChildren().removeIf(n -> n instanceof Label);

        bossStatusLabel = new Label("");
        bossStatusLabel.setLayoutX(14);
        bossStatusLabel.setLayoutY(72);
        bossStatusLabel.setStyle("-fx-text-fill: #cc3300; -fx-font-size: 10px;");

        // status and HP on separate Y positions so they never overlap
        playerStatusLabel = new Label("");
        playerStatusLabel.setLayoutX(4);
        playerStatusLabel.setLayoutY(1);
        playerStatusLabel.setStyle("-fx-text-fill: #cc3300; -fx-font-size: 11px;");

        playerHpLabel = new Label(playerMaxHp + " / " + playerMaxHp);
        playerHpLabel.setLayoutX(4);
        playerHpLabel.setLayoutY(16);
        playerHpLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #111; -fx-font-weight: bold;");

        bossIntentLabel = new Label("");
        bossIntentLabel.setLayoutX(10);
        bossIntentLabel.setLayoutY(10);
        bossIntentLabel.setStyle("-fx-text-fill: #222; -fx-font-size: 11px; " +
                "-fx-background-color: rgba(255,255,255,0.8); -fx-padding: 2 5 2 5;");

        bossHpPane.getChildren().add(bossStatusLabel);
        playerHpPane.getChildren().add(playerStatusLabel);
        playerHpPane.getChildren().add(playerHpLabel);
        enemyPane.getChildren().add(bossIntentLabel);
    }

    private void wireAbilityButtons() {
        abilityButtons = pressAttack.getChildren().stream()
                .filter(n -> n instanceof Button &&
                        !((Button) n).getText().equals("GO BACK"))
                .map(n -> (Button) n)
                .collect(Collectors.toList());

        List<Move> moves = player.getMoves();
        for (int i = 0; i < abilityButtons.size(); i++) {
            Button btn = abilityButtons.get(i);
            addHoverScale(btn);
            if (i < moves.size()) {
                Move move = moves.get(i);
                btn.setText(move.getName());
                addTooltip(btn, move.getDescription());
                final int index = i;
                btn.setOnAction(e -> handlePlayerAttack(index));
            } else {
                btn.setText("--");
                btn.setDisable(true);
            }
        }
    }

    private void wirePlaceholderButtons() {
        mainAnchor.getChildren().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .forEach(this::addHoverScale);

        for (AnchorPane pane : new AnchorPane[]{pressAttack, pressDefense, pressItem, pressTalk}) {
            pane.getChildren().stream()
                    .filter(n -> n instanceof Button &&
                            ((Button) n).getText().equals("GO BACK"))
                    .map(n -> (Button) n)
                    .forEach(btn -> { addHoverScale(btn); addTooltip(btn, "Go back."); });
        }

        List<Button> itemButtons = pressItem.getChildren().stream()
                .filter(n -> n instanceof Button &&
                        !((Button) n).getText().equals("GO BACK"))
                .map(n -> (Button) n)
                .collect(Collectors.toList());

        String[] itemLabels = {"item1", "item2", "item3", "item4"};
        for (int i = 0; i < itemButtons.size(); i++) {
            String label = itemLabels[i];
            Button btn = itemButtons.get(i);
            addHoverScale(btn);
            addTooltip(btn, "Use " + label + ". Not implemented yet.");
            btn.setOnAction(e -> { log("Used " + label + " (not implemented yet)."); goBack(); });
        }

        List<Button> defenseButtons = pressDefense.getChildren().stream()
                .filter(n -> n instanceof Button &&
                        !((Button) n).getText().equals("GO BACK"))
                .map(n -> (Button) n)
                .collect(Collectors.toList());

        if (defenseButtons.size() >= 1) {
            guardBtn = defenseButtons.get(0);
            addHoverScale(guardBtn);
            addTooltip(guardBtn, "Guard: 55% chance to block the boss attack. Once per turn. 3 turn cooldown.");
            guardBtn.setOnAction(e -> {
                if (!engine.isGuardAvailable()) { log("Guard is on cooldown."); return; }
                if (guardUsedThisTurn) { log("Already used guard this turn."); return; }
                guardUsedThisTurn = true;
                engine.activateGuard();
                log("Guard ready. 55% chance to block the boss attack this turn.");
                goBack();
            });
        }

        if (defenseButtons.size() >= 2) {
            counterBtn = defenseButtons.get(1);
            addHoverScale(counterBtn);
            addTooltip(counterBtn, "Counter: 30% chance to negate the boss attack. No cooldown.");
            counterBtn.setOnAction(e -> {
                if (guardUsedThisTurn) { log("Already used guard or counter this turn."); return; }
                guardUsedThisTurn = true;
                engine.activateCounter();
                log("Counter ready. 30% chance to negate the boss attack this turn.");
                goBack();
            });
        }

        List<Button> talkButtons = pressTalk.getChildren().stream()
                .filter(n -> n instanceof Button &&
                        !((Button) n).getText().equals("GO BACK"))
                .map(n -> (Button) n)
                .collect(Collectors.toList());

        if (talkButtons.size() >= 1) {
            addHoverScale(talkButtons.get(0));
            addTooltip(talkButtons.get(0), "Talk: 50% half damage, 50% +20% damage. Once per turn.");
            talkButtons.get(0).setOnAction(e -> { log(engine.activateTalk()); goBack(); });
        }

        if (talkButtons.size() >= 2) {
            addHoverScale(talkButtons.get(1));
            addTooltip(talkButtons.get(1), "Insult: 35% only 30% damage, 65% double damage. Once per turn.");
            talkButtons.get(1).setOnAction(e -> { log(engine.activateInsult()); goBack(); });
        }
    }

    // ── PLAYER ATTACK FLOW ────────────────────────────────────────────

    private void handlePlayerAttack(int moveIndex) {
        if (!engine.isOngoing()) return;
        lockAllActions(true);
        showBossThinking();

        PauseTransition waitThink = new PauseTransition(Duration.millis(800));
        waitThink.setOnFinished(e -> {
            TurnLog turnLog = engine.processTurnByIndex(moveIndex, null);
            AnchorPane bossPane = (AnchorPane) enemycharacterSprite.getParent();

            if (turnLog.getPlayerDamageDealt() > 0) {
                flashHit(enemycharacterSprite);
                spawnDamageLabel("-" + turnLog.getPlayerDamageDealt(),
                        bossPane, Color.RED, 70, 90, 26);
            }

            PauseTransition afterPlayerHit = new PauseTransition(Duration.millis(500));
            afterPlayerHit.setOnFinished(ev -> {
                tweenHpBar(bossHP, turnLog.getBossHpAfter(), bossMaxHp, BOSS_BAR_MAX);
                bossHPnumber.setText(turnLog.getBossHpAfter() + " / " + bossMaxHp);

                // revert to mood sprite — safe here, executeBossTurn will override
                updateBossSpriteMood();

                PauseTransition waitBoss = new PauseTransition(Duration.millis(600));
                waitBoss.setOnFinished(evv -> executeBossTurn(turnLog));
                waitBoss.play();
            });
            afterPlayerHit.play();
        });
        waitThink.play();
    }

    // ── BOSS TURN EXECUTION ───────────────────────────────────────────

    private void executeBossTurn(TurnLog turnLog) {
        // cancel thinking revert — we own the sprite from here
        if (thinkingRevertTimer != null) {
            thinkingRevertTimer.stop();
            thinkingRevertTimer = null;
        }

        List<Integer> hits = engine.getLastBossHitList();
        String hitStyle    = engine.getLastBossMoveHitStyle();
        String abilityPath = boss.getCurrentAbilitySprite();

        AnchorPane playerPane = (AnchorPane) playercharacterSprite.getParent();
        AnchorPane bossPane   = (AnchorPane) enemycharacterSprite.getParent();

        // show ability sprite only if path is set — never fall back to mood sprite here
        if (abilityPath != null && !abilityPath.isEmpty()) {
            loadSpriteOnto(enemycharacterSprite, abilityPath);
        }

        // boss defeated before acting
        if (turnLog.getBossMoveName() == null) {
            boss.clearAbilitySprite();
            updateBossSpriteMood();
            finishTurnUpdate(turnLog);
            return;
        }

        if ("STUNNED".equals(turnLog.getBossMoveName())) {
            spawnDamageLabel("STUNNED", bossPane, Color.GOLD, 50, 80, 18);
            PauseTransition done = new PauseTransition(Duration.millis(800));
            done.setOnFinished(e -> {
                boss.clearAbilitySprite();
                updateBossSpriteMood();
                finishTurnUpdate(turnLog);
            });
            done.play();

        } else if ("clone".equals(hitStyle)) {
            showCloneEffect(bossPane, turnLog);

        } else if ("heal".equals(hitStyle)) {
            spawnDamageLabel("+80 HP", bossPane, Color.LIMEGREEN, 55, 80, 20);
            PauseTransition healPause = new PauseTransition(Duration.millis(700));
            healPause.setOnFinished(e -> {
                tweenHpBar(bossHP, turnLog.getBossHpAfter(), bossMaxHp, BOSS_BAR_MAX);
                bossHPnumber.setText(turnLog.getBossHpAfter() + " / " + bossMaxHp);
                boss.clearAbilitySprite();
                updateBossSpriteMood();
                finishTurnUpdate(turnLog);
            });
            healPause.play();

        } else if (!hits.isEmpty()) {
            if ("rapid".equals(hitStyle)) {
                animateRapidHits(hits, playerPane, turnLog);
            } else {
                animateSingleHit(hits, playerPane, turnLog);
            }
        } else {
            boss.clearAbilitySprite();
            updateBossSpriteMood();
            finishTurnUpdate(turnLog);
        }
    }

    private void animateRapidHits(List<Integer> hits, AnchorPane playerPane, TurnLog turnLog) {
        int startHp = turnLog.getPlayerHpAfter() +
                hits.stream().mapToInt(Integer::intValue).sum();
        int[] displayHp = {Math.min(startHp, playerMaxHp)};

        int delayPerHit = 120;
        Timeline rapid  = new Timeline();

        for (int i = 0; i < hits.size(); i++) {
            final int hitVal = hits.get(i);
            double ox = -30 + (Math.random() * 60);
            double oy = 55  + (Math.random() * 55);

            KeyFrame kf = new KeyFrame(Duration.millis((long) i * delayPerHit), ev -> {
                flashHit(playercharacterSprite);
                spawnDamageLabel("-" + hitVal, playerPane,
                        Color.ORANGERED, 50 + ox, oy, 20);

                displayHp[0] = Math.max(0, displayHp[0] - hitVal);
                tweenHpBar(playerHP, displayHp[0], playerMaxHp, PLAYER_BAR_MAX);
                if (playerHpLabel != null)
                    playerHpLabel.setText(displayHp[0] + " / " + playerMaxHp);
            });
            rapid.getKeyFrames().add(kf);
        }

        long totalMs = (long) hits.size() * delayPerHit + 500;
        rapid.getKeyFrames().add(new KeyFrame(Duration.millis(totalMs), ev -> {
            // snap to exact final value
            tweenHpBar(playerHP, turnLog.getPlayerHpAfter(), playerMaxHp, PLAYER_BAR_MAX);
            if (playerHpLabel != null)
                playerHpLabel.setText(turnLog.getPlayerHpAfter() + " / " + playerMaxHp);
            boss.clearAbilitySprite();
            updateBossSpriteMood();
            finishTurnUpdate(turnLog);
        }));
        rapid.play();
    }

    private void animateSingleHit(List<Integer> hits, AnchorPane playerPane, TurnLog turnLog) {
        int totalDmg = hits.stream().mapToInt(Integer::intValue).sum();

        PauseTransition pre = new PauseTransition(Duration.millis(400));
        pre.setOnFinished(e -> {
            flashHit(playercharacterSprite);
            spawnDamageLabel("-" + totalDmg, playerPane, Color.RED, 55, 85, 30);

            PauseTransition post = new PauseTransition(Duration.millis(550));
            post.setOnFinished(ev -> {
                tweenHpBar(playerHP, turnLog.getPlayerHpAfter(), playerMaxHp, PLAYER_BAR_MAX);
                if (playerHpLabel != null)
                    playerHpLabel.setText(turnLog.getPlayerHpAfter() + " / " + playerMaxHp);
                boss.clearAbilitySprite();
                updateBossSpriteMood();
                finishTurnUpdate(turnLog);
            });
            post.play();
        });
        pre.play();
    }

    private void showCloneEffect(AnchorPane bossPane, TurnLog turnLog) {
        spawnDamageLabel("CLONE", bossPane, Color.PURPLE, 25, 65, 16);
        spawnDamageLabel("CLONE", bossPane, Color.PURPLE, 95, 80, 16);
        spawnDamageLabel("CLONE", bossPane, Color.PURPLE, 60, 50, 16);

        PauseTransition done = new PauseTransition(Duration.millis(1000));
        done.setOnFinished(e -> {
            tweenHpBar(bossHP, turnLog.getBossHpAfter(), bossMaxHp, BOSS_BAR_MAX);
            bossHPnumber.setText(turnLog.getBossHpAfter() + " / " + bossMaxHp);
            boss.clearAbilitySprite();
            updateBossSpriteMood();
            finishTurnUpdate(turnLog);
        });
        done.play();
    }

    // ── SPRITE MANAGEMENT ─────────────────────────────────────────────

    private void showBossThinking() {
        String thinkPath = boss.getThinkingSprite();
        if (thinkPath != null && !thinkPath.isEmpty()) {
            loadSpriteOnto(enemycharacterSprite, thinkPath);
        }
        thinkingRevertTimer = new PauseTransition(Duration.millis(600));
        thinkingRevertTimer.setOnFinished(e -> {
            thinkingRevertTimer = null;
            // only revert if no ability sprite is active
            if (boss.getCurrentAbilitySprite().isEmpty()) {
                updateBossSpriteMood();
            }
        });
        thinkingRevertTimer.play();
    }

    // loads any sprite path onto the ImageView, fills the parent pane
    private void loadSpriteOnto(ImageView view, String path) {
        if (path == null || path.isEmpty()) return;
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) { System.out.println("Sprite not found: " + path); return; }
        AnchorPane pane = (AnchorPane) view.getParent();
        view.setImage(new Image(is));
        view.setFitWidth(pane.getPrefWidth());
        view.setFitHeight(pane.getPrefHeight());
        view.setPreserveRatio(true);
        AnchorPane.setLeftAnchor(view,   0.0);
        AnchorPane.setRightAnchor(view,  0.0);
        AnchorPane.setTopAnchor(view,    0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
    }

    // always uses mood sprite — never uses ability sprite path
    private void updateBossSpriteMood() {
        loadSpriteOnto(enemycharacterSprite, boss.getCurrentSprite());
    }

    // ── FLOATING DAMAGE LABEL ───────────────────────────────────

    private void spawnDamageLabel(String text, AnchorPane parent,
                                   Color color, double x, double y, double size) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, size));
        lbl.setTextFill(color);
        lbl.setStyle("-fx-effect: dropshadow(gaussian, black, 4, 0.6, 1, 1);");
        lbl.setLayoutX(x);
        lbl.setLayoutY(y);
        parent.getChildren().add(lbl);

        TranslateTransition rise = new TranslateTransition(Duration.millis(900), lbl);
        rise.setByY(-65);

        FadeTransition fade = new FadeTransition(Duration.millis(900), lbl);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(rise, fade);
        pt.setOnFinished(e -> parent.getChildren().remove(lbl));
        pt.play();
    }

    // ── TURN FINISH ───────────────────────────────────────────────────

    private void finishTurnUpdate(TurnLog turnLog) {
        guardUsedThisTurn = false;
        setTurnNr(turnLog.getRoundNumber());
        updateStatusLabels();
        updateCooldownUI();

        if (playerHpLabel != null)
            playerHpLabel.setText(turnLog.getPlayerHpAfter() + " / " + playerMaxHp);

        StringBuilder sb = new StringBuilder();
        sb.append("Round ").append(turnLog.getRoundNumber()).append("\n");

        if ("STUNNED".equals(turnLog.getPlayerMoveName())) {
            sb.append("You are stunned. Turn skipped.\n");
        } else if (turnLog.getItemUsed() != null) {
            sb.append("You used ").append(turnLog.getItemUsed())
              .append(". Restored ").append(turnLog.getPlayerHpRestored()).append(" HP.\n");
        } else if (turnLog.getPlayerMoveName() != null) {
            sb.append("You used ").append(turnLog.getPlayerMoveName())
              .append(". Dealt ").append(turnLog.getPlayerDamageDealt()).append(" damage.\n");
        }

        StatusEffect pe = player.getActiveEffect();
        StatusEffect be = boss.getActiveEffect();
        if (pe != null) sb.append("Status on you: ").append(pe.getLabel()).append("\n");
        if (be != null) sb.append("Status on ").append(boss.getName())
                          .append(": ").append(be.getLabel()).append("\n");

        if ("STUNNED".equals(turnLog.getBossMoveName())) {
            sb.append(boss.getName()).append(" is stunned. Their turn skipped.\n");
        } else if ("clone".equals(engine.getLastBossMoveHitStyle())) {
            sb.append(boss.getName()).append(" used Twining. HP doubled.\n");
        } else if ("heal".equals(engine.getLastBossMoveHitStyle())) {
            sb.append(boss.getName()).append(" repaired systems. Healed 80 HP.\n");
        } else if (turnLog.getBossMoveName() != null) {
            sb.append(boss.getName()).append(" used ").append(turnLog.getBossMoveName())
              .append(". Dealt ").append(turnLog.getBossDamageDealt()).append(" damage.\n");
        } else {
            sb.append(boss.getName()).append(" was defeated before acting.\n");
        }

        sb.append("Your HP: ").append(turnLog.getPlayerHpAfter())
          .append(" / ").append(playerMaxHp)
          .append("  |  Boss HP: ").append(turnLog.getBossHpAfter())
          .append(" / ").append(bossMaxHp).append("\n");

        log(sb.toString());

        boolean combatOver =
                turnLog.getResultAfterRound() == CombatResult.PLAYER_WIN ||
                turnLog.getResultAfterRound() == CombatResult.PLAYER_LOSE;

        // only unlock buttons if combat is still going
        if (!combatOver) lockAllActions(false);

        if (turnLog.getResultAfterRound() == CombatResult.PLAYER_WIN)
            onCombatEnd(true);
        else if (turnLog.getResultAfterRound() == CombatResult.PLAYER_LOSE)
            onCombatEnd(false);
    }

    private void updateCooldownUI() {
        if (abilityButtons != null && abilityButtons.size() >= 4) {
            Button move4  = abilityButtons.get(3);
            boolean avail = engine.isMove4Available();
            move4.setDisable(!avail);
            move4.setOpacity(avail ? 1.0 : 0.4);
        }
        if (guardBtn != null) {
            boolean avail = engine.isGuardAvailable();
            guardBtn.setDisable(!avail);
            guardBtn.setOpacity(avail ? 1.0 : 0.4);
        }
    }

    private void lockAllActions(boolean lock) {
        mainAnchor.setDisable(lock);
        pressAttack.setDisable(lock);
        pressDefense.setDisable(lock);
        pressItem.setDisable(lock);
        pressTalk.setDisable(lock);

        if (lock) {
            pressAttack.setVisible(false);
            pressDefense.setVisible(false);
            pressItem.setVisible(false);
            pressTalk.setVisible(false);
            mainAnchor.setVisible(true);
        }
    }

    private void updateStatusLabels() {
        StatusEffect pe = player.getActiveEffect();
        StatusEffect be = boss.getActiveEffect();
        if (playerStatusLabel != null)
            playerStatusLabel.setText(pe != null ? pe.getLabel() : "");
        if (bossStatusLabel != null)
            bossStatusLabel.setText(be != null ? be.getLabel() : "");
    }

    private void tweenHpBar(Rectangle bar, int currentHp, int maxHp, double barMax) {
        double target = Math.max(0, barMax * ((double) currentHp / maxHp));
        new Timeline(
            new KeyFrame(Duration.ZERO,
                    new KeyValue(bar.widthProperty(), bar.getWidth())),
            new KeyFrame(Duration.millis(400),
                    new KeyValue(bar.widthProperty(), target, Interpolator.EASE_BOTH))
        ).play();
    }

    private void flashHit(ImageView sprite) {
        FadeTransition flash = new FadeTransition(Duration.millis(60), sprite);
        flash.setFromValue(1.0);
        flash.setToValue(0.1);
        flash.setCycleCount(6);
        flash.setAutoReverse(true);
        flash.play();
    }

    private void addHoverScale(Node node) {
        ScaleTransition up   = new ScaleTransition(Duration.millis(100), node);
        up.setToX(1.06); up.setToY(1.06);
        ScaleTransition down = new ScaleTransition(Duration.millis(100), node);
        down.setToX(1.0); down.setToY(1.0);
        node.setOnMouseEntered(e -> up.playFromStart());
        node.setOnMouseExited(e  -> down.playFromStart());
    }

    private void addTooltip(Node node, String text) {
        Tooltip tip = new Tooltip(text);
        tip.setWrapText(true);
        tip.setMaxWidth(220);
        tip.setStyle("-fx-font-size: 11px;");
        tip.setShowDelay(Duration.millis(300));
        Tooltip.install(node, tip);
    }

    // ── COMBAT END + SCENE TRANSITION ────────────────────────────────

    public void onCombatEnd(boolean playerWon) {
        // stop any pending timers
        if (thinkingRevertTimer != null) {
            thinkingRevertTimer.stop();
            thinkingRevertTimer = null;
        }

        lockAllActions(true);
        boss.clearAbilitySprite();

        if (player.getActiveEffect() != null) player.applyEffect(null);
        if (boss.getActiveEffect()   != null) boss.applyEffect(null);
        if (playerStatusLabel != null) playerStatusLabel.setText("");
        if (bossStatusLabel   != null) bossStatusLabel.setText("");
        if (bossIntentLabel   != null) bossIntentLabel.setText("");

        if (playerWon) {
            log("Victory. " + boss.getName() + " defeated. Loading next area...");
        } else {
            log("Defeated. " + player.getName() + " has fallen. Game over.");
        }

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            if (playerWon) {
                loadNextArea();
            } else {
                // player lost — handle game over here if needed
                System.out.println("GAME OVER");
            }
        });
        delay.play();
    }

    // loads the next map based on which boss was just defeated
    private void loadNextArea() {
    String bossId  = boss.getId();
    String nextMap = BOSS_NEXT_MAP.get(bossId);

    if ("JohnMKati".equals(bossId)) {
        // Here you can set what happens after JohnMKati is defeated (load room etc)
        System.out.println("JohnMKati defeated — end of boss chain.");
        // for now just return to game without map change
        GameScreen gs = com.dungeons.screens.GameScreen.getInstance();
        if (gs != null) gs.returnFromCombat();
        return;
    }

    if (nextMap == null) {
        System.out.println("No next map defined for boss: " + bossId);
        return;
    }

    com.dungeons.screens.GameScreen gs = com.dungeons.screens.GameScreen.getInstance();
    if (gs != null) {
        gs.returnFromCombatWithMap(nextMap);
    } else {
        System.out.println("GameScreen instance not found.");
    }
}

    private void log(String text) { turnInformation.appendText(text + "\n"); }

    public void setStart(String playerNamee, String bossNamee, int bossMaxHp) {
        playername.setText(playerNamee);
        bossName.setText(bossNamee);
        bossHPnumber.setText(bossMaxHp + " / " + bossMaxHp);
        updateBossSpriteMood();
    }

    public void updateBossHP(int currentHp, int maxHp) {
        tweenHpBar(bossHP, currentHp, maxHp, BOSS_BAR_MAX);
        bossHPnumber.setText(currentHp + " / " + maxHp);
    }

    public void updatePlayerHP(int currentHp, int maxHp) {
        tweenHpBar(playerHP, currentHp, maxHp, PLAYER_BAR_MAX);
    }

    public void setTurnNr(int n)        { turnNumber.setText("Turn: " + n); }
    public void setTurnLog(String text) { log(text); }

    @FXML public void openAttack() {
        mainAnchor.setVisible(false);  mainAnchor.setDisable(true);
        pressAttack.setVisible(true);  pressAttack.setDisable(false);
    }

    @FXML public void openDefense() {
        mainAnchor.setVisible(false);   mainAnchor.setDisable(true);
        pressDefense.setVisible(true);  pressDefense.setDisable(false);
    }

    @FXML public void openItems() {
        mainAnchor.setVisible(false); mainAnchor.setDisable(true);
        pressItem.setVisible(true);   pressItem.setDisable(false);
    }

    @FXML public void openTalk() {
        mainAnchor.setVisible(false);  mainAnchor.setDisable(true);
        pressTalk.setVisible(true);    pressTalk.setDisable(false);
    }

    @FXML public void goBack() {
        pressAttack.setVisible(false);  pressAttack.setDisable(true);
        pressDefense.setVisible(false); pressDefense.setDisable(true);
        pressItem.setVisible(false);    pressItem.setDisable(true);
        pressTalk.setVisible(false);    pressTalk.setDisable(true);
        mainAnchor.setVisible(true);    mainAnchor.setDisable(false);
    }
}
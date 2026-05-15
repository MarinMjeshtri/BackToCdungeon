package com.dungeons.Controllers;

import com.dungeons.MusicandSoundsCode.GameMusicManager;
import com.dungeons.systems.CombatSystem.*;
import com.dungeons.screens.GameScreen;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import com.dungeons.shopItemsManager.PlayerInventory;
import com.dungeons.shopItemsManager.Shop;

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
    @FXML private Label turnLogTitle;
    @FXML private TextArea turnInformation;
    @FXML private AnchorPane turnLogPanel;

    @FXML private Rectangle bossHP;
    @FXML private Rectangle playerHP;

    @FXML private ImageView playercharacterSprite;
    @FXML private ImageView enemycharacterSprite;

    private static final double BOSS_BAR_MAX   = 435.0;
    private static final double PLAYER_BAR_MAX = 355.0;

    private CombatEngine engine;
    private Player player;
    private BossLoader boss;
    private PlayerProgress progress;
    private int playerMaxHp;
    private int bossMaxHp;

    private Label playerStatusLabel;
    private Label bossStatusLabel;
    private HBox playerStatusBadges;
    private HBox bossStatusBadges;
    private Label bossIntentLabel;
    private Label playerHpLabel;

    private List<Button> abilityButtons;
    private Button guardBtn;
    private Button counterBtn;

    private boolean guardUsedThisTurn = false;
    private int pendingAtkRestore = -1; // -1 means no ATK boost pending

    private PauseTransition thinkingRevertTimer = null;
    private Timeline turnLogPulse = null;

    @FXML
    public void initialize() {
        setupTurnLog();
    }

    private void setupTurnLog() {
        if (turnInformation != null) {
            turnInformation.setWrapText(true);
            turnInformation.setFocusTraversable(false);
        }
        if (turnLogTitle != null) {
            FadeTransition blink = new FadeTransition(Duration.millis(1100), turnLogTitle);
            blink.setFromValue(0.72);
            blink.setToValue(1.0);
            blink.setAutoReverse(true);
            blink.setCycleCount(Animation.INDEFINITE);
            blink.play();
        }
    }

    private void pulseTurnLog() {
        if (turnLogPanel == null) return;
        if (turnLogPulse != null) turnLogPulse.stop();

        turnLogPulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(turnLogPanel.scaleXProperty(), 1.0),
                        new KeyValue(turnLogPanel.scaleYProperty(), 1.0),
                        new KeyValue(turnLogPanel.opacityProperty(), 0.92)),
                new KeyFrame(Duration.millis(120),
                        new KeyValue(turnLogPanel.scaleXProperty(), 1.012),
                        new KeyValue(turnLogPanel.scaleYProperty(), 1.012),
                        new KeyValue(turnLogPanel.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(360),
                        new KeyValue(turnLogPanel.scaleXProperty(), 1.0),
                        new KeyValue(turnLogPanel.scaleYProperty(), 1.0),
                        new KeyValue(turnLogPanel.opacityProperty(), 1.0))
        );
        turnLogPulse.playFromStart();
    }

    private void updatePlayerSpriteMood() {
        if (player.isDefeated()) {
            loadSpriteOnto(playercharacterSprite, player.getSpriteDefeated());
        } else {
            loadSpriteOnto(playercharacterSprite, player.getSpriteNeutral());
        }
    }
    private void refreshItemButtons(List<Button> buttons) {
        PlayerInventory inventory = PlayerInventory.getInstance();
        for (int i = 0; i < buttons.size(); i++) {
            Button btn = buttons.get(i);
            Shop item  = inventory.getSlot(i);
            if (item != null) {
                btn.setText(item.displayName);
                btn.setDisable(false);
                btn.setOpacity(1.0);
                addTooltip(btn, item.desc);
            } else {
                btn.setText("Empty");
                btn.setDisable(true);
                btn.setOpacity(0.4);
            }
        }
    }

    private void handleItemUse(int slotIndex, List<Button> itemButtons) {
        if (!engine.isOngoing()) return;

        PlayerInventory inventory = PlayerInventory.getInstance();
        Shop itemBeforeUse = inventory.getSlot(slotIndex);

        if (activateNewCombatItem(itemBeforeUse)) {
            inventory.clearSlot(slotIndex);
            finishItemUse(itemButtons);
            return;
        }

        // save original ATK in case ATK potion is used - restored after turn
        int originalAtk = player.getAttack();
        int hpBeforeItem = player.getCurrentHp();

        PlayerInventory.CombatItemResult result =
                inventory.useItemInCombat(slotIndex, player);

        switch (result) {
            case EMPTY:
                log("Slot " + (slotIndex + 1) + " is empty.");
                return; // no turn consumed

            case NO_EFFECT:
                log("Cannot use that item right now.");
                return; // no turn consumed

            case HEALED:
                int newHp = player.getCurrentHp();
                int restored = Math.max(0, newHp - hpBeforeItem);
                tweenHpBar(playerHP, newHp, playerMaxHp, PLAYER_BAR_MAX);
                if (playerHpLabel != null)
                    playerHpLabel.setText(newHp + " / " + playerMaxHp);
                playHealEffect(playerHP, (AnchorPane) playercharacterSprite.getParent(),
                        "+" + restored + " HP", 70, 88, 24);
                log("Used healing item. Restored HP. Now at " + newHp + "/" + playerMaxHp + ".");
                break;

            case ATK_BOOST:
                log("Used ATK potion. Attack boosted this turn by x"
                        + PlayerInventory.ATK_BOOST_MULTIPLIER + ".");
                break;

            case SHIELD:
                int shieldAmount = getEffectInt(itemBeforeUse, "shield", 40);
                int shieldTurns = getEffectInt(itemBeforeUse, "turns", 3);
                engine.activateShield(shieldAmount, shieldTurns);
                log("Shield Battery activated. For " + shieldTurns
                        + " turns, up to " + shieldAmount
                        + " incoming damage will be absorbed.");
                break;

            case LIFESTEAL:
                double lifestealPercent = getEffectDouble(itemBeforeUse, "lifesteal", 0.25);
                int lifestealTurns = getEffectInt(itemBeforeUse, "turns", 3);
                engine.activateLifesteal(lifestealPercent, lifestealTurns);
                log("Leech Serum activated. For " + lifestealTurns
                        + " turns, "
                        + (int)(lifestealPercent * 100)
                        + "% of the damage you deal returns as healing.");
                break;

            case MIRROR:
                double reflectPercent = getEffectDouble(itemBeforeUse, "reflect", 0.5);
                int reflectTurns = getEffectInt(itemBeforeUse, "turns", 1);
                engine.activateReflect(reflectPercent, reflectTurns);
                log("Mirror Shard activated. For " + reflectTurns
                        + " turn, "
                        + (int)(reflectPercent * 100)
                        + "% of damage you take is reflected back to the enemy.");
                break;
        }

        finishItemUse(itemButtons);

        if (result == PlayerInventory.CombatItemResult.ATK_BOOST) {
            pendingAtkRestore = originalAtk;
        }
    }

    private boolean activateNewCombatItem(Shop item) {
        if (item == null || item.effects == null) return false;

        if (item.effects.containsKey("shield")) {
            int shieldAmount = getEffectInt(item, "shield", 40);
            int shieldTurns = getEffectInt(item, "turns", 3);
            engine.activateShield(shieldAmount, shieldTurns);
            log("Shield Battery activated. For " + shieldTurns
                    + " turns, up to " + shieldAmount
                    + " incoming damage will be absorbed.");
            return true;
        }

        if (item.effects.containsKey("lifesteal")) {
            double lifestealPercent = getEffectDouble(item, "lifesteal", 0.25);
            int lifestealTurns = getEffectInt(item, "turns", 3);
            engine.activateLifesteal(lifestealPercent, lifestealTurns);
            log("Leech Serum activated. For " + lifestealTurns
                    + " turns, "
                    + (int)(lifestealPercent * 100)
                    + "% of the damage you deal returns as healing.");
            return true;
        }

        if (item.effects.containsKey("reflect")) {
            double reflectPercent = getEffectDouble(item, "reflect", 0.5);
            int reflectTurns = getEffectInt(item, "turns", 1);
            engine.activateReflect(reflectPercent, reflectTurns);
            log("Mirror Shard activated. For " + reflectTurns
                    + " turn, "
                    + (int)(reflectPercent * 100)
                    + "% of damage you take is reflected back to the enemy.");
            return true;
        }

        return false;
    }

    private void finishItemUse(List<Button> itemButtons) {
        // refresh item buttons to show Empty after use
        refreshItemButtons(itemButtons);

        // update HUD overlay so inventory slot goes blank
        com.dungeons.screens.GameScreen gs = com.dungeons.screens.GameScreen.getInstance();
        if (gs != null && gs.getOverlayController() != null) {
            gs.getOverlayController().updateUI();
        }

        // process the turn - moveIndex -2 means item was handled externally
        lockAllActions(true);
        // item is free - does not consume a turn

        updateStatusLabels();
        goBack();
    }

    private int getEffectInt(Shop item, String key, int fallback) {
        return (int)Math.round(getEffectDouble(item, key, fallback));
    }

    private double getEffectDouble(Shop item, String key, double fallback) {
        if (item == null || item.effects == null || !item.effects.containsKey(key)) {
            return fallback;
        }
        return item.effects.get(key);
    }
    private void showPlayerAttackSprite(int moveIndex) {
        loadSpriteOnto(playercharacterSprite, player.getSpriteAttack(moveIndex));
    }
    public void startCombatAtLevel(String bossId, int level) {
        if (playerHP != null) playerHP.setWidth(PLAYER_BAR_MAX);
        if (bossHP != null) bossHP.setWidth(BOSS_BAR_MAX);

        StatsLoader loader = new StatsLoader();
        player = loader.loadPlayer("Player");
        PlayerProgress.getInstance().applyToPlayer(player);
        PlayerProgress progress = PlayerProgress.getInstance();
        if (progress.getCurrentHp() != -1) {
            player.setCurrentHp(progress.getCurrentHp());
        }

        playerMaxHp = player.getMaxHp();
        boss        = loader.loadBossAtLevel(bossId, level);
        bossMaxHp   = boss.getMaxHp();
        engine      = new CombatEngine(player, boss);

        String displayName = boss.getId().startsWith("Mob")
                ? boss.getName() + " Lv." + boss.getMobLevel()
                : boss.getName();
        setStart(player.getName(), displayName, bossMaxHp);
        injectStatusLabels();

        playerHP.setWidth(PLAYER_BAR_MAX * ((double) player.getCurrentHp() / playerMaxHp));

        wireAbilityButtons();
        wirePlaceholderButtons();
        updateCooldownUI();
        turnInformation.setText("");
        log("Combat started. Choose your action.");

        if (GameMusicManager.FINAL_BOSS_ID.equals(bossId)) {
            GameMusicManager.playFinalBoss();
        } else {
            GameMusicManager.playCombat();
        }
    }

    public void startCombat(String bossId) {
        if (playerHP != null) playerHP.setWidth(PLAYER_BAR_MAX);
        if (bossHP != null) bossHP.setWidth(BOSS_BAR_MAX);

        StatsLoader loader = new StatsLoader();
        player = loader.loadPlayer("Player");
        PlayerProgress.getInstance().applyToPlayer(player);
        PlayerProgress progress = PlayerProgress.getInstance();
        if (progress.getCurrentHp() != -1) {
            player.setCurrentHp(progress.getCurrentHp());
        }

        playerMaxHp = player.getMaxHp();
        boss        = loader.loadBoss(bossId);
        bossMaxHp   = boss.getMaxHp();
        engine      = new CombatEngine(player, boss);

        String displayName = boss.getId().startsWith("Mob")
                ? boss.getName() + " Lv." + boss.getMobLevel()
                : boss.getName();
        setStart(player.getName(), displayName, bossMaxHp);
        injectStatusLabels();

        playerHP.setWidth(PLAYER_BAR_MAX * ((double) player.getCurrentHp() / playerMaxHp));

        wireAbilityButtons();
        wirePlaceholderButtons();
        updateCooldownUI();
        turnInformation.setText("");
        log("Combat started. Choose your action.");

        if (GameMusicManager.FINAL_BOSS_ID.equals(bossId)) {
            GameMusicManager.playFinalBoss();
        } else {
            GameMusicManager.playCombat();
        }
    }

    private void injectStatusLabels() {
        AnchorPane bossHpPane   = (AnchorPane) bossHP.getParent();
        AnchorPane playerHpPane = (AnchorPane) playerHP.getParent();
        AnchorPane enemyPane    = (AnchorPane) enemycharacterSprite.getParent();

        if (bossStatusLabel != null)   bossHpPane.getChildren().remove(bossStatusLabel);
        if (playerStatusLabel != null) playerHpPane.getChildren().remove(playerStatusLabel);
        if (bossStatusBadges != null) bossHpPane.getChildren().remove(bossStatusBadges);
        if (playerStatusBadges != null) playerHpPane.getChildren().remove(playerStatusBadges);
        if (playerHpLabel != null)     playerHpPane.getChildren().remove(playerHpLabel);
        enemyPane.getChildren().removeIf(n -> n instanceof Label);

        bossStatusLabel = null;
        playerStatusLabel = null;

        bossStatusBadges = new HBox(5);
        bossStatusBadges.setLayoutX(14);
        bossStatusBadges.setLayoutY(74);
        bossStatusBadges.setMouseTransparent(true);
        bossStatusBadges.setPickOnBounds(false);

        playerStatusBadges = new HBox(5);
        playerStatusBadges.setLayoutX(92);
        playerStatusBadges.setLayoutY(1);
        playerStatusBadges.setMouseTransparent(true);
        playerStatusBadges.setPickOnBounds(false);

        playerHpLabel = new Label(player.getCurrentHp() + " / " + playerMaxHp);
        playerHpLabel.setLayoutX(4);
        playerHpLabel.setLayoutY(16);
        playerHpLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #111; -fx-font-weight: bold;");

        bossIntentLabel = new Label("");
        bossIntentLabel.setLayoutX(10);
        bossIntentLabel.setLayoutY(10);
        bossIntentLabel.setStyle("-fx-text-fill: #222; -fx-font-size: 11px; " +
                "-fx-background-color: rgba(255,255,255,0.8); -fx-padding: 2 5 2 5;");

        bossHpPane.getChildren().add(bossStatusBadges);
        playerHpPane.getChildren().add(playerStatusBadges);
        playerHpPane.getChildren().add(playerHpLabel);
        enemyPane.getChildren().add(bossIntentLabel);
        updateStatusLabels();
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

        refreshItemButtons(itemButtons);
        for (int i = 0; i < itemButtons.size(); i++) {
            Button btn = itemButtons.get(i);
            addHoverScale(btn);
            final int slotIndex = i;
            btn.setOnAction(e -> handleItemUse(slotIndex, itemButtons));
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

    private void handlePlayerAttack(int moveIndex) {
        if (!engine.isOngoing()) return;
        lockAllActions(true);
        showPlayerAttackSprite(moveIndex);
        showBossThinking();

        PauseTransition waitThink = new PauseTransition(Duration.millis(800));
        waitThink.setOnFinished(e -> {
            TurnLog turnLog = engine.processTurnByIndex(moveIndex, null);

            List<Move> moves = player.getMoves();
            if (moveIndex < moves.size()) {
                GameMusicManager.playMoveSound(moves.get(moveIndex).getName());
                // restore ATK boost from item if one was used this turn
                if (pendingAtkRestore != -1) {
                    player.setAttack(pendingAtkRestore);
                    pendingAtkRestore = -1;
                }
            }

            AnchorPane bossPane = (AnchorPane) enemycharacterSprite.getParent();

            if (turnLog.getPlayerDamageDealt() > 0) {

                GameMusicManager.playHitSound();
                flashHit(enemycharacterSprite);
                playDamageEffect(bossHP, bossPane,
                        "-" + turnLog.getPlayerDamageDealt(), 70, 90, 26);
            }

            PauseTransition afterPlayerHit = new PauseTransition(Duration.millis(500));
            afterPlayerHit.setOnFinished(ev -> {
                tweenHpBar(bossHP, turnLog.getBossHpAfter(), bossMaxHp, BOSS_BAR_MAX);
                bossHPnumber.setText(turnLog.getBossHpAfter() + " / " + bossMaxHp);

                if (engine.getLastLifestealHeal() > 0) {
                    tweenHpBar(playerHP, player.getCurrentHp(), playerMaxHp, PLAYER_BAR_MAX);
                    if (playerHpLabel != null)
                        playerHpLabel.setText(player.getCurrentHp() + " / " + playerMaxHp);
                    playHealEffect(playerHP, (AnchorPane) playercharacterSprite.getParent(),
                            "+" + engine.getLastLifestealHeal() + " HP", 54, 88, 22);
                }

                updateBossSpriteMood();
                updatePlayerSpriteMood();

                PauseTransition waitBoss = new PauseTransition(Duration.millis(600));
                waitBoss.setOnFinished(evv -> executeBossTurn(turnLog));
                waitBoss.play();
            });
            afterPlayerHit.play();
        });
        waitThink.play();
    }

    private void executeBossTurn(TurnLog turnLog) {
        if (thinkingRevertTimer != null) {
            thinkingRevertTimer.stop();
            thinkingRevertTimer = null;
        }

        List<Integer> hits = engine.getLastBossHitList();
        String hitStyle    = engine.getLastBossMoveHitStyle();
        String abilityPath = boss.getCurrentAbilitySprite();

        AnchorPane playerPane = (AnchorPane) playercharacterSprite.getParent();
        AnchorPane bossPane   = (AnchorPane) enemycharacterSprite.getParent();

        if (abilityPath != null && !abilityPath.isEmpty()) {
            loadSpriteOnto(enemycharacterSprite, abilityPath);
        }

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
            final TurnLog log = turnLog;
            int healAmount = 80;
            final int finalHealAmount = healAmount;

            int preHealHp = Math.max(0, log.getBossHpAfter() - finalHealAmount);
            bossHP.setWidth(BOSS_BAR_MAX * ((double) preHealHp / bossMaxHp));
            bossHPnumber.setText(preHealHp + " / " + bossMaxHp);

            PauseTransition wait = new PauseTransition(Duration.millis(300));
            wait.setOnFinished(e -> {
                playHealEffect(bossHP, bossPane, "+" + finalHealAmount + " HP", 55, 80, 20);
                PauseTransition afterPopup = new PauseTransition(Duration.millis(400));
                afterPopup.setOnFinished(ev -> {
                    tweenHpBar(bossHP, log.getBossHpAfter(), bossMaxHp, BOSS_BAR_MAX);
                    bossHPnumber.setText(log.getBossHpAfter() + " / " + bossMaxHp);
                    boss.clearAbilitySprite();
                    updateBossSpriteMood();
                    finishTurnUpdate(log);
                });
                afterPopup.play();
            });
            wait.play();

        } else if (!hits.isEmpty()) {

            GameMusicManager.playHitSound();

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

        int delayPerHit = 50;
        Timeline rapid  = new Timeline();

        for (int i = 0; i < hits.size(); i++) {
            final int hitVal = hits.get(i);
            double ox = -30 + (Math.random() * 60);
            double oy = 55  + (Math.random() * 55);

            KeyFrame kf = new KeyFrame(Duration.millis((long) i * delayPerHit), ev -> {
                flashHit(playercharacterSprite);
                if (hitVal > 0) {
                    playDamageEffect(playerHP, playerPane,
                            "-" + hitVal, 50 + ox, oy, 20);
                } else {
                    spawnDamageLabel("BLOCK", playerPane,
                            Color.CYAN, 35 + ox, oy, 16);
                }

                displayHp[0] = Math.max(0, displayHp[0] - hitVal);
                tweenHpBar(playerHP, displayHp[0], playerMaxHp, PLAYER_BAR_MAX);
                if (playerHpLabel != null)
                    playerHpLabel.setText(displayHp[0] + " / " + playerMaxHp);
            });
            rapid.getKeyFrames().add(kf);
        }

        long totalMs = (long) hits.size() * delayPerHit + 500;
        rapid.getKeyFrames().add(new KeyFrame(Duration.millis(totalMs), ev -> {
            tweenHpBar(playerHP, turnLog.getPlayerHpAfter(), playerMaxHp, PLAYER_BAR_MAX);
            if (playerHpLabel != null)
                playerHpLabel.setText(turnLog.getPlayerHpAfter() + " / " + playerMaxHp);
            tweenHpBar(bossHP, turnLog.getBossHpAfter(), bossMaxHp, BOSS_BAR_MAX);
            bossHPnumber.setText(turnLog.getBossHpAfter() + " / " + bossMaxHp);
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
            if (totalDmg > 0) {
                playDamageEffect(playerHP, playerPane, "-" + totalDmg, 55, 85, 30);
            } else {
                spawnDamageLabel("BLOCK", playerPane, Color.CYAN, 35, 85, 24);
            }

            PauseTransition post = new PauseTransition(Duration.millis(550));
            post.setOnFinished(ev -> {
                tweenHpBar(playerHP, turnLog.getPlayerHpAfter(), playerMaxHp, PLAYER_BAR_MAX);
                if (playerHpLabel != null)
                    playerHpLabel.setText(turnLog.getPlayerHpAfter() + " / " + playerMaxHp);
                tweenHpBar(bossHP, turnLog.getBossHpAfter(), bossMaxHp, BOSS_BAR_MAX);
                bossHPnumber.setText(turnLog.getBossHpAfter() + " / " + bossMaxHp);
                boss.clearAbilitySprite();
                updateBossSpriteMood();
                finishTurnUpdate(turnLog);
            });
            post.play();
        });
        pre.play();
    }

    private void showCloneEffect(AnchorPane bossPane, TurnLog turnLog) {

        GameMusicManager.playMoveSound("clone");

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

    private void showBossThinking() {
        String thinkPath = boss.getThinkingSprite();
        if (thinkPath != null && !thinkPath.isEmpty()) {
            loadSpriteOnto(enemycharacterSprite, thinkPath);
        }
        thinkingRevertTimer = new PauseTransition(Duration.millis(600));
        thinkingRevertTimer.setOnFinished(e -> {
            thinkingRevertTimer = null;
            if (boss.getCurrentAbilitySprite().isEmpty()) {
                updateBossSpriteMood();
            }
        });
        thinkingRevertTimer.play();
    }

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

    private void updateBossSpriteMood() {
        loadSpriteOnto(enemycharacterSprite, boss.getCurrentSprite());
    }

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

    private void playHealEffect(Rectangle hpBar, AnchorPane parent,
                                String text, double x, double y, double size) {
        spawnDamageLabel(text, parent, Color.LIMEGREEN, x, y, size);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.LIMEGREEN);
        glow.setRadius(18);
        glow.setSpread(0.35);
        hpBar.setEffect(glow);

        ScaleTransition grow = new ScaleTransition(Duration.millis(120), hpBar);
        grow.setToY(1.18);

        ScaleTransition settle = new ScaleTransition(Duration.millis(220), hpBar);
        settle.setToY(1.0);

        PauseTransition clearGlow = new PauseTransition(Duration.millis(520));
        clearGlow.setOnFinished(e -> hpBar.setEffect(null));

        SequentialTransition pulse = new SequentialTransition(grow, settle);
        pulse.play();
        clearGlow.play();
    }

    private void playDamageEffect(Rectangle hpBar, AnchorPane parent,
                                  String text, double x, double y, double size) {
        spawnDamageLabel(text, parent, Color.ORANGERED, x, y, size);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.RED);
        glow.setRadius(18);
        glow.setSpread(0.38);
        hpBar.setEffect(glow);

        ScaleTransition hit = new ScaleTransition(Duration.millis(90), hpBar);
        hit.setToY(0.82);

        ScaleTransition settle = new ScaleTransition(Duration.millis(230), hpBar);
        settle.setToY(1.0);

        PauseTransition clearGlow = new PauseTransition(Duration.millis(520));
        clearGlow.setOnFinished(e -> hpBar.setEffect(null));

        SequentialTransition pulse = new SequentialTransition(hit, settle);
        pulse.play();
        clearGlow.play();
    }

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

        if (engine.getLastLifestealAmount() > 0) {
            sb.append("Leech Serum converted ")
                    .append(engine.getLastLifestealAmount())
                    .append(" HP from your damage");
            if (engine.getLastLifestealHeal() > 0) {
                sb.append(" and restored ")
                        .append(engine.getLastLifestealHeal())
                        .append(" HP.\n");
            } else {
                sb.append(", but your HP was already full.\n");
            }
        }
        if (engine.getLastShieldAbsorbed() > 0) {
            sb.append("Shield Battery absorbed ")
                    .append(engine.getLastShieldAbsorbed())
                    .append(" damage.\n");
        }
        if (engine.getLastReflectedDamage() > 0) {
            sb.append("Mirror Shard reflected ")
                    .append(engine.getLastReflectedDamage())
                    .append(" damage.\n");
        }

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

        if (playerStatusBadges != null) {
            playerStatusBadges.getChildren().clear();
            if (pe != null) {
                playerStatusBadges.getChildren().add(createStatusBadge(pe.getLabel(), "#7c2d12", "#fed7aa"));
            }
            if (engine.getShieldAbsorbLeft() > 0) {
                playerStatusBadges.getChildren().add(createStatusBadge(
                        "SHIELD " + engine.getShieldAbsorbLeft(),
                        "#0e7490",
                        "#cffafe"));
            }
            if (engine.getLifestealTurnsLeft() > 0) {
                playerStatusBadges.getChildren().add(createStatusBadge(
                        "LEECH " + engine.getLifestealTurnsLeft() + "T",
                        "#15803d",
                        "#dcfce7"));
            }
            if (engine.getReflectTurnsLeft() > 0) {
                playerStatusBadges.getChildren().add(createStatusBadge(
                        "MIRROR " + engine.getReflectTurnsLeft() + "T",
                        "#5b21b6",
                        "#ede9fe"));
            }
        }

        if (bossStatusBadges != null) {
            bossStatusBadges.getChildren().clear();
            if (be != null) {
                bossStatusBadges.getChildren().add(createStatusBadge(be.getLabel(), "#991b1b", "#fee2e2"));
            }
        }
    }

    private Label createStatusBadge(String text, String background, String textColor) {
        Label badge = new Label(text);
        badge.setMinHeight(18);
        badge.setMaxHeight(18);
        badge.setMouseTransparent(true);
        badge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web(textColor));
        badge.setStyle(
                "-fx-background-color: " + background + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: rgba(255,255,255,0.55);" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 2 7 2 7;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 5, 0.35, 0, 1);"
        );

        FadeTransition fade = new FadeTransition(Duration.millis(180), badge);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        ScaleTransition pop = new ScaleTransition(Duration.millis(160), badge);
        pop.setFromX(0.88);
        pop.setFromY(0.88);
        pop.setToX(1.0);
        pop.setToY(1.0);
        pop.play();

        return badge;
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

    public void onCombatEnd(boolean playerWon) {
        if (thinkingRevertTimer != null) {
            thinkingRevertTimer.stop();
            thinkingRevertTimer = null;
        }

        lockAllActions(true);
        updatePlayerSpriteMood();

        boss.clearAbilitySprite();

        if (player.getActiveEffect() != null) player.applyEffect(null);
        if (boss.getActiveEffect()   != null) boss.applyEffect(null);
        if (playerStatusLabel != null) playerStatusLabel.setText("");
        if (bossStatusLabel   != null) bossStatusLabel.setText("");
        if (playerStatusBadges != null) playerStatusBadges.getChildren().clear();
        if (bossStatusBadges != null) bossStatusBadges.getChildren().clear();
        if (bossIntentLabel   != null) bossIntentLabel.setText("");

        if (playerWon) {
            PlayerProgress.getInstance().setOldMaxHp(player.getMaxHp());
            // do NOT overwrite currentHp here - addXP() already updated it correctly during grantRewards()
            // only sync if no level up happened (currentHp in progress is still -1 or from previous fight)
            if (PlayerProgress.getInstance().getCurrentHp() == -1) {
                PlayerProgress.getInstance().setCurrentHp(player.getCurrentHp());
            }
            PlayerProgress progress = PlayerProgress.getInstance();
            log("Victory. " + boss.getName() + " defeated.");
            GameScreen.getInstance().showVictoryScreen();
            log("+" + boss.getXPReward() + " XP  |  +" + boss.getGoldReward() + " Gold");
            log("Level: " + progress.getLevel() + "  |  XP: " + progress.getXp() + "/" + progress.getXpToNextLevel());
            log("Loading next area...");

        } else {

            GameMusicManager.stopMusic();
            GameMusicManager.playGameOverSound();

            log("Defeated. " + player.getName() + " has fallen. Game over.");
            GameScreen.getInstance().showGameOver();
            PlayerProgress.getInstance().setCurrentHp(-1);
        }

        PauseTransition delay = new PauseTransition(Duration.seconds(0));
        updatePlayerSpriteMood();
        delay.setOnFinished(e -> {
            if (playerWon) {
                loadNextArea();
            } else {
                System.out.println("GAME OVER");
            }
        });
        delay.play();
    }

    private void loadNextArea() {
        GameScreen gs = GameScreen.getInstance();
        if (gs != null) {
            gs.returnFromCombat();
        } else {
            System.out.println("GameScreen instance not found.");
        }
    }

    private void log(String text) {
        turnInformation.appendText(text + "\n");
        turnInformation.positionCaret(turnInformation.getLength());
        pulseTurnLog();
    }

    public void setStart(String playerNamee, String bossNamee, int bossMaxHp) {
        playername.setText(playerNamee);
        bossName.setText(bossNamee);
        bossHPnumber.setText(bossMaxHp + " / " + bossMaxHp);
        updateBossSpriteMood();
        updatePlayerSpriteMood();
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

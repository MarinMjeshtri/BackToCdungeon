package com.dungeons.Controllers;

import javafx.fxml.FXML;
import com.dungeons.screens.GameScreen;
import com.dungeons.screens.areYouSureScreen;
import com.dungeons.screens.optionsMenu;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PauseController {


    @FXML private StackPane pauseRoot;

    Stage stage;
    private GameScreen gameScreen;
    private areYouSureScreen uSureScreen;
    private Parent optionsRoot;

    public void setStage(Stage stage) throws IOException {
        this.stage = stage;
        // now that we have the stage, create the overlay
        this.uSureScreen = new areYouSureScreen();
        loadOptionsOverlay();
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @FXML
    private void resume() {
        gameScreen.togglePause();
    }



    @FXML
    private void exit() {
        Pane currentRoot = (Pane) stage.getScene().getRoot();

        //CHECK IF ITS LOADED OR NOT
        if (!currentRoot.getChildren().contains(uSureScreen.getRoot())) {
            currentRoot.getChildren().add(uSureScreen.getRoot());
        }

        // MAKE VISIBLE
        uSureScreen.getRoot().setVisible(true);
    }

    @FXML
    private void options() {
        showOptions();
    }

    private void loadOptionsOverlay() throws IOException {
        optionsMenu menu = new optionsMenu(gameScreen, stage);
        optionsRoot = menu.getRoot();
        optionsRoot.setVisible(false);
        optionsRoot.setFocusTraversable(true);
        optionsRoot.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                closeOptionsIfOpen();
                e.consume();
            }
        });

        optionsController controller = menu.getLoader().getController();
        controller.setReturnAction(() -> {
            optionsRoot.setVisible(false);
            pauseRoot.requestFocus();
        });

        pauseRoot.getChildren().add(optionsRoot);
    }

    public void showOptions() {
        if (optionsRoot == null) return;
        optionsRoot.setVisible(true);
        optionsRoot.toFront();
        optionsRoot.requestFocus();
    }

    public boolean closeOptionsIfOpen() {
        if (optionsRoot != null && optionsRoot.isVisible()) {
            optionsRoot.setVisible(false);
            return true;
        }
        return false;
    }
}

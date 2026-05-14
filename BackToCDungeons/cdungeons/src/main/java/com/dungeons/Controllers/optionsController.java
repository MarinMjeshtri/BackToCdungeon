package com.dungeons.Controllers;

// Rename this class to OptionsController if you want to follow Java naming conventions.

import com.dungeons.MusicandSoundsCode.*;
import com.dungeons.theAlmagamation;
import com.dungeons.screens.startingScreen;
import com.dungeons.systems.CombatSystem.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class optionsController implements Initializable {

    @FXML private Slider volumeSlider;
    @FXML private CheckBox disableMusic;
    @FXML private CheckBox disableSfx;
    @FXML private CheckBox becomeOp;
    @FXML private ToggleGroup resolutionGroup;
    @FXML private RadioButton resNormal;
    @FXML private RadioButton resZoomed;
    @FXML private RadioButton resFullscreen;

    @FXML private StackPane optionsMenu;

    private Runnable returnAction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        volumeSlider.setValue(50);

        resNormal.setSelected(true);

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            GameMusicManager.setMusicVolume(newValue.doubleValue()/100.0);
        });

        disableMusic.setOnAction(event -> {
            if (disableMusic.isSelected()) {
                AudioManager.toggleMusic();
            } else {
                AudioManager.toggleMusic();
                GameMusicManager.playOpening();
            }
        });

        disableSfx.setOnAction(event -> {
            if (disableSfx.isSelected()) {
                AudioManager.toggleSfx();
            } else {
                AudioManager.toggleSfx();
            }
        });

        becomeOp.setOnAction(event -> {
            StatsLoader.opMode = becomeOp.isSelected();
            System.out.println("OP mode: " + StatsLoader.opMode);
        });

        resolutionGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {

            if (newToggle == resNormal) {
                System.out.println("Resolution: NORMAL");
            }

            if (newToggle == resZoomed) {
                System.out.println("Resolution: ZOOMED");
            }

            if (newToggle == resFullscreen) {
                System.out.println("Resolution: FULLSCREEN");
            }
        });

    }

    @FXML
    public void returnToMain() {
        if (returnAction != null) {
            returnAction.run();
            return;
        }

        startingScreen screen = new startingScreen();
        OptionsNStartingController controller = screen.getLoader().getController();
        controller.setStage(theAlmagamation.getStage());
        theAlmagamation.switchTo(screen.getRoot());

    }

    public void setReturnAction(Runnable returnAction) {
        this.returnAction = returnAction;
    }


    public double getVolume() {
        return volumeSlider.getValue();
    }

    public boolean isMusicDisabled() {
        return disableMusic.isSelected();
    }

    public boolean isSfxDisabled() {
        return disableSfx.isSelected();
    }

    public boolean isBecomeOpEnabled() {
        return becomeOp.isSelected();
    }

    public String getSelectedResolution() {

        if (resNormal.isSelected()) {
            return "NORMAL";
        }

        if (resZoomed.isSelected()) {
            return "ZOOMED";
        }

        if (resFullscreen.isSelected()) {
            return "FULLSCREEN";
        }

        return "UNKNOWN";
    }
}

package com.dungeons.Controllers;

// Rename this class to OptionsController if you want to follow Java naming conventions.

import com.dungeons.MusicandSoundsCode.*;
import com.dungeons.systems.CombatSystem.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.ResourceBundle;

public class optionsController implements Initializable {

    // =========================
    // FXML COMPONENTS
    // =========================

    @FXML
    private Slider volumeSlider;

    @FXML
    private CheckBox disableMusic;

    @FXML
    private CheckBox disableSfx;

    @FXML
    private CheckBox becomeOp;

    @FXML
    private ToggleGroup resolutionGroup;

    @FXML
    private RadioButton resNormal;

    @FXML
    private RadioButton resZoomed;

    @FXML
    private RadioButton resFullscreen;

    // =========================
    // INITIALIZATION
    // =========================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Default volume
        volumeSlider.setValue(50);

        // Default resolution
        resNormal.setSelected(true);

        // Volume listener
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Volume changed: " + newValue.intValue());
        });

        // Disable music checkbox
        disableMusic.setOnAction(event -> {
            if (disableMusic.isSelected()) {
                System.out.println("Music disabled");
            } else {
                System.out.println("Music enabled");
            }
        });

        // Disable SFX checkbox
        disableSfx.setOnAction(event -> {
            if (disableSfx.isSelected()) {
                System.out.println("SFX disabled");
            } else {
                System.out.println("SFX enabled");
            }
        });

        // Become OP checkbox
        becomeOp.setOnAction(event -> {
            if (becomeOp.isSelected()) {
                System.out.println("Player is now OVERPOWERED");
            } else {
                System.out.println("OP mode disabled");
            }
        });

        // Resolution selection listener
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

    // =========================
    // OPTIONAL GETTERS
    // =========================

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
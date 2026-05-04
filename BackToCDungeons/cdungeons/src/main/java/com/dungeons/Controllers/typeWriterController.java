package com.dungeons.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class typeWriterController {

    @FXML
    private Label storyLabel;

    private Timeline timeline;

    public void playText(String fullText) {
        storyLabel.setText("");

        timeline = new Timeline();
        StringBuilder currentText = new StringBuilder();

        for (int i = 0; i < fullText.length(); i++) {
            final int index = i;

            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(40 * i), e -> {
                        currentText.append(fullText.charAt(index));
                        storyLabel.setText(currentText.toString());
                    })
            );
        }

        timeline.play();
    }
}
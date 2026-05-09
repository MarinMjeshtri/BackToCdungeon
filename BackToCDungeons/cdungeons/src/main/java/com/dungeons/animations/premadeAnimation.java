package com.dungeons.animations;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


// THESE ARE NOT MADE BY ME, THESE ARE PREMADE ANIMATIONS YALL CAN USE N STUFF <3
public class premadeAnimation {

    public static void fadeIn(Parent node) {
        node.setOpacity(0);
        node.setVisible(true);
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(500), new KeyValue(node.opacityProperty(), 1))
        );
        t.play();
    }

    public static void fadeOut(Parent node) {
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(500), new KeyValue(node.opacityProperty(), 0))
        );
        t.setOnFinished(e -> node.setVisible(false));
        t.play();
    }

    public static void showFor(Parent node, double seconds) {
        fadeIn(node);
        PauseTransition wait = new PauseTransition(Duration.seconds(seconds));
        wait.setOnFinished(e -> fadeOut(node));
        wait.play();
    }
}
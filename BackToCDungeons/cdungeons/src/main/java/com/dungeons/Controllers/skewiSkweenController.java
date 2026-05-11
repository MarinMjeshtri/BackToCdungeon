package com.dungeons.Controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.util.List;

public class skewiSkweenController {

    @FXML
    private MediaView
            mv0, mv1, mv2, mv3, mv4, mv5, mv6, mv7, mv8, mv9, mv10,
            mv11, mv12, mv13, mv14, mv15, mv16, mv17, mv18, mv19,
            mv23, mv24, mv25, mv26, mv27, mv28, mv29,
            mv35, mv36, mv37, mv38, mv39, mv40, mv41, mv42, mv43,
            mv45, mv47, mv48, mv49, mv50, mv51, mv52, mv53, mv54,
            mv55, mv56, mv57, mv58, mv59, mv60, mv61, mv62, mv63,
            mv64, mv65, mv66, mv67, mv68, mv69, mv70, mv71, mv72;

    @FXML
    private ImageView characterImage;

    private List<MediaView> allEyes;

    @FXML
    public void initialize() {
        allEyes = List.of(
                mv0, mv1, mv2, mv3, mv4, mv5, mv6, mv7, mv8, mv9, mv10,
                mv11, mv12, mv13, mv14, mv15, mv16, mv17, mv18, mv19,
                mv23, mv24, mv25, mv26, mv27, mv28, mv29,
                mv35, mv36, mv37, mv38, mv39, mv40, mv41, mv42, mv43,
                mv45, mv47, mv48, mv49, mv50, mv51, mv52, mv53, mv54,
                mv55, mv56, mv57, mv58, mv59, mv60, mv61, mv62, mv63,
                mv64, mv65, mv66, mv67, mv68, mv69, mv70, mv71, mv72
        );

        Media eyeMedia = new Media(getClass().getResource("/sprites/scaryEyes.mp4").toExternalForm());

        for (MediaView view : allEyes) {
            MediaPlayer player = new MediaPlayer(eyeMedia);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            player.play();
            view.setMediaPlayer(player);
        }

        Image img = new Image(getClass().getResourceAsStream("/sprites/door.png"));
        characterImage.setImage(img);
    }
}

package com.dungeons.systems;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class SpriteSheet {

    private final Image image;
    private final int tileSize;

    public SpriteSheet(String path, int tileSize) {
        var stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new RuntimeException(
                "Sprite not found: " + path +
                " \u2014 check the file exists in src/main/resources/sprites/"
            );
        }
        this.image    = new Image(stream);
        this.tileSize = tileSize;
    }

    public void draw(GraphicsContext gc,
                     int col, int row,
                     double destX, double destY,
                     double scale) {
        gc.setImageSmoothing(false);

        double srcX     = col * tileSize;
        double srcY     = row * tileSize;
        double drawSize = tileSize * scale;

        gc.drawImage(
            image,
            srcX,  srcY,  tileSize, tileSize,
            destX, destY, drawSize, drawSize
        );
    }
}
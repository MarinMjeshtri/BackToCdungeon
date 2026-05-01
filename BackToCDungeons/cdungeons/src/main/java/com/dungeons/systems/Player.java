package com.dungeons.systems;

import com.dungeons.world.Map;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;

public class Player {

    private double x, y;
    private Map map;

    private boolean up, down, left, right;

    private static final double SPEED = 2.0;
    private static final int TILE_SIZE = 16;
    private static final int SCALE = 2;
    private static final int SIZE = 14;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // ---------------- INPUT ----------------

    public void keyPressed(KeyCode key) {
        if (key == KeyCode.W || key == KeyCode.UP)    up    = true;
        if (key == KeyCode.S || key == KeyCode.DOWN)  down  = true;
        if (key == KeyCode.A || key == KeyCode.LEFT)  left  = true;
        if (key == KeyCode.D || key == KeyCode.RIGHT) right = true;
    }

    public void keyReleased(KeyCode key) {
        if (key == KeyCode.W || key == KeyCode.UP)    up    = false;
        if (key == KeyCode.S || key == KeyCode.DOWN)  down  = false;
        if (key == KeyCode.A || key == KeyCode.LEFT)  left  = false;
        if (key == KeyCode.D || key == KeyCode.RIGHT) right = false;
    }

    public void clearInput() {
        up    = false;
        down  = false;
        left  = false;
        right = false;
    }

    // ---------------- UPDATE ----------------

    public void update() {
        double dx = 0;
        double dy = 0;

        if (up)    dy -= SPEED;
        if (down)  dy += SPEED;
        if (left)  dx -= SPEED;
        if (right) dx += SPEED;

        move(dx, dy);
    }

    private void move(double dx, double dy) {
        if (!collides(x + dx, y)) x += dx;
        if (!collides(x, y + dy)) y += dy;
    }

    // ---------------- COLLISION ----------------

    private boolean collides(double px, double py) {
        if (map == null) return false;

        int scaledTile = TILE_SIZE * SCALE;

        int leftTile   = (int)(px / scaledTile);
        int rightTile  = (int)((px + SIZE * SCALE - 1) / scaledTile);
        int topTile    = (int)(py / scaledTile);
        int bottomTile = (int)((py + SIZE * SCALE - 1) / scaledTile);

        return map.isSolid(leftTile, topTile)    ||
                map.isSolid(rightTile, topTile)   ||
                map.isSolid(leftTile, bottomTile) ||
                map.isSolid(rightTile, bottomTile);
    }

    // ---------------- RENDER ----------------

    public void render(GraphicsContext gc) {
        gc.fillRect(x, y, SIZE * SCALE, SIZE * SCALE);
    }

    // ---------------- GETTERS ----------------

    public double getX() { return x; }
    public double getY() { return y; }

    public int getTileX() { return (int)((x + (SIZE * SCALE) / 2.0) / (TILE_SIZE * SCALE)); }
    public int getTileY() { return (int)((y + (SIZE * SCALE) / 2.0) / (TILE_SIZE * SCALE)); }
}
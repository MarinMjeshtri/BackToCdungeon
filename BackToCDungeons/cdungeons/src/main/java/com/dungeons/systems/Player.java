package com.dungeons.systems;
import com.dungeons.world.Map;
import com.dungeons.world.MapRenderer;
import com.dungeons.world.TilesetManager;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;

public class Player {

    private double x, y;
    private Map map; // collision map

    private boolean up, down, left, right;

    private static final double SPEED = 2.0;
    private static final int TILE_SIZE = 16;
    private static final int SIZE = 14; // player hitbox (slightly smaller than tile)

    // Placeholder (you can reconnect SpriteSheet later)
    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    // ---------------- INPUT ----------------

    public void keyPressed(KeyCode key) {
        if (key == KeyCode.W || key == KeyCode.UP) up = true;
        if (key == KeyCode.S || key == KeyCode.DOWN) down = true;
        if (key == KeyCode.A || key == KeyCode.LEFT) left = true;
        if (key == KeyCode.D || key == KeyCode.RIGHT) right = true;
    }

    public void keyReleased(KeyCode key) {
        if (key == KeyCode.W || key == KeyCode.UP) up = false;
        if (key == KeyCode.S || key == KeyCode.DOWN) down = false;
        if (key == KeyCode.A || key == KeyCode.LEFT) left = false;
        if (key == KeyCode.D || key == KeyCode.RIGHT) right = false;
    }

    // ---------------- UPDATE ----------------

    public void update() {

        double dx = 0;
        double dy = 0;

        if (up) dy -= SPEED;
        if (down) dy += SPEED;
        if (left) dx -= SPEED;
        if (right) dx += SPEED;

        move(dx, dy);
    }

    private void move(double dx, double dy) {

        // X movement
        if (!collides(x + dx, y)) {
            x += dx;
        }

        // Y movement
        if (!collides(x, y + dy)) {
            y += dy;
        }
    }

    // ---------------- COLLISION ----------------

    private static final int SCALE = 3; // must match GameScreen

    private boolean collides(double px, double py) {
        if (map == null) return false;

        // divide by scaled tile size, not raw tile size
        int leftTile   = (int)(px / (TILE_SIZE * SCALE));
        int rightTile  = (int)((px + SIZE) / (TILE_SIZE * SCALE));
        int topTile    = (int)(py / (TILE_SIZE * SCALE));
        int bottomTile = (int)((py + SIZE) / (TILE_SIZE * SCALE));

        return map.isSolid(leftTile, topTile)  ||
                map.isSolid(rightTile, topTile) ||
                map.isSolid(leftTile, bottomTile) ||
                map.isSolid(rightTile, bottomTile);
    }

    // ---------------- RENDER ----------------

    public void render(GraphicsContext gc) {
        gc.fillRect(x, y, SIZE, SIZE); // now correctly 42x42px on screen
    }

    // ---------------- GETTERS ----------------

    public double getX() { return x; }
    public double getY() { return y; }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
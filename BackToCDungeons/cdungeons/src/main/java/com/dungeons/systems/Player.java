package com.dungeons.systems;

import com.dungeons.world.Map;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;

import com.dungeons.MusicandSoundsCode.*;

public class Player {

    private double x, y;
    private Map map;

    private boolean up, down, left, right;

    private static final double SPEED = 2.0;
    private static final int TILE_SIZE = 16;
    private static final int SCALE = 3;
    private static final int SIZE = 14;
    public static final int HITBOX_OFFSET_X = 43; // tweak to fit your sprite
    public static final int HITBOX_OFFSET_Y = 86; // tweak to fit your sprite

    private SpriteSheet sprite;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;

        this.sprite = new SpriteSheet(
                "/sprites/characters/Joni2/rotations/east.png",
                16
        );
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

    public enum Direction {
        IDLE,
        UP, DOWN, LEFT, RIGHT,
        UP_LEFT, UP_RIGHT,
        DOWN_LEFT, DOWN_RIGHT
    }

    private Direction currentDirection = Direction.IDLE;

    public void update() {
        double dx = 0;
        double dy = 0;

        if (up)    dy -= SPEED;
        if (down)  dy += SPEED;
        if (left)  dx -= SPEED;
        if (right) dx += SPEED;

        if      (up && left)    currentDirection = Direction.UP_LEFT;
        else if (up && right)   currentDirection = Direction.UP_RIGHT;
        else if (down && left)  currentDirection = Direction.DOWN_LEFT;
        else if (down && right) currentDirection = Direction.DOWN_RIGHT;
        else if (up)            currentDirection = Direction.UP;
        else if (down)          currentDirection = Direction.DOWN;
        else if (left)          currentDirection = Direction.LEFT;
        else if (right)         currentDirection = Direction.RIGHT;
        else                    currentDirection = Direction.IDLE;

        move(dx, dy);
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    private void move(double dx, double dy) {
        double prevX = x, prevY = y;

        if (!collides(x + dx, y)) x += dx;
        if (!collides(x, y + dy)) y += dy;

        boolean actuallyMoved = (x != prevX || y != prevY);
//        GameMusicManager.tickWalkSound(actuallyMoved);
    }

    // ---------------- COLLISION ----------------

    private boolean collides(double px, double py) {
        if (map == null) return false;

        int scaledTile = TILE_SIZE * SCALE;

        double hx = px + HITBOX_OFFSET_X;
        double hy = py + HITBOX_OFFSET_Y;

        int leftTile   = (int)(hx / scaledTile);
        int rightTile  = (int)((hx + SIZE * SCALE - 1) / scaledTile);
        int topTile    = (int)(hy / scaledTile);
        int bottomTile = (int)((hy + SIZE * SCALE - 1) / scaledTile);

        return map.isSolid(leftTile, topTile)    ||
                map.isSolid(rightTile, topTile)   ||
                map.isSolid(leftTile, bottomTile) ||
                map.isSolid(rightTile, bottomTile);
    }

    // ---------------- RENDER ----------------

    public void render(GraphicsContext gc) {
        String spritePath;

        if (currentDirection == Direction.UP) {
            spritePath = "/sprites/characters/Joni2/rotations/north.png";
        } else if (currentDirection == Direction.DOWN) {
            spritePath = "/sprites/characters/Joni2/rotations/south.png";
        } else if (currentDirection == Direction.LEFT) {
            spritePath = "/sprites/characters/Joni2/rotations/west.png";
        } else if (currentDirection == Direction.RIGHT) {
            spritePath = "/sprites/characters/Joni2/rotations/east.png";
        } else if (currentDirection == Direction.UP_LEFT) {
            spritePath = "/sprites/characters/Joni2/rotations/north-west.png";
        } else if (currentDirection == Direction.UP_RIGHT) {
            spritePath = "/sprites/characters/Joni2/rotations/north-east.png";
        } else if (currentDirection == Direction.DOWN_LEFT) {
            spritePath = "/sprites/characters/Joni2/rotations/south-west.png";
        } else if (currentDirection == Direction.DOWN_RIGHT) {
            spritePath = "/sprites/characters/Joni2/rotations/south-east.png";
        } else {
            spritePath = "/sprites/characters/Joni2/rotations/south.png";
        }

        Image img = new Image(getClass().getResourceAsStream(spritePath));
        gc.setImageSmoothing(false);
        double squish = 1 + 0.05 * Math.sin(System.currentTimeMillis() * 0.005);
        gc.drawImage(img, x, y, 128 * squish, 128 / squish);

    }

    // ---------------- GETTERS ----------------

    public double getX() { return x; }
    public double getY() { return y; }

    public int getTileX() {
        return (int)((x + HITBOX_OFFSET_X + (SIZE * SCALE) / 2.0) / (TILE_SIZE * SCALE));
    }

    public int getTileY() {
        return (int)((y + HITBOX_OFFSET_Y + (SIZE * SCALE) / 2.0) / (TILE_SIZE * SCALE));
    }
}
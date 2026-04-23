package com.dungeons.systems;

import com.dungeons.world.CollisionMap;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;

public class Player {

    private double x, y;
    private final SpriteSheet sheet;
    private CollisionMap collisionMap;

    private boolean up, down, left, right;
    private static final double SPEED = 3;
    private static final double SIZE  = 48;

    public Player(double startX, double startY) {
        this.x     = startX;
        this.y     = startY;
        this.sheet = new SpriteSheet("/sprites/characters/technoblade.png", 16);
        System.out.println("Player created at " + x + ", " + y);
    }

    public void setCollisionMap(CollisionMap collisionMap) {
        this.collisionMap = collisionMap;
    }

    public void keyPressed(KeyCode key) {
        if (key == KeyCode.UP    || key == KeyCode.W) up    = true;
        if (key == KeyCode.DOWN  || key == KeyCode.S) down  = true;
        if (key == KeyCode.LEFT  || key == KeyCode.A) left  = true;
        if (key == KeyCode.RIGHT || key == KeyCode.D) right = true;
    }

    public void keyReleased(KeyCode key) {
        if (key == KeyCode.UP    || key == KeyCode.W) up    = false;
        if (key == KeyCode.DOWN  || key == KeyCode.S) down  = false;
        if (key == KeyCode.LEFT  || key == KeyCode.A) left  = false;
        if (key == KeyCode.RIGHT || key == KeyCode.D) right = false;
    }

    public void update() {
        if (up)    tryMove(0,     -SPEED);
        if (down)  tryMove(0,     +SPEED);
        if (left)  tryMove(-SPEED, 0);
        if (right) tryMove(+SPEED, 0);
    }

    private void tryMove(double dx, double dy) {
        double newX = x + dx;
        double newY = y + dy;

        if (collisionMap == null) {
            x = newX;
            y = newY;
            return;
        }

        if (!collisionMap.isSolid(newX, newY, SIZE, SIZE, 48.0)) {
            x = newX;
            y = newY;
        }
    }

    public void render(GraphicsContext gc) {
        sheet.draw(gc, 0, 0, x, y, 3.0);
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
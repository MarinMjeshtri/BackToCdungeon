package com.dungeons.world;

public class TransitionZone {
    public final int x, y;
    public final String targetMap;
    public final int spawnX, spawnY;

    public TransitionZone(int x, int y, String targetMap, int spawnX, int spawnY) {
        this.x = x;
        this.y = y;
        this.targetMap = targetMap;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }
}
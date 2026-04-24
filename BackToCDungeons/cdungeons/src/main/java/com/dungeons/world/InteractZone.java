package com.dungeons.world;

public class InteractZone {
    public final int x, y;
    public final String type;
    public boolean triggered = false;

    public InteractZone(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
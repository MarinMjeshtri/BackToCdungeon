package com.dungeons.world;

public class InteractZone {
    public final int x, y;
    public final String type;
    public boolean triggered = false;
    public int id;

    public InteractZone(int x, int y, String type, int id) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.id = id;

    }
}
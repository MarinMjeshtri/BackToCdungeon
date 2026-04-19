package com.dungeons.world;

public class TilesetInfo {
    public final int firstGid;
    public final String imagePath;
    public final int tileWidth;
    public final int columns;

    public TilesetInfo(int firstGid, String imagePath, int tileWidth, int columns) {
        this.firstGid  = firstGid;
        this.imagePath = imagePath;
        this.tileWidth = tileWidth;
        this.columns   = columns;
    }
}
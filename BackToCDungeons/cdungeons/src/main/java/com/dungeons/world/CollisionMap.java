package com.dungeons.world;

import java.util.Map;

public class CollisionMap {

    private final boolean[][] solid;
    private final int rows;
    private final int cols;

    public CollisionMap(Map<String, int[][]> layers) {
        int[][] first = layers.values().iterator().next();
        this.rows  = first.length;
        this.cols  = first[0].length;
        this.solid = new boolean[rows][cols];

        for (Map.Entry<String, int[][]> entry : layers.entrySet()) {
            if (entry.getKey().toUpperCase().contains("COLLISION")) {
                int[][] layer = entry.getValue();
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        if (layer[r][c] > 0) {
                            solid[r][c] = true;
                        }
                    }
                }
            }
        }
        System.out.println("Collision map built: " + rows + "x" + cols);
    }

    public boolean isSolid(double x, double y, double width, double height,
                           double tileDisplaySize) {
        int left   = (int)(x / tileDisplaySize);
        int top    = (int)(y / tileDisplaySize);
        int right  = (int)((x + width  - 1) / tileDisplaySize);
        int bottom = (int)((y + height - 1) / tileDisplaySize);

        left   = Math.max(0, left);
        top    = Math.max(0, top);
        right  = Math.min(cols - 1, right);
        bottom = Math.min(rows  - 1, bottom);

        for (int r = top; r <= bottom; r++) {
            for (int c = left; c <= right; c++) {
                if (solid[r][c]) return true;
            }
        }
        return false;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public boolean isSolidTile(int row, int col) { return solid[row][col]; }
}
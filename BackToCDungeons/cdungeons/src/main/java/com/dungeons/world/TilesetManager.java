import javafx.scene.image.*;
import java.util.HashMap;

public class TilesetManager {

    private final HashMap<String, Image[]> tilesets = new HashMap<>();

    final int TILE_SIZE = 16;

    String path = "C:\\Users\\User\\BackToCdungeon\\BackToCDungeons\\cdungeons\\src\\main\\resources\\tiles\\";

    public void loadAll() {

        load("tilesFloor.png", "floor");
        load("tilesWalls.png", "walls");
        load("tilesStuff.png", "stuff");
        load("spriteSheet_tiledLiquids_16x16.png", "liquids");
    }

    private void load(String file, String key) {

        Image img = new Image("file:" + path + file);

        int cols = (int) (img.getWidth() / TILE_SIZE);

        Image[] tiles = new Image[600];

        for (int i = 0; i < tiles.length; i++) {

            int x = (i % cols) * TILE_SIZE;
            int y = (i / cols) * TILE_SIZE;

            if (x >= img.getWidth()) break;

            tiles[i] = new WritableImage(
                    img.getPixelReader(),
                    x, y,
                    TILE_SIZE, TILE_SIZE
            );
        }

        tilesets.put(key, tiles);
    }

    public Image get(String tileset, int id) {
        return tilesets.get(tileset)[id - 1];
    }
}
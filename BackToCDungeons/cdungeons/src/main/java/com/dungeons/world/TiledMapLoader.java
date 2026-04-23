package com.dungeons.world;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TiledMapLoader {

    public static Map<String, int[][]> loadAllLayers(String path) {
        try {
            InputStream is = TiledMapLoader.class.getResourceAsStream(path);
            if (is == null) throw new RuntimeException("Map not found: " + path);

            DocumentBuilder builder = DocumentBuilderFactory
                .newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);

            Map<String, int[][]> layers = new HashMap<>();
            NodeList layerNodes = doc.getElementsByTagName("layer");

            for (int i = 0; i < layerNodes.getLength(); i++) {
                Element layer = (Element) layerNodes.item(i);
                String name = layer.getAttribute("name");

                NodeList dataNodes = layer.getElementsByTagName("data");
                String csvData = dataNodes.item(0).getTextContent().trim();

                String[] rows = csvData.split("\n");
                int height = rows.length;
                int width  = rows[0].split(",").length;
                int[][] tiles = new int[height][width];

                for (int r = 0; r < height; r++) {
                    String[] cols = rows[r].trim().split(",");
                    for (int c = 0; c < cols.length; c++) {
                        String val = cols[c].trim();
                        if (!val.isEmpty()) {
                            tiles[r][c] = Integer.parseInt(val);
                        }
                    }
                }
                layers.put(name, tiles);
                System.out.println("Loaded layer: " + name);
            }
            return layers;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load map: " + path, e);
        }
    }

    public static List<TilesetInfo> loadTilesets(String path) {
        try {
            InputStream is = TiledMapLoader.class.getResourceAsStream(path);
            if (is == null) throw new RuntimeException("Map not found: " + path);

            DocumentBuilder builder = DocumentBuilderFactory
                .newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);

            List<TilesetInfo> tilesets = new ArrayList<>();
            NodeList tsNodes = doc.getElementsByTagName("tileset");

            for (int i = 0; i < tsNodes.getLength(); i++) {
                Element ts = (Element) tsNodes.item(i);
                int firstGid = Integer.parseInt(ts.getAttribute("firstgid"));

                // check if this tileset references an external .tsx file
                String source = ts.getAttribute("source");

                if (!source.isEmpty()) {
                    // external .tsx — load it from sprites folder
                    String tsxName = new java.io.File(source).getName();
                    String tsxPath = "/sprites/" + tsxName;

                    TilesetInfo info = loadFromTsx(tsxPath, firstGid);
                    if (info != null) {
                        tilesets.add(info);
                        System.out.println("Loaded tsx: " + tsxName
                            + " firstgid=" + firstGid
                            + " image=" + info.imagePath
                            + " columns=" + info.columns);
                    }
                } else {
                    // embedded tileset — read directly from tmx
                    String colsAttr = ts.getAttribute("columns");
                    String twAttr   = ts.getAttribute("tilewidth");
                    int columns   = colsAttr.isEmpty() ? 0  : Integer.parseInt(colsAttr);
                    int tileWidth = twAttr.isEmpty()   ? 16 : Integer.parseInt(twAttr);

                    NodeList imgNodes = ts.getElementsByTagName("image");
                    if (imgNodes.getLength() > 0) {
                        Element img = (Element) imgNodes.item(0);
                        String imgSrc = new java.io.File(
                            img.getAttribute("source")).getName();

                        if (columns == 0) {
                            String widthAttr = img.getAttribute("width");
                            if (!widthAttr.isEmpty()) {
                                columns = Integer.parseInt(widthAttr) / tileWidth;
                            }
                        }
                        tilesets.add(new TilesetInfo(firstGid, imgSrc, tileWidth, columns));
                        System.out.println("Loaded embedded tileset: " + imgSrc
                            + " firstgid=" + firstGid
                            + " columns=" + columns);
                    }
                }
            }

            tilesets.sort((a, b) -> a.firstGid - b.firstGid);
            return tilesets;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load tilesets: " + path, e);
        }
    }

    private static TilesetInfo loadFromTsx(String tsxPath, int firstGid) {
        try {
            InputStream is = TiledMapLoader.class.getResourceAsStream(tsxPath);
            if (is == null) {
                System.out.println("Warning: tsx not found at " + tsxPath);
                return null;
            }

            DocumentBuilder builder = DocumentBuilderFactory
                .newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);

            Element root = doc.getDocumentElement();
            String twAttr   = root.getAttribute("tilewidth");
            String colsAttr = root.getAttribute("columns");
            int tileWidth = twAttr.isEmpty()   ? 16 : Integer.parseInt(twAttr);
            int columns   = colsAttr.isEmpty() ? 0  : Integer.parseInt(colsAttr);

            NodeList imgNodes = doc.getElementsByTagName("image");
            if (imgNodes.getLength() == 0) return null;

            Element img = (Element) imgNodes.item(0);
            String imgSrc = new java.io.File(img.getAttribute("source")).getName();

            // calculate columns from image width if not set
            if (columns == 0) {
                String widthAttr = img.getAttribute("width");
                if (!widthAttr.isEmpty()) {
                    columns = Integer.parseInt(widthAttr) / tileWidth;
                }
            }

            return new TilesetInfo(firstGid, imgSrc, tileWidth, columns);

        } catch (Exception e) {
            System.out.println("Warning: failed to load tsx: " + tsxPath + " — " + e.getMessage());
            return null;
        }
    }
}
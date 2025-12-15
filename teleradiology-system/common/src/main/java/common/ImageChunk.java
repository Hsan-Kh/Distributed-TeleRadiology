package common;

import java.io.Serializable;

public class ImageChunk implements Serializable {
    public int id;
    public int startX, startY;
    public int width, height;
    public int[] pixelData;

    public ImageChunk(int id, int startX, int startY, int w, int h, int[] pixels) {
        this.id = id; this.startX = startX; this.startY = startY;
        this.width = w; this.height = h; this.pixelData = pixels;
    }
}
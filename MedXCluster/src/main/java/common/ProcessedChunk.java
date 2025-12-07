package common;
import java.io.Serializable;

public class ProcessedChunk implements Serializable {
    public int id;
    public int[] processedPixels; // The pixels AFTER edge detection

    public ProcessedChunk(int id, int[] pixels) {
        this.id = id;
        this.processedPixels = pixels;
    }
}
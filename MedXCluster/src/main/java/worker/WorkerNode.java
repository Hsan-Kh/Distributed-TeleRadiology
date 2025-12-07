package worker;
import common.*;
import java.rmi.Naming;

public class WorkerNode {
    public static void main(String[] args) {
        try {
            ComputeService service = (ComputeService) Naming.lookup("rmi://localhost:1099/ComputeService");

            while (true) {
                ImageChunk task = service.getTask();
                if (task == null) {
                    System.out.println("No more tasks. Waiting...");
                    Thread.sleep(2000);
                    continue;
                }

                System.out.println("Processing Chunk " + task.id);
                int[] newPixels = applySobelFilter(task.pixelData, task.width, task.height);

                service.submitResult(new ProcessedChunk(task.id, newPixels));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }



    private static int[] applySobelFilter(int[] pixels, int w, int h) {
        // 1. Create a new array for the result
        int[] output = new int[w * h];

        // 2. Define the Sobel Kernels (Horizontal and Vertical)
        int[][] Gx = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };

        int[][] Gy = {
                {-1, -2, -1},
                { 0,  0,  0},
                { 1,  2,  1}
        };

        // 3. Loop through every pixel (Skip borders to avoid IndexOutOfBounds)
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {

                int pixelX = 0;
                int pixelY = 0;

                // 4. Apply the Matrix (Convolution)
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        // Get neighbor pixel index
                        int neighborVal = pixels[(y + i) * w + (x + j)];

                        // Convert to Grayscale (Average of RGB) for calculation
                        int r = (neighborVal >> 16) & 0xff;
                        int g = (neighborVal >> 8) & 0xff;
                        int b = neighborVal & 0xff;
                        int intensity = (r + g + b) / 3;

                        // Accumulate gradients
                        pixelX += intensity * Gx[i + 1][j + 1];
                        pixelY += intensity * Gy[i + 1][j + 1];
                    }
                }

                // 5. Calculate Magnitude (The "Strength" of the edge)
                int magnitude = (int) Math.sqrt((pixelX * pixelX) + (pixelY * pixelY));

                // 6. Clamp value to 0-255 
                if (magnitude > 255) magnitude = 255;
                if (magnitude < 0) magnitude = 0;

                // 7. Create the new pixel (Grayscale output)
                output[y * w + x] = (0xFF << 24) | (magnitude << 16) | (magnitude << 8) | magnitude;
            }
        }
        return output;
    }

}


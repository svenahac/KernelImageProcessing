import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.File;

public class Parallel {

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public static void convolution(String fileLocation, float[][] kernelMatrix, float multiplier) throws IOException {
        int len = kernelMatrix.length;

        BufferedImage inputImg = ImageIO.read(new File(fileLocation));
        int width = inputImg.getWidth();
        int height = inputImg.getHeight();

        BufferedImage outputImg = new BufferedImage(width, height, inputImg.getType());

        long start = System.currentTimeMillis();

        Thread[] threads = new Thread[NUM_THREADS];

        int rowsPerThread = height / NUM_THREADS;
        int remainingRows = height % NUM_THREADS;

        int startRow = 0;

        for (int i = 0; i < NUM_THREADS; i++) {
            int rows = rowsPerThread;
            if (i == NUM_THREADS - 1) {
                rows += remainingRows;
            }

            threads[i] = new Thread(new ConvolutionTask(inputImg, outputImg, startRow, rows, len, kernelMatrix, multiplier));
            threads[i].start();

            startRow += rows;
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long finish = System.currentTimeMillis();
        long totalTime = finish - start;

        Utils.finalize(outputImg, totalTime);

    }

    private static class ConvolutionTask implements Runnable {
        private final BufferedImage inputImg;
        private final BufferedImage outputImg;
        private final int startRow;
        private final int numRows;
        private final int len;
        private final float[][] kernelMatrix;
        private final float multiplier;

        public ConvolutionTask(BufferedImage inputImg, BufferedImage outputImg, int startRow, int numRows,
                               int len, float[][] kernelMatrix, float multiplier) {
            this.inputImg = inputImg;
            this.outputImg = outputImg;
            this.startRow = startRow;
            this.numRows = numRows;
            this.len = len;
            this.kernelMatrix = kernelMatrix;
            this.multiplier = multiplier;
        }

        @Override
        public void run() {
            int width = inputImg.getWidth();
            int height = inputImg.getHeight();

            for (int i = 0; i < width; i++) {
                for (int j = startRow; j < startRow + numRows; j++) {
                    float oldRed = 0f;
                    float oldGreen = 0f;
                    float oldBlue = 0f;

                    for (int k = 0; k < len; k++) {
                        for (int l = 0; l < len; l++) {
                            int a = (i - len / 2 + k + width) % width;
                            int b = (j - len / 2 + l + height) % height;

                            int rgbTotal = inputImg.getRGB(a, b);

                            int rgbRed = (rgbTotal >> 16) & 0xff;
                            int rgbGreen = (rgbTotal >> 8) & 0xff;
                            int rgbBlue = (rgbTotal) & 0xff;

                            oldRed += (rgbRed * kernelMatrix[k][l]);
                            oldGreen += (rgbGreen * kernelMatrix[k][l]);
                            oldBlue += (rgbBlue * kernelMatrix[k][l]);
                        }
                    }

                    int red = Math.min(Math.max((int) (oldRed * multiplier), 0), 255);
                    int green = Math.min(Math.max((int) (oldGreen * multiplier), 0), 255);
                    int blue = Math.min(Math.max((int) (oldBlue * multiplier), 0), 255);

                    Color color = new Color(red, green, blue);
                    outputImg.setRGB(i, j, color.getRGB());
                }
            }
        }
    }
}

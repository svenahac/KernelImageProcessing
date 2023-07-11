
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.File;

public class Sequential {

    public static void convolution(String fileLocation, float[][] kernelMatrix, float multiplier) throws IOException {

        int len = kernelMatrix.length;

        BufferedImage inputImg = ImageIO.read(new File(fileLocation));
        int width = inputImg.getWidth();
        int height = inputImg.getHeight();

        BufferedImage outputImg = new BufferedImage(width, height, inputImg.getType());

        long start = System.currentTimeMillis();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                float oldRed = 0f;
                float oldGreen = 0f;
                float oldBlue = 0f;

                for (int k = 0; k < len; k++) {
                    for (int l = 0; l < len; l++) {

                        // Get coordinates, if it's on edge it goes to opposite side

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

                // Set color of the pixel
                Color color = new Color(red, green, blue);
                outputImg.setRGB(i, j, color.getRGB());
            }
        }

        long finish = System.currentTimeMillis();
        long totalTime = finish - start;

        Utils.finalize(outputImg, totalTime);


    }

}
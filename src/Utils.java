import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Utils {
    public static void finalize(BufferedImage output, long timeElapsed) throws IOException {

        String fileOutputPath = "src/Temp/temp";


        ImageIO.write(output, "jpg", new File(fileOutputPath + ".jpg"));

        StartScreen.outputImg = output;

        StartScreen.timeL.setText("Total time: " + timeElapsed + "ms");

    }
    public static void finalizeD(BufferedImage output, long startTime) throws IOException {

        String fileOutputPath = "src/Temp/temp";


        ImageIO.write(output, "jpg", new File(fileOutputPath + ".jpg"));

        StartScreen.outputImg = output;

        StartScreen.timeL.setText("Total time: " + (System.currentTimeMillis()-startTime) + "ms");

    }
}

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Utils {

    private static boolean done = false;
    public static void finalize(BufferedImage output, long timeElapsed) throws IOException {

        String fileOutputPath = "src/Temp/temp";


        ImageIO.write(output, "jpg", new File(fileOutputPath + ".jpg"));

        StartScreen.outputImg = output;

        StartScreen.timeL.setText("Total time: " + timeElapsed + "ms");

    }
}

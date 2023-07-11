import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;

public class Client implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            Socket client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                String[] data = inMessage.split("&");
                if (inMessage.startsWith("/process")) {
                    long start = System.currentTimeMillis();
                    float[][] kernelMatrix = Kernels.getKernelMatrix(data[1]);
                    float factor = Float.parseFloat(data[2]);
                    byte[] receivedArray = unpackImageData(data[3]);
                    BufferedImage receivedImage = byteArrayToBufferedImage(receivedArray);
                    BufferedImage processedImage = convolution(receivedImage, kernelMatrix, factor);
                    long finish = System.currentTimeMillis();
                    long totalTime = finish - start;
                    out.println("/finished" + "&"+totalTime+"&"+ Arrays.toString(packImageData(processedImage)));
                } else {
                    System.out.println(inMessage);
                }
            }

        } catch (IOException e) {
            shutdown();
        }
    }


    public byte[] packImageData(BufferedImage inputImg) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(inputImg, "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }

    public byte[] unpackImageData(String inputImg) throws IOException {
        String[] stringArray = inputImg.substring(1, inputImg.length() - 1).split(", ");
        byte[] byteArray = new byte[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            byteArray[i] = Byte.parseByte(stringArray[i]);
        }
        return byteArray;
    }





    // write byte[] to BufferedImage
    public BufferedImage byteArrayToBufferedImage(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        BufferedImage image = ImageIO.read(bis);
        bis.close();
        return image;
    }

    // write image to file
    public void writeImageToFile(BufferedImage image, String filePath) throws IOException {
        File outputfile = new File(filePath);
        ImageIO.write(image, "jpg", outputfile);
    }



    public void shutdown(){
        done = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String msg = inReader.readLine();
                    if(msg.equals("/quit")) {

                        inReader.close();
                        shutdown();
                    } else if (msg.startsWith("/conv")){

                    }
                    else
                    {
                        out.println(msg);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    private BufferedImage convolution(BufferedImage inputImg, float[][] kernelMatrix, float multiplier) throws IOException {
        int len = kernelMatrix.length;
        int height = inputImg.getHeight();
        int width = inputImg.getWidth();

        BufferedImage outputImg = new BufferedImage(width, height, inputImg.getType());

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
        return outputImg;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
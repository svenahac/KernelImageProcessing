import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private final String filePath;
    private final String kernelMatrix;
    private final float factor;
    private ArrayList<ConnectionHandler> connections;
    private ArrayList<Boolean> workingProcess;
    private ArrayList<BufferedImage> images;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(String filePath, String kernelMatrix, float factor) {
        connections = new ArrayList<>();
        done = false;
        this.filePath = filePath;
        this.kernelMatrix = kernelMatrix;
        this.factor = factor;
        workingProcess = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }

        } catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void broadcastSpecific(String message) throws IOException {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message + "&" + Arrays.toString(ch.packImageData((images.get(ch.id)))));
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {

                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String msg;
        public int id;

        private BufferedImage mergedImage;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                id = connections.indexOf(this);
                workingProcess.add(true);
                images = splitImage(filePath, connections.size());
                long start = System.currentTimeMillis();
                broadcastSpecific("/process" + "&" + kernelMatrix + "&" + factor);

                while ((msg = in.readLine())!= null) {
                    if (msg.startsWith("/finished")) {
                        String[] data = msg.split("&");
                        BufferedImage receivedImage = byteArrayToBufferedImage(unpackImageData(data[2]));
                        workingProcess.set(id, true);
                        images.set(id, receivedImage);

                        // check if every boolean in workingProcess is true
                        boolean allTrue = true;
                        for (boolean b : workingProcess) {
                            if (!b) {
                                allTrue = false;
                                break;
                            }
                        }
                        if (allTrue) {
                            if (connections.size()  > 1){
                                mergedImage = mergeImage(images);
                            } else {
                                mergedImage = images.get(0);
                            }
                            //writeImageToFile(mergedImage, "/home/vexane/Documents/Programiranje/ImgProc/out/production/ImgProc/Temp/temp.jpg");
                            //writeImageToFile(mergedImage,"./src/Temp/temp2.jpg");
                            long finish = System.currentTimeMillis();
                            long totalTime = finish - start;
                            Utils.finalize(mergedImage, Long.parseLong(data[1]));
                            StartScreen.insertImage("./src/Temp/temp.jpg", StartScreen.afterI);
                            broadcast("Image has been processed");
                        }
                    } else if (msg.startsWith("/quit")) {
                        broadcast("Client has disconnected");
                        shutdown();
                    } else {
                        broadcast(msg);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }

        }
        // check if all processes are finished then merge images
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
        public BufferedImage byteArrayToBufferedImage(byte[] bytes) throws IOException {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            BufferedImage image = ImageIO.read(bis);
            bis.close();
            return image;
        }

        public void writeImageToFile(BufferedImage image, String filePath) throws IOException {
            File outputfile = new File(filePath);
            ImageIO.write(image, "jpg", outputfile);
        }

        public BufferedImage mergeImage(ArrayList<BufferedImage> images) {
            int height = images.get(0).getHeight();
            int width = images.get(0).getWidth();
            BufferedImage mergedImage = new BufferedImage(width, height * images.size(), BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < images.size(); i++) {
                BufferedImage img = images.get(i);
                for (int y = 0; y < img.getHeight(); y++) {
                    for (int x = 0; x < img.getWidth(); x++) {
                        mergedImage.setRGB(x, y + i * height, img.getRGB(x, y));
                    }
                }
            }
            return mergedImage;
        }

        public ArrayList<BufferedImage> splitImage(String filePath, int numSplits) throws IOException {
            BufferedImage inputImg = ImageIO.read(new File(filePath));
            int height = inputImg.getHeight();
            int width = inputImg.getWidth();
            int splitHeight = height / numSplits;
            ArrayList<BufferedImage> splitImages = new ArrayList<>();
            for (int i = 0; i < numSplits; i++) {
                BufferedImage splitImg = inputImg.getSubimage(0, i * splitHeight, width, splitHeight);
                splitImages.add(splitImg);
            }
            return splitImages;
        }


        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
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
    }



}
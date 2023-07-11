public class Distributed {

    public static void startServer(String fileLocation, String kernel, float multiplier){
        Server server = new Server(fileLocation, kernel, multiplier);
        server.run();

    }
}

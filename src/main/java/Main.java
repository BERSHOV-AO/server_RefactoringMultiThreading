public class Main {

    private static final int PORT_SERVER = 9999;

    public static void main(String[] args) {
        Server server = new Server(PORT_SERVER);
        server.start();
    }
}

import java.io.IOException;

public class Main {

    private static final int PORT_SERVER = 9999;

    public static void main(String[] args) {
        Server server = new Server(PORT_SERVER);

        // добавление handler'ов (обработчиков)
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            try {
                server.outContentResponse(responseStream, "200", "OK");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> server.outContentResponse(responseStream, "503", "Service Unavailable"));

        server.addHandler("GET", "/", ((request, outputStream) -> server.defaultHandler(outputStream, "index.html")));

        server.start();
    }
}

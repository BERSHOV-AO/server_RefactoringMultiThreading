import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final String NOT_FOUND_TEXT = "Not Found";
    private final String NOT_FOUND_CODE = "404";
    private final int NUMBER_THREADS = 64;
    private final int PORT_SERVER_SOCKET;
    List<String> validPathsList = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    private ExecutorService threadPool;
    private ConcurrentHashMap<String, Map<String, Handler>> handlersStorageMap;

    public Server(int port) {

        PORT_SERVER_SOCKET = port;
        threadPool = Executors.newFixedThreadPool(NUMBER_THREADS);
        handlersStorageMap = new ConcurrentHashMap<>();
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(PORT_SERVER_SOCKET)) {
            System.out.println("Веб-сервер запущен на порту " + PORT_SERVER_SOCKET);
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключение принято от клиента: " + clientSocket.getInetAddress().getHostAddress());
                threadPool.execute(() -> handleConnection(clientSocket));
            }
        } catch (IOException e) {
            // throw new RuntimeException(e);
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (final var in = new BufferedInputStream(clientSocket.getInputStream());
             final var out = new BufferedOutputStream(clientSocket.getOutputStream())
        ) {
            Request request = Request.createRequest(in);
            // Проверяем наличие плохих запросов и разрываем соединение
            if (request == null || !handlersStorageMap.containsKey(request.getMethod())) {
                outContentResponse(out, NOT_FOUND_CODE, "Error Request");
                return;
            } else {
                // Печатаем инфорацию по нашему запросу
                showDebugRequest(request);
            }

            // Получаем путь, MAP
            Map<String, Handler> handlerMap = handlersStorageMap.get(request.getMethod());
            String requestPath = request.getPath().split("\\?")[0];
            if (handlerMap.containsKey(requestPath)) {
                Handler handler = handlerMap.get(requestPath);
                handler.handle(request, out);
            } else {
                // не найден
                if (!validPathsList.contains(requestPath)) {
                    outContentResponse(out, NOT_FOUND_CODE, NOT_FOUND_TEXT);
                } else {
                    // default
                    defaultHandler(out, requestPath);
                }
            }

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    void addHandler(String method, String path, Handler handler) {
        if (!handlersStorageMap.containsKey(method)) {
            handlersStorageMap.put(method, new HashMap<>());
        }
        handlersStorageMap.get(method).put(path, handler);
    }

    public void outContentResponse(BufferedOutputStream out, String code, String status) throws IOException {
        out.write((
                "HTTP/1.1 " + code + " " + status + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void showDebugRequest(Request request) {
        System.out.println("**********************************");
        System.out.println("Show request: ");
        System.out.println("Method - " + request.getMethod());
        System.out.println("Path - " + request.getPath());
        System.out.println("Headers - " + request.getHeaders());
        System.out.println("-----------------------------------");
        System.out.println("Query Params: ");
        for (var para : request.getQueryParams()) {
            System.out.println(para.getName() + " : " + para.getValue());
        }
        System.out.println("-----------------------------------");
        System.out.println("name test:");
        System.out.println(request.getQueryParam("Duke").getName());
        System.out.println("name-value test:");
        System.out.println(request.getQueryParam("Info").getValue());
        System.out.println("**********************************");

    }

    void defaultHandler(BufferedOutputStream out, String path) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // Если у нас приходит запрос на страничку "/classic.html"
        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
            return;
        }

        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }
}
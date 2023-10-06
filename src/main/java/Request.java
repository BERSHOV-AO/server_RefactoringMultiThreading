import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Request {

//    List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(responseString, Charset.forName("utf-8"));
//    NameValuePair - это специальная пара <Key, Value>, которая используется для представления параметров
//    в HTTP-запросе, т.е. www.example.com?key=value.
//    NameValuePair является интерфейсом и определяется в http-клиенте apache,
//    который широко используется в java для обработки операций HTTP. A List<NameValuePair> -
//    это всего лишь список пар <Key, Value> и будет использоваться в качестве параметров в запросе http post.

    private final List<String> headersList;
    private final String method;
    private final String path;
    public final static String GET = "GET";
    public final static String POST = "POST";

    // NameValuePair - это специальная пара <Key, Value> которая используется для представления параметров
    // в HTTP-запросе, т.е. www.example.com?key=value.
    private List<NameValuePair> paramsList;

    public Request(String method, String path, List<String> headersList, List<NameValuePair> paramsList) {
        this.method = method;
        this.path = path;
        this.headersList = headersList;
        this.paramsList = paramsList;
    }

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        headersList = null;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    static Request createRequest(BufferedInputStream in) throws IOException, URISyntaxException {
        final List<String> methodsList = List.of(GET, POST);

        // лимит на request line + заголовки
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return null;
        }

        // Зачитываем request line, он должен состоять из трех параметров
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        // check for method
        final var method = requestLine[0];
        if (!methodsList.contains(method)) {
            return null;
        }
        System.out.println(method);

        final var path = requestLine[1];
        if (!path.startsWith("/")) {          // проверить
            return null;
        }
        System.out.println(path);

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }

        // Отматываем на начало буфера
        in.reset();
        // Пропускаем requestLine
        in.skip(headersStart);

        final var headerBytes = in.readNBytes(headersEnd - headersStart);
        List<String> headers = Arrays.asList(new String(headerBytes).split("\r\n"));

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);

        return new Request(method, path, headers, params);
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {

        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public NameValuePair getQueryParam(String name) {
        return getQueryParams().stream()
                .filter(param -> param.getName().equalsIgnoreCase(name))
                .findFirst().orElse(new NameValuePair() {
                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public String getValue() {
                        return "";
                    }
                });
    }

    public List<NameValuePair> getQueryParams() {
        return paramsList;
    }

    public List<String> getHeaders() {
        return headersList;
    }


}

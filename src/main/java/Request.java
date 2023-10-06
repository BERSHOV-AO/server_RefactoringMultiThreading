import org.apache.http.NameValuePair;
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
}

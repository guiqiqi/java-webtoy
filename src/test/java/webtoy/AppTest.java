package webtoy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import webtoy.Request.InvalidRequest;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    public AppTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    @org.junit.Test
    public void testGETRequest() throws InvalidRequest {
        String data = "GET /hello?test=1&other=0&incompleteIgnore HTTP/1.1\r\n\r\n";
        Request request = new Request(data);
        assertEquals(request.method, HTTPMethod.GET);
        assertEquals(request.url, "/hello?test=1&other=0&incompleteIgnore");
        assertEquals(request.path, "/hello");
        assertEquals(request.args.get("test"), "1");
        assertEquals(request.args.get("other"), "0");
        assertFalse(request.args.containsKey("incompleteIgnore"));
    }

    @org.junit.Test
    public void testPOSTRequest() throws InvalidRequest {
        String headers = "POST /foo.php HTTP/1.1\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 43\r\nCookie: PHPSESSID=r2t5uvjq435r4q7ib3vtdjq120\r\n\r\n";
        String body = "first_name=John&last_name=Doe&action=Submit";
        Request request = new Request(headers);
        request.parseBody(body);
        assertEquals(request.cookies.get("PHPSESSID"), "r2t5uvjq435r4q7ib3vtdjq120");
        assertEquals(request.forms.get("first_name"), "John");
        assertEquals(request.forms.get("action"), "Submit");
    }

    @org.junit.Test
    public void testApplicationRequest() throws InvalidRequest {
        Application app = new Application();
        app.route("/hello", request -> new Response(request.args.getOrDefault("name", "anonymous")));
        Request request = new Request("GET /hello?name=guiqiqi HTTP/1.1\r\n\r\n");
        Response response = app.respond(request);
        assertEquals(response.code, Integer.valueOf(200));
        assertEquals(response.content, "guiqiqi");
    }

    /**
     * Please ensure your localhost 9999 is in free :)
     * @throws InterruptedException 
     */
    @org.junit.Test
    public void testServerLevelResponse() throws IOException, InterruptedException {
        Application app = new Application();
        app.route("/hello", request -> {
            String user = request.args.getOrDefault("name", "anonymous");
            return new Response(String.format("Hello %s!", user));
        });
        Server server = new Server("localhost", 9999, app);
        new Thread(server::start).start();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:9999/hello?name=guiqiqi"))
                .build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertEquals(response.body(), "Hello guiqiqi!");
        server.close();
    }
}

package webtoy;

import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        Application app = new Application();
        Handler handler = (request) -> {
            if (request.method == HTTPMethod.GET)
                return new Response(request.args.toString());
            if (request.method == HTTPMethod.POST)
                return new Response(request.forms.toString());
            return new Response(404);  // Never reached
        };
        app.route("/foo", List.of(HTTPMethod.GET, HTTPMethod.POST), handler);
        try {
            Server server = new Server("localhost", 9009, app);
            server.start();
        } catch (IOException error) {
            System.out.println("error in tcp");
            return;
        }
    }
}

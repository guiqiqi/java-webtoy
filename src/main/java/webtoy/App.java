package webtoy;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    public void main(String[] args) {
        System.out.println("Hello World!");
        Router router = new Router();
        Handler handler = (request) -> new Response(404);
        router.register("/", List.of(HTTPMethod.GET), handler);
    }
}

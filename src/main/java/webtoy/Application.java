package webtoy;

import java.util.List;

public class Application {

    private Router router = new Router();

    /**
     * Respond to TCP server with request.
     * 
     * If no suitable method found but some other path matched, return an HTTP 405 response;
     * If no path matched at all, return an HTTP 404 response;
     * If any Exception catched during handler's applying, return an HTTP 502 response.
     * 
     * @param request parsed from TCP server
     * @return generated response
     */
    public Response respond(Request request) {
        try {
            Handler handler = this.router.match(request.path, request.method);
            return handler.apply(request);
        } catch (Router.UnsupportedMethod error) {
            return new Response(405);
        } catch (Router.URLNotFound error) {
            return new Response(404);
        } catch (Exception error) {
            return new Response(502);
        }
    }

    /**
     * Register handler with given path and methods.
     * @param path of request
     * @param method bound to given url path
     * @param handler for generating response
     */
    public void route(String path, HTTPMethod method, Handler handler) {
        this.router.register(path, List.of(method), handler);
    }

    public void route(String path, List<HTTPMethod> methods, Handler handler) {
        this.router.register(path, methods, handler);
    }

    public void route(String path, Handler handler) {
        this.route(path, HTTPMethod.GET, handler);
    }
}

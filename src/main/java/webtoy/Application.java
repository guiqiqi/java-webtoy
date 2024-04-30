package webtoy;

import java.util.List;

public abstract class Application {

    public abstract Response respond(Request request);

    public abstract void route(String path, HTTPMethod method, Handler handler);

    public void route(String path, List<HTTPMethod> methods, Handler handler) {
        for (HTTPMethod method : methods)
            this.route(path, method, handler);
    }

    public void route(String path, Handler handler) {
        this.route(path, HTTPMethod.GET, handler);
    }
}

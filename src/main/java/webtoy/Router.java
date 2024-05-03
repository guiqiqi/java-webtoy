package webtoy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Router {

    /**
     * Path bind with method.
     */
    class PathMethod {
        public final String path;
        public final HTTPMethod method;

        PathMethod(String path, HTTPMethod method) {
            this.path = path;
            this.method = method;
        }

        /**
         * Hashcode of PathMethod should be hashCode of String "Method:Path".
         */
        @Override
        public int hashCode() {
            return String.format("%s:%s", this.method, this.path).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            PathMethod other = (PathMethod) obj;
            return this.path.equals(other.path) && this.method.equals(other.method);
        }
    }

    /**
     * Given path found in router but no method supported.
     * Generally need to respond an HTTP 405 response.
     */
    class UnsupportedMethod extends Exception {
        public UnsupportedMethod(String message) {
            super(message);
        }
    }

    /**
     * No handler for this path found.
     * Generallt need to repond an HTTP 404 response.
     */
    class URLNotFound extends Exception {
        public URLNotFound(String message) {
            super(message);
        }
    }

    private Map<String, List<HTTPMethod>> acceptableMethods;
    private Map<PathMethod, Handler> handlers;

    Router() {
        this.acceptableMethods = new HashMap<>();
        this.handlers = new HashMap<>();
    }

    /**
     * Register path with selected methods to given handler.
     * @param path of request
     * @param methods appceptable of handler
     * @param handler for responding
     */
    public void register(String path, List<HTTPMethod> methods, Handler handler) {
        for (HTTPMethod method : methods) {
            if (!this.acceptableMethods.containsKey(path))
                this.acceptableMethods.put(path, new LinkedList<>());
            this.acceptableMethods.get(path).add(method);
            this.handlers.put(new PathMethod(path, method), handler);
        }
    }

    /**
     * Try to match handler related to given path with method.
     * @param path of request
     * @param method of request
     * @return handler realted to given path and method
     * @throws UnsupportedMethod if any handler of given path found but no handler for given method
     * @throws URLNotFound if no handler realted to given url path
     */
    public Handler match(String path, HTTPMethod method) throws UnsupportedMethod, URLNotFound {
        if (!this.acceptableMethods.containsKey(path))
            throw new URLNotFound(String.format("invalid url %s", path));
        if (!this.acceptableMethods.get(path).contains(method))
            throw new UnsupportedMethod(String.format("unsupprted method %s for url %s", path, method.toString()));
        return this.handlers.get(new PathMethod(path, method));
    }
}

package webtoy;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class Response {

    public final Integer code;
    public final String content;
    public final Map<String, String> headers;

    /**
     * List all validate HTTP/1.1 response code and its default description.
     * 
     * See more: https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
     */
    public static final Map<Integer, String> StatusCodes = Map.ofEntries(
            Map.entry(100, "Continue"),
            Map.entry(101, "Switching Protocols"),
            Map.entry(102, "Processing"),
            Map.entry(103, "Early Hints"),
            Map.entry(200, "OK"),
            Map.entry(201, "Created"),
            Map.entry(202, "Accepted"),
            Map.entry(203, "Non-Authoritative Information"),
            Map.entry(204, "No Content"),
            Map.entry(205, "Reset Content"),
            Map.entry(206, "Partial Content"),
            Map.entry(207, "Multi-Status"),
            Map.entry(208, "Already Reported"),
            Map.entry(226, "IM Used"),
            Map.entry(300, "Multiple Choices"),
            Map.entry(301, "Moved Permanently"),
            Map.entry(302, "Found"),
            Map.entry(303, "See Other"),
            Map.entry(304, "Not Modified"),
            Map.entry(305, "Use Proxy"),
            Map.entry(306, "(Unused)"),
            Map.entry(307, "Temporary Redirect"),
            Map.entry(308, "Permanent Redirect"),
            Map.entry(400, "Bad Request"),
            Map.entry(401, "Unauthorized"),
            Map.entry(402, "Payment Required"),
            Map.entry(403, "Forbidden"),
            Map.entry(404, "Not Found"),
            Map.entry(405, "Method Not Allowed"),
            Map.entry(406, "Not Acceptable"),
            Map.entry(407, "Proxy Authentication Required"),
            Map.entry(408, "Request Timeout"),
            Map.entry(409, "Conflict"),
            Map.entry(410, "Gone"),
            Map.entry(411, "Length Required"),
            Map.entry(412, "Precondition Failed"),
            Map.entry(413, "Payload Too Large"),
            Map.entry(414, "URI Too Long"),
            Map.entry(415, "Unsupported Media Type"),
            Map.entry(416, "Range Not Satisfiable"),
            Map.entry(417, "Expectation Failed"),
            Map.entry(421, "Misdirected Request"),
            Map.entry(422, "Unprocessable Entity"),
            Map.entry(423, "Locked"),
            Map.entry(424, "Failed Dependency"),
            Map.entry(425, "Too Early"),
            Map.entry(426, "Upgrade Required"),
            Map.entry(427, "Unassigned"),
            Map.entry(428, "Precondition Required"),
            Map.entry(429, "Too Many Requests"),
            Map.entry(430, "Unassigned"),
            Map.entry(431, "Request Header Fields Too Large"),
            Map.entry(451, "Unavailable For Legal Reasons"),
            Map.entry(500, "Internal Server Error"),
            Map.entry(501, "Not Implemented"),
            Map.entry(502, "Bad Gateway"),
            Map.entry(503, "Service Unavailable"),
            Map.entry(504, "Gateway Timeout"),
            Map.entry(505, "HTTP Version Not Supported"),
            Map.entry(506, "Variant Also Negotiates"),
            Map.entry(507, "Insufficient Storage"),
            Map.entry(508, "Loop Detected"),
            Map.entry(509, "Unassigned"),
            Map.entry(510, "Not Extended"),
            Map.entry(511, "Network Authentication Required"));

    public static final String Version = "HTTP/1.1";
    public static String DefaultContentType = "text/html";

    /**
     * Throw when handler made an invalid response (like unknown response code).
     */
    public class InvalidResponse extends Exception {
        InvalidResponse(String message) {
            super(message);
        }
    }

    /**
     * Make an response, headers could be add in Response.headers.
     * @param code of response
     * @param content of response
     */
    public Response(Integer code, String content) {
        this.code = code;
        this.content = content;
        this.headers = new HashMap<>();
    }

    /**
     * Shortcuts for making simple status code response (like 404).
     * @param code of response
     */
    public Response(Integer code) {
        this(code, "");
    }

    /**
     * Shortcuts for making 200 response.
     * @param content of resposne
     */
    public Response(String content) {
        this(200, content);
    }

    /**
     * Make HTTP/1.1 response.
     * 
     * The full response should looks like:
     * 
     *   HTTP/1.1 200 OK<CRLF>
     *   Server: Simple-Python-HTTP-Server<CRLF>
     *   Content-Type: text/plain<CRLF>
     *   Content-Length: 37<CRLF>
     *   <CRLF>
     *   [Body]
     * 
     * See more: https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html
     */
    @Override
    public String toString() {
        StringJoiner lines = new StringJoiner("\r\n");
        lines.add(String.format("%s %d %s", Version, this.code, StatusCodes.getOrDefault(code, "unknown")));

        // Set Content-Length and Content-Type headers if not set
        this.headers.put("Content-Length", Integer.toString(this.content.length()));
        if (!headers.containsKey("Content-Type"))
            this.headers.put("Content-Typ", DefaultContentType);

        for (String key : this.headers.keySet())
            lines.add(String.format("%s: %s", key, this.headers.get(key)));
        lines.add("");
        lines.add(this.content);
        return lines.toString();
    }

    // For serving invalid request form client
    public static final String InvalidRequestResponse = new Response(501).toString();
    public static final String LengthRequiredResponse = new Response(411).toString();
    public static final String ContentTooLargeResponse = new Response(413).toString();
    public static final String HeaderTooLargeResponse = new Response(451).toString();
}

package webtoy;

/**
 * All supported HTTP/1.1 method defined in RFC2616.
 * See more: https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
 */
public enum HTTPMethod {
    OPTIONS,
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    TRACE,
    CONNECT
}

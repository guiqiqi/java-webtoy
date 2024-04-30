package webtoy;

import java.util.function.Function;

/**
 * An HTTP request handler is a function which could be called for reducing HTTP request.
 * 
 * Funciton will be called with an Request paramater generated from Application and need to return an Response.
 * Response could be initialized by multiple ways, see more in Response.
 */
public interface Handler extends Function<Request, Response> {

}

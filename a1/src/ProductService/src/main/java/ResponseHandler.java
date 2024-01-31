import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * The {@code ResponseHandler} class provides utility methods for sending HTTP responses.
 */
public class ResponseHandler {
    
    /**
     * Sends an HTTP response with the specified content, status code, and length.
     *
     * @param exchange   The HTTP exchange object representing the request and response.
     * @param response   The content of the response to be sent.
     * @param statusCode The HTTP status code to be set in the response.
     * @throws IOException If an I/O error occurs while sending the response.
     */
    public static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}


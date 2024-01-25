import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetUserServiceHandler implements HttpHandler {

    private static UserDatabaseManager db;

    public GetUserServiceHandler() {
        db = new UserDatabaseManager();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // String requestURI = exchange.getRequestURI().toString();
            String requestMethod = exchange.getRequestMethod();
            String clientAddress = exchange.getRemoteAddress().getAddress().toString();
            String requestURI = exchange.getRequestURI().toString();

            System.out.println("Request method: " + requestMethod);
            System.out.println("Client Address: " + clientAddress);
            System.out.println("Request URI: " + requestURI);
            String[] parts = requestURI.split("/");

            // Get the last part (user ID)
            String id = parts[parts.length - 1];
            Map<String, String> responseBodyMap = db.getUser(Integer.parseInt(id));
            String jsonResponse = new ObjectMapper().writeValueAsString(responseBodyMap);
            System.out.println(jsonResponse);
            sendResponse(exchange);
        }
    }

    private static void sendResponse(HttpExchange exchange) throws IOException {
        String response = "Lecture foobar foobar Received POST request for /test";
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}
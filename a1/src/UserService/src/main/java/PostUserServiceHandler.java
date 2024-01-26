import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpServer;

public class PostUserServiceHandler implements HttpHandler {

    private static HttpServer server;  
    private static UserDatabaseManager db;

    public PostUserServiceHandler(HttpServer userServer) {
        server = userServer;  // Used this.server to refer to the class-level field
        db = new UserDatabaseManager();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> requestBodyMap = processRequestBody(exchange);
        Integer id = Integer.parseInt(requestBodyMap.get("id"));
        String userURI = "/user/" + id;
        // Handle POST request for /test
        if ("POST".equals(exchange.getRequestMethod())) {
            String response = "";
            if ("update".equals(requestBodyMap.get("command"))) {
                Boolean isUpdated = db.updateExistingUser(id, requestBodyMap);
                if (isUpdated) {
                    response = "The user is successfully updated";
                } else {
                    response = "The user is unsuccessfully updated";
                }
            } else if ("delete".equals(requestBodyMap.get("command"))) {
                Boolean isDeleted = db.deleteExistingUser(id, requestBodyMap);
                server.removeContext(userURI);
                if (isDeleted) {
                    response = "The user is successfully deleted";
                } else {
                    response = "The user is unsuccessfully updated";
                }
            } else if ("create".equals(requestBodyMap.get("command"))) {
                Boolean isCreated = db.createNewUser(id, requestBodyMap);
                server.createContext(userURI, new GetUserServiceHandler());
                if (isCreated) {
                    response = "The user is successfully created";
                } else {
                    response = "The user is unsuccessfully created";
                }
            }
            sendResponse(exchange, response);
        } else {
            exchange.sendResponseHeaders(405,0);
            exchange.close();
        }
    }


    private static String getRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            return requestBody.toString();
        }
    }

    private static Map<String, String> processRequestBody(HttpExchange exchange) {
        try {
            String requestBody = getRequestBody(exchange);

            Map<String, String> requestBodyMap = Arrays.stream(requestBody.split("&"))
                    .map(pair -> pair.split("="))
                    .filter(keyValue -> keyValue.length == 2)
                    .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]));
            return requestBodyMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}


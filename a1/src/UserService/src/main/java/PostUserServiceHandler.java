import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpServer;

public class PostUserServiceHandler implements HttpHandler {

    private final HttpServer server;
    private final UserDatabaseManager db;

    public PostUserServiceHandler(HttpServer userServer) {
        server = userServer;
        db = new UserDatabaseManager();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                Map<String, Object> requestBodyMap = processRequestBody(exchange);
                // String requestURI = exchange.getRequestURI().toString();
                String requestMethod = exchange.getRequestMethod();
                String clientAddress = exchange.getRemoteAddress().getAddress().toString();
                String requestURI = exchange.getRequestURI().toString();

                System.out.println("Request method: " + requestMethod);
                System.out.println("Client Address: " + clientAddress);
                System.out.println("Request URI: " + requestURI);
                if (requestBodyMap != null) {
                    int id;
                    String response;
                    Object objectID = requestBodyMap.get("id");
                    String command = (String) requestBodyMap.get("command");
                    System.out.println("Request URI: " + requestURI);
                    // Assuming exchange is an HttpExchange object
                    System.out.println("Request Body: " + requestBodyMap);
                    System.out.println("Object ID: " + objectID);
                    System.out.println("Object ID Type: " + objectID.getClass().getName());

                    // Check if 'id' is present and of type Integer
                    if (objectID instanceof Integer) {
                        // Cast 'id' to Integer
                        id = (Integer) objectID;
                        System.out.println("Request URI: " + requestURI);

                        // Now 'id' can be used as an int
                    } else {
                        id = Integer.parseInt(objectID.toString());
                        System.out.println("Request: " + requestURI);

                    }
                    String userURI = "/user/" + id;
                    System.out.println("Hello: " + requestURI);

                    switch (command) {
                        case "create":
                            Boolean isCreated = db.createNewUser(requestBodyMap);
                            if (isCreated) {
                                response = "The user is successfully created";
                                server.createContext(userURI, new GetUserServiceHandler());
                                sendResponse(exchange, 200, response);
                            } else {
                                response = "The user is unsuccessfully created";
                                sendResponse(exchange, 500, response);
                            }
                            break;
                        case "update":
                            Boolean isUpdated = db.updateExistingUser(requestBodyMap);
                            if (isUpdated) {
                                response = "The user is successfully updated";
                                sendResponse(exchange, 200, response);
                            } else {
                                response = "The user is unsuccessfully updated";
                                sendResponse(exchange, 500, response);
                            }
                            break;
                        case "delete":
                            Boolean isDeleted = db.deleteExistingUser(requestBodyMap);
                            if (isDeleted) {
                                response = "The user is successfully deleted";
                                server.removeContext(userURI);
                                sendResponse(exchange, 200, response);
                            } else {
                                response = "The user is unsuccessfully deleted";
                                sendResponse(exchange, 500, response);
                            }
                            break;
                    default:
                        sendResponse(exchange, 400, "Invalid command");
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid ID format");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
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

    private static Map<String, Object> processRequestBody(HttpExchange exchange) {
        try {
            String requestBody = getRequestBody(exchange);

            return Arrays.stream(requestBody.split("&"))
                    .map(pair -> pair.split("="))
                    .filter(keyValue -> keyValue.length == 2)
                    .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

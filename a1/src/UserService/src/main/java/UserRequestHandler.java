import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class UserRequestHandler implements HttpHandler {

    private final HttpServer server;
    private final UserDatabaseManager db;
    private final ExecutorService threadPool;

    public UserRequestHandler(HttpServer userServer, ExecutorService httpThreadPool) {
        server = userServer;
        threadPool = httpThreadPool;
        db = new UserDatabaseManager();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                printRequestDetails(exchange);
                Map<String, String> requestBodyMap = getRequestBody(exchange);

                boolean hasEmptyValue = requestBodyMap.values().stream()
                        .anyMatch(value -> value instanceof String && ((String) value).isEmpty());

                System.out.println("Has Empty Value: " + hasEmptyValue);

                if (requestBodyMap != null) {
                    int id;
                    String response;
                    int statusCode;
                    String userURI;
                    ObjectMapper objectMapper = new ObjectMapper();

                    String command = requestBodyMap.remove("command");
                    String ID = requestBodyMap.getOrDefault("id", "1");
                    id = Integer.parseInt(ID);
                    userURI = "/user/" + id;
                    Map<String, String> stringMap = requestBodyMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));

                    switch (command) {
                        case "create":
                            handleCreate(exchange, id, hasEmptyValue, objectMapper, stringMap);
                            break;
                        case "update":
                            handleUpdate(exchange, id, objectMapper);
                            break;
                        case "delete":
                            handleDelete(exchange, id, hasEmptyValue, objectMapper);
                            break;
                        case "shutdown":
                            handleShutdown();
                            break;
                        case "restart":
                            handleRestart(exchange, objectMapper);
                            break;
                        default:
                            sendResponse(exchange, 400, "Invalid command");
                            exchange.close();
                            break;
                    }
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid ID format");
                exchange.close();
            }
        } else if ("GET".equals(exchange.getRequestMethod())) {
            handleGet(exchange);
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
            exchange.close();
        }
    }

    private void handleCreate(HttpExchange exchange, int id, boolean hasEmptyValue, ObjectMapper objectMapper, Map<String, String> stringMap) throws IOException {
        String response;
        int statusCode;
        if (hasEmptyValue) {
            statusCode = 400;
            response = "";
        } else {
            statusCode = db.createNewUser(stringMap, id);
            if (statusCode == 200) {
                System.out.println("The user is successfully created");
                response = objectMapper.writeValueAsString(stringMap);
            } else if (statusCode == 409) {
                response = "The user already exists";
            } else {
                response = "Internal Server Error";
            }
        }
        sendResponse(exchange, statusCode, response);
        exchange.close();
    }

    private void handleUpdate(HttpExchange exchange, int id, ObjectMapper objectMapper) throws IOException {
        String response;
        int statusCode = db.updateExistingUser(getRequestBody(exchange), id);
        if (statusCode == 200) {
            System.out.println("The user is successfully updated");
            response = objectMapper.writeValueAsString(db.getUser(id));
        } else if (statusCode == 400) {
            response = "The user is not found";
        } else {
            response = "Internal Server Error";
        }
        sendResponse(exchange, statusCode, response);
        exchange.close();
    }

    private void handleDelete(HttpExchange exchange, int id, boolean hasEmptyValue, ObjectMapper objectMapper) throws IOException {
        String response;
        int statusCode;
        if (hasEmptyValue) {
            statusCode = 400;
            response = "";
        } else {
            statusCode = db.deleteExistingUser(getRequestBody(exchange), id);
            if (statusCode == 200) {
                System.out.println("The user is successfully deleted");
                response = objectMapper.writeValueAsString(db.getUser(id));
            } else if (statusCode == 400) {
                response = "The user is not found";
            } else {
                response = "Internal Server Error";
            }
        }
        sendResponse(exchange, statusCode, response);
        exchange.close();
    }

    private void handleShutdown() {
        server.stop(5);
        threadPool.shutdownNow();
    }

    private void handleRestart(HttpExchange exchange, ObjectMapper objectMapper) throws IOException {
        List<User> existingUsers = db.getAllUsers();

        for (User user : existingUsers) {
            int userId = user.getId();
            String userURI = "/user/" + userId;
            String response = objectMapper.writeValueAsString(user);
            sendResponse(exchange, 200, response);
        }
        exchange.close();
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        try {
            printRequestDetails(exchange);
            String requestURI = exchange.getRequestURI().toString();
            String[] parts = requestURI.split("/");
            String id = parts[parts.length - 1];
            System.out.println(id);

            if (!db.isUserPresent(Integer.parseInt(id))) {
                String errorMessage = "User not found";
                sendResponse(exchange, 404, errorMessage);
            } else {
                Map<String, String> responseBodyMap = db.getUser(Integer.parseInt(id));
                String jsonResponse = new ObjectMapper().writeValueAsString(responseBodyMap);
                sendResponse(exchange, 200, jsonResponse);
                System.out.println(responseBodyMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Internal server error";
            sendResponse(exchange, 500, errorMessage);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static Map<String, String> getRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(requestBody.toString(), new TypeReference<HashMap<String, String>>() {});
        }
    }

    private void printRequestDetails(HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();
        String clientAddress = exchange.getRemoteAddress().getAddress().toString();
        String requestURI = exchange.getRequestURI().toString();

        System.out.println("Request method: " + requestMethod);
        System.out.println("Client Address: " + clientAddress);
        System.out.println("Request URI: " + requestURI);
    }
}

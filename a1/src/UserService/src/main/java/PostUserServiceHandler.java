import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class PostUserServiceHandler implements HttpHandler {

    private final HttpServer server;
    private final UserDatabaseManager db;
    private final ExecutorService threadPool;

    public PostUserServiceHandler(HttpServer userServer,  ExecutorService httpThreadPool) {
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

                    String objectID = (String) requestBodyMap.getOrDefault("id", "");
                    String command = (String) requestBodyMap.get("command");  
                    requestBodyMap.remove("command");
                    if (objectID.isEmpty()) {
                        switch (command) {
                            case "shutdown":
                                server.stop(5);
                                threadPool.shutdownNow();
                            case "restart":
                                server.start();
                                List<User> existingUsers = db.getAllUsers();

                                for (User user : existingUsers) {
                                    int userId = user.getId();
                                    userURI = "/user/" + userId;
                                    server.createContext(userURI, new GetUserServiceHandler());
                                }
                                response = objectMapper.writeValueAsString(existingUsers);
                                sendResponse(exchange, statusCode=200, response);
                                exchange.close();
                                break;
                        }
                    }
                    id = Integer.parseInt(objectID);
                    userURI = "/user/" + id;
                    Map<String,String> stringMap = requestBodyMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue()));
                    // Check if 'id' is present and of type Integer

                    switch (command) {
                        
                        case "create":
                            if (hasEmptyValue) {
                                statusCode = 400;
                                response = "";
                            } else {
                                statusCode = db.createNewUser(requestBodyMap, id);
                                if (statusCode == 200) {
                                    System.out.println("The user is successfully created");
                                    response = objectMapper.writeValueAsString(stringMap);      
                                    server.createContext(userURI, new GetUserServiceHandler());
                                } else if (statusCode == 409) {
                                    response = "The user already exists";
                                } else {
                                    response = "Internal Server Error";
                                }
                            }
                            sendResponse(exchange, statusCode, response);
                            exchange.close();
                            break;

                        case "update":
                            statusCode = db.updateExistingUser(requestBodyMap, id);
                            if (statusCode == 200) {
                                System.out.println("The user is successfully updated");
                                response =  objectMapper.writeValueAsString(db.getUser(id));
                            } else if (statusCode == 400) {
                                response = "The user is not found";
                            } else {
                                response = "Internal Server Error";
                            }
                            sendResponse(exchange, statusCode, response);
                            exchange.close();
                            break;

                        case "delete":
                            if (hasEmptyValue) {
                                statusCode = 400;
                                response = "";
                            } else {
                                statusCode = db.deleteExistingUser(requestBodyMap, id);
                                if (statusCode == 200) {
                                    System.out.println("The user is successfully deleted");
                                    response =  objectMapper.writeValueAsString(db.getUser(id));
                                    server.removeContext(userURI);
                                } else if (statusCode == 400) {
                                    response = "The user is not found";
                                } else {
                                    response = "Internal Server Error";
                                }
                            }
                            sendResponse(exchange, statusCode, response);
                            exchange.close();
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
            sendResponse(exchange, 200, "User Service Endpoint");
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
            exchange.close();
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

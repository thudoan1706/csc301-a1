import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;

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
                printRequestDetails(exchange);
                Map<String, Object> requestBodyMap = processRequestBody(exchange);

                boolean hasEmptyValue = requestBodyMap.values().stream()
                                    .anyMatch(value -> value instanceof String && ((String) value).isEmpty());

                System.out.println("Has Empty Value: " + hasEmptyValue);


                if (requestBodyMap != null) {
                    int id;
                    String response;
                    int statusCode;
                    Object objectID = requestBodyMap.get("id");
                    String command = (String) requestBodyMap.get("command");  
                    requestBodyMap.remove("command");
                    Map<String,String> stringMap = requestBodyMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue()));
                    ObjectMapper objectMapper = new ObjectMapper();
                    // Check if 'id' is present and of type Integer
                    if (objectID instanceof Integer) {
                        // Cast 'id' to Integer
                        id = (Integer) objectID;
                    } else {
                        id = Integer.parseInt(objectID.toString());
                    }
                    String userURI = "/user/" + id;

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

            Map<String, Object> hello = Arrays.stream(requestBody.split("&"))
                        .map(pair -> pair.split("="))
                        .collect(Collectors.toMap(
                                keyValue -> keyValue[0],
                                keyValue -> keyValue.length == 2 ? (String) keyValue[1] : "",
                                (existingValue, newValue) -> ((String) newValue).isEmpty() ? existingValue : newValue
                        ));
            System.out.println(hello);
            return hello;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
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

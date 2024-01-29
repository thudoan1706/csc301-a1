import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderService {
    public static void main(String[] args) throws IOException {
        int port = 8084;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(5));

        // Set context for /order req
        server.createContext("/order", new OrderRequestHandler());

        // Creates a default executor
        server.setExecutor(null);

        // Starts a server on the specified port
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class OrderRequestHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> requestBodyMap = getRequestBody(exchange);
                String command = requestBodyMap.get("command");

                if (command.equals("place order")) {
                    Order order = new Order(Integer.parseInt(requestBodyMap.get("user_id")),
                            Integer.parseInt(requestBodyMap.get("product_id")),
                            Integer.parseInt(requestBodyMap.get("quantity")),
                            "Success");

                    // Check if user exists
                    // if (!checkUserExists(order.getUser_id())) {
                    //     ResponseHandler.sendResponse(exchange, "Invalid Request: user doesn't exist", 400);
                    //     return;
                    // }

                    // Check if product exists
                    Map<String, String> productMap = getProductMap(order.getProduct_id());

                    // Check if quanitity is sufficient
                    int productQuantity = Integer.parseInt(productMap.get("quantity"));
                    if (productQuantity - order.getQuantity() < 0) {
                        ResponseHandler.sendResponse(exchange, "Invalid Request: not enough stock", 400);
                        return;
                    }

                    // Update remaining stock after filling order
                    productMap.put("quantity", Integer.toString(productQuantity - order.getQuantity()));
                    updateProduct(productMap);

                    // Send back a JSON object representing the filled order
                    ObjectMapper objectMapper = new ObjectMapper();
                    String response = objectMapper.writeValueAsString(order);
                    ResponseHandler.sendResponse(exchange, response, 200);

                }

            } else {
                // Send a 405 Method Not Allowed response for non-POST requests
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }

        private static void updateProduct(Map<String, String> productMap) {
            String serviceURL = "http://localhost:8081/product";
            HttpClient client = HttpClient.newHttpClient();

            String body = "{\"command\":\"update\",\"id\":" + productMap.get("id") + ",\"quantity\":"
                    + productMap.get("quantity") + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceURL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private static boolean checkUserExists(int userId) {
            String serviceURL = "http://127.0.0.1:8083/user/" + Integer.toString(userId);
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceURL))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> userMap = objectMapper.readValue(response.body(),
                        new TypeReference<HashMap<String, String>>() {
                        });
                return userMap.containsKey("id");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }

        private static Map<String, String> getProductMap(int productId) {
            String serviceURL = "http://127.0.0.1:8081/product/" + Integer.toString(productId);
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceURL))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(response.body(), new TypeReference<HashMap<String, String>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        private static Map<String, String> getRequestBody(HttpExchange exchange) throws IOException {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    requestBody.append(line);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(requestBody.toString(), new TypeReference<HashMap<String, String>>() {
                });
            }
        }
    }
}
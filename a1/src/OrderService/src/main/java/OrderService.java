import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
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
        try {
            if (args.length > 0) {
                File file = new File(args[0]);
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Map<String, String>> serviceEndpoints = objectMapper.readValue(
                        file, new TypeReference<HashMap<String, Map<String, String>>>() {
                        });

                Map<String, String> orderEndpoint = serviceEndpoints.get("OrderService");
                String port = orderEndpoint.get("port");
                String ip = orderEndpoint.get("ip");

                InetAddress localAddress = InetAddress.getByName(ip);
                HttpServer server = HttpServer.create(new InetSocketAddress(localAddress, Integer.parseInt(port)), 0);
                server.setExecutor(Executors.newFixedThreadPool(5));

                // Set context for /order req
                server.createContext("/order", new OrderRequestHandler());

                // Creates a default executor
                server.setExecutor(null);

                // Starts a server on the specified port
                server.start();
                System.out.println("Server started on port " + port);

            } else {
                System.out.println("Usage: java OrderService <config_file>");
                // Use default port if not provided
                String port = "8083";
                String ip = "127.0.0.1";

                InetAddress localAddress = InetAddress.getByName(ip);
                HttpServer server = HttpServer.create(new InetSocketAddress(localAddress, Integer.parseInt(port)), 0);
                server.setExecutor(Executors.newFixedThreadPool(5));

                // Set context for /order req
                server.createContext("/order", new OrderRequestHandler());

                // Creates a default executor
                server.setExecutor(null);

                // Starts a server on the specified port
                server.start();
                System.out.println("Server started on port " + port);
            }
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace(); // Print the stack trace or log the exception
        }
    }

    static class OrderRequestHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> requestBodyMap = getRequestBody(exchange);
                ObjectMapper objectMapper = new ObjectMapper();
                Order order;
                String response;
                
                // Check for all required fields
                if (!requestBodyMap.containsKey("command") ||
                    !requestBodyMap.containsKey("product_id") ||
                    !requestBodyMap.containsKey("user_id") ||
                    !requestBodyMap.containsKey("quantity")) {
                        order = new Order(0, 0, 0, "Invalid Request");
                        response = objectMapper.writeValueAsString(order);
                        ResponseHandler.sendResponse(exchange, response, 400);
                    }
                
                
                String command = requestBodyMap.get("command");
                if (command.equals("place order")) {
                    order = new Order(Integer.parseInt(requestBodyMap.get("user_id")),
                            Integer.parseInt(requestBodyMap.get("product_id")),
                            Integer.parseInt(requestBodyMap.get("quantity")),
                            "Success");

                    // Check if user exists
                    if (!checkUserExists(order.getUser_id())) {
                        order.setStatus("Invalid Request");
                        response = objectMapper.writeValueAsString(order);
                        ResponseHandler.sendResponse(exchange, response, 400);
                        return;
                    }

                    // Check if product exists
                    Map<String, String> productMap = getProductMap(order.getProduct_id());

                    // Check if quanitity is sufficient
                    int productQuantity = Integer.parseInt(productMap.get("quantity"));
                    if (productQuantity - order.getQuantity() < 0) {
                        order.setStatus("Exceeded quantity limit");
                        response = objectMapper.writeValueAsString(order);
                        ResponseHandler.sendResponse(exchange, response, 400);
                        return;
                    }

                    // Update remaining stock after filling order
                    productMap.put("quantity", Integer.toString(productQuantity - order.getQuantity()));
                    updateProduct(productMap);

                    // Send back a JSON object representing the filled order
                    response = objectMapper.writeValueAsString(order);
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
                client.send(request, HttpResponse.BodyHandlers.ofString());
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
                if (response.statusCode() == 200) {
                    return true;
                } else {
                    return false;
                }
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
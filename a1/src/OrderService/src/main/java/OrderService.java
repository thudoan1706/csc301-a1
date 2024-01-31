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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@code OrderService} class represents the main entry point for the Order microservice.
 * It initializes an HTTP server based on the provided configuration and handles incoming order requests.
 */
public class OrderService {
    final static int threadPoolSize = 20;
    
    /**
     * Main method to start the OrderService.
     *
     * @param args command-line arguments (expects the path to the configuration file)
     * @throws IOException if an I/O error occurs
     */
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
                ExecutorService httpThreadPool = Executors.newFixedThreadPool(threadPoolSize);
                server.setExecutor(httpThreadPool);

                // Set context for /order req
                server.createContext("/order", new OrderRequestHandler(server, httpThreadPool, serviceEndpoints));

                // Creates a default executor
                server.setExecutor(null);

                // Starts a server on the specified port
                server.start();
                System.out.println("Server started on port " + port);

            } else {
                System.out.println("Usage: java OrderService <config_file>");
            }
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace(); // Print the stack trace or log the exception
        }
    }

    /**
     * The OrderRequestHandler class handles incoming HTTP requests for the "/order" endpoint.
     * It processes POST requests to place orders and contains logic for order placement, shutdown, and restart commands.
     */
    static class OrderRequestHandler implements HttpHandler {
        HttpServer server;
        ExecutorService threadPool;
        Map<String, Map<String, String>> serviceEndpoints;

        /**
         * Constructs a new OrderRequestHandler with the specified HTTP server, thread pool, and service endpoints.
         *
         * @param server           the HTTP server
         * @param threadPool       the thread pool
         * @param serviceEndpoints the service endpoints configuration
         */
        public OrderRequestHandler(HttpServer server, ExecutorService threadPool, Map<String, Map<String, String>> serviceEndpoints) {
            this.server = server;
            this.threadPool = threadPool;
            this.serviceEndpoints = serviceEndpoints;
        }

        /**
         * Handles incoming HTTP requests for the "/order" endpoint.
         *
         * @param exchange the HttpExchange object representing the HTTP request and response
         * @throws IOException if an I/O error occurs
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> requestBodyMap = getRequestBody(exchange);
                ObjectMapper objectMapper = new ObjectMapper();
                Order order;
                String response;

                String command = requestBodyMap.get("command");
                switch (command) {
                    case "place order":
                        // Check for all required fields
                        if (!requestBodyMap.containsKey("command") ||
                                !requestBodyMap.containsKey("product_id") ||
                                !requestBodyMap.containsKey("user_id") ||
                                !requestBodyMap.containsKey("quantity")) {
                            order = new Order(0, 0, 0, "Invalid Request");
                            response = objectMapper.writeValueAsString(order);
                            ResponseHandler.sendResponse(exchange, response, 400);
                        }

                        order = new Order(Integer.parseInt(requestBodyMap.get("user_id")),
                                Integer.parseInt(requestBodyMap.get("product_id")),
                                Integer.parseInt(requestBodyMap.get("quantity")),
                                "Success");

                        // Check if user exists
                        if (!checkUserExists(order.getUser_id())) {
                            order.setStatus("Invalid Request: user does not exist" + order.getUser_id());
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

                        break;

                    case "shutdown":
                        server.stop(3);
                        threadPool.shutdownNow();
                        break;

                    case "restart":
                        exchange.close();

                    default:
                        ResponseHandler.sendResponse(exchange, "Received invalid POST command: " + command, 400);
                }

                if (command.equals("place order")) {

                }
            } else {
                // Send a 405 Method Not Allowed response for non-POST requests
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }

        private void updateProduct(Map<String, String> productMap) {
            Map<String, String> productEndpoint = serviceEndpoints.get("ProductService");
            String port = productEndpoint.get("port");
            String ip = productEndpoint.get("ip");
            String serviceURL = "http://" + ip + ":" + port + "/product";
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

        private boolean checkUserExists(int userId) {
            Map<String, String> userEndpoint = serviceEndpoints.get("UserService");
            String port = userEndpoint.get("port");
            String ip = userEndpoint.get("ip");
            String serviceURL = "http://" + ip + ":" + port + "/user/" + Integer.toString(userId);
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

        private Map<String, String> getProductMap(int productId) {
            Map<String, String> productEndpoint = serviceEndpoints.get("ProductService");
            String port = productEndpoint.get("port");
            String ip = productEndpoint.get("ip");
            String serviceURL = "http://" + ip + ":" + port + "/product/" + Integer.toString(productId);
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
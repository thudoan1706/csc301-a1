import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductService {
    public static void  main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(5));

        // Set context for /product req
        server.createContext("/product", new ProductRequestHandler());

        // Creates a default executor
        server.setExecutor(null);

        // Starts a server on the specified port
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class Product {
        // Class attributes
        int id;
        String productname;
        float price;
        int quantity;

        // Product constructor
        public Product(int id, String productname, float price, int quantity) {
            this.id = id;
            this.productname = productname;
            this.price = price;
            this.quantity = quantity;
        }
    }

    static class ProductRequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> requestBodyMap = getRequestBody(exchange);
                String command = requestBodyMap.get("command");

                String response = "";

                switch (command) {
                    case "create":
                        response = "ProductRequestHandler: received create POST req for /product";
                        break;
                    case "update":
                        response = "ProductRequestHandler: received update POST req for /product";
                        break;
                    case "delete":
                        response = "ProductRequestHandler: received delete POST req for /product";
                        break;
                    default:
                        System.err.println("Invalid post command: " + command);
                }

                sendResponse(exchange, response);

            } else if ("GET".equals(exchange.getRequestMethod())) {
                String response = "ProductRequestHandler: received GET req for /product";
                sendResponse(exchange, response);

            } else {
                // Send a 405 Method Not Allowed response for non-POST requests
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
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

        private static void sendResponse(HttpExchange exchange, String response) throws IOException {
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
}
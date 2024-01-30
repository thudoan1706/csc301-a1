import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import exceptions.DuplicateIdException;
import exceptions.InvalidPostCommand;
import exceptions.MissingRequiredFieldsException;
import exceptions.NegativePriceException;
import exceptions.NegativeQuantityException;
import exceptions.ProductNotFoundException;

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

    static class ProductRequestHandler implements HttpHandler {
        ProductDatabase productDatabase;

        public ProductRequestHandler() {
            // Create product database instance
            this.productDatabase = new ProductDatabase();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    Map<String, String> requestBodyMap = getRequestBody(exchange);
                    String command = requestBodyMap.get("command");
                    String response;

                    switch (command) {
                        case "create":
                            try {
                                productDatabase.createProduct(requestBodyMap);
                                response = "ProductService: Received create POST req for /product";
                                ResponseHandler.sendResponse(exchange, response, 200);
                                return;
                            } catch(MissingRequiredFieldsException e) {
                                ResponseHandler.sendResponse(exchange, e.getMessage(), 400);
                                return;
                            } catch(NegativePriceException e) {
                                ResponseHandler.sendResponse(exchange, e.getMessage(), 400);
                                return;
                            } catch(NegativeQuantityException e) {
                                ResponseHandler.sendResponse(exchange, e.getMessage(), 400);
                                return;
                            } catch(DuplicateIdException e) {
                                ResponseHandler.sendResponse(exchange, e.getMessage(), 409);
                                return;
                            }

                        case "update":
                            response = "ProductRequestHandler: received update POST req for /product";
                            productDatabase.updateProduct(requestBodyMap);
                            ResponseHandler.sendResponse(exchange, response, 200);
                            break;
                        case "delete":
                            response = "ProductRequestHandler: received delete POST req for /product";
                            productDatabase.deleteProduct(requestBodyMap);
                            ResponseHandler.sendResponse(exchange, response, 200);
                            break;
                        default:
                            throw new InvalidPostCommand("Received invalid POST command: " + command);
                    }
                } else if ("GET".equals(exchange.getRequestMethod())) {
                    String requestURI = exchange.getRequestURI().toString();
                    try {
                        String response = productDatabase.getProduct(requestURI);
                        ResponseHandler.sendResponse(exchange, response, 200);
                    } catch (ProductNotFoundException e) {
                        System.err.println(e.getMessage());
                        String response = e.getMessage();
                        ResponseHandler.sendResponse(exchange, response, 400);
                    }

                } else {
                    // Send a 405 Method Not Allowed response for non-POST requests
                    exchange.sendResponseHeaders(405, 0);
                    exchange.close();
                }
            } catch (InvalidPostCommand e) {
                System.err.println(e.getMessage());
                String response = "ProductRequestHandler: invalid post command received";
                ResponseHandler.sendResponse(exchange, response, 400);
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
    }
}
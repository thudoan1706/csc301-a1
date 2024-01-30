import com.sun.net.httpserver.HttpServer;
import java.net.InetAddress;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserServer {

    private final HttpServer server;
    private final ExecutorService httpThreadPool;
    public String endpoint;
    private final int threadPoolSize = 20;
    public InetAddress localAddress;

    public UserServer(String configPort, String configIP) {
        try {
            httpThreadPool = Executors.newFixedThreadPool(threadPoolSize);
            localAddress = InetAddress.getByName(configIP);
            server = HttpServer.create(new InetSocketAddress(localAddress, Integer.parseInt(configPort)), 0);
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating UserServer", e);
        }
    }

    public void startServer() {
        // Set the executor for the server
        server.setExecutor(httpThreadPool);

        // Set up context for /user POST request
        server.createContext("/user", new UserRequestHandler(server, httpThreadPool));

        // Start the server
        server.start();

        System.out.println("User Server started on port " + server.getAddress().getPort());
    }

    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                File file = new File(args[0]);
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Map<String, String>> serviceEndpoint = objectMapper.readValue(
                        file, new TypeReference<HashMap<String, String>>() {}
                );
                Map<String, String> userEndpoint = serviceEndpoint.get("UserService");
                String port = userEndpoint.get("port");
                String ip = userEndpoint.get("ip");
                UserServer us = new UserServer(port, ip); // Adjust the thread pool size as needed
                us.startServer();
            } else {
                System.out.println("Usage: java UserServer <config_file>");
                // Use default port if not provided
                UserServer us = new UserServer("8081", "127.0.0.1"); // Example default values
                us.startServer();
            }
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace();  // Print the stack trace or log the exception
        }
    }
}

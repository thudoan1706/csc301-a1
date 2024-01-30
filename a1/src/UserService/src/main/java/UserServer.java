import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class UserServer {

    public static void main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        ExecutorService httpThreadPool = Executors.newFixedThreadPool(20);
        // server.setExecutor(null); // creates a default executor
        server.setExecutor(httpThreadPool); // Adjust the pool size as needed
        // Set up context for /user POST request
        server.createContext("/user", new UserRequestHandler(server, httpThreadPool));
        
        server.start();

        System.out.println("User Server started on port " + port);
    }

}


import java.util.*;
import com.fasterxml.jackson.databind.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class UserDatabaseManager {

    private static final String JSON_FILE_PATH = "./data/user.json";
    private static List<User> existingUsers;

    public UserDatabaseManager() {
        existingUsers = getAllUsers();
    }

    public Map<String, String> getUser(Integer id) {
        Map<String, String> responseBodyMap = new HashMap<>();
        User user = existingUsers.stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElse(null);

        if (user != null) {
            responseBodyMap.put("id", String.valueOf(user.getId()));
            responseBodyMap.put("username", user.getUsername());
            responseBodyMap.put("email", user.getEmail());
            responseBodyMap.put("password", user.getPassword());
        } 
        return responseBodyMap;
    }


    public int updateExistingUser(Map<String, String> requestBodyMap, int id) {
        try {
            boolean isPresent = isUserPresent(id);
            if (isPresent) {
                System.out.println("Hello\n");
                String username = (String) requestBodyMap.get("username");
                String email = (String) requestBodyMap.get("email");
                String password = (String) requestBodyMap.get("password");
                System.out.println("Hello\n");

                existingUsers = existingUsers.stream().map(eu -> 
                        {
                            if (eu.getId() == id) {
                                // Use Objects.requireNonNullElse only if the replacement value is not an empty string
                                eu.setEmail(email.isEmpty() ? eu.getEmail() : Objects.requireNonNullElse(email, eu.getEmail()));
                                eu.setUsername(username.isEmpty() ? eu.getUsername() : Objects.requireNonNullElse(username, eu.getUsername()));
                                eu.setPassword(password.isEmpty() ? eu.getPassword() : Objects.requireNonNullElse(password, eu.getPassword()));
                            }
                            return eu;
                        }).collect(Collectors.toList());
                System.out.println("Hello\n");
                storeUsersToJson();
                System.out.println("Hello\n");
                return 200;
            }
            return 404;
        } catch (IOException e) {
            e.printStackTrace();
            return 500;
        }
    }
    
    public int deleteExistingUser(Map<String, String> requestBodyMap, int id) throws IOException {
        try {
            boolean isPresent = isUserPresent(id);
            if (isPresent) {
                String username = (String) requestBodyMap.getOrDefault("username", null);
                String email = (String) requestBodyMap.getOrDefault("email", null);
                String password = (String) requestBodyMap.getOrDefault("password", null);

                existingUsers.removeIf(existingUser ->
                        existingUser.getId() == id &&
                        existingUser.getEmail().equals(email) &&
                        existingUser.getUsername().equals(username) &&
                        existingUser.getPassword().equals(password));
                storeUsersToJson();
                return 200;
            }
            return 400;
        } catch (IOException e) {
            // handle IO exception, such as file not found or permission issues
            e.printStackTrace();
            return 500;
        }
    }
    
    public int createNewUser(Map<String, String> requestBodyMap, int id) throws IOException {
        try {
            if (!isUserPresent(id)) {
                User newUser = new User(
                        id,
                        (String) requestBodyMap.get("username"),
                        (String) requestBodyMap.get("email"),
                        (String) requestBodyMap.get("password")
                );
                existingUsers.add(newUser);
                storeUsersToJson();

                return 200;
            } else {
                return 409;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 500;
        }
    }
    

    private static boolean storeUsersToJson() throws IOException {
        try {
            // Write the updated list to the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
            // Convert the list of users to a pretty-printed JSON string
            String prettyJson = objectWriter.writeValueAsString(existingUsers);

            // Write the JSON string to the file
            Files.write(Paths.get(JSON_FILE_PATH), prettyJson.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return false;
        }
    }

    // Function to check if a user with a specific ID is present
    public boolean isUserPresent(int id) {
        for (User existingUser : existingUsers) {
            if (existingUser.getId() == id) {
                return true; // User with the same ID is present
            }
        }
        return false; // User is not present in the list
    }

    public List<User> getAllUsers() {
        try {
            // Read the existing list of users from the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(JSON_FILE_PATH);

            if (file.exists()) {
                return objectMapper.readValue(file, new TypeReference<List<User>>() {});
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return new ArrayList<>();
        }
    }
}

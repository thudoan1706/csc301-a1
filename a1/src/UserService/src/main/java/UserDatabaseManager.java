import java.util.*;
import com.fasterxml.jackson.databind.*;
import java.util.stream.Collectors;
import java.io.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class UserDatabaseManager {

    private static final String JSON_FILE_PATH = "./user.json";
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
        }
        
        return responseBodyMap;
    }


    public void updateExistingUser(Integer id, Map<String, String> requestBodyMap) {
        boolean isPresent = isUserPresent(id);
    
        if (isPresent) {
            String username = requestBodyMap.getOrDefault("username", null);
            String email = requestBodyMap.getOrDefault("email", null);
            String password = requestBodyMap.getOrDefault("password", null);
    
            existingUsers = existingUsers.stream()
                    .filter(eu -> eu.getId() == id)
                    .map(eu -> {
                        eu.setEmail(Objects.requireNonNullElse(email, eu.getEmail()));
                        eu.setUsername(Objects.requireNonNullElse(username, eu.getUsername()));
                        eu.setPassword(Objects.requireNonNullElse(password, eu.getPassword()));
                        return eu;
                    }).collect(Collectors.toList());
            storeUsersToJson();
        }
    }
    
    public void deleteExistingUser(Integer id, Map<String, String> requestBodyMap) {
        String username = requestBodyMap.getOrDefault("username", null);
        String email = requestBodyMap.getOrDefault("email", null);
        String password = requestBodyMap.getOrDefault("password", null);
    
        existingUsers.removeIf(existingUser ->
                existingUser.getId() == id &&
                existingUser.getEmail().equals(email) &&
                existingUser.getUsername().equals(username) &&
                existingUser.getPassword().equals(password));
        storeUsersToJson();
    }
    
    public void createNewUser(Integer id, Map<String, String> requestBodyMap) {
        if (!isUserPresent(id)) {
            User newUser = new User(
                    id,
                    requestBodyMap.get("username"),
                    requestBodyMap.get("email"),
                    requestBodyMap.get("password")
            );
            existingUsers.add(newUser);
            storeUsersToJson();
        }
    }
    

    private static boolean storeUsersToJson() {
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
    private static boolean isUserPresent(int id) {
        for (User existingUser : existingUsers) {
            if (existingUser.getId() == id) {
                return true; // User with the same ID is present
            }
        }
        return false; // User is not present in the list
    }

    private List<User> getAllUsers() {
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

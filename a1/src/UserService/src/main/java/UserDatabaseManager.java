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
        }
        
        return responseBodyMap;
    }


    public boolean updateExistingUser(Map<String, Object> requestBodyMap) {
        try {
            int id;         
            Object idObject = requestBodyMap.get("id");
            if (idObject instanceof Integer) {
                id = (Integer) idObject;
            } else {
                id = Integer.parseInt(idObject.toString());
            }
            boolean isPresent = isUserPresent(id);
            if (isPresent) {
                String username = (String) requestBodyMap.getOrDefault("username", null);
                String email = (String) requestBodyMap.getOrDefault("email", null);
                String password = (String) requestBodyMap.getOrDefault("password", null);
        
                existingUsers = existingUsers.stream()
                            .map(eu -> {
                                if (eu.getId() == id) {
                                    eu.setEmail(Objects.requireNonNullElse(email, eu.getEmail()));
                                    eu.setUsername(Objects.requireNonNullElse(username, eu.getUsername()));
                                    eu.setPassword(Objects.requireNonNullElse(password, eu.getPassword()));
                                }
                                return eu;
                            })
                            .collect(Collectors.toList());
                storeUsersToJson();
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteExistingUser(Map<String, Object> requestBodyMap) throws IOException {
        try {
            int id;
            String username = (String) requestBodyMap.getOrDefault("username", null);
            String email = (String) requestBodyMap.getOrDefault("email", null);
            String password = (String) requestBodyMap.getOrDefault("password", null);

            Object idObject = requestBodyMap.get("id");
            if (idObject instanceof Integer) {
                id = (Integer) idObject;
            } else {
                id = Integer.parseInt(idObject.toString());
            }
            boolean isDeleted = existingUsers.removeIf(existingUser ->
                    existingUser.getId() == id &&
                    existingUser.getEmail().equals(email) &&
                    existingUser.getUsername().equals(username) &&
                    existingUser.getPassword().equals(password));
            storeUsersToJson();
            return isDeleted;
        } catch (IOException e) {
            // handle IO exception, such as file not found or permission issues
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean createNewUser(Map<String, Object> requestBodyMap) throws IOException {
        try {
            int id;
            Object idObject = requestBodyMap.get("id");
            if (idObject instanceof Integer) {
                id = (Integer) idObject;
            } else {
                id = Integer.parseInt(idObject.toString());
            }

            if (!isUserPresent(id)) {
                User newUser = new User(
                        id,
                        (String) requestBodyMap.get("username"),
                        (String) requestBodyMap.get("email"),
                        (String) requestBodyMap.get("password")
                );
                existingUsers.add(newUser);
                storeUsersToJson();

                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

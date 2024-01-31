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

/**
 * The {@code UserDatabaseManager} class manages user data stored in a JSON file.
 * It provides functionality to interact with user information such as retrieval,
 * backup, and other operations.
 */
public class UserDatabaseManager {

    private static final String JSON_FILE_PATH = "./data/user.json";
    private static final String BACKUP_FILE_PATH = "./data/backup.json";
    private static List<User> existingUsers;

    public UserDatabaseManager() {
        existingUsers = getAllUsers();
    }

    
    /** 
     * Constructs a new {@code UserDatabaseManager} and initializes the list of existing users
     * by loading data from the main JSON file.
     *
     * @param id User's identifier
     * @return  Map<String, String>
     */
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


    
    /** 
     * Updates an existing user with the specified ID using the provided request body parameters.
     *
     * @param requestBodyMap A map containing request body parameters for updating the user.
     * @param id              The unique identifier of the user to be updated.
     * @return The status code indicating the success or failure of the update operation.
     * @throws IOException If an error occurs while updating the user in the database.
     */
    public int updateExistingUser(Map<String, String> requestBodyMap, int id) throws IOException {
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


    /**
     * Deletes an existing user with the specified ID using the provided request body parameters.
     *
     * @param requestBodyMap A map containing request body parameters for additional information
     *                       or validation during the delete operation (may be null if not needed).
     * @param id              The unique identifier of the user to be deleted.
     * @return The status code indicating the success or failure of the delete operation.
     * @throws IOException If an error occurs while deleting the user from the database.
     */
    public int deleteExistingUser(Map<String, String> requestBodyMap, int id) throws IOException {
        try {
            System.out.println("Hello!");
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
                System.out.println("Hello!");
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
    
    /**
     * Creates a new user with the specified ID using the provided request body parameters.
     *
     * @param requestBodyMap A map containing request body parameters for initializing the new user.
     * @param id              The unique identifier for the new user.
     * @return The status code indicating the success or failure of the create operation.
     * @throws IOException If an error occurs while creating the new user in the database.
     */
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
    /**
     * Checks if a user with the specified ID is present in the user database.
     *
     * @param id The unique identifier of the user to check for presence.
     * @return {@code true} if the user is present, {@code false} otherwise.
     */
    public boolean isUserPresent(int id) {
        for (User existingUser : existingUsers) {
            if (existingUser.getId() == id) {
                return true; // User with the same ID is present
            }
        }
        return false; // User is not present in the list
    }

    /**
     * Retrieves a list of all users from the user database.
     *
     * @return A list of User objects representing all users in the database.
     */
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

    /**
     * Persists the current state of user data for backup purposes.
     *
     * @return {@code true} if the backup operation is successful, {@code false} otherwise.
     * @throws IOException If an error occurs while persisting data for backup.
     */
    public boolean persistDataForBackUp() throws IOException {
        try {
            // Write the updated list to the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
            // Convert the list of users to a pretty-printed JSON string
            String prettyJson = objectWriter.writeValueAsString(existingUsers);

            // Write the JSON string to the file
            Files.write(Paths.get(BACKUP_FILE_PATH), prettyJson.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return false;
        }
    }

    /**
     * Removes the original stored data file from the system.
     * This operation is typically performed after a successful backup to avoid redundancy.
     */
    public void removeOriginalStoredDataFile() {
        // Write the updated list to the JSON file
        File file = new File(JSON_FILE_PATH);

        // Use delete method to delete the file
        if (file.delete()) {
            System.out.println("File deleted successfully.");
        } else {
            System.out.println("Failed to delete the file.");
        }
    }


    /**
     * Removes the backup stored data file from the system.
     * This operation is typically performed after ensuring the integrity of the backup.
     */
    public void removeOBackUpStoredDataFile() {
        // Write the updated list to the JSON file
        File file = new File(BACKUP_FILE_PATH);

        // Use delete method to delete the file
        if (file.delete()) {
            System.out.println("File deleted successfully.");
        } else {
            System.out.println("Failed to delete the file.");
        }
    }

    /**
     * Restores the backed-up data to the original stored data file.
     *
     * @throws IOException If an error occurs during the data restoration process.
     */
    public void restoreDataToOriginalFile() throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File originalFile = new File(JSON_FILE_PATH);
            File backupFile = new File(BACKUP_FILE_PATH);

            if (backupFile.exists()) {
                // Read data from the backup file
                List<User> restoredUsers = objectMapper.readValue(backupFile, new TypeReference<List<User>>() {});

                // Perform any additional processing or validation if needed

                // Write the restored data back to the original file
                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(restoredUsers);
                Files.write(Paths.get(JSON_FILE_PATH), prettyJson.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                // Handle the case when the backup file doesn't exist
                System.out.println("Backup file does not exist.");
            }

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
}

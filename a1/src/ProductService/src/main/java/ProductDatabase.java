import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import exceptions.DuplicateIdException;
import exceptions.MissingRequiredFieldsException;
import exceptions.NegativePriceException;
import exceptions.NegativeQuantityException;
import exceptions.ProductNotFoundException;
import exceptions.ProductSerializationException;

/**
 * The {@code ProductDatabase} class manages the storage and manipulation of product data.
 * It includes methods for creating, retrieving, updating, and deleting products,
 * as well as persisting data, creating backups, and restoring data.
 * 
 * @author Juan Gutierrez
 */
public class ProductDatabase {

    private static final String JSON_FILE_PATH = "./data/product.json";
    private static final String BACKUP_FILE_PATH = "./data/backup.json";
    private static List<Product> products;

    /**
     * Default constructor for the {@code ProductDatabase} class.
     * Initializes the list of products by retrieving data from the JSON file.
     */
    public ProductDatabase() {
        products = retrieveDatabase();
    }

    /**
     * Retrieves the details of a product based on the provided request URI.
     *
     * @param requestURI The request URI containing the product ID.
     * @return A JSON string representing the product details.
     * @throws ProductNotFoundException If the product with the specified ID is not found.
     */
    public String getProduct(String requestURI) {
        int lastIndex = requestURI.lastIndexOf("/");
        String idString = requestURI.substring(lastIndex + 1);
        int id = Integer.parseInt(idString);

        for (Product product : products) {
            if (product.getId() == id) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.writeValueAsString(product);
                } catch (JsonProcessingException e) {
                    throw new ProductSerializationException("An error occurred while serializing product to JSON", e);
                }
            }
        }
        throw new ProductNotFoundException("Cannot find product with id: " + id);
    }

    
<<<<<<< HEAD
    /**
     * Creates a new product based on the provided request body map.
     *
     * @param requestBodyMap The map containing product details.
     * @return The created product.
     * @throws MissingRequiredFieldsException If one or more required fields are missing.
     * @throws NegativePriceException         If the product price is negative.
     * @throws NegativeQuantityException      If the product quantity is negative.
     * @throws DuplicateIdException           If a product with the provided ID already exists.
=======
    /** 
     * @param requestBodyMap Payload for product request
     * @return Product
>>>>>>> f48dc7a07eb59430cf8b4564b10b71ef5d9e6b0b
     */
    public Product createProduct(Map<String, String> requestBodyMap) {
        if (!requestBodyMap.containsKey("id") ||
                !requestBodyMap.containsKey("name") ||
                !requestBodyMap.containsKey("description") ||
                !requestBodyMap.containsKey("price") ||
                !requestBodyMap.containsKey("quantity")) {
            throw new MissingRequiredFieldsException("Missing one or more required fields");
        }

        int id = Integer.parseInt(requestBodyMap.get("id"));
        String name = requestBodyMap.get("name");
        String description = requestBodyMap.get("description");
        float price = Float.parseFloat(requestBodyMap.get("price"));
        int quantity = Integer.parseInt(requestBodyMap.get("quantity"));

        if (price < 0) {
            throw new NegativePriceException("Product price cannot be negative");
        }
        if (quantity < 0) {
            throw new NegativeQuantityException("Product quantity cannot be negative");
        }

        if (!productExists(id)) {
            Product product = new Product(id, name, description, price, quantity);
            products.add(product);
            updateDatabase();
            return product;
        } else {
            throw new DuplicateIdException("A product with the provided ID already exists");
        }
    }

    /**
     * Deletes a product based on the provided request body map.
     *
     * @param requestBodyMap The map containing product details.
     * @throws ProductNotFoundException If no matching product is found to delete.
     */
    public void deleteProduct(Map<String, String> requestBodyMap) {
        int id = Integer.parseInt(requestBodyMap.get("id"));
        String name = requestBodyMap.get("name");
        String description = requestBodyMap.get("description");
        float price = Float.parseFloat(requestBodyMap.get("price"));
        int quantity = Integer.parseInt(requestBodyMap.get("quantity"));

        boolean productRemoved = products.removeIf(currProduct -> currProduct.getId() == id &&
                currProduct.getName().equals(name) &&
                currProduct.getDescription().equals(description) &&
                currProduct.getPrice() == price &&
                currProduct.getQuantity() == quantity);

        if (productRemoved) {
            updateDatabase();
        } else {
            throw new ProductNotFoundException("No matching product was found to delete");
        }
    }

    /**
     * Updates a product based on the provided request body map.
     *
     * @param requestBodyMap The map containing product details.
     * @return The updated product.
     * @throws ProductNotFoundException If the product with the specified ID is not found.
     */
    public Product updateProduct(Map<String, String> requestBodyMap) {
        int id = Integer.parseInt(requestBodyMap.get("id"));

        for (Product product : products) {
            if (product.getId() == id) {
                if (requestBodyMap.containsKey("name")) {
                    String name = requestBodyMap.get("name");
                    product.setName(name);
                }
                if (requestBodyMap.containsKey("description")) {
                    String description = requestBodyMap.get("description");
                    product.setDescription(description);
                }
                if (requestBodyMap.containsKey("price")) {
                    float price = Float.parseFloat(requestBodyMap.get("price"));
                    product.setPrice(price);
                }
                if (requestBodyMap.containsKey("quantity")) {
                    int quantity = Integer.parseInt(requestBodyMap.get("quantity"));
                    product.setQuantity(quantity);
                }
                updateDatabase();

                return product;
            }
        }

        throw new ProductNotFoundException("Cannot find product with id: " + id);
    }

    private List<Product> retrieveDatabase() {
        File file = new File(JSON_FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(file, new TypeReference<List<Product>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void updateDatabase() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(JSON_FILE_PATH), products);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean productExists(int id) {
        for (Product product : products) {
            if (product.getId() == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Persists the current list of products to the original JSON file.
     *
     * @return {@code true} if the operation is successful, {@code false} otherwise.
     * @throws IOException If an I/O error occurs during the operation.
     */
    public boolean persistDataForBackUp() throws IOException {
        try {
            // Write the updated list to the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
            // Convert the list of users to a pretty-printed JSON string
            String prettyJson = objectWriter.writeValueAsString(products);

            // Write the JSON string to the file
            Files.write(Paths.get(BACKUP_FILE_PATH), prettyJson.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return false;
        }
    }

    /**
     * Removes the original stored data file.
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
     * Removes the backup stored data file.
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
     * Restores data from the backup file to the original file.
     *
     * @throws IOException If an I/O error occurs during the operation.
     */
    public void restoreDataToOriginalFile() throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File backupFile = new File(BACKUP_FILE_PATH);

            if (backupFile.exists()) {
                // Read data from the backup file
                List<Product> restoredProducts = objectMapper.readValue(backupFile, new TypeReference<List<Product>>() {});

                // Perform any additional processing or validation if needed

                // Write the restored data back to the original file
                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(restoredProducts);
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProductDatabase {

    private static final String pathname = "./productDB.json";
    private static List<Product> products;

    public ProductDatabase() {
        products = retrieveDatabase();
    }

    private List<Product> retrieveDatabase() {
        File file = new File(pathname);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Product> productList = objectMapper.readValue(file, new TypeReference<List<Product>>() {});
            System.out.println("Product Database successfully retrieved data");
            return productList;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void updateDatabase() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(pathname), products);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error updating productDB.json file");
        }
    }

    public void saveProduct(Map<String, String> requestBodyMap) {
        int id = Integer.parseInt(requestBodyMap.get("id"));
        String productname = requestBodyMap.get("productname");
        float price = Float.parseFloat(requestBodyMap.get("price"));
        int quantity = Integer.parseInt(requestBodyMap.get("quantity"));
        Product product = new Product(id, productname, price, quantity);

        products.add(product);
        updateDatabase();

        System.out.println("Product Database added: " + product.getProductname());
    }

    public void deleteProduct(Map<String, String> requestBodyMap) {
        int id = Integer.parseInt(requestBodyMap.get("id"));
        String productname = requestBodyMap.get("productname");
        float price = Float.parseFloat(requestBodyMap.get("price"));
        int quantity = Integer.parseInt(requestBodyMap.get("quantity"));

        products.removeIf(currProduct -> currProduct.getId() == id &&
                currProduct.getProductname().equals(productname) &&
                currProduct.getPrice() == price &&
                currProduct.getQuantity() == quantity);
        updateDatabase();

        System.out.println("Product Database deleted: " + productname);
    }

    public void updateProduct(Map<String, String> requestBodyMap) {
        int id = Integer.parseInt(requestBodyMap.get("id"));

        for (Product product: products) {
            if (product.getId() == id) {
                if (requestBodyMap.containsKey("productname")) {
                    String productname = requestBodyMap.get("productname");
                    product.setProductname(productname);
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
            }
        }

        System.out.println("Product Database updated: " + requestBodyMap.get("productname"));
    }
}

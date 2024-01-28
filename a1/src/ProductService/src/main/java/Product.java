public class Product {
    // Class attributes
    private int id;
    private String productname;
    private String description;
    private float price;
    private int quantity;

    // Product constructor
    public Product() {

    }
    public Product(int id, String productname, String description, float price, int quantity) {
        this.id = id;
        this.productname = productname;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public int getId() {
        return id;
    }

    public float getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getProductname() {
        return productname;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

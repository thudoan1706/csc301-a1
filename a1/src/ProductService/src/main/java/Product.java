/**
 * The {@code Product} class represents a product with its attributes such as
 * ID, name, description, price, and quantity.
 * 
 * @author Juan Gutierrez
 */
public class Product {
    // Class attributes
    private int id;
    private String name;
    private String description;
    private float price;
    private int quantity;

    /**
     * Default constructor for the {@code Product} class.
     */
    public Product() {

    }

    /**
     * Parameterized constructor for creating a {@code Product} instance with
     * specified attributes.
     *
     * @param id          The unique identifier of the product.
     * @param name        The name of the product.
     * @param description The description of the product.
     * @param price       The price of the product.
     * @param quantity    The quantity of the product.
     */
    public Product(int id, String name, String description, float price, int quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * Retrieves the ID of the product.
     *
     * @return The unique identifier of the product.
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves the price of the product.
     *
     * @return The price of the product.
     */
    public float getPrice() {
        return price;
    }

    /**
     * Retrieves the quantity of the product.
     *
     * @return The quantity of the product.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Retrieves the name of the product.
     *
     * @return The name of the product.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the description of the product.
     *
     * @return The description of the product.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the ID of the product.
     *
     * @param id The unique identifier to set for the product.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the price of the product.
     *
     * @param price The price to set for the product.
     */
    public void setPrice(float price) {
        this.price = price;
    }

    /**
     * Sets the name of the product.
     *
     * @param name The name to set for the product.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the description of the product.
     *
     * @param description The description to set for the product.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the quantity of the product.
     *
     * @param quantity The quantity to set for the product.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

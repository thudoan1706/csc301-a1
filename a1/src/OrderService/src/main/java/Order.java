/**
 * The {@code Order} class represents an order with attributes such as user ID, product ID, quantity, and status.
 * It provides a constructor to initialize the order attributes and getters and setters for access and modification.
 */

public class Order {
    // Class attributes
    private int user_id;
    private int product_id;
    private int quantity;
    private String status;

    /**
     * Constructs a new Order with the specified user ID, product ID, quantity, and status.
     *
     * @param user_id    the user ID associated with the order
     * @param product_id the product ID associated with the order
     * @param quantity   the quantity of the product in the order
     * @param status     the status of the order
     */
    public Order(int user_id, int product_id, int quantity, String status) {
        this.user_id = user_id;
        this.product_id = product_id;
        this.quantity = quantity;
        this.status = status;
    }

    
    /**
     * Gets the user ID associated with the order.
     *
     * @return the user ID
     */
    public int getUser_id() {
        return user_id;
    }

    
    /**
     * Gets the product ID associated with the order.
     *
     * @return the product ID
     */
    public int getProduct_id() {
        return product_id;
    }

    /**
     * Gets the quantity of the product in the order.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the status of the order.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the user ID associated with the order.
     *
     * @param user_id the user ID to set
     */
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    /**
     * Sets the product ID associated with the order.
     *
     * @param product_id the product ID to set
     */
    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    /**
     * Sets the quantity of the product in the order.
     *
     * @param quantity the quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Sets the status of the order.
     *
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
}

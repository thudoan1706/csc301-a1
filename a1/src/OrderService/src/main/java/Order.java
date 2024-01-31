public class Order {
    // Class attributes
    private int user_id;
    private int product_id;
    private int quantity;
    private String status;

    // Product constructor
    public Order(int user_id, int product_id, int quantity, String status) {
        this.user_id = user_id;
        this.product_id = product_id;
        this.quantity = quantity;
        this.status = status;
    }

    
    /** 
     * @return int
     */
    // Getters

    public int getUser_id() {
        return user_id;
    }

    
    /** 
     * @return int
     */
    public int getProduct_id() {
        return product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

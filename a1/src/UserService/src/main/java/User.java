/**
 * The {@code User} class represents a basic entity for user information.
 * It encapsulates information about an individual user, providing
 * getter and setter methods for accessing and updating user details.
 *
 * <p>Important: This class currently handles passwords as plain text strings.
 * 
 * <p>Example usage:
 * <pre>{@code
 * User user = new User();
 * user.setFirstName("John");
 * user.setLastName("Doe");
 * user.setEmail("john.doe@example.com");
 * user.setPassword("securePassword123"); // NOTE: Password handling should be improved
 *
 * System.out.println("User Details: " + user.getFirstName() + " " +
 *                     user.getLastName() + ", Email: " + user.getEmail());
 * }</pre>
 * @author Anh Thu Doan
 */

public class User { 
    private int id;
    private String username;
    private String email;
    private String password;

    /**
     * Default constructor for User for instantiation without specific attributes.
     */
    public User() {
    }


    /**
     * Constructor for User with specified attributes.
     *
     * @param id       The unique identifier for the user.
     * @param username The username of the user.
     * @param email    The email address of the user.
     * @param password The password of the user.
     */
    public User(Integer id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    
    /**
     * Gets the unique identifier of the user.
     *
     * @return The unique identifier (ID) of the user as an integer.
     */
    public int getId() {
        return id;
    }
    
    
    /**
     * Gets the username of the user.
     *
     * @return The username as a String.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the email address of the user.
     *
     * @return The email address as a String.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the password of the user.
     *
     * @return The password as a String.
     */
    public String getPassword() {
        return password;
    }

 
    /**
     * Sets the identifier of the user.
     *
     * @param id The new identifier in Integer representation.
     */
    public void setId(Integer id) {
        this.id = id;
    }
    

    /**
     * Sets the identifier of the user.
     *
     * @param id The new identifier in String representation.
     *           
     */
    public void setId(String id) {
        try {
            this.id = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the username of the user.
     *
     * @param username Username of the user
     *           
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the email of the user.
     *
     * @param email Email of the user
     *           
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the password of the user.
     *
     * @param password : Password of the user
     *           
     */
    public void setPassword(String password) {
        this.password = password;
    }
}

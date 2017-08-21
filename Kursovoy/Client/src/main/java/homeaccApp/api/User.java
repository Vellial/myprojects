package homeaccApp.api;

/**
 * User model.
 */
public class User {

    private int uid;
    private String username;
    private String email;
    private int categoryCosts;
    private String categoryName;
    //todo add categoryIncomes

    public int getUid() {
        return uid;
    }

    public void setUid(int newUid) {
        uid = newUid;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String newCat) {
        categoryName = newCat;
    }

    public int getCategoryCosts() {
        return categoryCosts;
    }

    public void setCategoryCosts(int newCat) {
        categoryCosts = newCat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        email = newEmail;
    }

    public void setUsername(String newUsername) {
        username = newUsername;
    }

    public String getUsername() {
        return username;
    }
}

package homeaccApp.user.session;

/**
 * Object session
 */
public class Session {
    private String username;
    private String password;
    private boolean keepLogged;
    private int userId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isKeepLogged() {
        return keepLogged;
    }

    public void setKeepLogged(boolean keepLogged) {
        this.keepLogged = keepLogged;
    }

    public int authUser() {
        return 0;
    }
}

package homeaccApp.mainwindow.menuDialogs.miSettings;

import homeaccApp.categories.Categories;
import homeaccApp.mainwindow.bills.Bills;
import homeaccApp.measures.Measures;
import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Settings
 */
public class miSettings {
    // Sync
    private BooleanProperty isRemoteUser;
    private StringProperty remoteUsername;
    private StringProperty remotePassword;
    private StringProperty remoteEmail;

    public miSettings() {
        this(false, null, null, null);
    }

    public miSettings(boolean isRemote, String username, String password, String email) {
        this.isRemoteUser = new SimpleBooleanProperty(isRemote);
        this.remoteUsername = new SimpleStringProperty(username);
        this.remotePassword = new SimpleStringProperty(password);
        this.remoteEmail = new SimpleStringProperty(email);
    }

    public boolean getIsRemoteUser() {
        return isRemoteUser.get();
    }

    public BooleanProperty isRemoteUserProperty() {
        return isRemoteUser;
    }

    public void setIsRemoteUser(boolean isRemoteUser) {
        this.isRemoteUser.set(isRemoteUser);
    }

    public String getRemoteUsername() {
        return remoteUsername.get();
    }

    public StringProperty remoteUsernameProperty() {
        return remoteUsername;
    }

    public void setRemoteUsername(String remoteUsername) {
        this.remoteUsername.set(remoteUsername);
    }

    public String getRemotePassword() {
        return remotePassword.get();
    }

    public StringProperty remotePasswordProperty() {
        return remotePassword;
    }

    public void setRemotePassword(String remotePassword) {
        this.remotePassword.set(remotePassword);
    }

    public String getRemoteEmail() {
        return remoteEmail.get();
    }

    public StringProperty remoteEmailProperty() {
        return remoteEmail;
    }

    public void setRemoteEmail(String remoteEmail) {
        this.remoteEmail.set(remoteEmail);
    }
}

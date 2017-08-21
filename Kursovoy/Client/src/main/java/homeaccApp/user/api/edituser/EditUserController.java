package homeaccApp.user.api.edituser;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import homeaccApp.Main;
import homeaccApp.user.api.createuser.User;

import java.sql.SQLException;

import static homeaccApp.api.DAO.UserDAO.confirmUsername;
import static homeaccApp.api.DAO.UserDAO.createUser;
import static homeaccApp.api.DAO.UserDAO.md5Custom;

/**
 * Edit homeaccApp.user.
 */
public class EditUserController {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private TextField emailaddr;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnCancel;

    // Link on application
    private Main main;
    private Stage dialogStage;
    private User user;

    /**
     * Constructor.
     */
    public EditUserController() {
        main = new Main();
    }

    /**
     * Set dialog stage.
     * @param dialogStage window.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Controller initialization.
     */
    @FXML
    private void initialize() {

    }

    /**
     * Listener for Edit Button
     */
    @FXML
    private void handleEdit() {
        if (isInputValid()) {
            User user = new User();
            String md5pass;

            user.setUsername(username.getText());
            user.setPassword(password.getText().toCharArray());
            user.setEmail(emailaddr.getText());

            try {
                if (!confirmUsername(user.getUsername())) {
                    String passString = new String(user.getPassword());
                    md5pass = md5Custom(passString);
                    try {
                        createUser(user.getUsername(), md5pass, user.getEmail());
                        main.showLoginDialog(); // TODO: 03.06.16  check it
                        dialogStage.close();
                    } catch (SQLException | ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(dialogStage);
                    alert.setTitle("Имя пользователя не верно");
                    alert.setHeaderText("Пожалуйста, проверьте следующие поля.");
                    alert.setContentText("Это имя пользователя уже используется. Выберите другое имя или восстановите пароль.");

                    alert.showAndWait();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * Listener for Cancel Button.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Checking fields.
     * @return true if no errors.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (username.getText() == null || username.getText().length() == 0) {
            errorMessage += "Поле \"Имя пользователя\" не заполнено!\n";
        }
        if (password.getText() == null || password.getText().length() == 0) {
            errorMessage += "Поле \"Пароль\" не заполнено!\n";
        }
        if (confirmPassword.getText() == null || confirmPassword.getText().length() == 0 || !confirmPassword.getText().equals(password.getText())) {
            errorMessage += "Пароль не подтверждён или подтверждён не верно!\n";
        }
        if (emailaddr.getText() == null || emailaddr.getText().length() == 0) {
            errorMessage += "Поле \"Электронная почта\" не заполнено!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Показываем сообщение об ошибке.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Поля заполнены не верно");
            alert.setHeaderText("Пожалуйста, проверьте следующие поля.");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    /**
     * Set homeaccApp.user info for updating.
     * @param user homeaccApp.user.
     */
    public void setUser(User user) {
        this.user = user;

        username.setText(user.getUsername());
        emailaddr.setText(user.getEmail());
    }

}

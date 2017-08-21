package homeaccApp.user.session;

import homeaccApp.Main;
import homeaccApp.api.DAO.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;

import static homeaccApp.api.DAO.UserDAO.authoriseUser;

/**
 * Session controller
 */
public class SessionController {

    private Stage dialogStage;
    private boolean okClicked = false;
    private Session userSession;

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private CheckBox keepLogged;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private Button ok;
    @FXML
    private Button cancel;

    // Link on application
    private Main main;

    /**
     * Constructor.
     */
    public SessionController() {

    }

    /**
     * Controller initialization.
     */
    @FXML
    private void initialize() {
//        main = new homeaccApp.Main();
    }

    /**
     * Set dialog stage.
     * @param dialogStage window.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * If ok clicked, return true
     * @return true or false.
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Listener for Ok Button
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            userSession = new Session();
            userSession.setUsername(username.getText());
            userSession.setPassword(password.getText());
            userSession.setKeepLogged(keepLogged.isSelected());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            String md5pass = userSession.getPassword();

            int authUid = 0;
            try {
                authUid = authoriseUser(userSession.getUsername(), md5pass);
                userSession.setUserId(authUid);
            } catch (Exception e1) {
                //throw new MyException(e1);
            }

            if (authUid != 0) {
                alert.setTitle("Авторизация");
                alert.setContentText("Вы авторизованы.");
                alert.showAndWait();

                dialogStage.close();
                main.showMainWindow(); // TODO: 03.06.16 check it
            }
            else {
                alert.setContentText("Вы ввели неверное имя пользователя или пароль.\n" +
                        "Проверьте правильность данных и попробуйте ещё раз или нажмите \"Отмена\".");
            }

            if (userSession.isKeepLogged() && authUid != 0) {
                // Get data for saving into file.
                int id = UserDAO.authUserId;
                LocalDate authDate = LocalDate.now();

                byte[] savedData = new byte[2];
                Timestamp datestamp = Timestamp.valueOf(authDate.atStartOfDay());
                savedData[0] = (byte) datestamp.getTime();
                savedData[1] = (byte) id;

                // Write data into file.
                try {
                    writeSmallBinaryFile(savedData, "authInfo");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Вызывается главным приложением, которое даёт на себя ссылку.
     *
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.main = mainApp;
    }

    /**
     * Listener for Cancel Button. todo need opening dialog "Choose homeaccApp.user"
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
            errorMessage += "No valid homeaccApp.user name!\n";
        }
        if (password.getText() == null || password.getText().length() == 0) {
            errorMessage += "No valid password!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Показываем сообщение об ошибке.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    /**
     * Read bytes from binary file.
     * @param aFileName filename
     * @return readed bytes
     * @throws IOException exception
     */
    public static byte[] readSmallBinaryFile(String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
        if (Files.exists(path)) {
            return Files.readAllBytes(path);
        }
        else return null;
    }

    /**
     * Writes to binary file (create, overwrite file)
     * @param aBytes array of bytes
     * @param aFileName filename
     * @throws IOException exception
     */
    public static void writeSmallBinaryFile(byte[] aBytes, String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
        Files.write(path, aBytes); //creates, overwrites
    }

    /**
     * Sets username from "Choose homeaccApp.user" dialog to field "username".
     * @param usernameText given username.
     */
    public void setUsername(String usernameText) {
        username.setText(usernameText);
    }

}

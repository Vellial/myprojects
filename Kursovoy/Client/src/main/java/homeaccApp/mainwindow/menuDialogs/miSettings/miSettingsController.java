package homeaccApp.mainwindow.menuDialogs.miSettings;

import homeaccApp.Main;
import homeaccApp.api.DAO.CommonDAO;
import homeaccApp.api.DAO.UserDAO;
import homeaccApp.api.MyException;
import homeaccApp.api.Sync.Syncronization;
import homeaccApp.api.Sync.homeaccWSCClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Settings controller
 */
public class miSettingsController {
    @FXML
    private PasswordField miSettingsSyncPassword;
    @FXML
    private TextField miSettingsSyncEmail;
    @FXML
    private Button miSettingsSyncSend;

    // Fields for controller
    private miSettings settings;
    private Stage dialogStage;
    private boolean okClicked = false;

    private String username;
    private String password;

    /**
     * Constructor
     */
    public miSettingsController() {

    }

    @FXML
    private void initialize() {
        settings = CommonDAO.selectSettings();
        if (settings.getRemoteEmail() != null) {
            miSettingsSyncEmail.setText(settings.getRemoteEmail());
            miSettingsSyncPassword.setText("   ");
        }
    }

    @FXML
    private void handleSend() throws MyException {
        getSettings();

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Завершение регистрации");
        dialog.setHeaderText("Подтверждение пароля");
        dialog.setContentText("Пожалуйста, повторите пароль:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();

        String repeatPass = "";
        if (result.isPresent()){

            repeatPass = UserDAO.md5Custom(result.get());
        }

        if (!repeatPass.equals("") && repeatPass.equals(password)) {
            //Start syncronize.
            Syncronization.getInstance();
            JSONObject resultJson = new JSONObject();

            resultJson.put("codeOperation", "register"); // TODO: 26.07.16 "authorize" - is constant
            resultJson.put("UUIDDevice", Main.uuidDevice.toString());
            resultJson.put("username", username);
            resultJson.put("pass", password);

            Syncronization.getInstance().sendMessage(resultJson.toJSONString());

            okClicked = true;
            dialogStage.close();
        } else {
            // Показываем сообщение об ошибке.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Подтвердите пароль");
            alert.setHeaderText("Пожалуйста, проверьте и заполните поля правильно.");
            alert.setContentText("Подтвердите пароль для синхронизации с сервером.");

            alert.showAndWait();
        }
    }

    @FXML
    private void handleSave() throws SQLException, ClassNotFoundException {
        getSettings();
        //Store settings to database.
        boolean isExist = CommonDAO.checkEmail(username);
        if (!isExist) {
            CommonDAO.createSettings(username, password);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(dialogStage);
            alert.setTitle("Данные сохранены");
            alert.setHeaderText("Данные вашей учётной записи сохранены.");
            alert.setContentText("");

            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(dialogStage);
            alert.setTitle("Пользователь уже существует");
            alert.setHeaderText("Пользователь с таким e-mail уже существует");
            alert.setContentText("");

            alert.showAndWait();
        }
    }

    private void getSettings() {
        if (isInputValid() && settings.getRemoteEmail() == null) {
            String pass = miSettingsSyncPassword.getText();
            password = UserDAO.md5Custom(pass);
            username = miSettingsSyncEmail.getText();
        }
        else if (isInputValid() && settings.getRemoteEmail() != null) {
            password = settings.getRemotePassword();
            username = settings.getRemoteEmail();
        }
    }

    /**
     * Returns true, если пользователь кликнул OK, в другом случае false.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private boolean isInputValid() {
        String errorMessage = "";
        // TODO: 25.07.16 check email for format
        if (miSettingsSyncEmail.getText() == null) {
            errorMessage += "Не указан электронный адрес (e-mail)\n";
        }
        if (miSettingsSyncPassword.getText() == null || miSettingsSyncPassword.getText().length() == 0) {
            errorMessage += "Вы не ввели пароль\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Показываем сообщение об ошибке.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Поля заполнены неверно");
            alert.setHeaderText("Пожалуйста, проверьте и заполните поля правильно.");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

}

package homeaccApp.user.api.chooseuser;

import homeaccApp.api.DAO.UserDAO;
import homeaccApp.api.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import homeaccApp.Main;
import homeaccApp.user.api.createuser.User;

import java.sql.SQLException;
import java.util.Optional;

import static homeaccApp.api.DAO.UserDAO.deleteUser;
import static homeaccApp.api.DAO.UserDAO.selectUserInfo;

/**
 * Choose homeaccApp.user controller.
 */
public class ChooseUserController {

    @FXML
    private ListView<Item> usersList;
    @FXML
    private Button btnChoose;
    @FXML
    private Button btnCreate;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnClose;

    private Main main;
    private Stage dialogStage;

    /**
     * Constructor.
     */
    public ChooseUserController() {
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
    private void initialize() throws SQLException, ClassNotFoundException {
        ObservableList<Item> observableList = FXCollections.observableArrayList(UserDAO.selectAllUsersList());
        usersList.setItems(observableList);
    }

    /**
     * Choose homeaccApp.user from list.
     */
    @FXML
    private void handleChoose() {
        MultipleSelectionModel<Item> chosen = usersList.getSelectionModel();
        Item userItem = chosen.getSelectedItem();
        String username = userItem.getDescription();
        main.showLoginDialog(username);
        dialogStage.close();
    }

    /**
     * Create new homeaccApp.user.
     */
    @FXML
    private void handleCreate() {
        try {
            main.showCreateUserDialog();
            dialogStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Edit homeaccApp.user data (homeaccApp.user from list).
     */
    @FXML
    private void handleUpdate() {
        MultipleSelectionModel<Item> chosen = usersList.getSelectionModel();
        Item userItem = chosen.getSelectedItem();
        int userid = userItem.getId();
        try {
            User user = selectUserInfo(userid);
            main.showEditUserDialog(user);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete homeaccApp.user from list.
     */
    @FXML
    private void handleDelete() {
        MultipleSelectionModel<Item> chosen = usersList.getSelectionModel();
        Item userItem = chosen.getSelectedItem();
        int userid = userItem.getId();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(dialogStage);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Подтвердите удаление пользователя.");
        alert.setContentText("Вы действительно хотите удалить пользователя " + userItem.getDescription() + " ?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            try {
                deleteUser(userid);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Close the application.
     */
    @FXML
    private void handleClose() {
        dialogStage.close();
    }

}

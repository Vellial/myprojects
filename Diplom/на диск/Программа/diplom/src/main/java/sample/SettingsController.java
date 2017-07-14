package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by Vellial on 18.01.2017.
 */
public class SettingsController {
    @FXML
    private TextField txtServMysql;
    @FXML
    private TextField txtPortMysql;
    @FXML
    private TextField txtDBName;
    @FXML
    private TextField txtUserMysql;
    @FXML
    private PasswordField txtPassMysql;
    @FXML
    private TextField txtServOr;
    @FXML
    private TextField txtPortOr;
    @FXML
    private TextField txtSID;
    @FXML
    private TextField txtUserOr;
    @FXML
    private PasswordField txtPassOr;
    @FXML
    private Button btnOk;
    @FXML
    private Button btnCancel;

    private Stage dialogStage;
    private boolean okClicked = false;
    public static DSettings settings;

    @FXML
    private void handleOk() {
        Main.mysqlServer = txtServMysql.getText();
        Main.mysqlPort = txtPortMysql.getText();
        Main.mysqlDb = txtDBName.getText();
        Main.mysqlUser = txtUserMysql.getText();
        Main.mysqlPass = txtPassMysql.getText();

        Main.oraServer = txtServOr.getText();
        Main.oraPort = txtPortOr.getText();
        Main.oraDb = txtSID.getText();
        Main.oraUser = txtUserOr.getText();
        Main.oraPass = txtPassOr.getText();

        settings = new DSettings(txtServMysql.getText(), txtPortMysql.getText(), txtDBName.getText(), txtUserMysql.getText(),
                txtPassMysql.getText(), Main.oraServer, Main.oraPort,  Main.oraDb, Main.oraUser, Main.oraPass);

        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }
}

package homeaccApp.currencies.edit;

import homeaccApp.currencies.Currency;
import homeaccApp.mainwindow.bills.Bills;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by vellial on 27.09.16.
 */
public class CurrenciesEditController {
    @FXML
    private TextField curName;
    @FXML
    private TextField curVal;
    @FXML
    private Button addCur;
    @FXML
    private Button cancel;

    private Currency currency;
    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private void initialize() {

    }

    /**
     * Returns true, если пользователь кликнул OK, в другом случае false.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Вызывается, когда пользователь кликнул по кнопке OK.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            currency.setCurrencyName(curName.getText());
            currency.setCurrencyShortName(curVal.getText());

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Вызывается, когда пользователь кликнул по кнопке Cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Проверяет пользовательский ввод в текстовых полях.
     *
     * @return true, если пользовательский ввод корректен
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (curName.getText() == null || curName.getText().length() == 0) {
            errorMessage += "Неверное название для единицы измерения\n";
        }
        if (curVal.getText() == null || curVal.getText().length() == 0) {
            errorMessage += "Неверное название для единицы измерения\n";
        }
        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Показываем сообщение об ошибке.
            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.initOwner(dialogStage);
            alert.setTitle("Поля заполнены неверно");
            alert.setHeaderText("Пожалуйста, проверьте и заполните поля правильно.");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    /**
     * Задаёт валюту, информацию о которой будем менять.
     *
     * @param currency
     */
    public void setCur(Currency currency) {
        this.currency = currency;

        curName.setText(currency.getCurrencyName());
        curVal.setText(currency.getCurrencyShortName());

    }


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}

package homeaccApp.mainwindow.bills.remittance;

import homeaccApp.api.DAO.BillDAO;
import homeaccApp.api.DAO.UserDAO;
import homeaccApp.api.Item;
import homeaccApp.mainwindow.bills.Bills;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Created by vellial on 18.10.16.
 */
public class BillsRemittanceController {
    @FXML
    private ComboBox<Bills> inBills;
    @FXML
    private ComboBox<Bills> outBills;
    @FXML
    private TextField amount;
    private ObservableList<Bills> bills;
    private Stage dialogStage;

    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {

        bills = BillDAO.selectBillsInfo(UserDAO.authUserId);

        inBills.getItems().addAll(bills);
        outBills.getItems().addAll(bills);

    }

    /**
     * Вызывается, когда пользователь кликнул по кнопке OK.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            Bills fromBill = inBills.getSelectionModel().getSelectedItem();
            Bills toBill = outBills.getSelectionModel().getSelectedItem();

            // Checking currency
            Item curFrom = fromBill.getBillCurrency();
            Item curTo = toBill.getBillCurrency();
            if (curFrom.getId() != curTo.getId()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initModality(Modality.WINDOW_MODAL);
                alert.setResizable(true);
                alert.setTitle("Операция невозможна");
                alert.setHeaderText("Невозможно перевести средства, поскольку на счетах используется разная валюта.");
                alert.setContentText("Пожалуйста, измените валюту счёта или выберите другой счёт");

                alert.showAndWait();
            } else {

                String sum = amount.getText();
                Double fromSum = fromBill.getCashAmount();
                Double sumSum = Double.parseDouble(sum);

                if (fromSum < sumSum) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initModality(Modality.WINDOW_MODAL);
                    alert.setResizable(true);
                    alert.setTitle("Поля заполнены неверно");
                    alert.setHeaderText("Пожалуйста, проверьте и заполните поля правильно.");
                    alert.setContentText("На счету недостаточно средств для перевода");

                    alert.showAndWait();
                } else {

                    Double newFromCash = fromSum - sumSum;
                    Double newToCash = toBill.getCashAmount() + sumSum;

                    fromBill.setCashAmount(newFromCash);
                    toBill.setCashAmount(newToCash);

                    BillDAO.updateBill(newFromCash, fromBill.getBillId());
                    BillDAO.updateBill(newToCash, toBill.getBillId());

                    dialogStage.close();
                }
            }
        }
    }

    /**
     * Вызывается, когда пользователь кликнул по кнопке Cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private boolean isInputValid() {
        String errorMessage = "";
        String sum = amount.getText();
        if (sum.equals("") || sum.length() == 0 || Double.parseDouble(sum) < 0) {
            errorMessage += "Неверная сумма перевода\n";
        }
        if (inBills.getValue() == null) {
            errorMessage += "Выберите счёт, с которого хотите сделать перевод";
        }
        if (outBills.getValue() == null) {
            errorMessage += "Выберите счёт, на который хотите сделать перевод";
        }
        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Показываем сообщение об ошибке.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setResizable(true);
            alert.setTitle("Поля заполнены неверно");
            alert.setHeaderText("Пожалуйста, проверьте и заполните поля правильно.");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

}

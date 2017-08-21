package homeaccApp.mainwindow.bills.edit;

import homeaccApp.api.DAO.CurrencyDAO;
import homeaccApp.api.Item;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import homeaccApp.currencies.Currency;
import homeaccApp.mainwindow.bills.Bills;

import java.sql.SQLException;

/**
 * Created by vellial on 20.06.16.
 */
public class BillsEditController {
    @FXML
    private TextField txtBillName;
    @FXML
    private TextField txtBillStartBalance;
    @FXML
    private TextArea txtBillNote;
    @FXML
    private ComboBox<Item> cmbCurrency;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnCancel;

    private Stage dialogStage;
    private Bills bill;
    private boolean okClicked = false;
    private ObservableList<Item> currencies;

    public BillsEditController() {
    }

    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {
        currencies = CurrencyDAO.selectAllCurrenciesList();
        cmbCurrency.getItems().addAll(currencies);
        cmbCurrency.getSelectionModel().selectFirst();

        cmbCurrency.setCellFactory(new Callback<ListView<Item>, ListCell<Item>>() {
            @Override
            public ListCell<Item> call(ListView<Item> param) {

                return new ListCell<Item>(){
                    @Override
                    public void updateItem(Item item, boolean empty){
                        super.updateItem(item, empty);
                        if(!empty) {
                            setText(item.getDescription());
                            setGraphic(null);
                        }
                    }
                };
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Задаёт счёт, информацию о котором будем менять.
     *
     * @param bill
     */
    public void setBill(Bills bill) {
        this.bill = bill;

        txtBillName.setText(bill.getBillName());
        txtBillNote.setText(bill.getNote());
        txtBillStartBalance.setText(Double.toString(bill.getStartBalance()));

        cmbCurrency.setValue(bill.getBillCurrency());
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
            bill.setBillName(txtBillName.getText());
            bill.setStartBalance(Double.valueOf(txtBillStartBalance.getText()));
            bill.setNote(txtBillNote.getText());
            Item i = cmbCurrency.getSelectionModel().getSelectedItem();
            bill.setBillCurrency(i);
            bill.setCashAmount(Double.valueOf(txtBillStartBalance.getText()));

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

        if (txtBillName.getText() == null || txtBillName.getText().length() == 0) {
            errorMessage += "Неверное имя счёта!\n";
        }
        if (txtBillStartBalance.getText() == null || txtBillStartBalance.getText().length() == 0) {
            errorMessage += "Неверный стартовый баланс!\n";
        }

        if (cmbCurrency.getValue() == null) {
            errorMessage += "Неверная валюта!\n";
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

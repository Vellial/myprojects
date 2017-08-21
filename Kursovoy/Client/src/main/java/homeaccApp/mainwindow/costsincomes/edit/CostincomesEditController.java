package homeaccApp.mainwindow.costsincomes.edit;

import homeaccApp.api.DAO.BillDAO;
import homeaccApp.api.DAO.CategoryDAO;
import homeaccApp.api.DAO.MeasureDAO;
import homeaccApp.api.LocalDateReciever;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import homeaccApp.categories.Categories;
import homeaccApp.mainwindow.bills.Bills;
import homeaccApp.mainwindow.costsincomes.Costincomes;
import homeaccApp.measures.Measures;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Edit costincome note dialog.
 */
public class CostincomesEditController {
    @FXML
    private DatePicker dateOfCreation;
    @FXML
    private ComboBox<Bills> cmbBill;
    @FXML
    private ComboBox<Measures> cmbMeasure;
    @FXML
    private ComboBox<Categories> cmbCostincCategory;
    @FXML
    private TextField txtCount;
    @FXML
    private TextField txtCostincAmount;
    @FXML
    private TextArea txtCostincNote;

    private Stage dialogStage;
    private Costincomes costincomeItem;
    private boolean okClicked = false;
    private ObservableList<Categories> categories;
    private ObservableList<Bills> bills;
    private ObservableList<Measures> measures;
    private boolean costincome;

    public CostincomesEditController() throws SQLException, ClassNotFoundException {

    }

    public void initialize() throws SQLException, ClassNotFoundException {
        measures = MeasureDAO.selectAllMeasures();
        bills = BillDAO.selectBills();

        cmbBill.getItems().addAll(bills);

        cmbBill.setCellFactory(new Callback<ListView<Bills>, ListCell<Bills>>() {
            @Override
            public ListCell<Bills> call(ListView<Bills> param) {

                return new ListCell<Bills>(){
                    @Override
                    public void updateItem(Bills item, boolean empty){
                        super.updateItem(item, empty);
                        if(!empty) {
                            setText(item.getBillName());
                            setGraphic(null);
                        }
                    }
                };
            }
        });



        cmbMeasure.getItems().addAll(measures);

        cmbMeasure.setCellFactory(new Callback<ListView<Measures>, ListCell<Measures>>() {
            @Override
            public ListCell<Measures> call(ListView<Measures> param) {

                return new ListCell<Measures>(){
                    @Override
                    public void updateItem(Measures item, boolean empty){
                        super.updateItem(item, empty);
                        if(!empty) {
                            setText(item.getMeasureName());
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
     * Задаёт запись, которую будем менять.
     *
     * @param costinc
     */
    public void setCostincome(Costincomes costinc) {
        this.costincomeItem = costinc;

        LocalDate date = costincomeItem.getCostincDate() != 0 ? LocalDateReciever.getLocalDateFromLong(costincomeItem.getCostincDate()) : LocalDate.now();
        dateOfCreation.setValue(date);
        cmbBill.setValue(costincomeItem.getCostincBill());

        cmbBill.setCellFactory(new Callback<ListView<Bills>, ListCell<Bills>>() {
            @Override
            public ListCell<Bills> call(ListView<Bills> param) {

                return new ListCell<Bills>(){
                    @Override
                    public void updateItem(Bills item, boolean empty){
                        super.updateItem(item, empty);
                        if(!empty) {
                            setText(item.getBillName());
                            setGraphic(null);
                        }
                    }
                };
            }
        });

        cmbMeasure.setValue(costincomeItem.getCostincMeasure());
        cmbCostincCategory.setValue(costincomeItem.getCostincCategory());
        txtCount.setText(Integer.valueOf(costincomeItem.getCostincCount()).toString());
        txtCostincAmount.setText(Double.valueOf(costincomeItem.getCostincAmount()).toString());
        txtCostincNote.setText(costincomeItem.getCostincNote());

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
            costincomeItem.setCostincDate(LocalDateReciever.getLongTimeFromLocalDate(dateOfCreation.getValue()));
            costincomeItem.setCostincBill(cmbBill.getSelectionModel().getSelectedItem());
            costincomeItem.setCostincBillName(costincomeItem.getCostincBill().getBillName());
            costincomeItem.setCostincBillId(cmbBill.getSelectionModel().getSelectedItem().getBillId());
            costincomeItem.setCostincMeasure(cmbMeasure.getSelectionModel().getSelectedItem());
            costincomeItem.setCostincCategory(cmbCostincCategory.getSelectionModel().getSelectedItem());
            costincomeItem.setCostincCount(Integer.valueOf(txtCount.getText()));
            costincomeItem.setCostincAmount(Double.valueOf(txtCostincAmount.getText()));
            costincomeItem.setCostincNote(txtCostincNote.getText());
            costincomeItem.setCostincome(this.costincome);

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

        if (dateOfCreation.getValue() == null) {
            errorMessage += "Дата не указана или указана неверно!\n";
        }
        if (txtCount.getText() == null || txtCount.getText().length() == 0) {
            errorMessage += "Неверное количество!\n";
        }
        if (txtCostincAmount.getText() == null || txtCostincAmount.getText().length() == 0 || txtCostincAmount.getText().equals(0)) {
            errorMessage += "Неверная сумма!\n";
        }
        if (cmbBill.getValue() == null) {
            errorMessage += "Выберите счёт";
        }
        if (cmbCostincCategory.getValue() == null) {
            cmbCostincCategory.getSelectionModel().selectFirst();
        }
        if (cmbMeasure.getValue() == null) {
            cmbMeasure.getSelectionModel().selectFirst();
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

    public void setIsCostincome(boolean costincome) throws SQLException, ClassNotFoundException {
        this.costincome = costincome;

        System.out.println("2: " +costincome);
        categories = CategoryDAO.selectCategories(costincome);
        cmbCostincCategory.getItems().addAll(categories);

        cmbCostincCategory.setCellFactory(new Callback<ListView<Categories>, ListCell<Categories>>() {
            @Override
            public ListCell<Categories> call(ListView<Categories> param) {

                return new ListCell<Categories>(){
                    @Override
                    public void updateItem(Categories item, boolean empty){
                        super.updateItem(item, empty);
                        if(!empty) {
                            setText(item.getCategoryName());
                            setGraphic(null);
                        }
                    }
                };
            }
        });
    }
}

package homeaccApp.mainwindow.planning.edit;

import homeaccApp.api.DAO.*;
import homeaccApp.api.LocalDateReciever;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import homeaccApp.categories.Categories;
import homeaccApp.mainwindow.bills.Bills;
import homeaccApp.mainwindow.planning.Planning;
import homeaccApp.measures.Measures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Edit notes about planning operations.
 */
public class PlanningEditController {
    @FXML
    private DatePicker planningDate;
    @FXML
    private ComboBox<Bills> planningBill;
    @FXML
    private ComboBox<Measures> planningMeasure;
    @FXML
    private ComboBox<Categories> planningCategory;
    @FXML
    private ComboBox<String> planningCostIncome;
    @FXML
    private TextField planningCount;
    @FXML
    private TextField planningAmount;
    @FXML
    private TextArea planningNote;
    @FXML
    private ComboBox<String> planningStatus;
    @FXML
    private ComboBox<String> planningPeriods;

    private Stage dialogStage;
    private Planning planning;
    private boolean okClicked = false;
    private ObservableList<Categories> categories;
    private ObservableList<Bills> bills;
    private ObservableList<Measures> measures;
    private boolean costincome;

    private static String cost = "Расход";
    private static String income = "Доход";
    private List<String> states = new ArrayList<String>();
    private List<String> periods = new ArrayList<String>();

    public PlanningEditController() throws SQLException, ClassNotFoundException {

    }

    public void initialize() throws SQLException, ClassNotFoundException {
        measures = MeasureDAO.selectAllMeasures();
        bills = BillDAO.selectBills();
        categories = CategoryDAO.selectCategories();
        planningCostIncome.getItems().addAll(income, cost);
        states.add("Запланировано");
        states.add("Выполнено");
        planningStatus.getItems().addAll(states);

        periods.add("Однократно");
        periods.add("Ежедневно");
        periods.add("Еженедельно");
        periods.add("Ежемесячно");
        periods.add("Ежегодно");
        planningPeriods.getItems().addAll(periods);

        planningBill.getItems().addAll(bills);

        planningBill.setCellFactory(new Callback<ListView<Bills>, ListCell<Bills>>() {
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

        planningCategory.getItems().addAll(categories);

        planningCategory.setCellFactory(new Callback<ListView<Categories>, ListCell<Categories>>() {
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

        planningMeasure.getItems().addAll(measures);

        planningMeasure.setCellFactory(new Callback<ListView<Measures>, ListCell<Measures>>() {
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
     * @param planning
     */
    public void setPlan(Planning planning) {
        this.planning = planning;

        LocalDate date = planning.getPlanningDate() != 0 ? LocalDateReciever.getLocalDateFromLong(planning.getPlanningDate()) : LocalDate.now();
        planningDate.setValue(date);
        planningBill.setValue(this.planning.getPlanningBill());
        planningMeasure.setValue(this.planning.getPlanningMeasure());
        planningCategory.setValue(this.planning.getPlanningCategory());
        planningCount.setText(Integer.valueOf(this.planning.getPlanningCount()).toString());
        planningAmount.setText(Double.valueOf(this.planning.getPlanningAmount()).toString());
        planningNote.setText(this.planning.getPlanningNote());
        costincome = this.planning.getPlanningCostincome();
        planningCostIncome.setValue(income);
        if (!costincome) {
            planningCostIncome.setValue(cost);
        }

        planningStatus.setValue(this.planning.getPlanStatus());
        planningPeriods.setValue(this.planning.getPlanPeriod());

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
            long date = LocalDateReciever.getLongTimeFromLocalDate(planningDate.getValue());
            System.out.println(date);
            planning.setPlanningDate(LocalDateReciever.getLongTimeFromLocalDate(planningDate.getValue()));
            planning.setPlanningBill(planningBill.getSelectionModel().getSelectedItem());
            planning.setPlanningMeasure(planningMeasure.getSelectionModel().getSelectedItem());
            planning.setPlanningCategory(planningCategory.getSelectionModel().getSelectedItem());
            planning.setPlanningCount(Integer.valueOf(planningCount.getText()));
            planning.setPlanningAmount(Double.valueOf(planningAmount.getText()));
            planning.setPlanningNote(planningNote.getText());
            String s = planningCostIncome.getSelectionModel().getSelectedItem();
            boolean costinc = false;
            if (s.equals("Доход")) {
                costinc = true;
            }
            planning.setPlanningCostincome(costinc);
            planning.setPlanStatus(planningStatus.getSelectionModel().getSelectedItem());
            planning.setPlanPeriod(planningPeriods.getSelectionModel().getSelectedItem());

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

        if (planningDate.getValue() == null) {
            errorMessage += "Не указана дата\n";
        }
        if (planningAmount.getText() == null || planningAmount.getText().length() == 0) {
            errorMessage += "Не указана сумма\n";
        }
        if (planningCount.getText() == null || planningCount.getText().length() == 0) {
            errorMessage += "Не указано количество\n";
        }

        if (planningBill.getValue() == null) {
            errorMessage += "Выберите счёт\n";
        }
        if (planningCategory.getValue() == null) {
            planningCategory.getSelectionModel().selectFirst();
        }
        if (planningMeasure.getValue() == null) {
            planningMeasure.getSelectionModel().selectFirst();
        }
        if (planningStatus.getValue() == null) {
            planningStatus.getSelectionModel().selectFirst();
        }
        if (planningPeriods.getValue() == null) {
            planningPeriods.getSelectionModel().selectFirst();
        }
        if (planningCostIncome.getValue() == null) {
            errorMessage += "Выберите операцию: доход или расход\n";
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

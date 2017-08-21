package homeaccApp.mainwindow.planning;

import homeaccApp.api.DAO.CategoryDAO;
import homeaccApp.api.DAO.CostsIncomesDAO;
import homeaccApp.api.DAO.PlanningDAO;
import homeaccApp.api.LocalDateReciever;
import homeaccApp.mainwindow.planning.edit.PlanningEditController;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import homeaccApp.Main;
import homeaccApp.mainwindow.MainWindowController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Planning controller for planning tab.
 */
public class PlanningController {
    @FXML
    private TableView<Planning> planningTable;
    @FXML
    private TableColumn<Planning, String> planningBill;
    @FXML
    private TableColumn<Planning, String> planningDate;
    @FXML
    private TableColumn<Planning, Integer> planningCount;
    @FXML
    private TableColumn<Planning, Double> planningAmount;
    @FXML
    private TableColumn<Planning, String> planningNote;
    @FXML
    private TableColumn<Planning, String> planningMeasure;
    @FXML
    private TableColumn<Planning, String> planningCategory;
    @FXML
    private TableColumn<Planning, String> planningCostincome;
    @FXML
    private TableColumn<Planning, String> planningState;
    @FXML
    private TableColumn<Planning, String> planningPeriod;

    private Main main;
    private ObservableList<Planning> planningnotes;
    private MainWindowController mainWindowContr;

    public PlanningController() throws SQLException, ClassNotFoundException {

    }

    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {
        this.planningAmount.setCellValueFactory(cellData -> cellData.getValue().planningAmountProperty().asObject());
        this.planningBill.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPlanningBill().getBillName()));
        this.planningCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPlanningCategory().getCategoryName()));
        this.planningCount.setCellValueFactory(cellData -> cellData.getValue().planningCountProperty().asObject());
        this.planningDate.setCellValueFactory(cellData ->  new ReadOnlyStringWrapper(LocalDateReciever.getLocalDateFromLong(cellData.getValue().getPlanningDate()).toString()));
        this.planningMeasure.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPlanningMeasure().getMeasureName()));
        this.planningNote.setCellValueFactory(cellData -> cellData.getValue().planningNoteProperty());
        this.planningCostincome.setCellValueFactory(cellData -> {
            String s = cellData.getValue().getPlanningCostincome() ? "Доход" : "Расход";
            return new SimpleStringProperty(s);
        });
        this.planningState.setCellValueFactory(cellData -> cellData.getValue().planStatusProperty());
        this.planningPeriod.setCellValueFactory(cellData -> cellData.getValue().planPeriodProperty());

        buildData();
    }

    private void buildData() throws SQLException, ClassNotFoundException {
        planningnotes = PlanningDAO.selectPlanningData();
        planningTable.setItems(planningnotes);
    }

    /**
     * Вызывается главным приложением, которое даёт на себя ссылку.
     *
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.main = mainApp;
    }

    @FXML
    private void handleCreate() {
        Planning planning = new Planning();
        boolean okClicked = showCostincomeEditDialog(planning);

        if (okClicked) {
            // Adding new note to database.
            try {
                PlanningDAO.createPlan(planning.getPlanningDate(), planning.getPlanningBill().getBillId(), planning.getPlanningCount(), planning.getPlanningMeasure().getMeasureId(), planning.getPlanningCategory().getCategoryId(), planning.getPlanningNote(), planning.getPlanningCostincome(), planning.getPlanningAmount(), planning.getPlanStatus(), planning.getPlanPeriod());

                String status = planning.getPlanStatus();
                planning.setPlanStatus(status);

                if (status.equals("Выполнено")) {
                    CostsIncomesDAO.createCostIncome(planning.getPlanningDate(), planning.getPlanningBill().getBillId(), planning.getPlanningCount(), planning.getPlanningMeasure().getMeasureId(), planning.getPlanningCategory().getCategoryId(), planning.getPlanningNote(), planning.getPlanningCostincome(), planning.getPlanningAmount());
                }

            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            planningTable.getItems().add(planning);
        }

    }

    @FXML
    private void handleUpdate() throws SQLException, ClassNotFoundException {
        Planning selectedItem = planningTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            boolean okClicked = showCostincomeEditDialog(selectedItem);
            if (okClicked) {
                PlanningDAO.editPlan(selectedItem.getPlanningDate(), selectedItem.getPlanningBill().getBillId(), selectedItem.getPlanningNote(), selectedItem.getPlanningCategory().getCategoryId(), selectedItem.getPlanningMeasure().getMeasureId(), selectedItem.getPlanningCount(), selectedItem.getPlanningAmount(), selectedItem.getPlanningCostincome(), selectedItem.getPlanningId(), selectedItem.getPlanStatus(), selectedItem.getPlanPeriod());
            }
        } else {
            // Ничего не выбрано.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(planningTable.getScene().getWindow());
            alert.setTitle("Ничего не выбрано");
            alert.setHeaderText("Счёт не выбран");
            alert.setContentText("Пожалуйста, выберите счёт из таблицы.");

            alert.showAndWait();
        }

    }

    @FXML
    private void handleDelete() throws SQLException, ClassNotFoundException {
        int planningId = planningTable.getSelectionModel().getSelectedItem().getPlanningId();
        int selectedIndex = planningTable.getSelectionModel().getSelectedIndex();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Подтвердите действие");
        alert.setContentText("Вы действительно хотите удалить запись?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            PlanningDAO.deletePlan(planningId);
            planningTable.getItems().remove(selectedIndex);
        }

    }

    @FXML
    private void handleComplete() throws SQLException, ClassNotFoundException {
        long dateOfNow = LocalDateReciever.getDateOfNow();
        ObservableList<Planning> oldNotes = PlanningDAO.selectOldPlans(dateOfNow);
        for (Planning note : oldNotes) {
            CostsIncomesDAO.createCostIncome(note.getPlanningDate(), note.getBillId(), note.getPlanningCount(), note.getPlanningMeasure().getMeasureId(), note.getPlanningCategory().getCategoryId(), note.getPlanningNote(), note.getPlanningCostincome(), note.getPlanningAmount());
            PlanningDAO.deletePlan(note.getPlanningId());
        }
        planningTable.refresh();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Операция выполнена успешно");
        alert.setHeaderText("Записи перенесены в доходы/расходы");
        alert.showAndWait();
    }

    /**
     * Открывает диалоговое окно для изменения деталей указанного адресата.
     * Если пользователь кликнул OK, то изменения сохраняются в предоставленном
     * объекте адресата и возвращается значение true.
     *
     * @param planning - объект адресата, который надо изменить
     * @return true, если пользователь кликнул OK, в противном случае false.
     */
    public boolean showCostincomeEditDialog(Planning planning) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(Main.dir, "mainwindow/planning/edit/PlanningEditDialog.fxml").toURI().toURL();
            loader.setLocation(location);
            AnchorPane page = (AnchorPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Создать/изменить запись");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(planningTable.getScene().getWindow());
            dialogStage.setResizable(true);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            PlanningEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPlan(planning);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}

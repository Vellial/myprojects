package homeaccApp.mainwindow.costsincomes;

import homeaccApp.api.DAO.BillDAO;
import homeaccApp.api.DAO.CostsIncomesDAO;
import homeaccApp.api.LocalDateReciever;
import homeaccApp.mainwindow.bills.Bills;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import homeaccApp.mainwindow.costsincomes.edit.CostincomesEditController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Cost/Incomes controller.
 */
public class CostsController {
    @FXML
    private TableView<Costincomes> costincTable;
    @FXML
    private TableColumn<Costincomes, String> costincBill;
    @FXML
    private TableColumn<Costincomes, String> costincDate;
    @FXML
    private TableColumn<Costincomes, Integer> costincCount;
    @FXML
    private TableColumn<Costincomes, Double> costincAmount;
    @FXML
    private TableColumn<Costincomes, String> costincNote;
    @FXML
    private TableColumn<Costincomes, String> costincMeasure;
    @FXML
    private TableColumn<Costincomes, String> costincCategory;

    private Main main;
    private ObservableList<Costincomes> costincomes;
    private MainWindowController mainWindowContr;
    private final boolean costincome = false;

    public CostsController() {

    }

    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {
        costincBill.setCellValueFactory(cellData -> cellData.getValue().costincBillNameProperty());
        costincDate.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(LocalDateReciever.getLocalDateFromLong(cellData.getValue().getCostincDate()).toString()));
        costincAmount.setCellValueFactory(cellData -> cellData.getValue().costincAmountProperty().asObject());
        costincCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCostincCategory().getCategoryName()));
        costincCount.setCellValueFactory(cellData -> cellData.getValue().costincCountProperty().asObject());
        costincNote.setCellValueFactory(cellData -> cellData.getValue().costincNoteProperty());
        costincMeasure.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCostincMeasure().getMeasureName()));

        buildData(costincome);
    }

    private void buildData(boolean costincome) throws SQLException, ClassNotFoundException {
        costincomes = CostsIncomesDAO.selectCostsIncomesData(costincome);
        costincTable.setItems(costincomes);
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
        Costincomes costincomes = new Costincomes();
        boolean okClicked = showCostincomeEditDialog(costincomes, costincome);

        if (okClicked) {
            // Working with Date.
            LocalDate localDate = LocalDate.now();
            // TODO: 24.02.16 we need to think about time zone, aren't we?
            Timestamp time = Timestamp.valueOf(localDate.atStartOfDay());
            long date = time.getTime();

            // Adding new note to database.
            try {
                CostsIncomesDAO.createCostIncome(date, costincomes.getCostincBillId(), costincomes.getCostincCount(), costincomes.getCostincMeasure().getMeasureId(), costincomes.getCostincCategory().getCategoryId(), costincomes.getCostincNote(), costincome, costincomes.getCostincAmount());
                BillDAO.updateBill(costincome, costincomes.getCostincAmount(), costincomes.getCostincBillId());
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            costincomes.setCostincDate(LocalDateReciever.getLongTimeFromLocalDate(localDate));

            costincTable.getItems().add(costincomes);
        }

    }

    @FXML
    private void handleUpdate() throws SQLException, ClassNotFoundException {
        Costincomes selectedItem = costincTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            boolean okClicked = showCostincomeEditDialog(selectedItem, costincome);
            if (okClicked) {
                CostsIncomesDAO.editCostIncome(selectedItem.getCostincDate(), selectedItem.getCostincBill().getBillId(), selectedItem.getCostincNote(), selectedItem.getCostincCategory().getCategoryId(), selectedItem.getCostincMeasure().getMeasureId(), selectedItem.getCostincCount(), selectedItem.getCostincAmount(), costincome, selectedItem.getCostincId());
                BillDAO.updateBill(costincome, selectedItem.getCostincAmount(), selectedItem.getCostincBillId());
            }
        } else {
            // Ничего не выбрано.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(costincTable.getScene().getWindow());
            alert.setTitle("Ничего не выбрано");
            alert.setHeaderText("Счёт не выбран");
            alert.setContentText("Пожалуйста, выберите счёт из таблицы.");

            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete() throws SQLException, ClassNotFoundException {
        int costincId = costincTable.getSelectionModel().getSelectedItem().getCostincId();
        int selectedIndex = costincTable.getSelectionModel().getSelectedIndex();
        CostsIncomesDAO.deleteCostIncome(costincId);
        costincTable.getItems().remove(selectedIndex);
    }

    public void init(MainWindowController mainWindowController) {
        this.mainWindowContr = mainWindowController;
    }

    /**
     * Открывает диалоговое окно для изменения деталей указанного адресата.
     * Если пользователь кликнул OK, то изменения сохраняются в предоставленном
     * объекте адресата и возвращается значение true.
     *
     * @param costincomes - объект адресата, который надо изменить
     * @return true, если пользователь кликнул OK, в противном случае false.
     */
    public boolean showCostincomeEditDialog(Costincomes costincomes, boolean costincome) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(Main.dir, "mainwindow/costsincomes/edit/CostincomesEditDialog.fxml").toURI().toURL();
            loader.setLocation(location);
            AnchorPane page = (AnchorPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Создать/изменить запись");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(costincTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            CostincomesEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCostincome(costincomes);
            controller.setIsCostincome(costincome);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

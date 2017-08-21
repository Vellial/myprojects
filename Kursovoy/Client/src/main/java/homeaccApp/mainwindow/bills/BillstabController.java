package homeaccApp.mainwindow.bills;

import homeaccApp.api.DAO.BillDAO;
import homeaccApp.api.DAO.UserDAO;
import homeaccApp.api.LocalDateReciever;
import homeaccApp.mainwindow.bills.remittance.BillsRemittanceController;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import homeaccApp.Main;
import homeaccApp.mainwindow.MainWindowController;
import homeaccApp.mainwindow.bills.edit.BillsEditController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Bills controller.
 */
public class BillstabController {
    @FXML
    private TableView<Bills> billsTable;
    @FXML
    private TableColumn<Bills, String> billName;
    @FXML
    private TableColumn<Bills, String> billDate;
    @FXML
    private TableColumn<Bills, String> billStartBalance;
    @FXML
    private TableColumn<Bills, String> billCashAmount;
    @FXML
    private TableColumn<Bills, String> billNote;
    @FXML
    private TableColumn<Bills, String> billCurrency;

    private Main main;
    private MainWindowController mainWindowContr;
    private ObservableList<Bills> bills;

    public BillstabController() {

    }

    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {
        billName.setCellValueFactory(cellData -> cellData.getValue().billNameProperty());
        billDate.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(LocalDateReciever.getLocalDateFromLong(cellData.getValue().getDate()).toString()));
        billStartBalance.setCellValueFactory(cellData -> cellData.getValue().startBalanceProperty().asString());
        billCashAmount.setCellValueFactory(cellData -> cellData.getValue().cashAmountProperty().asString());
        billNote.setCellValueFactory(cellData -> cellData.getValue().noteProperty());
        billCurrency.setCellValueFactory(cellData -> cellData.getValue().billCurrencyProperty().asString());

        buildData();
    }

    private void buildData() throws SQLException, ClassNotFoundException {
        bills = BillDAO.selectBillsInfo(UserDAO.authUserId);
        billsTable.setItems(bills);
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
        Bills bill = new Bills();
        boolean okClicked = showBillEditDialog(bill);

        if (okClicked) {
            long date = LocalDateReciever.getDateOfNow();

            // Adding new bill to database.
            try {
                BillDAO.createBill(date, bill.getBillName(), bill.getNote(), bill.getStartBalance(), bill.getBillCurrency().getId(), UserDAO.authUserId);
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            bill.setDate(date);

            billsTable.getItems().add(bill);
            billsTable.refresh();
        }

    }

    @FXML
    private void handleUpdate() throws SQLException, ClassNotFoundException {
        Bills selectedBill = billsTable.getSelectionModel().getSelectedItem();
        if (selectedBill != null) {
            boolean okClicked = showBillEditDialog(selectedBill);
            if (okClicked) {
                Timestamp time = Timestamp.valueOf(LocalDateReciever.getLocalDateFromLong(selectedBill.getDate()).atStartOfDay());
                BillDAO.editBill(time.getTime(), selectedBill.getBillName(), selectedBill.getNote(), selectedBill.getStartBalance(), selectedBill.getUserId(), selectedBill.getBillId());
            }
        } else {
            // Ничего не выбрано.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(billsTable.getScene().getWindow());
            alert.setTitle("Ничего не выбрано");
            alert.setHeaderText("Счёт не выбран");
            alert.setContentText("Пожалуйста, выберите счёт из таблицы.");

            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete() throws SQLException, ClassNotFoundException {
        int billId = billsTable.getSelectionModel().getSelectedItem().getBillId();
        int selectedIndex = billsTable.getSelectionModel().getSelectedIndex();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setResizable(true);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Подтвердите действие");
        alert.setContentText("Вы действительно хотите удалить запись?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            BillDAO.deleteBill(billId);
            billsTable.getItems().remove(selectedIndex);
        }
    }

    @FXML
    private void handleRemittance() {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(Main.dir, "mainwindow/bills/remittance/BillsRemittance.fxml").toURI().toURL();
            loader.setLocation(location);
            GridPane page = (GridPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Создать/изменить запись");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(billsTable.getScene().getWindow());
            dialogStage.setResizable(true);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            BillsRemittanceController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();
            billsTable.refresh();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(MainWindowController mainWindowController) {
        this.mainWindowContr = mainWindowController;
    }

    /**
     * Открывает диалоговое окно для изменения деталей указанного адресата.
     * Если пользователь кликнул OK, то изменения сохраняются в предоставленном
     * объекте адресата и возвращается значение true.
     *
     * @param bill - объект адресата, который надо изменить
     * @return true, если пользователь кликнул OK, в противном случае false.
     */
    public boolean showBillEditDialog(Bills bill) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(Main.dir, "mainwindow/bills/edit/billsEditBill.fxml").toURI().toURL();
            loader.setLocation(location);
            AnchorPane page = (AnchorPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Создать/изменить запись");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(billsTable.getScene().getWindow());
            dialogStage.setResizable(true);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            BillsEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setBill(bill);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}

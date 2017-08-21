package homeaccApp.currencies;

import homeaccApp.Main;
import homeaccApp.api.DAO.CurrencyDAO;
import homeaccApp.currencies.edit.CurrenciesEditController;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Currencies controller.
 */
public class CurrenciesController {

    @FXML
    private TableView<Currency> currenciesTable;
    @FXML
    private TableColumn<Currency, String> currencyName;
    @FXML
    private TableColumn<Currency, String> currencyShortName;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnCancel;

    private Main main;
    private Stage dialogStage;
    private ObservableList<Currency> currencies;

    /**
     * Constructor.
     */
    public CurrenciesController() {
        main = new Main();
    }

    /**
     * Controller initialization.
     */
    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {
        currencyName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCurrencyName()));
        currencyShortName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCurrencyShortName()));
        buildData();
    }

    private void buildData() throws SQLException, ClassNotFoundException {
        currencies = CurrencyDAO.selectAllCurrencies();
        currenciesTable.setItems(currencies);
    }

    /**
     * Set dialog stage.
     * @param dialogStage window.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Add new currency.
     */
    @FXML
    private void handleAdd() {
        Currency currency = new Currency();
        boolean okClicked = showCurrencyEditDialog(currency);

        if (okClicked) {

            // Adding new bill to database.
            try {
                CurrencyDAO.createCurrency(currency.getCurrencyName(), currency.getCurrencyShortName());
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            currenciesTable.getItems().add(currency);
        }
    }

    /**
     * Edit currency.
     */
    @FXML
    private void handleUpdate() throws SQLException, ClassNotFoundException {
        Currency selectedCurrency = currenciesTable.getSelectionModel().getSelectedItem();
        if (selectedCurrency != null) {
            boolean okClicked = showCurrencyEditDialog(selectedCurrency);
            if (okClicked) {
                CurrencyDAO.editCurrency(selectedCurrency.getCurrencyName(), selectedCurrency.getCurrencyShortName(), selectedCurrency.getCurrencyId());
                currenciesTable.refresh();
            }
        } else {
            // Ничего не выбрано.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(currenciesTable.getScene().getWindow());
            alert.setTitle("Ничего не выбрано");
            alert.setHeaderText("Счёт не выбран");
            alert.setContentText("Пожалуйста, выберите счёт из таблицы.");

            alert.showAndWait();
        }
    }

    /**
     * Delete currency.
     */
    @FXML
    private void handleDelete() throws SQLException, ClassNotFoundException {
        int currencyId = currenciesTable.getSelectionModel().getSelectedItem().getCurrencyId();
        int selectedIndex = currenciesTable.getSelectionModel().getSelectedIndex();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Подтвердите действие");
        alert.setContentText("Вы действительно хотите удалить запись?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            CurrencyDAO.deleteCurrency(currencyId);
            currenciesTable.getItems().remove(selectedIndex);
        }
    }

    /**
     * Close the application.
     */
    @FXML
    private void handleClose() {
        dialogStage.close();
    }

    /**
     * Открывает диалоговое окно для изменения деталей указанного адресата.
     * Если пользователь кликнул OK, то изменения сохраняются в предоставленном
     * объекте адресата и возвращается значение true.
     *
     * @param currency - объект адресата, который надо изменить
     * @return true, если пользователь кликнул OK, в противном случае false.
     */
    public boolean showCurrencyEditDialog(Currency currency) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(Main.dir, "currencies/edit/CurrenciesEditView.fxml").toURI().toURL();
            loader.setLocation(location);
            GridPane page = (GridPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Создать/изменить запись");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(currenciesTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            CurrenciesEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCur(currency);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}

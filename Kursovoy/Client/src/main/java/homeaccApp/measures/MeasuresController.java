package homeaccApp.measures;

import homeaccApp.Main;
import homeaccApp.api.DAO.MeasureDAO;
import homeaccApp.api.LocalDateReciever;
import homeaccApp.measures.edit.MeasureEditController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by vellial on 26.09.16.
 */
public class MeasuresController {
    @FXML
    private ListView<Measures> measuresListView;
    @FXML
    private Button addMeasure;
    @FXML
    private Button deleteMeasure;

    private ObservableList<Measures> measures;

    @FXML
    public void initialize() {
        buildData();
    }

    private void buildData() {

        try {
            measures = MeasureDAO.selectAllMeasures();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        measuresListView.setItems(measures);

    }

    @FXML
    private void handleCreate() {
        Measures measure = new Measures();
        boolean okClicked = showMeasureEditDialog(measure);

        if (okClicked) {
            long date = LocalDateReciever.getDateOfNow();

            // Adding new bill to database.
            try {
                MeasureDAO.createMeasure(measure.getMeasureName());
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            measuresListView.getItems().add(measure);
        }
    }

    @FXML
    private void handleDelete() throws SQLException, ClassNotFoundException {
        int measureId = measuresListView.getSelectionModel().getSelectedItem().getMeasureId();
        int selectedIndex = measuresListView.getSelectionModel().getSelectedIndex();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Подтвердите действие");
        alert.setContentText("Вы действительно хотите удалить запись?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            MeasureDAO.deleteMeasure(measureId);
            measuresListView.getItems().remove(selectedIndex);
        }
    }

    /**
     * Открывает диалоговое окно для изменения деталей указанной записи.
     * Если пользователь кликнул OK, то изменения сохраняются в предоставленном
     * объекте адресата и возвращается значение true.
     *
     * @return true, если пользователь кликнул OK, в противном случае false.
     */
    public boolean showMeasureEditDialog(Measures measure) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(Main.dir, "measures/edit/MeasureEditView.fxml").toURI().toURL();
            loader.setLocation(location);
            AnchorPane page = (AnchorPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Создать запись");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            MeasureEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMeasure(measure);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}

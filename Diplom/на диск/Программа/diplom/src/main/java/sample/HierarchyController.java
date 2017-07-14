package sample;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import oracle.jdbc.OracleConnection;
import oracle.olapi.data.source.DataProvider;
import oracle.olapi.metadata.mdm.MdmDatabaseSchema;
import oracle.olapi.metadata.mdm.MdmMetadataProvider;
import oracle.olapi.metadata.mdm.MdmRootSchema;
import oracle.olapi.session.UserSession;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Vellial on 21.01.2017.
 */
public class HierarchyController {
    @FXML
    private TextField hierName;
    @FXML
    private TextField awName;
    @FXML
    private TreeView hierColumns;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnSkip;
    private Stage dialogStage;
    private String tableName;

    @FXML
    private void initialize() {

    }

    @FXML
    private void handleAdd() {
        String hierNameS = hierName.getText();
        String awNameS = awName.getText();
        if (!hierNameS.equals("")) {

            OracleConnection conn = Main.connectOracle();
            DataProvider dp = new DataProvider();
            try {
                UserSession session = dp.createSession(conn);

                MdmMetadataProvider mp = null;

                mp = (MdmMetadataProvider) dp.getDefaultMetadataProvider();

                MdmRootSchema mdmRootSchema = (MdmRootSchema) mp.getRootSchema();
                MdmDatabaseSchema mdmDBSchema = mdmRootSchema.getDatabaseSchema(Main.schema.toUpperCase());

                ArrayList<String> dimLevels = new ArrayList<>();
                ObservableList<TreeItem<String>> cols = hierColumns.getSelectionModel().getSelectedItems();
                for (TreeItem<String> col : cols) {
                    dimLevels.add(col.getValue());
                }

                Map<String, ArrayList<String>> hierLevels = new HashMap<>();
                hierLevels.put(hierNameS, dimLevels);
                Main.dimHierAndLevels.put(tableName, hierLevels);
                Main.createAW(mdmDBSchema, mp, dp, awNameS.toUpperCase(), tableName, hierNameS, dimLevels);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            MainApi.alertError("Fill hierarchy name");
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Подтвердите действие");
        alert.setContentText("Создать ещё одну таблицу измерений?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            showDimensionsDialog();
        }

        dialogStage.close();
    }

    @FXML
    private void handleSkip() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Confirm act");
        alert.setContentText("Create another dimension table?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            showDimensionsDialog();
        }

        dialogStage.close();
    }

    public void setHierColumns(ObservableList<TreeItem<String>> fields, boolean isDate) {
        TreeItem<String> root = new TreeItem<>("Available fields");
        if (!isDate) {
            root.getChildren().addAll(fields);
        } else {
            root.getChildren().addAll(Main.calendar());
        }
        hierColumns.setRoot(root);
        hierColumns.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void showDimensionsDialog() {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            String fxmlFile = "/fxml/MainWindow.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Добавить таблицу измерений");
            dialogStage.initModality(Modality.WINDOW_MODAL);
//            dialogStage.initOwner(mainMenu.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Передаём dialogStage в контроллер.
            Controller controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package sample;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import oracle.jdbc.OracleConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vellial on 21.01.2017.
 */
public class DateController {
    @FXML
    private TreeView<String> ttree;
    @FXML
    private TextField txtDimName;
    @FXML
    private Button next;
    @FXML
    private Button cancel;

    private Stage primaryStage;
    private Stage dialogStage;
    private String fieldName;
    private String mysqlTable;
    private String tableName;

    public DateController() {}

    @FXML
    private void initialize() {
        TreeItem rootItem = new TreeItem ("Поля для измерения");
        ObservableList<String> tables = Main.getTableNames();

        ArrayList<TreeItem> tablesAndFields = Main.calendar();
        rootItem.getChildren().addAll(tablesAndFields);

        rootItem.setExpanded(true);

        ttree.setRoot(rootItem);
        ttree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Listener for Next Button
     */
    @FXML
    private void handleNext() {
        ObservableList<TreeItem<String>> fields = ttree.getSelectionModel().getSelectedItems();
        Map<String, Map<Integer, Integer>> fieldsAndTypes = new HashMap<>();
        ArrayList<String> fieldsList = new ArrayList<>();
        for (TreeItem<String> field : fields) {
            Map<Integer, Integer> fieldType = new HashMap<>();
            fieldType.put(1, 0);
            fieldsAndTypes.put(field.getValue() + "ID", fieldType);
            fieldsAndTypes.put(field.getValue(), fieldType);
            fieldsList.add(field.getValue());
        }
        Main.createTable(fieldsAndTypes, tableName);
        // перенести данные из mysql в таблицу "время"
        getDateDataFromMySQLtoOracle(tableName, fieldsList);
        showHierarchyDialog(fields, true);
    }

    /**
     * Алгоритм: для каждого поля нужно создать поле типа <field<ID>>
     *     оно будет содержать ID записи из MySQL
     * @param table
     * @param fields
     */
    private void getDateDataFromMySQLtoOracle(String table, ArrayList<String> fields) {
        Connection conn = Main.connect();
        OracleConnection connOracle = Main.connectOracle();
        for (String field : fields) {
            // построение запроса для вставки данных в Oracle DB


            PreparedStatement statement = null;
            try {
                // получение полного правильного названия колонки с первичным ключом из MySQL
                DatabaseMetaData md = conn.getMetaData();
                ResultSet rsPrimKeys = md.getPrimaryKeys(null, null, mysqlTable);
                String primKeyName = "";
                while (rsPrimKeys.next()) {
                    primKeyName = rsPrimKeys.getString("COLUMN_NAME");
                }

                // построение запроса для получения значений из MySQL
                String query = "SELECT " + fieldName + ", " + primKeyName + " FROM " + mysqlTable;
                Date value;
                // выполнение запроса к MySQL
                statement = conn.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    // получаем значение из результата
                    value = resultSet.getDate(fieldName);
                    // получаем ключ записи
                    int key = resultSet.getInt(primKeyName);
                    // в зависимости от переданного значения получить нужное число
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(value);
                    int dateValue = 0;
                    if (field.toUpperCase().equals("MONTH")) {
                        dateValue = cal.get(Calendar.MONTH);
                    } else if (field.toUpperCase().equals("DAY")) {
                        dateValue = cal.get(Calendar.DAY_OF_MONTH);
                    } else if (field.toUpperCase().equals("YEAR")) {
                        dateValue = cal.get(Calendar.YEAR);
                    }

                    if (dateValue != 0) {
                        String values = Integer.toString(dateValue) + "," + key + ")";
                        // добавляем его к запросу на вставку данных
                        String insQuery = "INSERT INTO " + table + "(" + field + ", " + field + "ID" + ")" + " VALUES (" + values;
                        // выполнение запроса на вставку данных в Oracle DB
                        statement = connOracle.prepareStatement(insQuery);
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void showHierarchyDialog(ObservableList<TreeItem<String>> fields, boolean isDate) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            String fxmlFile = "/fxml/Hierarchy.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Добавить иерархию");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(this.dialogStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Передаём dialogStage в контроллер.
            HierarchyController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setHierColumns(fields, isDate);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener for Cancel Button
     */
    @FXML
    private void handleCancel() {
        primaryStage.close();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setFieldName(String value) {
        this.fieldName = value;
    }

    public void setMysqlTableName(String mysqltable) {
        this.mysqlTable = mysqltable;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}

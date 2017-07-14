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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    @FXML
    private MenuBar mainMenu;
    @FXML
    private MenuItem miSettings;
    @FXML
    private TreeView<String> ttree;
    @FXML
    private TextField txtDimName;
    @FXML
    private Button btnCreateAll;
    @FXML
    private Button next;
    @FXML
    private Button cancel;

    private Stage primaryStage;

    public Controller() {}

    @FXML
    private void initialize() {
        if (Main.isConnect()) {

            TreeItem rootItem = new TreeItem("Tables and fields from MySQL");
            ObservableList<String> tables = Main.getTableNames();

            System.out.println(tables);
            ArrayList<TreeItem> tablesAndFields = getTablesAndFields(tables);
            rootItem.getChildren().addAll(tablesAndFields);

            rootItem.setExpanded(true);

            ttree.setRoot(rootItem);
            ttree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            miSettings.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent t) {
                    showSettingsDialog();
                }
            });
        } else {
            showSettingsDialog();
        }
    }

    /**
     * Listener for Next Button
     */
    @FXML
    private void handleNext() {
        String tableName = txtDimName.getText();
        ObservableList<TreeItem<String>> fields = ttree.getSelectionModel().getSelectedItems();
        createTablesAndHierarchyDialog(tableName, fields);
    }

    /**
     * Listener for Cancel Button
     */
    @FXML
    private void handleCancel() {
        Stage st = (Stage) this.mainMenu.getScene().getWindow();
        st.close();
    }

    /**
     * Listener for Ok Button
     */
    @FXML
    private void handleCreateAll() {
        Map<String, String> tables = MainApi.getDimTables();
        if (tables.size() != 0) {
            showFactDialog();
        } else {
            MainApi.alertError("Create dimension table");
        }
    }

    public void showFactDialog() {
        try {
            String fxmlFile = "/fxml/Fact.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add table fact");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainMenu.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            FactController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTablesAndHierarchyDialog(String tableName, ObservableList<TreeItem<String>> fields) {
        if (!tableName.equals("")) {
            ArrayList<TreeItem<String>> parents = new ArrayList<TreeItem<String>>();
            Map<String, ArrayList<String>> tablesAndfieldNames = new HashMap<>();
            for (TreeItem<String> field : fields) {
                TreeItem<String> parent = field.getParent();
                if (parents.contains(parent)) {
                    continue;
                }
                parents.add(parent);
            }

            Map<String, Map<Integer, Integer>> fieldsAndTypes = new HashMap<>();

            for (TreeItem<String> field : fields) {
                Map<Integer, Integer> fieldType = Main.getFieldType(field.getValue(), field.getParent().getValue());

                if (fieldType.containsKey(3)) {
                    showDateDimentionDialog(field.getValue(), field.getParent().getValue(), tableName);
                    break;
                }
                fieldsAndTypes.put(field.getValue(), fieldType);
            }

            ArrayList<String> fieldNames = null;
            for (TreeItem<String> parent : parents) {
                fieldNames = new ArrayList<>();
                for (TreeItem<String> field : fields) {
                    if (parent.getValue().equals(field.getParent().getValue())) {
                        fieldNames.add(field.getValue());
                    }
                }
                tablesAndfieldNames.put(parent.getValue(), fieldNames);
            }

            Main.createTable(fieldsAndTypes, tableName);

            for (TreeItem<String> parent : parents) {
                String mysqlTable = parent.getValue();
                Map<String, List<String>> t = issetFK(mysqlTable);
                if (t.size() > 0) {
                    insertOracleFromMysql(tableName, mysqlTable, tablesAndfieldNames, t);
                } else {
                    insertOracleFromMysql(tableName, tablesAndfieldNames);
                }
            }

            showHierarchyDialog(fields, false, tableName);
        } else {
            MainApi.alertError("Fill the name of table");
        }
    }

    private void insertOracleFromMysql(String table, Map<String, ArrayList<String>> tablesAndFields) {
        Connection conn = Main.connect();
        OracleConnection connOracle = Main.connectOracle();

        String fromTables = "";
        String selectFields = "";
        String insertFields = "";

        tablesAndFields.size();
        int t = 0;
        for (Map.Entry entry : tablesAndFields.entrySet()) {
            String table1 = (String) entry.getKey();
            ArrayList<String> fields1 = (ArrayList) entry.getValue();
            if (t != tablesAndFields.size()) {
                fromTables += table1 + ", ";
                int j = 0;
                for (String field : fields1) {
                    if (j != (fields1.size() - 1)) {
                        selectFields += table1 + "." + field + ", ";
                        insertFields += field + ", ";
                    } else {
                        selectFields += table1 + "." + field;
                        insertFields += field;
                    }
                    j++;
                }
            } else {
                fromTables += table1;
                int j = 0;
                for (String field : fields1) {
                    if (j != (fields1.size() - 1)) {
                        selectFields += table1 + "." + field + ", ";
                        insertFields += field + ", ";
                    } else {
                        selectFields += table1 + "." + field;
                        insertFields += field;
                    }
                    j++;
                }
            }
            t++;
        }

        String query = "SELECT " + selectFields + " FROM " + fromTables;
        System.out.println(query);

        PreparedStatement statement = null;
        try {
            String value = "";
            statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String insQuery = "INSERT INTO " + table + "(" + insertFields + ") VALUES (";
                ResultSetMetaData ttt = resultSet.getMetaData();
                int zCount = ttt.getColumnCount();
                for (int z = 1; z < zCount + 1; z++) {
                    String colName = ttt.getColumnName(z);
                    value = resultSet.getString(colName);
                    if (zCount != z) {
                        insQuery += "'" + value + "', ";
                    } else {
                        insQuery += "'" + value + "')";
                    }
                }
                System.out.println(insQuery);

                PreparedStatement statement2 = connOracle.prepareStatement(insQuery);
                statement2.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertOracleFromMysql(String table, String mysqlTable,  Map<String, ArrayList<String>> tablesAndFields, Map<String, List<String>> fk) {
        Connection conn = Main.connect();
        OracleConnection connOracle = Main.connectOracle();

        String selectFields = "";
        String insertFields = "";

        for (Map.Entry entry : fk.entrySet()) {
            String fkfield = (String) entry.getKey();
            List<String> tableAndField = (List<String>) entry.getValue();
            String fktable = tableAndField.get(0);

            int j = 0;
            for (Map.Entry entry1 : tablesAndFields.entrySet()) {
                j++;
                String table1 = (String) entry1.getKey();
                ArrayList<String> fields1 = (ArrayList) entry1.getValue();
                int fs = 0;
                for (String field : fields1) {
                    fs++;
                    if ((j == tablesAndFields.size()) && (fs == fields1.size())) {
                        selectFields += table1 + "." + field;
                        insertFields += field;
                    } else {
                        selectFields += table1 + "." + field + ", ";
                        insertFields += field + ", ";
                    }
                }
            }

            // get data from mysql
            String query = "SELECT " + selectFields + " FROM " + mysqlTable + " LEFT JOIN " + fktable + " ON " + fktable + "."+ fkfield + " = " + mysqlTable + "." + fkfield;
            System.out.println(query);

            PreparedStatement statement = null;
            try {
                String value = "";
                statement = conn.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String insQuery = "INSERT INTO " + table + "(" + insertFields + ", " + fkfield + ") VALUES (";
                    ResultSetMetaData t = resultSet.getMetaData();
                    int zCount = t.getColumnCount();
                    for (int z = 1; z < zCount + 1; z++) {
                        String colName = t.getColumnName(z);
                        value = resultSet.getString(colName);
                        if (zCount != z) {
                            insQuery += "'" + value + "', ";
                        } else {
                            insQuery += "'" + value + "')";
                        }
                    }
                    System.out.println(insQuery);

                    PreparedStatement statement2 = connOracle.prepareStatement(insQuery);
                    statement2.executeUpdate();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Map issetFK (String mysqlTable) {
        Connection conn = Main.connect();
        Map<String, List<String>> fkMap = new HashMap<String, List<String>>();
        try {
            DatabaseMetaData dbmd = conn.getMetaData();

            ResultSet ffkeys = dbmd.getImportedKeys(conn.getCatalog(), null, mysqlTable);
            while (ffkeys.next()) {
                List<String> fkValues = new ArrayList<String>();
                fkValues.add(ffkeys.getString("PKTABLE_NAME"));
                fkValues.add(ffkeys.getString("FKCOLUMN_NAME"));

                fkMap.put(ffkeys.getString("FKCOLUMN_NAME"), fkValues);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fkMap;
    }

    public void showDateDimentionDialog(String value, String mysqltable, String tableName) {
        try {
            String fxmlFile = "/fxml/Date.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = null;
            Stage dialogStage = new Stage();

            root = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));
            dialogStage.setTitle("Create date dimension");
            dialogStage.setScene(new Scene(root));

            DateController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setFieldName(value);
            controller.setMysqlTableName(mysqltable);
            controller.setTableName(tableName);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This method creates an ArrayList of TreeItems
    public ArrayList<TreeItem> getTablesAndFields(ObservableList<String> tables) {
        ArrayList<TreeItem> iTables = new ArrayList<TreeItem>();

        for (String table : tables) {
            TreeItem iTable = new TreeItem(table);
            iTable.getChildren().addAll(Main.getFieldNames(table));
            iTables.add(iTable);
        }

        return iTables;
    }

    public boolean showSettingsDialog() {
        try {
            String fxmlFile = "/fxml/Settings.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Settings");
            dialogStage.initModality(Modality.WINDOW_MODAL);
//            dialogStage.initOwner(mainMenu.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            SettingsController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void showHierarchyDialog(ObservableList<TreeItem<String>> fields, boolean isDate, String tableName) {
        try {
            // ????????? fxml-???? ? ?????? ????? ?????
            // ??? ???????????? ??????????? ????.
            String fxmlFile = "/fxml/Hierarchy.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

            // ?????? ?????????? ???? Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("???????? ????????");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainMenu.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // ??????? dialogStage ? ??????????.
            HierarchyController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setHierColumns(fields, isDate);
            controller.setTableName(tableName);

            // ?????????? ?????????? ???? ? ???, ???? ???????????? ??? ?? ???????
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDialogStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}

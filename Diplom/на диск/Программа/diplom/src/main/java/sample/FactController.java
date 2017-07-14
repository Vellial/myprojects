package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import oracle.jdbc.OracleConnection;
import oracle.olapi.data.source.DataProvider;
import oracle.olapi.metadata.deployment.AW;
import oracle.olapi.metadata.mdm.MdmDatabaseSchema;
import oracle.olapi.metadata.mdm.MdmMetadataProvider;
import oracle.olapi.metadata.mdm.MdmRootSchema;
import oracle.olapi.session.UserSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vellial on 21.01.2017.
 */
public class FactController {
    @FXML
    private TextField factName;
    @FXML
    private TreeView factColumns;
    @FXML
    private Button btnOk;
    private Stage dialogStage;

    @FXML
    private void initialize() {
        TreeItem rootItem = new TreeItem ("Таблицы и поля базы данных");
        ObservableList<String> tables = Main.getTableNames();

        ArrayList<TreeItem> tablesAndFields = getTablesAndFields(tables);
        rootItem.getChildren().addAll(tablesAndFields);

        rootItem.setExpanded(true);

        factColumns.setRoot(rootItem);
        factColumns.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    // This method creates an ArrayList of TreeItems
    public ArrayList<TreeItem> getTablesAndFields(ObservableList<String> tables) {
        ArrayList<TreeItem> iTables = new ArrayList<TreeItem>();

        for (String table : tables) {
            TreeItem iTable = new TreeItem(table);
            iTable.getChildren().addAll(Main.getFactFieldNames(table));
            iTables.add(iTable);
        }

        return iTables;
    }

    @FXML
    private void handleOk() {
        String tFactName = factName.getText();
        ObservableList<TreeItem<String>> fields = factColumns.getSelectionModel().getSelectedItems();
        ArrayList<String> fieldsNames = new ArrayList<>();
        if (!tFactName.equals("")) {
            // получение выбранных колонок
            Map<String, String> fieldTypes = new HashMap<>();
            for (TreeItem<String> field : fields) {
                fieldTypes = getFactFieldTypes(field.getValue(), field.getParent().getValue());
                fieldsNames.add(field.getValue());
            }
            // получение уже имеющихся таблиц измерений и полей для создания уникального составного ключа
            Map<String, String> primaryKeysWithType = getExistingPrimaryKeys();
            Main.createFactTable(fieldTypes, tFactName + "_FACT", primaryKeysWithType);
        }

        // create cube
        OracleConnection conn = Main.connectOracle();
        DataProvider dp = new DataProvider();
        try {
            UserSession session = dp.createSession(conn);

            MdmMetadataProvider mp = null;

            mp = (MdmMetadataProvider) dp.getDefaultMetadataProvider();

            MdmRootSchema mdmRootSchema = (MdmRootSchema) mp.getRootSchema();
            MdmDatabaseSchema mdmDBSchema = mdmRootSchema.getDatabaseSchema(Main.schema.toUpperCase());

            List dimList2 = mdmDBSchema.getDimensions();

            // имя измерения, имя иерархии, уровни
            MainApi.createAndMapCube(dimList2, mp, dp, mdmDBSchema, Main.existAW, tFactName, fieldsNames);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, String> getFactFieldTypes(String fieldName, String table) {
        Connection conn = Main.connect();
        String query = "SELECT " + fieldName + " FROM " + table;
        Map<String, String> fieldsAndTypes = new HashMap<>();
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet resultSet = ps.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount(); //count of columns
            String columnName[] = new String[count];
            for (int i = 1; i <= count; i++) {
                String col = metaData.getColumnLabel(i);
                columnName[i-1] = col;
                if (!metaData.getColumnTypeName(i).equals("VARCHAR")){
                    fieldsAndTypes.put(col, metaData.getColumnTypeName(i));
                } else {
                    MainApi.alertError("Выберите поле числового типа");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fieldsAndTypes;
    }

    /**
     *
     * @return Map<String, String> colsAndType - Columns and Types
     */
    public static Map<String, String> getExistingPrimaryKeys() {
        // get all table names
        OracleConnection connection = (OracleConnection) Main.connectOracle();
        DatabaseMetaData md = null;
        ObservableList<String> tables = FXCollections.observableArrayList();
        Map<String, String> colsAndType = new HashMap<>();
        try {
            md = connection.getMetaData();
            ResultSet rs = md.getTables(null, connection.getCurrentSchema(), "%", null);
            while (rs.next()) {
                tables.add(rs.getString(3));
            }

            if (tables.size() > 0) {
                for (String table : tables) {
                    ResultSet resultSet = md.getPrimaryKeys(null, connection.getCurrentSchema(), table);
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int count = metaData.getColumnCount(); //count of columns

                    for (int i = 1; i <= count; i++) {
                        while (resultSet.next()) {
                            String columnName = resultSet.getString("COLUMN_NAME");
                            colsAndType.put(columnName, "NUMBER");
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return colsAndType;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}

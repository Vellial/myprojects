package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
import oracle.olapi.data.source.DataProvider;
import oracle.olapi.metadata.deployment.AW;
import oracle.olapi.metadata.deployment.AWPrimaryDimensionOrganization;
import oracle.olapi.metadata.mdm.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Main extends Application {
    //Settings
    public static String mysqlServer;
    public static String mysqlPort;
    public static String mysqlDb;
    public static String mysqlUser;
    public static String mysqlPass;
    public static String oraServer;
    public static String oraPort;
    public static String oraDb;
    public static String oraUser;
    public static String oraPass;

    public static String schema;
    public static Map<Integer,String> types = new HashMap<Integer, String>();
    public static AW existAW;
    public static Map<String, Map<String, ArrayList<String>>> dimHierAndLevels = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        types.put(1, "number");
        types.put(2, "varchar2");
        types.put(3, "date");
        types.put(4, "float");

        if (mysqlDb == null || mysqlPort == null || mysqlServer == null || mysqlUser == null || mysqlPass == null) {
            showSettingsDialog();
        }

        String fxmlFile = "/fxml/MainWindow.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent root = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

        primaryStage.setTitle("Integration");
        primaryStage.setScene(new Scene(root));

        Controller controller = loader.getController();
        controller.setDialogStage(primaryStage);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static ArrayList calendar() {
        ArrayList<TreeItem<String>> calendar = new ArrayList<TreeItem<String>>();

        TreeItem<String> day = new TreeItem<>("Day");
        calendar.add(day);

        TreeItem<String> month = new TreeItem<>("Month");
        calendar.add(month);

        TreeItem<String> year = new TreeItem<>("Year");
        calendar.add(year);

        return calendar;
    }

    public static void createAW(MdmDatabaseSchema mdmDBSchema, MdmMetadataProvider mp, DataProvider dp, String awName, String dimName, String hierarchyName, ArrayList<String> dimLevels) {
        // create analytic workspace
        AW aw = mdmDBSchema.findOrCreateAW(awName); // schema name = username from db.
        MdmStandardDimension mdmChanDim = mdmDBSchema.findOrCreateStandardDimension(dimName); // name of new dimension
        AWPrimaryDimensionOrganization awChanDimOrg = mdmChanDim.createAWOrganization(aw, true);

        existAW = aw;

        // create dimentions and hierarchies
        MainApi.createAndMapDimensionLevels(mdmChanDim, mp, dimLevels, mdmDBSchema.getName());
        MainApi.createAndMapHierarchies(mdmChanDim, mp, hierarchyName);
        MainApi.commit(mdmChanDim, dp);
    }

    public static Connection connect(){
        Connection connection = null;
//        if (mysqlDb == null || mysqlPort == null || mysqlServer == null || mysqlUser == null || mysqlPass == null) {
//            showSettingsDialog();
//        } else {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                System.out.println("Driver loaded!");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot find the driver in the classpath!", e);
            }

           String s = SettingsController.settings.getMysqlDb();
            // get properties
//        String url = "jdbc:mysql://localhost:3306/mydb";
            String url = "jdbc:mysql://" + mysqlServer + ":" + mysqlPort + "/" + mysqlDb;
//        String username = "root";
//        String password = "root";

            System.out.println("Connecting database...");

            try {
//            connection = DriverManager.getConnection(url, username, password);
                connection = DriverManager.getConnection(url, mysqlUser, mysqlPass);
            } catch (SQLException e) {
                e.printStackTrace();
            }
//        }
        return connection;
    }

    public static OracleConnection connectOracle() {
        OracleConnection conn = null;
        try {
            Properties props = new Properties();
//////            props.setProperty("url", "jdbc:oracle:thin:@localhost:1521:dbtest");
//            props.setProperty("user", "c##test");
//            props.setProperty("password", "123");
            props.setProperty("user", oraUser);
            props.setProperty("password", oraPass);

            // get properties
            OracleDataSource ods = new OracleDataSource();
//            ods.setURL("jdbc:oracle:thin:@localhost:1521:dbtest");
            ods.setURL("jdbc:oracle:thin:@" + oraServer + ":" + oraPort + ":" + oraDb);
            ods.setUser(props.getProperty("user"));
            ods.setPassword(props.getProperty("password"));

            conn = (OracleConnection) ods.getConnection();
            schema = conn.getCurrentSchema();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static ObservableList<String> getTableNames() {
        // get all table names
        Connection connection = connect();
        DatabaseMetaData md = null;
        ObservableList<String> tables = FXCollections.observableArrayList();
        try {
            md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                tables.add(rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public static ArrayList<TreeItem> getFieldNames(String table) {
        Connection connection = connect();
        String query = "SELECT * FROM " + table;
        ArrayList<TreeItem> iFields = new ArrayList<TreeItem>();

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);

            ResultSet resultSet = ps.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount(); //count of columns
            for (int i = 1; i <= count; i++) {
                String col = null;
                try {
                    col = metaData.getColumnLabel(i);
                    TreeItem iTable = new TreeItem(col);
                    iFields.add(iTable);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return iFields;
    }

    public static ArrayList<TreeItem> getFactFieldNames(String table) {
        Connection connection = connect();
        String query = "SELECT * FROM " + table;
        ArrayList<TreeItem> iFields = new ArrayList<TreeItem>();

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);

            ResultSet resultSet = ps.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount(); //count of columns
            for (int i = 1; i <= count; i++) {
                String col = null;
                try {
                    col = metaData.getColumnLabel(i);
                    if (col.toLowerCase().contains("name") || metaData.isAutoIncrement(i)) {
                        continue;
                    }
                    TreeItem iTable = new TreeItem(col);
                    iFields.add(iTable);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return iFields;
    }

    public static Map<Integer, Integer> getFieldType(String fieldName, String table) {
        Connection conn = connect();
        String query = "SELECT " + fieldName + " FROM " + table;
        Map<Integer, Integer> fieldsAndTypes = new HashMap<>();
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet resultSet = ps.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount(); //count of columns
            String columnName[] = new String[count];
            for (int i = 1; i <= count; i++) {
                String col = metaData.getColumnLabel(i);
                columnName[i-1] = col;
                if (metaData.getColumnTypeName(i).equals("INT")){
                    fieldsAndTypes.put(1, 0);
                } else if (metaData.getColumnTypeName(i).equals("VARCHAR")) {
                    fieldsAndTypes.put(2, metaData.getColumnDisplaySize(i));
                } else if (metaData.getColumnTypeName(i).equals("DATE")) {
                    fieldsAndTypes.put(3, 0);
                } else if (metaData.getColumnTypeName(i).equals("DOUBLE")) {
                    fieldsAndTypes.put(4, 0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fieldsAndTypes;
    }

    public static void createTable(Map<String, Map<Integer, Integer>> fieldsAndTypes, String tableName) {
        Connection conn = connectOracle();
        String fieldsTypes = "";
        int size = fieldsAndTypes.size();
        int i = 0;
        for (Map.Entry entry : fieldsAndTypes.entrySet()) {
            i++;
            String field = (String) entry.getKey();
            Map<Integer, Integer> typeLength = (Map<Integer, Integer>) entry.getValue();

            Set keys = typeLength.keySet();
            Integer type = 0;
            for (Object key: keys) {
                type = (Integer) key;
            }

            String fieldType = types.get(type);
            String fieldLength = "";
            if (type == 2) {
                int length = typeLength.get(type);
                fieldLength = "(" + length + ")";
            }
            if (i == size) {
                fieldsTypes += field + " " + fieldType + fieldLength;
            }
            else {
                fieldsTypes += field + " " + fieldType + fieldLength + ", ";
            }
        }

        if (!fieldsTypes.equals("")) {
            String autoinc = "GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1)";
            String tableKey = tableName + "Key number " + autoinc + ", ";
            String tableCreateQuery = "CREATE TABLE " + tableName + "(" + tableKey + fieldsTypes + ")";

            String alt = "ALTER TABLE " + tableName + " ADD PRIMARY KEY (" + tableName + "Key)";
            System.out.println(tableCreateQuery);
            Statement statement = null;
            try {
                statement = conn.createStatement();
                statement.execute(tableCreateQuery);

                System.out.println("Table Created Successfully");

                PreparedStatement ps = conn.prepareStatement(alt);
                ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createFactTable(Map<String, String> fieldsAndTypes, String tFactName, Map<String, String> primaryKeysWithType) {
        Connection conn = connectOracle();
        String fieldsTypes = "";
        int j = 0;
        int size2 = primaryKeysWithType.size();
        for (Map.Entry entry : fieldsAndTypes.entrySet()) {
            String field = (String) entry.getKey();

            String fieldType = (String) entry.getValue();
            fieldsTypes += field + " " + fieldType + ", ";
        }

        for (Map.Entry entry : primaryKeysWithType.entrySet()) {
            j++;
            String field = (String) entry.getKey();

            String fieldType = (String) entry.getValue();
            if (j == size2) {
                fieldsTypes += field + " " + fieldType;
            }
            else {
                fieldsTypes += field + " " + fieldType + ", ";
            }
        }

        if (!fieldsTypes.equals("")) {
            String tableCreateQuery = "CREATE TABLE " + tFactName + "(" + fieldsTypes + ")";
            System.out.println(tableCreateQuery);
            Statement statement = null;
            try {
                statement = conn.createStatement();
                statement.execute(tableCreateQuery);

                System.out.println("Table Created Successfully");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isConnect() {
        boolean isconnext = false;
        Connection conn = connect();
        OracleConnection connOr = connectOracle();

        if (conn != null && connOr != null) {
            isconnext = true;
        }

        return isconnext;
    }

    private boolean showSettingsDialog() {
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
}
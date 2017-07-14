package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import oracle.jdbc.OracleConnection;
import oracle.olapi.data.source.DataProvider;
import oracle.olapi.metadata.deployment.AW;
import oracle.olapi.metadata.deployment.AWCubeOrganization;
import oracle.olapi.metadata.mapping.*;
import oracle.olapi.metadata.mdm.*;
import oracle.olapi.syntax.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vellial on 08.01.2017.
 */
public class MainApi {
    private static ArrayList<MdmDimensionLevel> dimLevelList = new ArrayList();
    private static ArrayList<String> dimLevelNames = new ArrayList();
    private static ArrayList<String> keyColumns = new ArrayList();

    public static void createAndMapDimensionLevels(MdmPrimaryDimension mdmChanDim, MdmMetadataProvider metadataProvider, ArrayList<String> dimLevels, String schemaName) {
        Map<String, String> tablesAndKeys = getDimPrimaryKeys();

        // get dimension level names
        for (String dimLevel : dimLevels) {
            dimLevelNames.add(dimLevel.toUpperCase());
        }

        // get dimension keys
        for (Map.Entry entry : tablesAndKeys.entrySet()) {
            String key = schemaName + "." + entry.getKey() + "." + entry.getValue();
            keyColumns.add(key.toUpperCase());
        }

        // Create the MdmDimensionLevel and MemberListMap objects.
        int i = 0;
        for (String dimLevelName : dimLevelNames) {
            MdmDimensionLevel mdmDimLevel = mdmChanDim.findOrCreateDimensionLevel(dimLevelNames.get(i));
            dimLevelList.add(mdmDimLevel);

            // Create a MemberListMap for the dimension level.
            MemberListMap mdmDimLevelMemListMap = mdmDimLevel.findOrCreateMemberListMap();
            ColumnExpression keyColExp = (ColumnExpression) SyntaxObject.fromSyntax(keyColumns.get(i), metadataProvider);
            mdmDimLevelMemListMap.setKeyExpression(keyColExp);
            mdmDimLevelMemListMap.setQuery(keyColExp.getQuery());
            i++;
        }
    }

    public static void createAndMapHierarchies(MdmPrimaryDimension mdmChanDim, MdmMetadataProvider metadataProvider, String hierarchyName) {
        MdmLevelHierarchy mdmLevelHier = mdmChanDim.findOrCreateLevelHierarchy(hierarchyName.toUpperCase());

        // Create the MdmHierarchyLevel and HierarchyLevelMap objects.
        int i = 0;
        for(String hierLevelName : dimLevelNames) {
            MdmHierarchyLevel mdmHierLevel = mdmLevelHier.findOrCreateHierarchyLevel(mdmLevelHier.getPrimaryDimension()
                    .findOrCreateDimensionLevel(hierLevelName));
            HierarchyLevelMap hierLevelMap = mdmHierLevel.findOrCreateHierarchyLevelMap();
            ColumnExpression keyColExp = (ColumnExpression) SyntaxObject.fromSyntax(keyColumns.get(i), metadataProvider);
            hierLevelMap.setKeyExpression(keyColExp);
            hierLevelMap.setQuery(keyColExp.getQuery());

            //Set the MdmDimensionLevel for the MdmHierarchyLevel.
            mdmHierLevel.setDimensionLevel(dimLevelList.get(i));
            i++;
        }
    }

    public static MdmCube createAndMapCube(List mdmDimentions, MdmMetadataProvider metadataProvider, DataProvider dp, MdmDatabaseSchema mdmDBSchema, AW aw, String cubeName, ArrayList<String> measureNames) {
        MdmCube mdmCube = mdmDBSchema.findOrCreateCube(cubeName.toUpperCase());
        // Add dimensions to the cube.
        for (Object dim : mdmDimentions) {
            mdmCube.addDimension((MdmDimension) dim);
        }

        AWCubeOrganization awCubeOrg = mdmCube.createAWOrganization(aw, true);
        awCubeOrg.setMVOption(AWCubeOrganization.NONE_MV_OPTION);
        awCubeOrg.setMeasureStorage(AWCubeOrganization.SHARED_MEASURE_STORAGE);
        awCubeOrg.setCubeStorageType("NUMBER");

        AggregationCommand aggCommand = new AggregationCommand("AVG");
        ArrayList<ConsistentSolveCommand> solveCommands = new ArrayList();
        solveCommands.add(aggCommand);
        ConsistentSolveSpecification conSolveSpec =
                new ConsistentSolveSpecification(solveCommands);
        mdmCube.setConsistentSolveSpecification(conSolveSpec);

        // Create and map the measures of the cube.
        createAndMapMeasures(mdmCube, metadataProvider, mdmDBSchema, measureNames, cubeName + "_FACT");
        // Commit the Transaction.
        commit(mdmCube, dp);
        return mdmCube;
    }

    private static void createAndMapMeasures(MdmCube mdmCube, MdmMetadataProvider metadataProvider, MdmDatabaseSchema mdmDBSchema, ArrayList<String> measureNames, String tableName) {
        ArrayList<MdmBaseMeasure> measures = new ArrayList();

        for (String measureName : measureNames) {
            MdmBaseMeasure mdmNewMeasure = mdmCube.findOrCreateBaseMeasure(measureName.toUpperCase());
            SQLDataType sdt = new SQLDataType("NUMBER");
            mdmNewMeasure.setSQLDataType(sdt);
            measures.add(mdmNewMeasure);
        }

        String schemaName = mdmDBSchema.getName();

        MdmTable mdmTable = (MdmTable) mdmDBSchema.getTopLevelObject(tableName.toUpperCase());
        Query cubeQuery = mdmTable.getQuery();
        ArrayList<String> measureColumns = new ArrayList();

        for (MdmBaseMeasure measure : measures) {
            String key = schemaName + "." + tableName.toUpperCase() + "." + measure.getName();
            measureColumns.add(key.toUpperCase());
        }

        CubeMap cubeMap = mdmCube.createCubeMap();
        cubeMap.setQuery(cubeQuery);

        // Create MeasureMap objects for the measures of the cube and
        // set the expressions for the measures. The expressions specify the
        // columns of the fact table for the measures.
        int i = 0;
        for(MdmBaseMeasure mdmBaseMeasure : measures) {
            MeasureMap measureMap = cubeMap.findOrCreateMeasureMap(mdmBaseMeasure);
            Expression expr = (Expression) SyntaxObject.fromSyntax(measureColumns.get(i), metadataProvider);
            measureMap.setExpression(expr);
            i++;
        }

        // Create CubeDimensionalityMap objects for the dimensions of the cube and
        // set the expressions for the dimensions. The expressions specify the
        // columns of the fact table for the dimensions.
        // соединить ключи измерений с ключами таблицы фактов
        ArrayList<String> fieldKeyNames = getFactFieldKeyNames(tableName);

        List<MdmDimensionality> mdmDimltys = mdmCube.getDimensionality();
        for (MdmDimensionality mdmDimlty : mdmDimltys) {
            CubeDimensionalityMap cubeDimMap = cubeMap.findOrCreateCubeDimensionalityMap(mdmDimlty);
            MdmPrimaryDimension mdmPrimDim = (MdmPrimaryDimension) mdmDimlty.getDimension();
            String columnMap = "";
            String dimName = mdmPrimDim.getName();
            for (String key : fieldKeyNames) {
                String newKey = mdmDBSchema.getName() + "." + tableName + "." + key;
                if (key.endsWith("KEY")) {
                    key = key.substring(0, key.length() - 3);
                    if (dimName.equals(key)) {
                        columnMap = newKey;
                    }
                }
            }

            if (!columnMap.equals("")) {
                Expression expr = (Expression) SyntaxObject.fromSyntax(columnMap, metadataProvider);
                cubeDimMap.setExpression(expr);

                // Associate the leaf level of the hierarchy with the cube.
                MdmHierarchy mdmDefHier = mdmPrimDim.getDefaultHierarchy();
                MdmLevelHierarchy mdmLevHier = (MdmLevelHierarchy) mdmDefHier;
                List<MdmHierarchyLevel> levHierList = mdmLevHier.getHierarchyLevels();
                // The last element in the list must be the leaf level of the hierarchy.
                MdmHierarchyLevel leafLevel = levHierList.get(levHierList.size() - 1);
                cubeDimMap.setMappedDimension(leafLevel);
            }
        }
    }

    /**
     * Возвращает поля с "KEY" в конце - они же ключи
     * @param tableName
     * @return
     */
    private static ArrayList<String> getFactFieldKeyNames(String tableName) {
        OracleConnection connection = Main.connectOracle();
        String query = "SELECT * FROM " + tableName;
        ArrayList<String> iFields = new ArrayList<String>();

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);

            ResultSet resultSet = ps.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount(); //count of columns
            for (int i = 1; i <= count; i++) {
                String col = metaData.getColumnLabel(i);
                if (col.endsWith("KEY")) {
                    iFields.add(col);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return iFields;
    }

    public static void commit(MdmSource mdmSource, DataProvider dp) {
        try {
            System.out.println("Committing the transaction for " + mdmSource.getName() + ".");
            (dp.getTransactionProvider()).commitCurrentTransaction();
        }
        catch (Exception ex) {
            System.out.println("Could not commit the Transaction. " + ex);
        }
    }

    public static void alertError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Поля заполнены неверно");
        alert.setHeaderText("Пожалуйста, проверьте и заполните поля правильно.");
        alert.setContentText(errorMessage);

        alert.showAndWait();
    }

    /**
     *
     * @return Map<String, String> tablesAndTypes - Tables and PrimaryKeys
     */
    public static Map<String, String> getDimPrimaryKeys() {
        // get all table names
        oracle.jdbc.driver.OracleConnection connection = (oracle.jdbc.driver.OracleConnection) Main.connectOracle();
        DatabaseMetaData md = null;
        ObservableList<String> tables = FXCollections.observableArrayList();
        Map<String, String> tablesAndTypes = new HashMap<>();

        try {
            md = connection.getMetaData();
            ResultSet rs = md.getTables(null, connection.getCurrentSchema(), "%", null);
            while (rs.next()) {
                tables.add(rs.getString(3));
            }

            if (tables.size() > 0) {
                for (String table : tables) {
                    ResultSet resultSet = md.getPrimaryKeys(null, connection.getCurrentSchema(), table);
                    while (resultSet.next()) {
                        String columnName = resultSet.getString("COLUMN_NAME");
                        tablesAndTypes.put(table, columnName);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tablesAndTypes;
    }

    /**
     *
     * @return Map<String, String> tablesAndTypes - Tables and PrimaryKeys
     */
    public static Map<String, String> getDimTables() {
        // get all table names
        oracle.jdbc.driver.OracleConnection connection = (oracle.jdbc.driver.OracleConnection) Main.connectOracle();
        DatabaseMetaData md = null;
        ObservableList<String> tables = FXCollections.observableArrayList();
        Map<String, String> tablesAndTypes = new HashMap<>();

        try {
            md = connection.getMetaData();
            ResultSet rs = md.getTables(null, connection.getCurrentSchema(), "%", null);
            while (rs.next()) {
                tables.add(rs.getString(3));
            }

            if (tables.size() > 0) {
                for (String table : tables) {
                    ResultSet resultSet = md.getPrimaryKeys(null, connection.getCurrentSchema(), table);
                    while (resultSet.next()) {
                        String columnName = resultSet.getString("COLUMN_NAME");
                        tablesAndTypes.put(table, columnName);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tablesAndTypes;
    }

    // testing speed
    public static void main(String[] args) {
        Connection conn = Main.connect();
        OracleConnection connOracle = Main.connectOracle();

        String query = "select sup.SupplierName, p.ProductName, s.ShippingCity, (po.ProductCount * p.ProductCost) AS ProdOrderCost, o.OrderDate From orders AS o " +
                "INNER join shipping AS s ON o.ShippingId = s.ShippingId " +
                "INNER join productorder AS po ON o.OrderId = po.OrderId " +
                "INNER join products AS p ON po.ProductId = p.ProductId " +
                "INNER join suppliers as sup on p.SupplierId = sup.SupplierId " +
                "WHERE YEAR(o.OrderDate) = 2014;";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);
            long start = System.currentTimeMillis();
            ResultSet rs = ps.executeQuery();
            long finish = System.currentTimeMillis();

            long timeConsumedMillis = finish - start;
            System.out.println("MYSQL time: " + timeConsumedMillis);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String queryor = "select countries.shippingcity, times.year, suppliers.productname, sales_fact.ordercost from sales_fact " +
                "INNER join times on sales_fact.timeskey = times.timeskey " +
                "INNER join suppliers on sales_fact.supplierskey = suppliers.supplierskey " +
                "INNER join countries on sales_fact.countrieskey = countries.countrieskey";

        System.out.println(queryor);
        PreparedStatement psor = null;
        try {
            psor = connOracle.prepareStatement(queryor);
            long start = System.currentTimeMillis();
            ResultSet rs = psor.executeQuery();
            long finish = System.currentTimeMillis();

            long timeConsumedMillis = finish - start;
            System.out.println("ORACLE time: " + timeConsumedMillis);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}

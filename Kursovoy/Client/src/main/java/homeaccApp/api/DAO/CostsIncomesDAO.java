package homeaccApp.api.DAO;

import homeaccApp.api.DBapi;
import homeaccApp.api.LocalDateReciever;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import homeaccApp.categories.Categories;
import homeaccApp.mainwindow.bills.Bills;
import homeaccApp.mainwindow.costsincomes.Costincomes;
import homeaccApp.measures.Measures;

import java.sql.*;
import java.time.LocalDate;

/**
 * CostIncomes data access.
 */
public class CostsIncomesDAO {
    // CostIncome = 1 if income, 0 if cost

    // RETRIEVE. CostIncome = 1 if income, 0 if cost
    public static ObservableList<Costincomes> selectCostsIncomesData(boolean costincome) throws SQLException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps;
        ObservableList<Costincomes> dm = FXCollections.observableArrayList();

        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT mt.Date, mt.Note, mt.Count, mt.Amount, b.BillId, b.BillName, m.MeasureId, m.MeasureName, c.CategoryId, c.CategoryName, mt.MoneyTurnId FROM MoneyTurn AS mt " +
                    "LEFT JOIN Bills AS b ON mt.BillId = b.BillId " +
                    "LEFT JOIN Categories c ON mt.CategoryId = c.CategoryId " +
                    "LEFT JOIN Measures m ON mt.MeasureId = m.MeasureId " +
                    "WHERE mt.CostIncome = ? AND mt.DeletedDate IS NULL");
            ps.setBoolean(1, costincome);
            rs = ps.executeQuery();

            while (rs.next()) {
                long datelong = rs.getLong("Date");
                String note = rs.getString("Note");
                Bills bill = new Bills(rs.getInt("BillId"), rs.getString("BillName"));
                int count = rs.getInt("Count");
                Categories category = new Categories(rs.getInt("CategoryId"), rs.getString("CategoryName"));
                Measures measure = new Measures(rs.getInt("MeasureId"), rs.getString("MeasureName"));
                double amount = rs.getDouble("Amount");
                int mtId = rs.getInt("MoneyTurnId");

                dm.add(new Costincomes(mtId, datelong, bill, note, measure, category, count, amount, costincome));
            }

            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dm;
    }

    // CREATE.
    public static void createCostIncome(long date, int billId, int count, int measureId, int categoryId, String note, boolean costIncome, double amount) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT Into MoneyTurn(Date, BillId, Count, MeasureId, CategoryId, Note, CostIncome, Amount, CreatedDate) VALUES (?,?,?,?,?,?,?,?,?)");
        ) {
            ps.setLong(1, date);
            ps.setInt(2, billId);
            ps.setInt(3, count);
            ps.setInt(4, measureId);
            ps.setInt(5, categoryId);
            ps.setString(6, note);
            ps.setBoolean(7, costIncome);
            ps.setDouble(8, amount);
            ps.setLong(9, LocalDateReciever.getDateOfNow());

            ps.executeUpdate();
            ps.close();
        }

    }

    // UPDATE.
    public static void editCostIncome(long datestamp, int billId, String note, int categoryId, int measureId, int count, double sum, boolean costincome, int moneyTurnId) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE MoneyTurn SET Date = ?, BillId = ?, Note = ?, CategoryId = ?, MeasureId = ?, Count = ?, CostIncome = ?, Amount = ?, UpdatedDate = ?  WHERE MoneyTurnId = ?");
        ) {
            ps.setLong(1, datestamp);
            ps.setInt(2, billId);
            ps.setString(3, note);
            ps.setInt(4, categoryId);
            ps.setInt(5, measureId);
            ps.setInt(6, count);
            ps.setBoolean(7, costincome);
            ps.setDouble(8, sum);
            ps.setLong(9, LocalDateReciever.getDateOfNow());
            ps.setInt(10, moneyTurnId);

            ps.executeUpdate();
            ps.close();
        }


    }

    // DELETE
    public static void deleteCostIncome(int moneyTurnId) throws SQLException, ClassNotFoundException {
        try {
            Connection c = DBapi.getConnection();
            PreparedStatement ps;
            if (CommonDAO.getLastSyncDate() != null) {
                ps = c.prepareStatement("UPDATE MoneyTurn SET DeletedDate = ? WHERE moneyTurnId = ?");
                ps.setLong(1, LocalDateReciever.getDateOfNow());
                ps.setInt(2, moneyTurnId);
            }
            else {
                ps = c.prepareStatement("DELETE FROM MoneyTurn WHERE MoneyTurnId = ?");
                ps.setInt(1, moneyTurnId);
            }

            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Select data for first Syncronization
    public static ObservableList<Costincomes> selectCostincomesSyncData() {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Costincomes> costincomes = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT mt.MoneyTurnId, mt.Date, mt.Count, mt.Note, mt.Amount, mt.CostIncome, mt.MeasureId, mt.BillId, mt.CategoryId, mt.DeletedDate, mt.UUIDMoneyTurn FROM MoneyTurn mt");
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                int billid = rs.getInt("BillId");
                Costincomes cs = new Costincomes(rs.getInt("MoneyTurnId"), rs.getLong("Date"), rs.getInt("Count"), rs.getString("Note"), rs.getDouble("Amount"), rs.getBoolean("CostIncome"), rs.getInt("MeasureId"), rs.getInt("BillId"), rs.getInt("CategoryId"), rs.getLong("DeletedDate"), rs.getString("UUIDMoneyTurn"));
                costincomes.add(cs);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return costincomes;
    }

    /**
     * Select data for syncronization
     * @param localDate
     * @return
     */
    public static ObservableList<Costincomes> selectCostincomesSyncDataFromLastDate(LocalDate localDate) {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Costincomes> costincomes = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            long lDate = LocalDateReciever.getLongTimeFromLocalDate(localDate);
            ps = c.prepareStatement("SELECT mt.Date, mt.Count, mt.Note, mt.Amount, mt.CostIncome, mt.DeletedDate, mt.UUIDMoneyTurn, mt.UUIDBill, mt.UUIDMeasure, mt.UUIDCategory FROM MoneyTurn mt WHERE mt.UpdatedDate BETWEEN ? AND (SELECT mt.UpdatedDate From MoneyTurn mt)");
            ps.setLong(1, lDate);
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                Costincomes cs = new Costincomes(rs.getString("UUIDMoneyTurn"), rs.getLong("Date"), rs.getString("UUIDBill"), rs.getInt("Count"), rs.getString("UUIDMeasure"), rs.getString("Note"), rs.getBoolean("CostIncome"), rs.getDouble("Amount"), rs.getString("UUIDCategory"), rs.getLong("DeletedDate"));
                costincomes.add(cs);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return costincomes;
    }

    public static void editCostincomeFromServer(long date, int billId, int count, int measureId, int categoryId, String note, boolean costincome, double amount, String uuidMoneyTurn, int idMoneyTurn) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE MoneyTurn SET Date = ?, BillId = ?, Count = ?, MeasureId = ?, CategoryId = ?, Note = ?, CostIncome = ?, Amount =?, UUIDMoneyTurn = ?, UpdatedDate = ? WHERE MoneyTurnId = ?");
        ) {
            psBills.setLong(1, date);
            psBills.setInt(2, billId);
            psBills.setInt(3, count);
            psBills.setInt(4, measureId);
            psBills.setInt(5, categoryId);
            psBills.setString(6, note);
            psBills.setBoolean(7, costincome);
            psBills.setDouble(8, amount);
            psBills.setString(9, uuidMoneyTurn);
            psBills.setLong(10, LocalDateReciever.getDateOfNow());
            psBills.setInt(11, idMoneyTurn);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editCostincomeUUID(int costincId, String costincUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE MoneyTurn SET UpdatedDate = ?, UUIDMoneyTurn = ? WHERE MoneyTurnId = ?");
        ) {
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
            psBills.setString(2, costincUUID);
            psBills.setInt(3, costincId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editBillUUID(int billId, String billUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE MoneyTurn SET UpdatedDate = ?, UUIDBill = ? WHERE BillId = ?");
        ) {
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
            psBills.setString(2, billUUID);
            psBills.setInt(3, billId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editMeasureUUID(int curId, String curUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE MoneyTurn SET UUIDMeasure = ?, UpdatedDate = ? WHERE MeasureId = ?");
        ) {
            psBills.setString(1, curUUID);
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
            psBills.setInt(3, curId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editCategoryUUID(int catId, String catUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE MoneyTurn SET UUIDCategory = ?, UpdatedDate = ? WHERE CategoryId = ?");
        ) {
            psBills.setString(1, catUUID);
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
            psBills.setInt(3, catId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

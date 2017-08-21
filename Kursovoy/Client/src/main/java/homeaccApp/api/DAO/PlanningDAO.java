package homeaccApp.api.DAO;

import homeaccApp.api.DBapi;
import homeaccApp.api.LocalDateReciever;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import homeaccApp.categories.Categories;
import homeaccApp.mainwindow.bills.Bills;
import homeaccApp.mainwindow.planning.Planning;
import homeaccApp.measures.Measures;

import java.sql.*;
import java.time.LocalDate;

/**
 *  Planning data access.
 */
public class PlanningDAO {
    // RETRIEVE. CostIncome = 1 if income, 0 if cost
    public static ObservableList<Planning> selectPlanningData() throws SQLException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Planning> plans = FXCollections.observableArrayList();

        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT pl.Date, pl.Note, pl.Count, pl.Amount, pl.CostIncome, pl.Status, pl.Period, pl.UserId, b.BillId, b.BillName, m.MeasureId, m.MeasureName, c.CategoryId, c.CategoryName, pl.PlanId FROM Planning AS pl " +
                    "LEFT JOIN Bills AS b ON pl.BillId = b.BillId " +
                    "LEFT JOIN Categories c ON pl.CategoryId = c.CategoryId " +
                    "LEFT JOIN Measures m ON pl.MeasureId = m.MeasureId " +
                    "WHERE pl.UserId = ? AND pl.DeletedDate IS NULL");
            ps.setInt(1, UserDAO.authUserId);
            rs = ps.executeQuery();

            while (rs.next()) {
                long datelong = rs.getLong("Date");

                String note = rs.getString("Note");
                Bills bill = new Bills(rs.getInt("BillId"), rs.getString("BillName"));
                Categories category = new Categories(rs.getInt("CategoryId"), rs.getString("CategoryName"));
                Measures measure = new Measures(rs.getInt("MeasureId"), rs.getString("MeasureName"));
                int count = rs.getInt("Count");
                double amount = rs.getDouble("Amount");
                int mtId = rs.getInt("PlanId");
                boolean costincome = rs.getBoolean("CostIncome");
                int userId = rs.getInt("UserId");
                String status = rs.getString("Status");
                String period = rs.getString("Period");

                plans.add(new Planning(mtId, datelong, bill, note, measure, category, count, amount, userId, costincome, status, period));
            }

            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plans;
    }

    // CREATE.
    public static void createPlan(long date, int billId, int count, int measureId, int categoryId, String note, boolean costIncome, double amount, String status, String period) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT Into Planning(Date, BillId, Count, MeasureId, CategoryId, Note, CostIncome, Amount, UserId, Status, Period, UpdatedDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
        ) {
            ps.setLong(1, date);
            ps.setInt(2, billId);
            ps.setInt(3, count);
            ps.setInt(4, measureId);
            ps.setInt(5, categoryId);
            ps.setString(6, note);
            ps.setBoolean(7, costIncome);
            ps.setDouble(8, amount);
            ps.setInt(9, UserDAO.authUserId);
            ps.setString(10, status);
            ps.setString(11, period);
            ps.setLong(12, LocalDateReciever.getDateOfNow());

            ps.executeUpdate();
            ps.close();
        }

    }

    // UPDATE.
    public static void editPlan(long datestamp, int billId, String note, int categoryId, int measureId, int count, double sum, boolean costincome, int planId, String status, String period) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE Planning SET Date = ?, BillId = ?, Note = ?, CategoryId = ?, MeasureId = ?, Count = ?, CostIncome = ?, Amount = ?, UpdatedDate = ?, Status = ?, Period = ? WHERE PlanId = ?");
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
            ps.setString(10, status);
            ps.setString(11, period);
            ps.setInt(12, planId);

            ps.executeUpdate();
            ps.close();
        }
    }

    // DELETE
    public static void deletePlan(int planId) throws SQLException, ClassNotFoundException {
        if (CommonDAO.getLastSyncDate() != null) {
            try (Connection c = DBapi.getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE Planning SET DeletedDate = ? WHERE PlanId = ?");
            ) {
                ps.setLong(1, LocalDateReciever.getDateOfNow());
                ps.setInt(2, planId);
                ps.executeUpdate();
                ps.close();
            }
        } else {
            try (Connection c = DBapi.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM Planning WHERE PlanId = ?");
            ) {
                ps.setInt(1, planId);
                ps.executeUpdate();
                ps.close();
            }

        }
    }

    public static ObservableList<Planning> selectPlansSyncData() {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Planning> plans = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT pl.PlanId, pl.Date, pl.Count, pl.Note, pl.Amount, pl.CostIncome, pl.MeasureId, pl.BillId, pl.UserId, pl.DeletedDate, pl.Status, pl.Period, pl.UUIDPlanning, pl.CategoryId FROM Planning pl");
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                Planning pl = new Planning(rs.getInt("PlanId"), rs.getLong("Date"), rs.getInt("Count"), rs.getString("Note"), rs.getDouble("Amount"), rs.getBoolean("CostIncome"), rs.getInt("MeasureId"), rs.getInt("BillId"), rs.getInt("UserId"), rs.getLong("DeletedDate"), rs.getString("Status"), rs.getString("Period"), rs.getString("UUIDPlanning"), rs.getInt("CategoryId"));
                plans.add(pl);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plans;
    }

    public static ObservableList<Planning> selectPlansSyncDataFromLastDate(LocalDate localDate) {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Planning> plans = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT pl.Date, pl.Count, pl.Note, pl.Amount, pl.CostIncome, pl.DeletedDate, pl.Status, pl.Period, pl.UUIDPlanning, pl.UUIDBill, pl.UUIDMeasure, pl.UUIDCategory FROM Planning pl WHERE pl.UpdatedDate BETWEEN ? AND (SELECT pl.UpdatedDate From Planning pl)");
            long longDate = LocalDateReciever.getLongTimeFromLocalDate(localDate);
            ps.setLong(1,longDate);
            rs = ps.executeQuery();

            while (rs.next()) {
                Planning pl = new Planning(rs.getString("UUIDPlanning"), rs.getLong("Date"), rs.getString("UUIDBill"), rs.getInt("Count"), rs.getString("Note"), rs.getDouble("Amount"), rs.getString("UUIDMeasure"), rs.getBoolean("CostIncome"), rs.getString("UUIDCategory"), rs.getLong("DeletedDate"), rs.getString("Status"), rs.getString("Period"));
                plans.add(pl);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plans;
    }

    // TODO: 28.09.16 check with different users

    /**
     * Plans before now.
     * @param dateOfNow
     * @return
     */
    public static ObservableList<Planning> selectOldPlans(long dateOfNow) {
        ResultSet rs;
        Connection c;
        PreparedStatement ps;
        ObservableList<Planning> plans = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT pl.PlanId, pl.Date, pl.Count, pl.Note, pl.Amount, pl.CostIncome, pl.MeasureId, pl.BillId, pl.UserId, pl.DeletedDate, pl.Status, pl.Period, pl.UUIDPlanning, pl.CategoryId FROM Planning pl WHERE pl.Date < ? AND pl.DeletedDate = NULL");
            ps.setLong(1,dateOfNow);
            rs = ps.executeQuery();

            while (rs.next()) {
                Planning pl = new Planning(rs.getInt("PlanId"), rs.getLong("Date"), rs.getInt("Count"), rs.getString("Note"), rs.getDouble("Amount"), rs.getBoolean("CostIncome"), rs.getInt("MeasureId"), rs.getInt("BillId"), rs.getInt("UserId"), rs.getLong("DeletedDate"), rs.getString("Status"), rs.getString("Period"), rs.getString("UUIDPlanning"), rs.getInt("CategoryId"));
                plans.add(pl);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plans;

    }

    /**
     * Plans for now
     * @param dateOfNow
     * @return
     */
    public static ObservableList<Planning> selectPlansForNow(long dateOfNow) {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Planning> plans = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT pl.PlanId, pl.Date, pl.Count, pl.Note, pl.Amount, pl.CostIncome, pl.MeasureId, pl.BillId, pl.UserId, pl.DeletedDate, pl.Status, pl.Period, pl.UUIDPlanning, pl.CategoryId FROM Planning pl WHERE pl.Date = ? AND pl.DeletedDate = NULL");
            ps.setLong(1,dateOfNow);
            rs = ps.executeQuery();

            while (rs.next()) {
                Planning pl = new Planning(rs.getInt("PlanId"), rs.getLong("Date"), rs.getInt("Count"), rs.getString("Note"), rs.getDouble("Amount"), rs.getBoolean("CostIncome"), rs.getInt("MeasureId"), rs.getInt("BillId"), rs.getInt("UserId"), rs.getLong("DeletedDate"), rs.getString("Status"), rs.getString("Period"), rs.getString("UUIDPlanning"), rs.getInt("CategoryId"));
                plans.add(pl);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plans;

    }

    public static void updateDate(long newDate, int planId) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE Planning SET Date = ? WHERE PlanId = ?");
        ) {
            ps.setLong(1, newDate);
            ps.setInt(2, planId);

            ps.executeUpdate();
            ps.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateStatus(String planStatus, int planId) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE Planning SET Status = ? WHERE PlanId = ?");
        ) {
            ps.setString(1, planStatus);
            ps.setInt(2, planId);

            ps.executeUpdate();
            ps.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editPlansFromServer(long date, int billId, int measureId, int count, int categoryId, String note, double amount, boolean costincome, String uuidPlanning, String status, String period, int idPlanning) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Planning SET Date = ?, BillId = ?, MeasureId = ?, Count = ?, CategoryId = ?, Note = ?, Amount =?, CostIncome = ?, UUIDPlanning = ?, UpdatedDate = ?, Status = ?, Period = ? WHERE PlanId = ?");
        ) {
            psBills.setLong(1, date);
            psBills.setInt(2, billId);
            psBills.setInt(3, measureId);
            psBills.setInt(4, count);
            psBills.setInt(5, categoryId);
            psBills.setString(6, note);
            psBills.setDouble(7, amount);
            psBills.setBoolean(8, costincome);
            psBills.setString(9, uuidPlanning);
            psBills.setLong(10, LocalDateReciever.getDateOfNow());
            psBills.setString(11, status);
            psBills.setString(12, period);
            psBills.setInt(13, idPlanning);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void editPlansUUID(int planId, String planUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Planning SET UpdatedDate = ?, UUIDPlanning = ? WHERE PlanId = ?");
        ) {
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
            psBills.setString(2, planUUID);
            psBills.setInt(3, planId);
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
             PreparedStatement psBills = c.prepareStatement("UPDATE Planning SET UUIDBill = ?, UpdatedDate = ? WHERE BillId = ?");
        ) {
            psBills.setString(1, billUUID);
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
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
             PreparedStatement psBills = c.prepareStatement("UPDATE Planning SET UUIDMeasure = ?, UpdatedDate = ? WHERE MeasureId = ?");
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
             PreparedStatement psBills = c.prepareStatement("UPDATE Planning SET UUIDCategory = ?, UpdatedDate = ? WHERE CategoryId = ?");
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

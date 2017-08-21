package homeaccApp.api.DAO;

import homeaccApp.api.DBapi;
import homeaccApp.api.Item;
import homeaccApp.api.LocalDateReciever;
import homeaccApp.cashes.Cashes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import homeaccApp.mainwindow.bills.Bills;

import java.sql.*;
import java.time.LocalDate;

/**
 * Created by vellial on 05.02.16.
 */
public class BillDAO {

    // For combobox, I need access to Id and Name.
    public static ObservableList<Bills> selectBills() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT BillId, BillName FROM Bills");
        ) {
            rs = ps.executeQuery();
            ObservableList<Bills> billsList = FXCollections.observableArrayList();

            while ( rs.next() ) {
                billsList.add(new Bills(rs.getInt("BillId"), rs.getString("BillName")));
            }
            rs.close();
            ps.close();
            return billsList;
        }
    }

    // RETRIEVE. Get all data for bills from database.
    public static ObservableList<Bills> selectBillsInfo(int userid) throws SQLException, ClassNotFoundException {
        ObservableList<Bills> bills = FXCollections.observableArrayList();
        ResultSet rs;
        Connection c;
        PreparedStatement ps;

        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT b.BillName, b.StartBalance, b.Note, csh.Amount, b.BillId, c.CurrencyId, c.CurrencyName, b.Date FROM Bills b LEFT JOIN Cashes csh ON csh.billId = b.billId LEFT JOIN Currencies c ON c.CurrencyId = csh.CurrencyId WHERE b.UserId = ? AND b.DeletedDate IS NULL");
            ps.setInt(1, userid);
            rs = ps.executeQuery();

            while (rs.next()) {
                Item currency = new Item(rs.getInt("CurrencyId"), rs.getString("CurrencyName"));
                Bills bill = new Bills(rs.getLong("Date"), rs.getString("BillName"), rs.getString("Note"), currency, rs.getDouble("StartBalance"), rs.getDouble("Amount"), userid, rs.getInt("BillId"));

                bills.add(bill);
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bills;
    }

    // CREATE.
    public static void createBill(long date, String billName, String note, Double startBalance, int currencyId, int userId) throws SQLException, ClassNotFoundException {
        int billId = 0;

        // Insert new string to bills table and get id from new string.
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("INSERT Into bills(Date, BillName, Note, StartBalance, UserId) VALUES (?,?,?,?,?)");
        ) {
            psBills.setLong(1, date);
            psBills.setString(2, billName);
            psBills.setString(3, note);
            psBills.setDouble(4, startBalance);
            psBills.setInt(5, userId);
            psBills.executeUpdate();
            psBills.close();

            // Get id for new string.
            PreparedStatement psNewBill = c.prepareStatement("SELECT last_insert_rowid() AS billId FROM Bills;");
            ResultSet rs = psNewBill.executeQuery();
            while (rs.next()) {
                billId = rs.getInt(1);
            }
            psNewBill.close();
            rs.close();
        }

        if (billId != 0) {
            try (Connection c = DBapi.getConnection();
                 PreparedStatement psCash = c.prepareStatement("INSERT Into Cashes(Amount, CurrencyId, BillId) VALUES (?,?,?)");
            ) {
                psCash.setDouble(1, startBalance);
                psCash.setInt(2, currencyId);
                psCash.setInt(3, billId);
                psCash.executeUpdate();
                psCash.close();
            }
        }

    }

    public int getBillId(int userId, String billName) throws SQLException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        int billId = 0;
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT BillId FROM Bills WHERE UserId = ? AND BillName = ?");
            ps.setInt(1, userId);
            ps.setString(2, billName);
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                billId = rs.getInt("BillId");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return billId;
    }

    // UPDATE
    public static void editBill(long date, String billName, String note, double startBalance, int userId, int billId) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Bills SET Date = ?, BillName = ?, Note = ?, StartBalance = ?, UpdatedDate = ? WHERE UserId = ? AND BillId = ?");
        ) {
            psBills.setLong(1, date);
            psBills.setString(2, billName);
            psBills.setString(3, note);
            psBills.setDouble(4, startBalance);
            psBills.setLong(5, LocalDateReciever.getDateOfNow());
            psBills.setInt(6, userId);
            psBills.setInt(7, billId);
            psBills.executeUpdate();
            psBills.close();
        }
    }

    /**
     * DELETE - we don't delete note, we just update DeletedDate field
     * but if LastSyncDate is null, we can delete note.
     * @param billId
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void deleteBill(int billId) throws SQLException, ClassNotFoundException {
        try {
            Connection c = DBapi.getConnection();
            PreparedStatement ps, psDelLinkedCosts, psDelLinkedPlans;

            if (CommonDAO.getLastSyncDate() != null) {
                ps = c.prepareStatement("UPDATE bills SET DeletedDate = ? WHERE BillId = ? AND UserId = ?");
                psDelLinkedCosts = c.prepareStatement("UPDATE MoneyTurn SET DeletedDate = ? WHERE BillId = ?");
                psDelLinkedPlans = c.prepareStatement("UPDATE Planning SET DeletedDate = ? WHERE BillId = ? AND UserId = ?");
                ps.setLong(1, LocalDateReciever.getDateOfNow());
                ps.setInt(2, billId);
                ps.setInt(3, UserDAO.authUserId);
            }
            else {
                ps = c.prepareStatement("DELETE FROM Bills WHERE BillId = ?");
                psDelLinkedCosts = c.prepareStatement("DELETE FROM MoneyTurn WHERE BillId = ?");
                psDelLinkedPlans = c.prepareStatement("DELETE FROM Planning WHERE BillId = ?");
                ps.setInt(1, billId);
                psDelLinkedCosts.setInt(1, billId);
                psDelLinkedPlans.setInt(1, billId);
            }

            ps.executeUpdate();
            psDelLinkedCosts.executeUpdate();
            psDelLinkedPlans.executeUpdate();

            ps.close();
            psDelLinkedCosts.close();
            psDelLinkedPlans.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // UPDATE from server
    public static void editBill(long date, String billName, String note, double startBalance, int userId, int billId, String uuidBill, String uuidUser) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Bills SET Date = ?, BillName = ?, Note = ?, StartBalance = ?, UpdatedDate = ?, UUIDBill = ?, UUIDUser = ? WHERE UserId = ? AND BillId = ?");
        ) {
            psBills.setLong(1, date);
            psBills.setString(2, billName);
            psBills.setString(3, note);
            psBills.setDouble(4, startBalance);
            psBills.setLong(5, LocalDateReciever.getDateOfNow());
            psBills.setString(6, uuidBill);
            psBills.setString(7, uuidUser);
            psBills.setInt(8, userId);
            psBills.setInt(9, billId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editCashes(double amount, int currencyId, String uuidCash, int billId, String uuidBill, String uuidCurrency) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Cashes SET Amount = ?, CurrencyId = ?, UpdatedDate = ?, UUIDCash = ?, UUIDBill = ?, UUIDCurrency = ? WHERE BillId = ?");
        ) {
            psBills.setDouble(1, amount);
            psBills.setInt(2, currencyId);
            psBills.setLong(3, LocalDateReciever.getDateOfNow());
            psBills.setString(4, uuidCash);
            psBills.setString(5, uuidBill);
            psBills.setString(6, uuidCurrency);
            psBills.setInt(7, billId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editUUIDBill(int billId, String billUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Bills SET UpdatedDate = ?, UUIDBill = ? WHERE BillId = ?");
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

    // For first syncronization.
    public static ObservableList<Bills> selectBillsSyncData() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT b.Date, b.BillName, b.Note, b.StartBalance, b.BillId, b.UserId, b.DeletedDate, b.UUIDBill FROM Bills b");
        ) {
            rs = ps.executeQuery();
            ObservableList<Bills> billsList = FXCollections.observableArrayList();
            while ( rs.next() ) {
                billsList.add(new Bills(rs.getLong("Date"), rs.getString("BillName"), rs.getString("Note"), rs.getDouble("StartBalance"), rs.getInt("BillId"), rs.getInt("UserId"), rs.getLong("DeletedDate"), rs.getString("UUIDBill")));
            }
            rs.close();
            ps.close();
            return billsList;
        }
    }

    public static ObservableList<Bills> selectBillsSyncDataFromLastDate(LocalDate lastSyncDate) throws SQLException, ClassNotFoundException {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT b.Date, b.BillName, b.Note, b.StartBalance, b.BillId, b.UserId, b.DeletedDate, b.UUIDBill FROM Bills b WHERE b.UpdatedDate BETWEEN ? AND (SELECT b.UpdatedDate From Bills b)");
        ) {
            long longDate = LocalDateReciever.getLongTimeFromLocalDate(lastSyncDate);
            ps.setLong(1, longDate);
            rs = ps.executeQuery();

            ObservableList<Bills> billsList = FXCollections.observableArrayList();
            while ( rs.next() ) {
                billsList.add(new Bills(rs.getLong("Date"), rs.getString("BillName"), rs.getString("Note"), rs.getDouble("StartBalance"), rs.getInt("BillId"), rs.getInt("UserId"), rs.getLong("DeletedDate"), rs.getString("UUIDBill")));
            }
            rs.close();
            ps.close();
            return billsList;
        }
    }

    public static ObservableList<Cashes> selectCashedSyncData() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT c.CashId, c.Amount, c.BillId, c.CurrencyId, c.DeletedDate, c.UUIDCash FROM Cashes c");
        ) {
            rs = ps.executeQuery();
            ObservableList<Cashes> cashes = FXCollections.observableArrayList();
            while ( rs.next() ) {
                cashes.add(new Cashes(rs.getInt("CashId"), rs.getInt("BillId"), rs.getInt("CurrencyId"), rs.getDouble("Amount"), rs.getLong("DeletedDate"), rs.getString("UUIDCash")));
            }
            rs.close();
            ps.close();
            return cashes;
        }
    }

    public static ObservableList<Cashes> selectCashesSyncDataFromLastDate(LocalDate localDate) throws SQLException, ClassNotFoundException {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT c.CashId, c.Amount, c.BillId, c.CurrencyId, c.DeletedDate, c.UUIDCash, c.UUIDCurrency, c.UUIDBill FROM Cashes c WHERE c.UpdatedDate BETWEEN ? AND (SELECT c.UpdatedDate From Cashes c)");
        ) {
            long longDate = LocalDateReciever.getLongTimeFromLocalDate(localDate);
            ps.setLong(1, longDate);
            rs = ps.executeQuery();
            ObservableList<Cashes> cashes = FXCollections.observableArrayList();
            while ( rs.next() ) {
                cashes.add(new Cashes(rs.getInt("CashId"), rs.getInt("BillId"), rs.getInt("CurrencyId"), rs.getDouble("Amount"), rs.getLong("DeletedDate"), rs.getString("UUIDCash"), rs.getString("UUIDBill"), rs.getString("UUIDCurrency")));
            }
            rs.close();
            ps.close();
            return cashes;
        }
    }

    /**
     * This function is for update amount if we get costs or incomes with our bill
     *
     * @param costincome - cost or income
     * @param costincAmount - amount for cost or income
     * @param costincBillId - bill id
     */
    public static void updateBill(boolean costincome, double costincAmount, int costincBillId) {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBillAmount = c.prepareStatement("SELECT Cashes.Amount FROM Cashes WHERE BillId = ?");
        ) {
            psBillAmount.setInt(1, costincBillId);
            rs = psBillAmount.executeQuery();
            double billAmount = 0, billSum = 0;
            while ( rs.next() ) {
                billAmount = rs.getDouble("Amount");
            }
            long longDate = LocalDateReciever.getDateOfNow();

            if (costincome) {
                billSum = billAmount + costincAmount;
            } else {
                billSum = billAmount - costincAmount;
            }

            PreparedStatement ps = c.prepareStatement("UPDATE Cashes SET Amount = ?, UpdatedDate = ? WHERE BillId = ?");
            ps.setDouble(1, billSum);
            ps.setLong(2, longDate);
            ps.setInt(3, costincBillId);
            ps.executeUpdate();

            rs.close();
            ps.close();
            psBillAmount.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update amount if user made remittance.
     *
     * @param amount
     * @param billId
     */
    public static void updateBill(double amount, int billId) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE Cashes SET Amount = ?, UpdatedDate = ? WHERE BillId = ?");
        ) {
            long longDate = LocalDateReciever.getDateOfNow();

            ps.setDouble(1, amount);
            ps.setLong(2, longDate);
            ps.setInt(3, billId);
            ps.executeUpdate();

            ps.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editCashUUID(int cashId, String cashUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Cashes SET UpdatedDate = ?, UUIDCash = ? WHERE CashId = ?");
        ) {
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
            psBills.setString(2, cashUUID);
            psBills.setInt(3, cashId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editCashBillUUID(int billId, String billUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Cashes SET UpdatedDate = ?, UUIDBill = ? WHERE BillId = ?");
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

    public static void editCashCurUUID(int billId, String billUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Cashes SET UpdatedDate = ?, UUIDCurrency = ? WHERE CurrencyId = ?");
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

    public static void updateUserUUID(String uuidUser) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE Bills SET UUIDUser = ? WHERE UserId = ?");
        ) {
            ps.setString(1, uuidUser);
            ps.setInt(2, UserDAO.authUserId);
            ps.executeUpdate();
            ps.close();
        }

    }
}

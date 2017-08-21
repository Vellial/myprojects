package homeaccApp.api.DAO;

import homeaccApp.api.DBapi;
import homeaccApp.api.Item;
import homeaccApp.api.LocalDateReciever;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import homeaccApp.currencies.Currency;

import java.sql.*;
import java.time.LocalDate;

/**
 * Currency database objects.
 */
public class CurrencyDAO {

    // CREATE
    public static void createCurrency(String name, String nameShort) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT Into Currencies(CurrencyName, NameShort, UpdatedDate) VALUES (?,?,?)");
        ) {
            ps.setString(1, name);
            ps.setString(2, nameShort);
            ps.setLong(3, LocalDateReciever.getDateOfNow());
            ps.executeUpdate();
            ps.close();
        }
    }

    // DELETE
    public static void deleteCurrency(int id) throws SQLException, ClassNotFoundException {
        try {
            Connection c = DBapi.getConnection();
            PreparedStatement ps;
            if (CommonDAO.getLastSyncDate() != null) {
                ps = c.prepareStatement("UPDATE Currencies SET DeletedDate = ? WHERE CurrencyId = ?");
                ps.setLong(1, LocalDateReciever.getDateOfNow());
                ps.setInt(2, id);
            } else {
                ps = c.prepareStatement("DELETE FROM Currencies WHERE CurrencyId=?");
                ps.setInt(1, id);
            }

            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get all currencies.
    public static ObservableList<Currency> selectAllCurrencies() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Currency> currencies = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT * FROM currencies");
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                Currency currency = new Currency();
                currency.setCurrencyName(rs.getString("CurrencyName"));
                currency.setCurrencyId(rs.getInt("CurrencyId"));
                currency.setCurrencyShortName(rs.getString("NameShort"));
                currencies.add(currency);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currencies;
    }

    // For first sync.
    public static ObservableList<Currency> selectCurrenciesSyncData() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Currency> currencies = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT c.CurrencyName, c.CurrencyId, c.NameShort, c.UUIDCurrency, c.DeletedDate FROM currencies c");
            rs = ps.executeQuery();

            while (rs.next()) {
                Currency currency = new Currency();
                currency.setCurrencyName(rs.getString("CurrencyName"));
                currency.setCurrencyId(rs.getInt("CurrencyId"));
                currency.setCurrencyShortName(rs.getString("NameShort"));
                currency.setCurrencyUUID(rs.getString("UUIDCurrency"));
                currency.setDeletedDate(rs.getLong("DeletedDate"));
                currencies.add(currency);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currencies;
    }

    /**
     * Select data for syncronization.
     * @param date date of last syncronization
     * @return currencies with deletedDate - if currency was deleted
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static ObservableList<Currency> selectCurrenciesFromLastDate(LocalDate date) throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Currency> currencies = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT c.CurrencyName, c.CurrencyId, c.NameShort, c.UUIDCurrency, c.DeletedDate FROM Currencies c WHERE c.UpdatedDate BETWEEN ? AND (SELECT c.UpdatedDate From Currencies c)");
            ps.setLong(1,LocalDateReciever.getLongTimeFromLocalDate(date));
            rs = ps.executeQuery();

            while (rs.next()) {
                Currency currency = new Currency();
                currency.setCurrencyName(rs.getString("CurrencyName"));
                currency.setCurrencyId(rs.getInt("CurrencyId"));
                currency.setCurrencyShortName(rs.getString("NameShort"));
                currency.setCurrencyUUID(rs.getString("UUIDCurrency"));
                currency.setDeletedDate(rs.getLong("DeletedDate"));
                currencies.add(currency);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currencies;
    }

    // Get currencies.
    public static ObservableList<Item> selectAllCurrenciesList() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Item> currencies = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();

            ps = c.prepareStatement("SELECT c.CurrencyId, c.CurrencyName FROM currencies c");
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                Item currency = new Item(rs.getInt("CurrencyId"), rs.getString("CurrencyName"));
                currencies.add(currency);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currencies;
    }

    public static void editCurrency(String currencyName, String currencyShortName, int curId) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Currencies SET CurrencyName = ?, NameShort = ? WHERE CurrencyId = ?");
        ) {
            psBills.setString(1, currencyName);
            psBills.setString(2, currencyShortName);
            psBills.setInt(3, curId);
            psBills.executeUpdate();
            psBills.close();
        }
    }

    // UPDATE from server
    public static void editCurrencyFromServer(String currencyName, String currencyShortName, String uuidCurrency, int curId) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Currencies SET CurrencyName = ?, NameShort = ?, UUIDCurrency = ?, UpdatedDate = ? WHERE CurrencyId = ?");
        ) {
            psBills.setString(1, currencyName);
            psBills.setString(2, currencyShortName);
            psBills.setString(3, uuidCurrency);
            psBills.setLong(4, LocalDateReciever.getDateOfNow());
            psBills.setInt(5, curId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editCurrencyUUID(int curId, String curUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Currencies SET UpdatedDate = ?, UUIDCurrency = ? WHERE CurrencyId = ?");
        ) {
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
            psBills.setString(2, curUUID);
            psBills.setInt(3, curId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

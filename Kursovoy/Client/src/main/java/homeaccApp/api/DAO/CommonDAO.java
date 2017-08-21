package homeaccApp.api.DAO;

import homeaccApp.Main;
import homeaccApp.api.DBapi;
import homeaccApp.api.Sync.Syncronization;
import homeaccApp.mainwindow.menuDialogs.miSettings.miSettings;
import org.json.simple.JSONObject;

import java.sql.*;
import java.time.LocalDate;

/**
 * Common operations with database.
 */
public class CommonDAO {

    // SETTINGS
    // CREATE SETTINGS DATA.
    public static void createSettings(String username, String password) throws SQLException, ClassNotFoundException {

        // Insert new string to bills table and get id from new string.
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("INSERT Into Settings(remoteEmail, remotePassword) VALUES (?,?)");
        ) {
            psBills.setString(1, username);
            psBills.setString(2, password);
            psBills.executeUpdate();
            psBills.close();
        }
    }

    // RETRIEVE SETTINGS
    public static miSettings selectSettings() {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        miSettings settings = new miSettings();
        try {
            c = DBapi.getConnection();
            String query = "SELECT * FROM Settings";
            ps = c.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                settings.setRemotePassword(rs.getString("remotePassword"));
                settings.setRemoteEmail(rs.getString("remoteEmail"));
            }
            rs.close();
            ps.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settings;
    }

    // Check email in database
    public static boolean checkEmail(String remoteEmail) {
        ResultSet rs;
        boolean isExist = false;
        try (
            Connection c = DBapi.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM Settings WHERE remoteEmail = ?");
        ) {
            ps.setString(1, remoteEmail);
            rs = ps.executeQuery();

            while (rs.next()) {
                isExist = true;
            }
            rs.close();
            ps.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isExist;
    }


    // Get LastSyncDate for sync.
    public static LocalDate getLastSyncDate() {
        // Insert new string to bills table and get id from new string.
        Timestamp timestamp = null;
        LocalDate date = null;
        try {
            ResultSet rs;
            Connection c = DBapi.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * From LastSyncTimestamp");
            rs = ps.executeQuery();
            while (rs.next()) {
                int indate = rs.getInt("LastSyncTime");
                timestamp = new Timestamp(indate);
            }
            rs.close();
            ps.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (timestamp != null) {
            date = timestamp.toLocalDateTime().toLocalDate();
        }

        return date;
    }

    public static void authorizeOnServer(String password, String email) {
        JSONObject resultJson = new JSONObject();

        resultJson.put("codeOperation", "authorize"); // TODO: 26.07.16 "authorize" - is constant
        resultJson.put("UUIDDevice", Main.uuidDevice.toString());
        resultJson.put("username", email);
        resultJson.put("pass", password);

        Syncronization.getInstance().sendMessage(resultJson.toJSONString());

    }

    public static void setLastSyncDate(long date) {
        try {
            LocalDate oldDate = CommonDAO.getLastSyncDate();
            if (oldDate == null) {
                Connection c = DBapi.getConnection();
                PreparedStatement ps = c.prepareStatement("INSERT INTO LastSyncTimestamp(LastSyncTime) VALUES (?)");
                ps.setLong(1, date);
                ps.executeUpdate();
                ps.close();
            } else {
                Connection c = DBapi.getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE LastSyncTimestamp SET LastSyncTime = ?");
                ps.setLong(1, date);
                ps.executeUpdate();
                ps.close();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


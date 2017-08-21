package homeaccApp.api.DAO;

import homeaccApp.api.DBapi;
import homeaccApp.api.LocalDateReciever;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import homeaccApp.measures.Measures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Measure data access.
 */
public class MeasureDAO {

    // CREATE
    public static void createMeasure(String name) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT Into Measures(MeasureName, UpdatedDate) VALUES (?,?)");
        ) {
            ps.setString(1, name);
            ps.setLong(2, LocalDateReciever.getDateOfNow());
            ps.executeUpdate();
            ps.close();
        }
    }

    // DELETE
    public static void deleteMeasure(int id) throws SQLException, ClassNotFoundException {
        try {
            Connection c = DBapi.getConnection();
            PreparedStatement ps;
            if (CommonDAO.getLastSyncDate() != null) {
                ps = c.prepareStatement("UPDATE Measures SET DeletedDate = ? WHERE MeasureId = ?");
                ps.setLong(1, LocalDateReciever.getDateOfNow());
                ps.setInt(2, id);
            } else {
                ps = c.prepareStatement("DELETE FROM Measures WHERE MeasureId=?");
                ps.setInt(1, id);
            }

            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get measures.
    public static ObservableList<Measures> selectAllMeasures() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Measures> measures = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT m.MeasureName, m.MeasureId FROM Measures m");
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                Measures me = new Measures();
                me.setMeasureName(rs.getString("MeasureName"));
                me.setMeasureId(rs.getInt("MeasureId"));
                measures.add(me);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return measures;
    }

    // For first sync
    public static ObservableList<Measures> selectMeasuresSyncData() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Measures> measures = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT m.MeasureName, m.MeasureId, m.UUIDMeasure, m.DeletedDate FROM Measures m");
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                Measures me = new Measures();
                me.setMeasureName(rs.getString("MeasureName"));
                me.setMeasureId(rs.getInt("MeasureId"));
                me.setUuidMeasure(rs.getString("UUIDMeasure"));
                me.setDeletedDate(rs.getLong("DeletedDate"));
                measures.add(me);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return measures;
    }

    // For syncronization
    public static ObservableList<Measures> selectMeasuresFromLastDate(LocalDate localDate) throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        ObservableList<Measures> measures = FXCollections.observableArrayList();
        try {
            c = DBapi.getConnection();
            ps = c.prepareStatement("SELECT * FROM Measures m WHERE m.UpdatedDate BETWEEN ? AND (SELECT m.UpdatedDate From Measures m)");
            ps.setLong(1, LocalDateReciever.getLongTimeFromLocalDate(localDate));
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                Measures me = new Measures(rs.getInt("MeasureId"), rs.getString("MeasureName"), rs.getString("UUIDMeasure"), rs.getLong("DeletedDate"));
                measures.add(me);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return measures;
    }

    public static void updateFromServer(String measureName, String uuidMeasure, int measureId) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Measures SET MeasureName = ?, UUIDMeasure = ?, UpdatedDate = ? WHERE MeasureId = ?");
        ) {
            psBills.setString(1, measureName);
            psBills.setString(2, uuidMeasure);
            psBills.setLong(3, LocalDateReciever.getDateOfNow());
            psBills.setInt(4, measureId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateMeasureUUID(int measureId, String measureUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Measures SET UpdatedDate = ?, UUIDMeasure = ? WHERE MeasureId = ?");
        ) {
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
            psBills.setString(2, measureUUID);
            psBills.setInt(3, measureId);
            psBills.executeUpdate();
            psBills.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

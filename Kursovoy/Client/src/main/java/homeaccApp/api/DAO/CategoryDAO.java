package homeaccApp.api.DAO;

import homeaccApp.api.DBapi;
import homeaccApp.api.Item;
import homeaccApp.api.LocalDateReciever;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import homeaccApp.categories.Categories;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Vector;

/**
 * Class for working with database - table Categories
 */
public class CategoryDAO {

    // CREATE
    public static void createCategory(String name, boolean costincome) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT Into Categories(CategoryName, UpdatedDate, CostIncome) VALUES (?,?,?)");
        ) {
            ps.setString(1, name);
            ps.setLong(2, LocalDateReciever.getDateOfNow());
            ps.setBoolean(3, costincome);
            ps.executeUpdate();
            ps.close();
        }
    }

    // DELETE
    public static void deleteCategory(int id) throws SQLException, ClassNotFoundException {
        try {
            Connection c = DBapi.getConnection();
            PreparedStatement ps;
            if (CommonDAO.getLastSyncDate() != null) {
                ps = c.prepareStatement("UPDATE Categories SET DeletedDate = ? WHERE CategoryId = ?");
                ps.setLong(1, LocalDateReciever.getDateOfNow());
                ps.setInt(2, id);
            } else {
                ps = c.prepareStatement("DELETE FROM Categories WHERE CategoryId=?");
                ps.setInt(1, id);
            }

            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPDATE from server
    public static void updateCategoryFromServer(String categoryName, boolean costincome, String uuidCategory, int categoryId) {
        try {
            Connection c = DBapi.getConnection();
            PreparedStatement ps;
            ps = c.prepareStatement("UPDATE Categories SET CategoryName = ?, CostIntcome = ?, UUIDCategory = ?, UpdatedDate = ? WHERE CategoryId = ?");
            ps.setString(1, categoryName);
            ps.setBoolean(2, costincome);
            ps.setString(3, uuidCategory);
            ps.setLong(4, LocalDateReciever.getDateOfNow());
            ps.setInt(5, categoryId);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Get category id
    public static int selectCategoryId(int userId) throws SQLException, ClassNotFoundException {
        ResultSet rs;
        int categoryId = 0;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT CategoryId FROM categories WHERE UserId=?");
        ) {
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while ( rs.next() ) {
                categoryId = rs.getInt("CategoryId");
            }
            ps.close();
        }
        rs.close();
        return categoryId;
    }

    // For combobox, I need access to Id and Name. Cost or Income.
    public static ObservableList<Categories> selectCategories(boolean costincome) throws SQLException, ClassNotFoundException {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM categories WHERE CostIncome = ?");
        ) {
            ps.setBoolean(1, costincome);
            rs = ps.executeQuery();

            ObservableList<Categories> categoriesList = FXCollections.observableArrayList();
            while ( rs.next() ) {
                Integer key = rs.getInt("CategoryId");
                String value = rs.getString("CategoryName");
                categoriesList.add( new Categories(key, value ) );
            }
            rs.close();
            ps.close();
            return categoriesList;
        }
    }

    // For combobox, I need access to Id and Name. All homeaccApp.categories.
    public static  ObservableList<Categories> selectCategories() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT c.CategoryId, c.CategoryName FROM categories c");
        ) {
            rs = ps.executeQuery();

            ObservableList<Categories> model = FXCollections.observableArrayList();
            while ( rs.next() ) {
                Integer key = rs.getInt("CategoryId");
                String value = rs.getString("CategoryName");
                model.add( new Categories(key, value ) );
            }
            rs.close();
            ps.close();
            return model;
        }
    }

    // For first syncronization.
    public static  ObservableList<Categories> selectCategoriesSyncData() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT c.CategoryName, c.CostIncome, c.CategoryId, c.DeletedDate, c.UUIDCategory FROM categories c");
        ) {
            rs = ps.executeQuery();

            ObservableList<Categories> model = FXCollections.observableArrayList();
            while ( rs.next() ) {
                int categoryId = rs.getInt("CategoryId");
                boolean costIncome = rs.getBoolean("CostIncome");
                String categoryName = rs.getString("CategoryName");
                long deletedDate = rs.getLong("DeletedDate");
                model.add( new Categories(categoryId, categoryName, costIncome, deletedDate, rs.getString("UUIDCategory")) );
            }
            rs.close();
            ps.close();
            return model;
        }
    }

    // For syncronization.
    public static  ObservableList<Categories> selectCategoriesSyncDataFromLastDate(LocalDate localDate) throws SQLException, ClassNotFoundException {
        ResultSet rs;
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT c.CategoryName, c.CostIncome, c.CategoryId, c.DeletedDate, c.UUIDCategory FROM categories c WHERE c.UpdatedDate BETWEEN ? AND (SELECT c.UpdatedDate From Categories c)");
        ) {
            long longDate = LocalDateReciever.getLongTimeFromLocalDate(localDate);
            ps.setLong(1,longDate);
            rs = ps.executeQuery();

            ObservableList<Categories> model = FXCollections.observableArrayList();
            while ( rs.next() ) {
                model.add( new Categories(rs.getInt("CategoryId"), rs.getString("CategoryName"), rs.getBoolean("CostIncome"), rs.getLong("DeletedDate"), rs.getString("UUIDCategory")) );
            }
            rs.close();
            ps.close();
            return model;
        }
    }

    public static void updateCategoryUUID(int catId, String catUUID) {
        try (Connection c = DBapi.getConnection();
             PreparedStatement psBills = c.prepareStatement("UPDATE Categories SET UpdatedDate = ?, UUIDCategory = ? WHERE CategoryId = ?");
        ) {
            psBills.setLong(1, LocalDateReciever.getDateOfNow());
            psBills.setString(2, catUUID);
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

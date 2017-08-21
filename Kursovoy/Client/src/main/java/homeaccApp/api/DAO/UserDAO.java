package homeaccApp.api.DAO;

import homeaccApp.api.DBapi;
import homeaccApp.api.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import homeaccApp.user.api.createuser.User;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class UserDAO {

    public static int authUserId = 0;
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static int authoriseUser(String name, String pass) throws SQLException, ClassNotFoundException, NoSuchAlgorithmException, UnsupportedEncodingException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT password, userid From users WHERE username = ?");
        ) {
            ps.setString(1, name);
            ResultSet resultSet = ps.executeQuery();

            String password = "";
            int userid = 0;
            if (resultSet != null) {
                while (resultSet.next()) {
                    password = resultSet.getString("password");
                    userid = resultSet.getInt("userid");
                }

                String mdPassword = md5Custom(pass);
                if (password.equals(mdPassword)) {
                    authUserId = userid;
                }
                resultSet.close();
            }
            ps.close();
            return authUserId;
        }
    }

    public static int authoriseUser(int userid) {
        return authUserId = userid;
    }

    public static String md5Custom(String st) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert m != null;
        m.update(st.getBytes(), 0, st.length());
        return new BigInteger(1, m.digest()).toString(16);

    }

    public static void createUser(String username, String password, String email) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT Into users(username, password, email) VALUES (?,?,?)");
        ) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.executeUpdate();
            ps.close();
        }
    }

    public static void deleteUser(int uid) throws SQLException, ClassNotFoundException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE userid=?");
        ) {
            ps.setInt(1, uid);
            ps.executeUpdate();
            ps.close();
        }

    }

    public static void editUser(String token, int userId) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET Token = ? WHERE UserId = ?");
        ) {
            ps.setString(1, token);
            ps.setInt(2, userId);
            ps.executeUpdate();
            ps.close();
        }

    }

    // TODO: may be I will need to get all users with all data.
//    public static DefaultListModel<String> selectAllUsers() throws SQLException, ClassNotFoundException {
//        ResultSet rs;
//        Connection c;
//        PreparedStatement ps = null;
//        DefaultListModel<String> listModel = null;
//        try {
//            c = getConnection();
//            ps = c.prepareStatement("SELECT * FROM users");
//            rs = ps.executeQuery();
//            listModel = new DefaultListModel<String>();
//
//            while (rs.next()) {
//                String username = rs.getString("username");
//                listModel.addElement(username);
//            }
//            rs.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            assert ps != null;
//            ps.close();
//        }
//        return listModel;
//    }

    public static ObservableList<Item> selectAllUsersList() throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps;
        ObservableList<Item> model = FXCollections.observableArrayList();
        try {
            c = getConnection();
            ps = c.prepareStatement("SELECT * FROM users");
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                int userId = rs.getInt("UserId");
                String username = rs.getString("username");
                model.add( new Item(userId, username ) );
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public static User selectUserInfo(int userid) throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps = null;
        User userInfo = new User();
        try {
            c = getConnection();
            ps = c.prepareStatement("SELECT u.Username, u.Email, cat.Name, cat.CategoryCostsId FROM users u " +
                    "INNER JOIN usercategorycosts usercat ON u.userid = usercat.userid " +
                    "WHERE u.userid = ?");
            ps.setInt(1, userid);
            rs = ps.executeQuery();

            // Get all fields.
            while (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                userInfo.setUserId(userid);
                userInfo.setUsername(username);
                userInfo.setEmail(email);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    public static boolean confirmUsername(String dummyName) throws SQLException, ClassNotFoundException {
        ResultSet rs;
        Connection c;
        PreparedStatement ps;
        boolean confirmed = false;
        try {
            c = getConnection();
            ps = c.prepareStatement("SELECT username FROM users WHERE username = ?");
            ps.setString(1, dummyName);
            rs = ps.executeQuery();

            // Get usernames and add its to list.
            while (rs.next()) {
                confirmed = true;
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return confirmed;
    }


    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        return DBapi.getConnection();
    }

    public static void updateUserUUID(String uuidUser) throws SQLException, ClassNotFoundException {
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE Users SET GUIDUser = ? WHERE UserId = ?");
        ) {
            ps.setString(1, uuidUser);
            ps.setInt(2, authUserId);
            ps.executeUpdate();
            ps.close();
        }

    }

    public static String getUserUUID(int userId) {
        String uuidUser = "";
        try (Connection c = DBapi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT GUIDUser FROM Users WHERE UserId = ?");
        ) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                uuidUser = rs.getString("GUIDUser");
            }
            ps.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuidUser;
    }
}
package DAO;

import com.mysql.cj.jdbc.MysqlDataSource;
import entities.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.JSONObject;

import javax.naming.NamingException;
import javax.websocket.Session;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

/**
 * Operations with database.
 */
public class CommonDAO {
    private static HashMap<Integer, String> billsUUIDs = new HashMap<>();
    private static HashMap<Integer, String> cursUUIDs = new HashMap<>();
    private static HashMap<Integer, String> measuresUUIDs = new HashMap<>();
    private static HashMap<Integer, String> categoriesUUIDs = new HashMap<>();

    private static Session session;

    public static void setSession(Session sess) {
        session = sess;
    }

    private static Connection getConnection() throws NamingException, SQLException {
        String username = "root";
        String pass = "";
        String url = "jdbc:mysql://localhost:3306/lightsideServer";

        MysqlDataSource ds = new MysqlDataSource();
        ds.setUser(username);
        ds.setPassword(pass);
        ds.setURL(url);

        return ds.getConnection(username, pass);
    }

    /**
     * Authorize or registration for user.
     * @param username username - email.
     * @param pass password.
     * @param isRegistred if user registred, we authorize. if not, we regirster user.
     * @return answer for client
     * @throws SQLException exception from database or sql syntax
     * @throws NamingException exception from class names
     */
    public static JSONObject authorizeUser(String username, String pass, int isRegistred, String uuidDevice) throws SQLException, NamingException {
        JSONObject compactJws = new JSONObject();
        if (isRegistred == 1) {
            compactJws.put("key", "error");
            compactJws.put("value", "Пользователь с таким именем не найден");
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT u.Password FROM Users u WHERE Username = ?");
            ) {
                ps.setString(1, username);
                ResultSet resultSet = ps.executeQuery();

                String password = "";
                while (resultSet.next()) {
                    password = resultSet.getString("Password");
                }

                compactJws.replace("value", "Вы ввели неверное имя пользователя или пароль");
//                    String mdPassword = md5Custom(pass);
                if (password.equals(pass)) {
                    //generate token, return token
                    Key key = MacProvider.generateKey();

                    String token = Jwts.builder()
                            .setSubject(username)
                            .signWith(SignatureAlgorithm.HS512, key)
                            .compact();

                    compactJws.remove("key");
                    compactJws.remove("value");
                    compactJws.put("key", "token");
                    compactJws.put("value", token);
                    // create note in Sessions table
                    PreparedStatement sessStatement = c.prepareStatement("INSERT Into Sessions(SessionId, UserToken, UUIDDevice) VALUES (?,?,?)");

                    sessStatement.setString(1, session.getId());
                    sessStatement.setString(2, token);
                    sessStatement.setString(3, uuidDevice);

                }
                resultSet.close();
                ps.close();
                return compactJws;
            }
        }
        else {
            // check email in database (may be exist)
            boolean userExist = false;
            try {
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT UUIDUser FROM Users WHERE Email = ?");
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();

                // Get usernames and add its to list.
                while (rs.next()) {
                    userExist = true;
                }
                rs.close();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!userExist) {
                String uudUser = "";
                try {
                    Connection c = getConnection();
                    PreparedStatement ps = c.prepareStatement("INSERT INTO Users(UUIDUser, username, password) VALUES (?,?,?)");
                    uudUser = UUID.nameUUIDFromBytes((username + uuidDevice).getBytes()).toString();
                    ps.setString(1, uudUser);
                    ps.setString(2, username);
                    ps.setString(3, pass);
                    ps.executeUpdate();
                    ps.close();

                    PreparedStatement psDev = c.prepareStatement("INSERT INTO Devices(UUIDDevices, UUIDUser) VALUES (?,?)");
                    ps.setString(1, uuidDevice);
                    ps.setString(2, uudUser);
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                compactJws.remove("key");
                compactJws.remove("value");
                compactJws.put("key", "message");
                compactJws.put("value", uudUser);
            }
            else {
                compactJws.remove("key");
                compactJws.remove("value");
                compactJws.put("key", "userExist");
                compactJws.put("value", "Пользователь с таким e-mail уже зарегистрирован.");
            }
        }
        return compactJws;
    }

    private static String generateUUID(String name, String uuidDevice) {
        return UUID.nameUUIDFromBytes((name + uuidDevice).getBytes()).toString();
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

    public static void sendMessage(JSONObject message) {
        try {
            String str = message.toJSONString();
            System.out.println(str);
            session.getBasicRemote().sendText(str);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void updateLastSyncDate() {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE LastSyncTimestamp SET LastSyncTime = ?");
        ) {
            ps.setLong(1, getDateOfNow());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public static void insertLastSyncDate() {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO LastSyncTimestamp(LastSyncTime) VALUES (LastSyncTime = ?)");
        ) {
            ps.setLong(1, getDateOfNow());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public static long selectLastSyncDate() {
        long lastSyncDate = 0;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT LastSyncTime FROM LastSyncTimestamp");
        ) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lastSyncDate = rs.getLong("LastSyncTime");
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return lastSyncDate;
    }

    public static long getDateOfNow() {
        LocalDate localDate = LocalDate.now();
        Timestamp time = Timestamp.valueOf(localDate.atStartOfDay());
        return time.getTime();
    }

    /**
     * Checking deleted notes
     * todo передать клиенту, что запись удалена 
     */
    public static void deleteOldBills(ObservableList<Bills> billses, String uuidDevice, String uuidUser) {
        billses.forEach(element -> {
            long delDate = element.getDelDate();
            if (delDate > 0) {
                int delDevCount = 0;
                int devCount = 0;
                try (Connection c = getConnection();
                     PreparedStatement psDelNotes = c.prepareStatement("SELECT COUNT(UUIDDevice) AS delDevCount FROM DeletedNotes WHERE UUIDUser = ? AND UUIDNote = ? AND TableName = ?");
                     PreparedStatement psDevs = c.prepareStatement("SELECT COUNT(*) AS devCount FROM Devices WHERE UUIDUser = ?");
                ) {
                    psDelNotes.setString(1, element.getUuidUser());
                    psDelNotes.setString(2, element.getUuidBill());
                    psDelNotes.setString(3, "Bills");
                    ResultSet rs = psDelNotes.executeQuery();
                    while (rs.next()) {
                        delDevCount = rs.getInt("delDevCount");
                    }

                    psDevs.setString(1, element.getUuidUser());
                    ResultSet rsrs = psDevs.executeQuery();
                    while (rs.next()) {
                        devCount = rsrs.getInt("devCount");
                    }
                    psDelNotes.close();
                    psDevs.close();
                    rs.close();
                    rsrs.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

                if (delDevCount == devCount && delDevCount == 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("UPDATE Bills SET DeletedDate = ? WHERE UUIDBills = ?");
                    ) {
                        ps.setLong(1, delDate);
                        ps.setString(2, element.getUuidBill());
                        ps.executeUpdate();

                        PreparedStatement psDelNotes = c.prepareStatement("INSERT INTO DeletedNotes(UUIDDevice, UUIDUser, UUIDNote, TableName) VALUES (?,?,?,?)");
                        psDelNotes.setString(1, uuidDevice);
                        psDelNotes.setString(2, uuidUser);
                        psDelNotes.setString(3, element.getUuidBill());
                        psDelNotes.setString(4, "Bills");
                        psDelNotes.executeUpdate();

                        ps.close();
                        psDelNotes.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                } else if (delDevCount == devCount && delDevCount > 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("DELETE FROM Bills WHERE UUIDBills = ?");
                    ) {
                        ps.setString(1, element.getUuidBill());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void deleteOldCashes(ObservableList<Cashes> cashes, String uuidDevice, String uuidUser) {
        cashes.forEach(element -> {
            long delDate = element.getDelDate();
            if (delDate > 0) {
                int delDevCount = 0;
                int devCount = 0;
                try (Connection c = getConnection();
                     PreparedStatement psDelNotes = c.prepareStatement("SELECT COUNT(UUIDDevice) AS delDevCount FROM DeletedNotes WHERE UUIDUser = ? AND UUIDNote = ? AND TableName = ?");
                     PreparedStatement psDevs = c.prepareStatement("SELECT COUNT(*) AS devCount FROM Devices WHERE UUIDUser = ?");
                ) {
                    psDelNotes.setString(1, uuidUser);
                    psDelNotes.setString(2, element.getUuidCash());
                    psDelNotes.setString(3, "Cashes");
                    ResultSet rs = psDelNotes.executeQuery();
                    while (rs.next()) {
                        delDevCount = rs.getInt("delDevCount");
                    }

                    psDevs.setString(1, uuidUser);
                    ResultSet rsrs = psDevs.executeQuery();
                    while (rs.next()) {
                        devCount = rsrs.getInt("devCount");
                    }
                    psDelNotes.close();
                    psDevs.close();
                    rs.close();
                    rsrs.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

                if (delDevCount == devCount && delDevCount == 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("UPDATE Cashes SET DeletedDate = ? WHERE UUIDCash = ?");
                    ) {
                        ps.setLong(1, delDate);
                        ps.setString(2, element.getUuidCash());

                        PreparedStatement psDelNotes = c.prepareStatement("INSERT INTO DeletedNotes(UUIDDevice, UUIDUser, UUIDNote, TableName) VALUES (?,?,?,?)");
                        psDelNotes.setString(1, uuidDevice);
                        psDelNotes.setString(2, uuidUser);
                        psDelNotes.setString(3, element.getUuidCash());
                        psDelNotes.setString(4, "Cashes");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                } else if (delDevCount == devCount && delDevCount > 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("DELETE FROM Cashes WHERE UUIDCash = ?");
                    ) {
                        ps.setString(1, element.getUuidCash());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void deleteOldCategories(ObservableList<Categories> categories, String uuidUser, String uuidDevice) {
        categories.forEach(element -> {
            long delDate = element.getDeletedDate();
            if (delDate > 0) {
                int delDevCount = 0;
                int devCount = 0;
                try (Connection c = getConnection();
                     PreparedStatement psDelNotes = c.prepareStatement("SELECT COUNT(UUIDDevice) AS delDevCount FROM DeletedNotes WHERE UUIDUser = ? AND UUIDNote = ? AND TableName = ?");
                     PreparedStatement psDevs = c.prepareStatement("SELECT COUNT(*) AS devCount FROM Devices WHERE UUIDUser = ?");
                ) {
                    psDelNotes.setString(1, uuidUser);
                    psDelNotes.setString(2, element.getUuidCategory());
                    psDelNotes.setString(3, "Categories");
                    ResultSet rs = psDelNotes.executeQuery();
                    while (rs.next()) {
                        delDevCount = rs.getInt("delDevCount");
                    }

                    psDevs.setString(1, uuidUser);
                    ResultSet rsrs = psDevs.executeQuery();
                    while (rs.next()) {
                        devCount = rsrs.getInt("devCount");
                    }
                    psDelNotes.close();
                    psDevs.close();
                    rs.close();
                    rsrs.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

                if (delDevCount == devCount && delDevCount == 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("UPDATE Categories SET DeletedDate = ? WHERE UUIDCategories = ?");
                    ) {
                        ps.setLong(1, delDate);
                        ps.setString(2, element.getUuidCategory());

                        PreparedStatement psDelNotes = c.prepareStatement("INSERT INTO DeletedNotes(UUIDDevice, UUIDUser, UUIDNote, TableName) VALUES (?,?,?,?)");
                        psDelNotes.setString(1, uuidDevice);
                        psDelNotes.setString(2, uuidUser);
                        psDelNotes.setString(3, element.getUuidCategory());
                        psDelNotes.setString(4, "Categories");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                } else if (delDevCount == devCount && delDevCount > 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("DELETE FROM Categories WHERE UUIDCategory = ?");
                    ) {
                        ps.setString(1, element.getUuidCategory());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void deleteOldCostincomes(ObservableList<Costincomes> costincomes, String uuidUser, String uuidDevice) {
        costincomes.forEach(element -> {
            long delDate = element.getDeletedDate();
            if (delDate > 0) {
                int delDevCount = 0;
                int devCount = 0;
                try (Connection c = getConnection();
                     PreparedStatement psDelNotes = c.prepareStatement("SELECT COUNT(UUIDDevice) AS delDevCount FROM DeletedNotes WHERE UUIDUser = ? AND UUIDNote = ? AND TableName = ?");
                     PreparedStatement psDevs = c.prepareStatement("SELECT COUNT(*) AS devCount FROM Devices WHERE UUIDUser = ?");
                ) {
                    psDelNotes.setString(1, uuidUser);
                    psDelNotes.setString(2, element.getCostincUUID());
                    psDelNotes.setString(3, "MoneyTurn");
                    ResultSet rs = psDelNotes.executeQuery();
                    while (rs.next()) {
                        delDevCount = rs.getInt("delDevCount");
                    }

                    psDevs.setString(1, uuidUser);
                    ResultSet rsrs = psDevs.executeQuery();
                    while (rs.next()) {
                        devCount = rsrs.getInt("devCount");
                    }
                    psDelNotes.close();
                    psDevs.close();
                    rs.close();
                    rsrs.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

                if (delDevCount == devCount && delDevCount == 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("UPDATE MoneyTurn SET DeletedDate = ? WHERE UUIDMoneyTurn = ?");
                    ) {
                        ps.setLong(1, delDate);
                        ps.setString(2, element.getUuidCategory());

                        PreparedStatement psDelNotes = c.prepareStatement("INSERT INTO DeletedNotes(UUIDDevice, UUIDUser, UUIDNote, TableName) VALUES (?,?,?,?)");
                        psDelNotes.setString(1, uuidDevice);
                        psDelNotes.setString(2, uuidUser);
                        psDelNotes.setString(3, element.getCostincUUID());
                        psDelNotes.setString(4, "MoneyTurn");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                } else if (delDevCount == devCount && delDevCount > 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("DELETE FROM MoneyTurn WHERE UUIDMoneyTurn = ?");
                    ) {
                        ps.setString(1, element.getCostincUUID());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void deleteOldCurrencies(ObservableList<Currencies> currencies, String uuidUser, String uuidDevice) {
        currencies.forEach(element -> {
            long delDate = element.getDeletedDate();
            if (delDate > 0) {
                int delDevCount = 0;
                int devCount = 0;
                try (Connection c = getConnection();
                     PreparedStatement psDelNotes = c.prepareStatement("SELECT COUNT(UUIDDevice) AS delDevCount FROM DeletedNotes WHERE UUIDUser = ? AND UUIDNote = ? AND TableName = ?");
                     PreparedStatement psDevs = c.prepareStatement("SELECT COUNT(*) AS devCount FROM Devices WHERE UUIDUser = ?");
                ) {
                    psDelNotes.setString(1, uuidUser);
                    psDelNotes.setString(2, element.getCurrencyUUID());
                    psDelNotes.setString(3, "Currencies");
                    ResultSet rs = psDelNotes.executeQuery();
                    while (rs.next()) {
                        delDevCount = rs.getInt("delDevCount");
                    }

                    psDevs.setString(1, uuidUser);
                    ResultSet rsrs = psDevs.executeQuery();
                    while (rs.next()) {
                        devCount = rsrs.getInt("devCount");
                    }
                    psDelNotes.close();
                    psDevs.close();
                    rs.close();
                    rsrs.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

                if (delDevCount == devCount && delDevCount == 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("UPDATE Currencies SET DeletedDate = ? WHERE UUIDCurrency = ?");
                    ) {
                        ps.setLong(1, delDate);
                        ps.setString(2, element.getCurrencyUUID());

                        PreparedStatement psDelNotes = c.prepareStatement("INSERT INTO DeletedNotes(UUIDDevice, UUIDUser, UUIDNote, TableName) VALUES (?,?,?,?)");
                        psDelNotes.setString(1, uuidDevice);
                        psDelNotes.setString(2, uuidUser);
                        psDelNotes.setString(3, element.getCurrencyUUID());
                        psDelNotes.setString(4, "Currencies");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                } else if (delDevCount == devCount && delDevCount > 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("DELETE FROM Currencies WHERE UUIDCurrency = ?");
                    ) {
                        ps.setString(1, element.getCurrencyUUID());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void deleteOldMeasures(ObservableList<Measures> measures, String uuidUser, String uuidDevice) {
        measures.forEach(element -> {
            long delDate = element.getDeletedDate();
            if (delDate > 0) {
                int delDevCount = 0;
                int devCount = 0;
                try (Connection c = getConnection();
                     PreparedStatement psDelNotes = c.prepareStatement("SELECT COUNT(UUIDDevice) AS delDevCount FROM DeletedNotes WHERE UUIDUser = ? AND UUIDNote = ? AND TableName = ?");
                     PreparedStatement psDevs = c.prepareStatement("SELECT COUNT(*) AS devCount FROM Devices WHERE UUIDUser = ?");
                ) {
                    psDelNotes.setString(1, uuidUser);
                    psDelNotes.setString(2, element.getUuidMeasure());
                    psDelNotes.setString(3, "Measures");
                    ResultSet rs = psDelNotes.executeQuery();
                    while (rs.next()) {
                        delDevCount = rs.getInt("delDevCount");
                    }

                    psDevs.setString(1, uuidUser);
                    ResultSet rsrs = psDevs.executeQuery();
                    while (rs.next()) {
                        devCount = rsrs.getInt("devCount");
                    }
                    psDelNotes.close();
                    psDevs.close();
                    rs.close();
                    rsrs.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

                if (delDevCount == devCount && delDevCount == 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("UPDATE Measures SET DeletedDate = ? WHERE UUIDMeasures = ?");
                    ) {
                        ps.setLong(1, delDate);
                        ps.setString(2, element.getUuidMeasure());

                        PreparedStatement psDelNotes = c.prepareStatement("INSERT INTO DeletedNotes(UUIDDevice, UUIDUser, UUIDNote, TableName) VALUES (?,?,?,?)");
                        psDelNotes.setString(1, uuidDevice);
                        psDelNotes.setString(2, uuidUser);
                        psDelNotes.setString(3, element.getUuidMeasure());
                        psDelNotes.setString(4, "Measures");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                } else if (delDevCount == devCount && delDevCount > 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("DELETE FROM Measures WHERE UUIDMeasures = ?");
                    ) {
                        ps.setString(1, element.getUuidMeasure());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void deleteOldPlans(ObservableList<Planning> plannings, String uuidUser, String uuidDevice) {
        plannings.forEach(element -> {
            long delDate = element.getDeletedDate();
            if (delDate > 0) {
                int delDevCount = 0;
                int devCount = 0;
                try (Connection c = getConnection();
                     PreparedStatement psDelNotes = c.prepareStatement("SELECT COUNT(UUIDDevice) AS delDevCount FROM DeletedNotes WHERE UUIDUser = ? AND UUIDNote = ? AND TableName = ?");
                     PreparedStatement psDevs = c.prepareStatement("SELECT COUNT(*) AS devCount FROM Devices WHERE UUIDUser = ?");
                ) {
                    psDelNotes.setString(1, uuidUser);
                    psDelNotes.setString(2, element.getPlanUUID());
                    psDelNotes.setString(3, "Planning");
                    ResultSet rs = psDelNotes.executeQuery();
                    while (rs.next()) {
                        delDevCount = rs.getInt("delDevCount");
                    }

                    psDevs.setString(1, uuidUser);
                    ResultSet rsrs = psDevs.executeQuery();
                    while (rs.next()) {
                        devCount = rsrs.getInt("devCount");
                    }
                    psDelNotes.close();
                    psDevs.close();
                    rs.close();
                    rsrs.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

                if (delDevCount == devCount && delDevCount == 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("UPDATE Planning SET DeletedDate = ? WHERE UUIDPlanning = ?");
                    ) {
                        ps.setLong(1, delDate);
                        ps.setString(2, element.getPlanUUID());

                        PreparedStatement psDelNotes = c.prepareStatement("INSERT INTO DeletedNotes(UUIDDevice, UUIDUser, UUIDNote, TableName) VALUES (?,?,?,?)");
                        psDelNotes.setString(1, uuidDevice);
                        psDelNotes.setString(2, uuidUser);
                        psDelNotes.setString(3, element.getPlanUUID());
                        psDelNotes.setString(4, "Planning");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                } else if (delDevCount == devCount && delDevCount > 0) {
                    try (Connection c = getConnection();
                         PreparedStatement ps = c.prepareStatement("DELETE FROM Planning WHERE UUIDPlanning = ?");
                    ) {
                        ps.setString(1, element.getPlanUUID());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NamingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Insert or update notes from client.
     *
     */

    public static void insertNewBills(ObservableList<Bills> bList, String uuidDevice, JSONObject responseWithData) {
        bList.forEach(element -> {
            String uuid = element.getUuidBill();
            if (uuid.isEmpty()) {
                // Insert bills to database, if uuid equals null
                // it's first syncronization
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO Bills(UUIDBills, BillName, Date, StartBalance, Note, UUIDUser) VALUES (?,?,?,?,?,?)");
                ) {
                    String uuidBill = generateUUID(element.getBillName(), uuidDevice);
                    ps.setString(1, uuidBill);
                    ps.setString(2, element.getBillName());
                    ps.setLong(3, element.getDate());
                    ps.setDouble(4, element.getStartBalance());
                    ps.setString(5, element.getNote());
                    ps.setString(6, uuidBill);
                    ps.executeUpdate();

                    billsUUIDs.put(element.getBillId(), uuidBill);

                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
            else {
                // Update bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("UPDATE Bills SET BillName = ?, Date = ?, StartBalance = ?, Note = ?, UUIDUser = ?, UpdatedDate = ?");
                ) {
                    ps.setString(2, element.getBillName());
                    ps.setLong(3, element.getDate());
                    ps.setDouble(4, element.getStartBalance());
                    ps.setString(5, element.getNote());
                    ps.setString(6, element.getUuidUser());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

            }
        });

        if (!billsUUIDs.isEmpty()) {
            responseWithData.put("key", "billsUUIDs");
            responseWithData.put("value", billsUUIDs);
        }
    }

    public static void insertNewCurrencies(ObservableList<Currencies> currencies, String uuidDevice, JSONObject responseWithData) {
        currencies.forEach(element -> {
            String uuid = element.getCurrencyUUID();
            if (uuid.isEmpty()) {
                // Insert bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO Currencies(UUIDCurrency, CurrencyName, NameShort) VALUES (?,?,?)");
                ) {
                    String uuidCur = generateUUID(element.getCurrencyName(), uuidDevice);
                    ps.setString(1, uuidCur);
                    ps.setString(2, element.getCurrencyName());
                    ps.setString(3, element.getCurrencyShortName());
                    ps.executeUpdate();

                    cursUUIDs.put(element.getCurrencyId(), uuidCur);

                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
            else {
                // Update bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("UPDATE Currencies SET CurrencyName = ?, NameShort = ?");
                ) {
                    ps.setString(1, element.getCurrencyName());
                    ps.setString(2, element.getCurrencyShortName());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

            }
        });

        if (!cursUUIDs.isEmpty()) {
            responseWithData.put("key", "cursUUIDs");
            responseWithData.put("value", cursUUIDs);
        }

    }

    public static void insertNewCashes(ObservableList<Cashes> cashes, String uuidDevice, JSONObject responseWithData) {
        HashMap<Integer, String> cashUUIDs = new HashMap<>();
        cashes.forEach(element -> {
            String uuid = element.getUuidCash();
            if (uuid.isEmpty()) {
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO Cashes(UUIDCash, Amount, UUIDCurrency, UUIDBills) VALUES (?,?,?,?)");
                ) {
                    String nameForUUID = Integer.toString(element.getBillId()) + Integer.toString(element.getCurrencyId()) + Double.toString(element.getAmount());
                    String uuidCash = generateUUID(nameForUUID, uuidDevice);
                    ps.setString(1, uuidCash);
                    ps.setDouble(2, element.getAmount());
                    ps.setString(4, cursUUIDs.get(element.getCurrencyId()));
                    ps.setString(3, billsUUIDs.get(element.getBillId()));
                    ps.executeUpdate();

                    cashUUIDs.put(element.getCashId(), uuidCash);

                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            } else {
                // Update bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("UPDATE Cashes SET Amount = ?, UUIDCurrency = ?");
                ) {
                    ps.setDouble(1, element.getAmount());
                    ps.setString(2, cursUUIDs.get(element.getCurrencyId()));
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

            }
        });

        if (!cashUUIDs.isEmpty()) {
            responseWithData.put("key", "cashUUIDs");
            responseWithData.put("value", cashUUIDs);
        }

    }

    public static void insertNewCategories(ObservableList<Categories> categories, String uuidDevice, JSONObject responseWithData) {
        categories.forEach(element -> {
            String uuid = element.getUuidCategory();
            if (uuid.isEmpty()) {
                // Insert bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO Categories(UUIDCategories, CategoryName, CostIncome) VALUES (?,?,?)");
                ) {
                    String uuidCat = generateUUID(element.getCategoryName(), uuidDevice);
                    ps.setString(1, uuidCat);
                    ps.setString(2, element.getCategoryName());
                    ps.setBoolean(3, element.isCostincome());
                    ps.executeUpdate();

                    categoriesUUIDs.put(element.getCategoryId(), uuidCat);

                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
            else {
                // Update bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("UPDATE Categories SET CategoryName = ?");
                ) {
                    ps.setString(1, element.getCategoryName());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

            }
        });

        if (!categoriesUUIDs.isEmpty()) {
            responseWithData.put("key", "categoriesUUIDs");
            responseWithData.put("value", categoriesUUIDs);
        }

    }

    public static void insertNewMeasures(ObservableList<Measures> measures, String uuidDevice, JSONObject responseWithData) {
        measures.forEach(element -> {
            String uuid = element.getUuidMeasure();
            if (uuid.isEmpty()) {
                // Insert bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO Measures(UUIDMeasures, MeasureName) VALUES (?,?)");
                ) {
                    String uuidMeasure = generateUUID(element.getMeasureName(), uuidDevice);
                    ps.setString(1, uuidMeasure);
                    ps.setString(2, element.getMeasureName());
                    ps.executeUpdate();

                    measuresUUIDs.put(element.getMeasureId(), uuidMeasure);

                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
            else {
                // Update bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("UPDATE Measures SET MeasureName = ?");
                ) {
                    ps.setString(1, element.getMeasureName());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

            }
        });

        if (!measuresUUIDs.isEmpty()) {
            responseWithData.put("key", "measuresUUIDs");
            responseWithData.put("value", measuresUUIDs);
        }

    }

    public static void insertNewCostincomes(ObservableList<Costincomes> costincomes, String uuidDevice, JSONObject responseWithData) {
        HashMap<Integer, String> costincUUIDs = new HashMap<>();
        costincomes.forEach(element -> {
            String uuid = element.getCostincUUID();
            if (uuid.isEmpty()) {
                // Insert bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO MoneyTurn(UUIDMoneyTurn, Date, UUIDBills, Count, UUIDMeasure, Note, CostIncome, Amount, UUIDCategory) VALUES (?,?,?,?,?,?,?,?,?)");
                ) {
                    String uuidCostinc = generateUUID(element.getCostincNote(), uuidDevice);
                    ps.setString(1, uuidCostinc);
                    ps.setLong(2, element.getCostincDate());
                    ps.setString(3, billsUUIDs.get(element.getBillId()));
                    ps.setInt(4, element.getCostincCount());
                    ps.setString(5, measuresUUIDs.get(element.getMeasureId()));
                    ps.setString(6, element.getCostincNote());
                    ps.setBoolean(9, element.isCostincome());
                    ps.setDouble(7, element.getCostincAmount());
                    ps.setString(8, categoriesUUIDs.get(element.getCategoryId()));
                    ps.executeUpdate();

                    costincUUIDs.put(element.getCostincId(), uuidCostinc);

                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
            else {
                // Update bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("UPDATE MoneyTurn SET Date = ?, UUIDBills = ?, Count = ?, UUIDMeasure = ?, Note = ?, Costincome = ?, Amount = ?, UUIDCategory = ?");
                ) {
                    ps.setLong(1, element.getCostincDate());
                    ps.setString(2, billsUUIDs.get(element.getBillId()));
                    ps.setInt(3, element.getCostincCount());
                    ps.setString(4, measuresUUIDs.get(element.getMeasureId()));
                    ps.setString(5, element.getCostincNote());
                    ps.setDouble(6, element.getCostincAmount());
                    ps.setString(7, categoriesUUIDs.get(element.getCategoryId()));
                    ps.setBoolean(8, element.isCostincome());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

            }
        });

        if (!costincUUIDs.isEmpty()) {
            responseWithData.put("key", "costincUUIDs");
            responseWithData.put("value", costincUUIDs);
        }

    }

    public static void insertNewPlannings(ObservableList<Planning> plannings, String uuidDevice, JSONObject responseWithData) {
        HashMap<Integer, String> plansUUIDs = new HashMap<>();
        plannings.forEach(element -> {
            String uuid = element.getPlanUUID();
            if (uuid.equals("")) {
                // Insert bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO Planning(UUIDPlanning, Date, UUIDBills, Count, Note, Amount, UUIDMeasure, CostIncome, UUIDCategory) VALUES (?,?,?,?,?,?,?,?,?)");
                ) {
                    String uuidPlan = generateUUID(element.getPlanningNote(), uuidDevice);
                    ps.setString(1, uuidPlan);
                    ps.setLong(2, element.getPlanningDate());
                    ps.setString(3, billsUUIDs.get(element.getBillId()));
                    ps.setInt(4, element.getPlanningCount());
                    ps.setString(5, element.getPlanningNote());
                    ps.setDouble(6, element.getPlanningAmount());
                    ps.setString(7, measuresUUIDs.get(element.getMeasureId()));
                    ps.setBoolean(8, element.getPlanningCostincome());
                    ps.setString(9, categoriesUUIDs.get(element.getCategoryId()));

                    plansUUIDs.put(element.getPlanningId(), uuidPlan);

                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
            else {
                // Update bills to database, if uuid equals null
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement("UPDATE Planning SET Date = ?, UUIDBills = ?, Count = ?, Note = ?, Amount = ?, UUIDMeasure = ?, CostIncome = ?, UUIDCategory = ?");
                ) {
                    ps.setLong(1, element.getPlanningDate());
                    ps.setString(2, billsUUIDs.get(element.getBillId()));
                    ps.setInt(3, element.getPlanningCount());
                    ps.setString(4, element.getPlanningNote());
                    ps.setDouble(5, element.getPlanningAmount());
                    ps.setString(6, measuresUUIDs.get(element.getMeasureId()));
                    ps.setBoolean(7, element.getPlanningCostincome());
                    ps.setString(8, categoriesUUIDs.get(element.getCategoryId()));
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

            }
        });

        if (!plansUUIDs.isEmpty()) {
            responseWithData.put("key", "plansUUIDs");
            responseWithData.put("value", plansUUIDs);
        }

    }

    /**
     * Get last updates from server
     *
     */

    public static ObservableList<Cashes> getOldCashesFromServer(long lsd) {
        ResultSet rs;
        ObservableList<Cashes> cashesList = FXCollections.observableArrayList();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM Cashes b WHERE b.UpdatedDate BETWEEN (SELECT b.UpdatedDate FROM Bills b) AND ?");
        ) {
            ps.setLong(1, lsd);
            rs = ps.executeQuery();

            while ( rs.next() ) {
                cashesList.add(new Cashes(rs.getInt("CashId"), rs.getInt("BillId"), rs.getInt("CurrencyId"), rs.getDouble("Amount"), rs.getLong("DeletedDate"), rs.getString("UUIDCash")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return cashesList;
    }

    public static ObservableList<Bills> getOldBillsFromServer(long lsd) {
        ResultSet rs;
        ObservableList<Bills> billsList = FXCollections.observableArrayList();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM Bills b WHERE b.UpdatedDate BETWEEN (SELECT b.UpdatedDate FROM Bills b) AND ?");
        ) {
            ps.setLong(1, lsd);
            rs = ps.executeQuery();

            while ( rs.next() ) {
                billsList.add(new Bills(rs.getLong("Date"), rs.getString("BillName"), rs.getString("Note"), rs.getDouble("StartBalance"), rs.getString("UUIDBills"), rs.getString("UUIDUser")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return billsList;
    }

    public static ObservableList<Categories> getOldCategoriesFromServer(long lsd) {
        ResultSet rs;
        ObservableList<Categories> categoriesList = FXCollections.observableArrayList();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM Categories b WHERE b.UpdatedDate BETWEEN (SELECT b.UpdatedDate FROM Bills b) AND ?");
        ) {
            ps.setLong(1, lsd);
            rs = ps.executeQuery();

            while ( rs.next() ) {
                categoriesList.add(new Categories(rs.getString("UUIDCategory"), rs.getString("CategoryName"), rs.getBoolean("CostIncome")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return categoriesList;
    }

    public static ObservableList<Costincomes> getOldCostincomesFromServer(long lsd) {
        ResultSet rs;
        ObservableList<Costincomes> costincomesList = FXCollections.observableArrayList();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM MoneyTurn b WHERE b.UpdatedDate BETWEEN (SELECT b.UpdatedDate FROM Bills b) AND ?");
        ) {
            ps.setLong(1, lsd);
            rs = ps.executeQuery();

            while ( rs.next() ) {
                costincomesList.add(new Costincomes(rs.getString("UUIDMoneyTurn"), rs.getLong("Date"), rs.getString("UUIDBills"), rs.getInt("Count"), rs.getString("UUIDMeasure"), rs.getString("Note"), rs.getBoolean("CostIntcome"), rs.getDouble("Amount"), rs.getString("UUIDCategory")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return costincomesList;
    }

    public static ObservableList<Currencies> getOldCurrenciesFromServer(long lsd) {
        ResultSet rs;
        ObservableList<Currencies> currenciesList = FXCollections.observableArrayList();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM Currencies b WHERE b.UpdatedDate BETWEEN (SELECT b.UpdatedDate FROM Bills b) AND ?");
        ) {
            ps.setLong(1, lsd);
            rs = ps.executeQuery();

            while ( rs.next() ) {
                currenciesList.add(new Currencies(rs.getString("UUIDCurrency"), rs.getString("CurrencyName"), rs.getString("NameShort")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return currenciesList;
    }

    public static ObservableList<Measures> getOldMeasuressFromServer(long lsd) {
        ResultSet rs;
        ObservableList<Measures> measuresList = FXCollections.observableArrayList();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM Measures b WHERE b.UpdatedDate BETWEEN (SELECT b.UpdatedDate FROM Bills b) AND ?");
        ) {
            ps.setLong(1, lsd);
            rs = ps.executeQuery();

            while ( rs.next() ) {
                measuresList.add(new Measures(rs.getString("UUIDMeasures"), rs.getString("MeasureName")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return measuresList;
    }

    public static ObservableList<Planning> getOldPlanningFromServer(long lsd) {
        ResultSet rs;
        ObservableList<Planning> planningsList = FXCollections.observableArrayList();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM Planning b WHERE b.UpdatedDate BETWEEN (SELECT b.UpdatedDate FROM Bills b) AND ?");
        ) {
            ps.setLong(1, lsd);
            rs = ps.executeQuery();

            while ( rs.next() ) {
                planningsList.add(new Planning(rs.getString("UUIDPlanning"), rs.getLong("Date"), rs.getString("UUIDBills"), rs.getInt("Count"), rs.getString("Note"), rs.getDouble("Amount"), rs.getString("UUIDMeasure"), rs.getBoolean("CostIntcome") , rs.getString("UUIDCategory")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return planningsList;
    }

}


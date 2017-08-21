package homeaccApp.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Vellial on 20.11.2015.
 */
public class DBapi {
    public static String dbUrl = "jdbc:sqlite:lightside2";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(dbUrl);
    }

}

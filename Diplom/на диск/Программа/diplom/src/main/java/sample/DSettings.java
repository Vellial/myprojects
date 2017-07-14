package sample;

/**
 * Created by Vellial on 18.01.2017.
 */
public class DSettings {
    private String mysqlServer = "";
    private String mysqlDb = "";
    private String mysqlUser = "";
    private String mysqlPassword = "";
    private String mysqlPort = "";

    private String oracleServer = "";
    private String oracleDb = "";
    private String oracleUser = "";
    private String oraclePassword = "";
    private String oraclePort = "";

    public DSettings(String mysqlServer, String mysqlPort, String mysqlDb, String mysqlUser, String mysqlPassword, String oracleServer, String oraclePort, String oracleDb, String oracleUser, String oraclePassword) {
        this.mysqlServer = mysqlServer;
        this.mysqlPort = mysqlPort;
        this.mysqlDb = mysqlDb;
        this.mysqlUser = mysqlUser;
        this.mysqlPassword = mysqlPassword;

        this.oracleServer = oracleServer;
        this.oraclePort = oraclePort;
        this.oracleDb = oracleDb;
        this.oracleUser = oracleUser;
        this.oraclePassword = oraclePassword;
    }

    public String getMysqlServer() {
        return mysqlServer;
    }

    public void setMysqlServer(String mysqlServer) {
        this.mysqlServer = mysqlServer;
    }

    public String getMysqlDb() {
        return mysqlDb;
    }

    public void setMysqlDb(String mysqlDb) {
        this.mysqlDb = mysqlDb;
    }

    public String getMysqlUser() {
        return mysqlUser;
    }

    public void setMysqlUser(String mysqlUser) {
        this.mysqlUser = mysqlUser;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }

    public String getMysqlPort() {
        return mysqlPort;
    }

    public void setMysqlPort(String mysqlPort) {
        this.mysqlPort = mysqlPort;
    }

    public String getOracleServer() {
        return oracleServer;
    }

    public void setOracleServer(String oracleServer) {
        this.oracleServer = oracleServer;
    }

    public String getOracleDb() {
        return oracleDb;
    }

    public void setOracleDb(String oracleDb) {
        this.oracleDb = oracleDb;
    }

    public String getOracleUser() {
        return oracleUser;
    }

    public void setOracleUser(String oracleUser) {
        this.oracleUser = oracleUser;
    }

    public String getOraclePassword() {
        return oraclePassword;
    }

    public void setOraclePassword(String oraclePassword) {
        this.oraclePassword = oraclePassword;
    }

    public String getOraclePort() {
        return oraclePort;
    }

    public void setOraclePort(String oraclePort) {
        this.oraclePort = oraclePort;
    }
}

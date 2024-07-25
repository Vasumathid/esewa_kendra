package com.esewa_kendra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConfig {
    private static final String URL = "jdbc:sqlserver://DESKTOP-KRISHNA\\SQLEXPRESS01:1433;databaseName=esewa;encrypt=false;trustServerCertificate=false;integratedSecurity=true;";

    private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER_CLASS);
        return DriverManager.getConnection(URL);
    }
}

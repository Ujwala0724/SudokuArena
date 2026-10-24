package com.sudokuarena.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Opens JDBC connections using the settings in src/main/resources/db.properties. */
public final class DBUtil {

    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties was not found on the classpath.");
            }
            CONFIG.load(in);
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password"));
    }
}

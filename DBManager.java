package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class DBManager {
    private static Connection conn;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
        }
        return conn;
    }

    // Generic helper to run SELECT queries returning list of maps (column->value)
    public static List<Map<String, Object>> fetchAll(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(sql, params);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> out = new ArrayList<>();
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(md.getColumnLabel(i), rs.getObject(i));
                }
                out.add(row);
            }
            return out;
        }
    }

    public static int execute(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(sql, params)) {
            return ps.executeUpdate();
        }
    }

    public static PreparedStatement prepare(String sql, Object... params) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }

    public static Object insertAndGetId(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(sql, params)) {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getObject(1);
            }
        }
        return null;
    }
}

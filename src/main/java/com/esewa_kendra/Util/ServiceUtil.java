package com.esewa_kendra.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import com.esewa_kendra.DBConfig;

public class ServiceUtil {
    public Map<String, String> getServiceDetails(Connection conn, String serviceId, int bookingId) throws SQLException {
        Map<String, String> serviceDetails = new LinkedHashMap<>();
        String serviceName = getServiceNameById(conn, serviceId);
        if (serviceName != null) {
            String tableName = getTableNameForService(serviceName);
            if (tableName != null) {
                String query = "SELECT * FROM " + tableName + " WHERE booking_id = ?";
                try (PreparedStatement serviceStmt = conn.prepareStatement(query)) {
                    serviceStmt.setInt(1, bookingId);

                    try (ResultSet rs = serviceStmt.executeQuery()) {
                        if (rs.next()) {
                            ResultSetMetaData metaData = rs.getMetaData();
                            int columnCount = metaData.getColumnCount();

                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = metaData.getColumnName(i);
                                String value = rs.getString(columnName);
                                serviceDetails.put(columnName, value);
                            }
                        }
                    }
                }
            }
        }
        return serviceDetails;
    }

    public String formatColumnName(String columnName) {
        // Split the column name by underscores
        String[] words = columnName.split("_");

        // Capitalize the first letter of each word
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        // Trim the trailing space and return the result
        return formattedName.toString().trim();
    }

    public String getServiceNameById(Connection conn, String serviceId) throws SQLException {
        String serviceName = null;
        String query = "SELECT name FROM services WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(serviceId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    serviceName = rs.getString("name");
                }
            }
        }
        return serviceName;
    }

    public String getTableNameForService(String serviceName) {
        return serviceName.replace("for", "").replaceAll("\\s+", "_").toLowerCase();
    }

    public String getStateNameById(Connection conn, String stateId) throws SQLException {
        String stateName = null;
        String query = "SELECT name FROM states WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(stateId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stateName = rs.getString("name");
                }
            }
        }
        return stateName;
    }

    public String getDistrictNameById(Connection conn, String districtId) throws SQLException {
        String districtName = null;
        String query = "SELECT name FROM districts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(districtId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    districtName = rs.getString("name");
                }
            }
        }
        return districtName;
    }

    public String getCourtComplexNameById(Connection conn, String courtComplexId) throws SQLException {
        String courtComplexName = null;
        String query = "SELECT name FROM court_complexes WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(courtComplexId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    courtComplexName = rs.getString("name");
                }
            }
        }
        return courtComplexName;
    }

    public String getKendraNameById(Connection conn, String kendraId) throws SQLException {
        String kendraName = null;
        String query = "SELECT name FROM sewa_kendras WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(kendraId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    kendraName = rs.getString("name");
                }
            }
        }
        return kendraName;
    }

    public String getTableNameForServiceById(int serviceId) throws SQLException {
        String tableName = null;
        String query = "SELECT name FROM services WHERE id = ?";
        DBConfig DBConfig = new DBConfig();
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, serviceId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String serviceName = rs.getString("name");
                tableName = getTableNameForService(serviceName);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return tableName;
    }
}

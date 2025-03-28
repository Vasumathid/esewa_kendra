package com.esewa_kendra;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/bookService")
public class BookServiceServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String stateId = request.getParameter("state");
        String districtId = request.getParameter("district");
        String courtComplexId = request.getParameter("courtComplex");
        String kendraId = request.getParameter("kendra");
        String serviceId = request.getParameter("serviceType");
        String advocateName = request.getParameter("advocateName");
        String enrollmentNumber = request.getParameter("enrollmentNumber");
        String phoneNumber = request.getParameter("phoneNumber");
        String email = request.getParameter("email");
        String status = "Confirmed";
        String tokenNumber = UUID.randomUUID().toString(); // Generate unique token number

        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);

            // Insert booking details
            String bookingQuery = "INSERT INTO bookings (state_id, district_id, court_complex_id, kendra_id, service_id, advocate_name, enrollment_number, phone_number, email, status, token_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingQuery, Statement.RETURN_GENERATED_KEYS)) {
                bookingStmt.setInt(1, Integer.parseInt(stateId));
                bookingStmt.setInt(2, Integer.parseInt(districtId));
                bookingStmt.setInt(3, Integer.parseInt(courtComplexId));
                bookingStmt.setInt(4, Integer.parseInt(kendraId));
                bookingStmt.setInt(5, Integer.parseInt(serviceId));
                bookingStmt.setString(6, advocateName);
                bookingStmt.setString(7, enrollmentNumber);
                bookingStmt.setString(8, phoneNumber);
                bookingStmt.setString(9, email);
                bookingStmt.setString(10, status);
                bookingStmt.setString(11, tokenNumber);
                bookingStmt.executeUpdate();

                try (ResultSet rs = bookingStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int bookingId = rs.getInt(1);
                        // Insert service-specific details dynamically
                        insertServiceDetails(conn, serviceId, bookingId, request);
                    }
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw new ServletException("Error inserting booking details", e);
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new ServletException("Database connection error", e);
        }

        response.sendRedirect("generateToken?token=" + tokenNumber);
    }

    private void insertServiceDetails(Connection conn, String serviceId, int bookingId, HttpServletRequest request)
            throws SQLException {
        String serviceName = getServiceNameById(conn, serviceId);
        if (serviceName != null) {
            String tableName = getTableNameForService(serviceName);
            if (tableName != null) {
                String insertQuery = buildDynamicInsertQuery(conn, tableName, serviceId, bookingId, request);
                if (insertQuery != null) {
                    try (PreparedStatement serviceStmt = conn.prepareStatement(insertQuery)) {
                        setPreparedStatementParameters(serviceStmt, serviceId, bookingId, request);
                        serviceStmt.executeUpdate();
                    }
                }
            }
        }
    }

    private String getServiceNameById(Connection conn, String serviceId) throws SQLException {
        String serviceName = null;
        String serviceTypeQuery = "SELECT name FROM services WHERE id = ?";
        try (PreparedStatement serviceTypeStmt = conn.prepareStatement(serviceTypeQuery)) {
            serviceTypeStmt.setInt(1, Integer.parseInt(serviceId));
            try (ResultSet serviceTypeRs = serviceTypeStmt.executeQuery()) {
                if (serviceTypeRs.next()) {
                    serviceName = serviceTypeRs.getString("name");
                }
            }
        }
        return serviceName;
    }

    private String getTableNameForService(String serviceName) {
        switch (serviceName.toLowerCase()) {
            case "efiling registration":
                return "efiling_registration";
            case "scanning":
                return "scanning";
            case "video conferencing":
                return "video_conferencing";
            case "assistance for filing":
                return "assistance_filing";
            default:
                return null;
        }
    }

    private String buildDynamicInsertQuery(Connection conn, String tableName, String serviceId, int bookingId,
            HttpServletRequest request) throws SQLException {
        StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" (booking_id");

        Map<String, String> columnValues = new HashMap<>();
        columnValues.put("booking_id", String.valueOf(bookingId));

        String selectColumnsQuery = "SELECT column_name, data_type FROM service_columns WHERE service_id = ?";
        Map<String, String> columnDataTypes = new HashMap<>();

        try (PreparedStatement columnStmt = conn.prepareStatement(selectColumnsQuery)) {
            columnStmt.setInt(1, Integer.parseInt(serviceId));
            try (ResultSet columnRs = columnStmt.executeQuery()) {
                while (columnRs.next()) {
                    String columnName = columnRs.getString("column_name");
                    String dataType = columnRs.getString("data_type");
                    String value = request.getParameter(columnName);

                    if (value != null && !value.isEmpty()) {
                        query.append(", ").append(columnName);
                        columnValues.put(columnName, value);
                        columnDataTypes.put(columnName, dataType);
                    }
                }
            }
        }

        query.append(") VALUES (?");
        for (int i = 1; i < columnValues.size(); i++) {
            query.append(", ?");
        }
        query.append(")");

        return query.toString();
    }

    private void setPreparedStatementParameters(PreparedStatement preparedStatement, String serviceId, int bookingId,
            HttpServletRequest request) throws SQLException {
        Map<String, String> columnValues = new HashMap<>();
        columnValues.put("booking_id", String.valueOf(bookingId));

        String selectColumnsQuery = "SELECT column_name, data_type FROM service_columns WHERE service_id = ?";
        Map<String, String> columnDataTypes = new HashMap<>();

        try (PreparedStatement columnStmt = preparedStatement.getConnection().prepareStatement(selectColumnsQuery)) {
            columnStmt.setInt(1, Integer.parseInt(serviceId));
            try (ResultSet columnRs = columnStmt.executeQuery()) {
                while (columnRs.next()) {
                    String columnName = columnRs.getString("column_name");
                    String dataType = columnRs.getString("data_type");
                    String value = request.getParameter(columnName);

                    if (value != null && !value.isEmpty()) {
                        columnValues.put(columnName, value);
                        columnDataTypes.put(columnName, dataType);
                    }
                }
            }
        }

        int index = 1;
        for (Map.Entry<String, String> entry : columnValues.entrySet()) {
            String columnName = entry.getKey();
            String value = entry.getValue();
            String dataType = columnDataTypes.get(columnName);

            if ("booking_id".equals(columnName)) {
                preparedStatement.setInt(index++, Integer.parseInt(value));
            } else {
                switch (dataType.toUpperCase()) {
                    case "INT":
                        preparedStatement.setInt(index++, Integer.parseInt(value));
                        break;
                    case "DECIMAL":
                        preparedStatement.setBigDecimal(index++, new BigDecimal(value));
                        break;
                    case "DATE":
                        if (value != null && !value.trim().isEmpty()) {
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                java.util.Date parsedDate = dateFormat.parse(value);
                                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                                preparedStatement.setDate(index++, sqlDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                throw new SQLException("Error parsing date: " + value, e);
                            }
                        } else {
                            preparedStatement.setNull(index++, java.sql.Types.DATE);
                        }
                        break;
                    default:
                        preparedStatement.setString(index++, value);
                }
            }
        }
    }
}

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
import java.sql.Date;
import com.google.gson.Gson;
import java.time.Instant;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.esewa_kendra.Util.ServiceUtil;

@WebServlet("/modifyBooking")
public class ModifyBookingServlet extends HttpServlet {
    ServiceUtil serviceUtil = new ServiceUtil();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingIdParam = request.getParameter("bookingId");
        int bookingId = Integer.parseInt(bookingIdParam);
        String stateId = request.getParameter("state");
        String districtId = request.getParameter("district");
        String courtComplexId = request.getParameter("courtComplex");
        String kendraId = request.getParameter("kendra");
        String serviceId = request.getParameter("serviceType");
        String tokenNumber = request.getParameter("tokenNumber");
        String date = request.getParameter("date");
        java.sql.Date sqlDate = null;
        SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("MM/dd/yyyy"), // Format for "08/30/2024"
                new SimpleDateFormat("yyyy-MM-dd") // Another common format
                // Add more formats as needed
        };

        for (SimpleDateFormat dateFormat : dateFormats) {
            try {
                if (date != null && !date.isEmpty()) {
                    java.util.Date utilDate = dateFormat.parse(date); // Attempt to parse the date string
                    sqlDate = new Date(utilDate.getTime()); // Convert java.util.Date to java.sql.Date
                    break; // Exit loop if parsing is successful
                }
            } catch (ParseException e) {
                // Continue to try other formats if parsing fails
                System.err.println("Invalid date format for format " + dateFormat.toPattern() + ": " + date);
            }
        } // Define the expected date format

        String timeSlot = request.getParameter("time_slot");
        String status = "Modified";

        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);

            // Update booking details
            String updateBookingQuery = "UPDATE bookings SET state_id = ?, district_id = ?, court_complex_id = ?, " +
                    "kendra_id = ?, service_id = ?, status = ?, booking_time = ?,date=?,time_slot=? WHERE token_number = ?";
            try (PreparedStatement bookingStmt = conn.prepareStatement(updateBookingQuery)) {
                bookingStmt.setInt(1, Integer.parseInt(stateId));
                bookingStmt.setInt(2, Integer.parseInt(districtId));
                bookingStmt.setInt(3, Integer.parseInt(courtComplexId));
                bookingStmt.setInt(4, Integer.parseInt(kendraId));
                bookingStmt.setInt(5, Integer.parseInt(serviceId));
                bookingStmt.setString(6, status);
                bookingStmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                bookingStmt.setDate(8, sqlDate);
                bookingStmt.setString(9, timeSlot);
                bookingStmt.setString(10, tokenNumber);
                bookingStmt.executeUpdate();

                // Update service-specific details dynamically
                updateServiceDetails(conn, serviceId, bookingId, request);

                conn.commit();

                // Send success response
                response.setContentType("application/json");
                response.getWriter().write(new Gson().toJson(Map.of("status", "success", "message",
                        "Your booking has been successfully modified.")));

            } catch (SQLException e) {
                conn.rollback();
                response.setContentType("application/json");
                response.getWriter().write(new Gson().toJson(
                        Map.of("status", "error", "message",
                                "We encountered an issue while modifying your booking. Please try again later or contact support.")));
            }
        } catch (SQLException e) {
            response.setContentType("application/json");
            response.getWriter().write(new Gson()
                    .toJson(Map.of("status", "error", "message",
                            "We are currently experiencing technical difficulties. Please try again later or contact support.")));
        } catch (ClassNotFoundException e) {
            response.setContentType("application/json");
            response.getWriter().write(new Gson()
                    .toJson(Map.of("status", "error", "message",
                            "Required class was not found. Please contact support.")));
        }
    }

    private boolean isBookingAllowed(String enrollmentNumber, String phoneNumber, String serviceId,
            Boolean isAdvocate, int bookingId) {
        String enrollmentCondition = "";
        String dateCondition = "";
        if (isAdvocate) {
            enrollmentCondition = "b.enrollment_number = ? AND";
        }
        try (Connection conn = DBConfig.getConnection()) {
            String tableName = serviceUtil.getTableNameForServiceById(serviceId);
            if (!tableName.equals("video_conferencing")) {
                dateCondition = "AND CAST(st.date AS DATE) = CAST(GETDATE() AS DATE)";
            }
            String query = "SELECT COUNT(*) FROM bookings b " + "JOIN " + tableName
                    + " st ON b.id = st.booking_id WHERE " + enrollmentCondition
                    + " b.phone_number = ? " + dateCondition + " AND b.id != ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                if (isAdvocate) {
                    stmt.setString(1, enrollmentNumber);
                    stmt.setString(2, phoneNumber);
                    stmt.setInt(3, bookingId);
                } else {
                    stmt.setString(1, phoneNumber);
                    stmt.setInt(2, bookingId);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        return count == 0;
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Map<String, String> validateRequiredFields(HttpServletRequest request, String serviceId) {
        Map<String, String> errors = new HashMap<>();

        try (Connection conn = DBConfig.getConnection()) {
            String query = "SELECT column_name FROM service_columns WHERE service_id = ? AND isRequired = true";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, Integer.parseInt(serviceId));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String columnName = rs.getString("column_name");
                        String value = request.getParameter(columnName);
                        if (value == null || value.trim().isEmpty()) {
                            errors.put(columnName, "The field " + columnName + " is required.");
                        }
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return errors;
    }

    private void updateServiceDetails(Connection conn, String serviceId, int bookingId, HttpServletRequest request)
            throws SQLException {
        ServiceUtil serviceUtil = new ServiceUtil();
        String tableName = serviceUtil.getTableNameForServiceById(serviceId);
        if (tableName != null) {
            String updateQuery = buildDynamicUpdateQuery(conn, tableName, serviceId, bookingId, request);
            if (updateQuery != null) {
                try (PreparedStatement serviceStmt = conn.prepareStatement(updateQuery)) {
                    setPreparedStatementParameters(serviceStmt, serviceId, bookingId, request);
                    serviceStmt.executeUpdate();
                }
            }
        }
    }

    private String buildDynamicUpdateQuery(Connection conn, String tableName, String serviceId, int bookingId,
            HttpServletRequest request) throws SQLException {
        StringBuilder query = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        Map<String, String> columnValues = new HashMap<>();
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
                        query.append(columnName).append(" = ?, ");
                        columnValues.put(columnName, value);
                        columnDataTypes.put(columnName, dataType);
                    }
                }
            }
        }

        if (!columnValues.isEmpty()) {
            query.setLength(query.length() - 2); // Remove the last comma
            query.append(" WHERE booking_id = ").append(bookingId);
            return query.toString();
        }
        return null;
    }

    private void setPreparedStatementParameters(PreparedStatement stmt, String serviceId, int bookingId,
            HttpServletRequest request) throws SQLException {
        ServiceUtil serviceUtil = new ServiceUtil();
        String tableName = serviceUtil.getTableNameForServiceById(serviceId);
        if (tableName != null) {
            int parameterIndex = 1;
            String selectColumnsQuery = "SELECT column_name, data_type FROM service_columns WHERE service_id = ?";
            Map<String, String> columnDataTypes = new HashMap<>();

            try (PreparedStatement columnStmt = stmt.getConnection().prepareStatement(selectColumnsQuery)) {
                columnStmt.setInt(1, Integer.parseInt(serviceId));
                try (ResultSet columnRs = columnStmt.executeQuery()) {
                    while (columnRs.next()) {
                        String columnName = columnRs.getString("column_name");
                        String dataType = columnRs.getString("data_type");
                        String value = request.getParameter(columnName);

                        if (value != null && !value.isEmpty()) {
                            switch (dataType.toLowerCase()) {
                                case "int":
                                    stmt.setInt(parameterIndex++, Integer.parseInt(value));
                                    break;
                                case "decimal":
                                    stmt.setBigDecimal(parameterIndex++, new BigDecimal(value));
                                    break;
                                case "datetime":
                                    stmt.setTimestamp(parameterIndex++, parseTimestamp(value));
                                    break;
                                default:
                                    stmt.setString(parameterIndex++, value);
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    private Timestamp parseTimestamp(String datetimeString) {
        try {
            return Timestamp
                    .from(Instant.from(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(datetimeString).toInstant()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}

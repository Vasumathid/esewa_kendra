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
import java.net.URLEncoder;

@WebServlet("/bookService")
public class BookServiceServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServiceUtil serviceUtil = new ServiceUtil();
        String stateId = request.getParameter("state");
        String districtId = request.getParameter("district");
        String courtComplexId = request.getParameter("courtComplex");
        String kendraId = request.getParameter("kendra");
        String serviceId = request.getParameter("serviceType");
        String advocateName = request.getParameter("advocateName");
        String isAdvocateParam = request.getParameter("advocateOrParty");
        boolean isAdvocate = isAdvocateParam != null && isAdvocateParam.equals("advocate");

        String enrollmentNumber;
        if (isAdvocate) {
            enrollmentNumber = request.getParameter("enrollmentNumber");
        } else {
            enrollmentNumber = "";
        }
        String phoneNumber = request.getParameter("phoneNumber");
        String email = request.getParameter("email");
        String status = "Confirmed";
        String tokenNumber = null;
        Map<String, String> validationErrors = validateRequiredFields(request, serviceId);

        if (!validationErrors.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(validationErrors));
            return;
        }

        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);

            // Format the token number
            String statePrefix = (serviceUtil.getStateNameById(conn, stateId)).substring(0, 2).toUpperCase();
            String districtPrefix = (serviceUtil.getDistrictNameById(conn, districtId)).substring(0, 3).toUpperCase();
            long timestamp = Instant.now().toEpochMilli(); // Get current time in milliseconds

            tokenNumber = statePrefix + districtPrefix + timestamp;

            // Insert booking details
            String bookingQuery = "INSERT INTO bookings (state_id, district_id, court_complex_id, kendra_id, service_id, advocate_name, isAdvocate ,enrollment_number, phone_number, email, status, token_number,booking_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingQuery, Statement.RETURN_GENERATED_KEYS)) {
                bookingStmt.setInt(1, Integer.parseInt(stateId));
                bookingStmt.setInt(2, Integer.parseInt(districtId));
                bookingStmt.setInt(3, Integer.parseInt(courtComplexId));
                bookingStmt.setInt(4, Integer.parseInt(kendraId));
                bookingStmt.setInt(5, Integer.parseInt(serviceId));
                bookingStmt.setString(6, advocateName);
                bookingStmt.setBoolean(7, isAdvocate);
                bookingStmt.setString(8, enrollmentNumber);
                bookingStmt.setString(9, phoneNumber);
                bookingStmt.setString(10, email);
                bookingStmt.setString(11, status);
                bookingStmt.setString(12, tokenNumber);
                bookingStmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
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
        String pdfUrl = request.getContextPath() + "/generateToken?token=" + tokenNumber;

        // Redirect to displayPdf.html with the PDF URL
        response.sendRedirect("displayPdf.html?pdfUrl=" + URLEncoder.encode(pdfUrl, "UTF-8"));
        // response.sendRedirect("generateToken?token=" + tokenNumber);
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
                            errors.put(columnName, columnName + " is required.");
                        }
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return errors;
    }

    private void insertServiceDetails(Connection conn, String serviceId, int bookingId, HttpServletRequest request)
            throws SQLException {
        ServiceUtil serviceUtil = new ServiceUtil();
        String tableName = serviceUtil.getTableNameForServiceById(serviceId);
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

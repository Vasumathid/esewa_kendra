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
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import com.esewa_kendra.Util.ServiceUtil;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/checkAvailability")
public class CheckAvailabilityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    ServiceUtil serviceUtil = new ServiceUtil();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String serviceIdParam = request.getParameter("serviceId");
        String date = request.getParameter("date");
        String timeSlot = request.getParameter("timeSlot");
        String kendraIdParam = request.getParameter("kendraId");
        String courtComplexIdParam = request.getParameter("courtComplexId");

        if (serviceIdParam != null && date != null && timeSlot != null && kendraIdParam != null
                && courtComplexIdParam != null) {
            try (Connection conn = DBConfig.getConnection()) {
                String serviceId = serviceIdParam;
                int kendraId = Integer.parseInt(kendraIdParam);
                int courtComplexId = Integer.parseInt(courtComplexIdParam);

                JSONObject jsonResponse = new JSONObject();
                boolean isAvailable = checkAndRespondAvailability(conn, serviceId, date, timeSlot, kendraId,
                        courtComplexId, jsonResponse);

                if (!isAvailable) {
                    // If not available, suggest other available sewa kendras
                    JSONArray availableKendras = suggestAvailableKendras(conn, serviceId, date, timeSlot,
                            courtComplexId);
                    jsonResponse.put("suggestions", availableKendras);
                }

                out.print(jsonResponse.toString());
            } catch (SQLException | ClassNotFoundException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error checking availability: " + e.getMessage());
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
        }
    }

    private boolean checkAndRespondAvailability(Connection conn, String serviceId, String date, String timeSlot,
            int kendraId, int courtComplexId, JSONObject jsonResponse) throws SQLException {
        int count = getBookingCount(conn, serviceId, date, timeSlot, kendraId, courtComplexId);

        // Check availability
        if (count >= 25) {
            jsonResponse.put("available", false);
            return false;
        } else {
            jsonResponse.put("available", true);
            return true;
        }
    }

    private int getBookingCount(Connection conn, String serviceId, String date, String timeSlot, int kendraId,
            int courtComplexId) throws SQLException {

        String tableName = serviceUtil.getTableNameForServiceById(serviceId);
        if (tableName == null) {
            return 0;
        }

        String countQuery = String.format(
                "SELECT COUNT(*) FROM %s b JOIN bookings bk ON b.booking_id = bk.id WHERE b.date = ? AND b.time_slot = ? AND bk.kendra_id = ? AND bk.court_complex_id = ? AND bk.status = 'Confirmed'",
                tableName);
        try (PreparedStatement pstmt = conn.prepareStatement(countQuery)) {
            pstmt.setString(1, date);
            pstmt.setString(2, timeSlot);
            pstmt.setInt(3, kendraId);
            pstmt.setInt(4, courtComplexId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private JSONArray suggestAvailableKendras(Connection conn, String serviceId, String date, String timeSlot,
            int courtComplexId) throws SQLException {
        JSONArray availableKendras = new JSONArray();

        // Determine the table to query based on the serviceId
        String serviceTable = serviceUtil.getTableNameForServiceById(serviceId);

        if (serviceTable == null) {
            throw new SQLException("Invalid service ID");
        }

        // Construct the query to filter by date and time slot specific to the service
        String query = "SELECT DISTINCT sk.id, sk.name FROM sewa_kendras sk " +
                "LEFT JOIN bookings b ON sk.id = b.kendra_id AND s.date = ? AND s.time_slot = ? AND b.status = 'Confirmed' "
                +
                "LEFT JOIN " + serviceTable + " s ON b.id = s.booking_id AND s.date = ? AND s.time_slot = ? " +
                "WHERE b.id IS NULL AND sk.court_complex_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, date);
            pstmt.setString(2, timeSlot);
            pstmt.setString(3, date);
            pstmt.setString(4, timeSlot);
            pstmt.setInt(5, courtComplexId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JSONObject kendra = new JSONObject();
                kendra.put("name", rs.getString("name"));
                availableKendras.put(kendra);
            }
        }
        return availableKendras;
    }
}

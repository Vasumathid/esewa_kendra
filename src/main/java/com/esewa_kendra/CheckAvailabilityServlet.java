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

@WebServlet("/checkAvailability")
public class CheckAvailabilityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String serviceIdParam = request.getParameter("serviceId");
        String date = request.getParameter("date");
        String timeSlot = request.getParameter("timeSlot");

        if (serviceIdParam != null && date != null) {
            try (Connection conn = DBConfig.getConnection()) {
                int serviceId = Integer.parseInt(serviceIdParam);
                int count = getBookingCount(conn, serviceId, date, timeSlot);

                // Prepare response based on the count
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("count", count);

                // Check availability
                if (count >= 25) {
                    jsonResponse.put("available", false);
                } else {
                    jsonResponse.put("available", true);
                }

                out.print(jsonResponse.toString());
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                out.print("{\"error\":\"Error checking availability.\"}");
            }
        }
    }

    private int getBookingCount(Connection conn, int serviceId, String date, String timeSlot) throws SQLException {
        String tableName = getTableNameForService(serviceId);
        if (tableName == null) {
            return 0;
        }

        String countQuery = String.format("SELECT COUNT(*) FROM %s WHERE date = ? AND time_slot = ?", tableName);
        try (PreparedStatement pstmt = conn.prepareStatement(countQuery)) {
            pstmt.setString(1, date);
            pstmt.setString(2, timeSlot);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private String getTableNameForService(int serviceId) throws SQLException {
        String tableName = null;
        String query = "SELECT name FROM services WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, serviceId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String serviceName = rs.getString("name");
                switch (serviceName) {
                    case "Efiling Registration":
                        tableName = "efiling_registration";
                        break;
                    case "Scanning":
                        tableName = "scanning";
                        break;
                    case "Video Conferencing":
                        tableName = "video_conferencing";
                        break;
                    case "Assistance for filing":
                        tableName = "assistance_filing";
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown service: " + serviceName);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return tableName;
    }
}

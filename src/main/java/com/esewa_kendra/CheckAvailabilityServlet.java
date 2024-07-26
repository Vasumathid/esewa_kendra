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
                request.setAttribute("errorMessage", "Error checking availability." + e.getMessage());
                // request.getRequestDispatcher("/registration.html").forward(request,
                // response);
                // e.printStackTrace();
            }
        }
    }

    private int getBookingCount(Connection conn, int serviceId, String date, String timeSlot) throws SQLException {
        ServiceUtil service = new ServiceUtil();
        String tableName = service.getTableNameForServiceById(serviceId);
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

}

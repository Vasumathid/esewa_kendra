package com.esewa_kendra;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@WebServlet("/CancelBookingServlet")
public class CancelBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String tokenNumber = request.getParameter("token");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DBConfig.getConnection()) {
            String sql = "UPDATE bookings SET status = 'Cancelled' WHERE token_number = ? AND status <> 'Cancelled'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tokenNumber);
                int rowsUpdated = pstmt.executeUpdate();

                PrintWriter out = response.getWriter();
                if (rowsUpdated > 0) {
                    out.print("{\"status\":\"success\", \"message\":\"Booking cancelled successfully.\"}");
                } else {
                    out.print(
                            "{\"status\":\"error\", \"message\":\"Failed to cancel booking. Token number not found.\"}");
                }
                out.flush();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            PrintWriter out = response.getWriter();
            out.print("{\"status\":\"error\", \"message\":\"Error cancelling booking: " + e.getMessage() + "\"}");
            out.flush();
        }
    }
}

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

@WebServlet("/CancelBookingServlet")
public class CancelBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String tokenNumber = request.getParameter("token");

        try (Connection conn = DBConfig.getConnection()) {
            String sql = "UPDATE bookings SET status = 'Cancelled' WHERE token_number = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tokenNumber);
                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    response.getWriter().println("Booking cancelled successfully.");
                } else {
                    response.getWriter().println("Failed to cancel booking. Token number not found.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.getWriter().println("Error cancelling booking: " + e.getMessage());
        }
    }
}

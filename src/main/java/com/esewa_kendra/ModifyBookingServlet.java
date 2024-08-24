package com.esewa_kendra;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.esewa_kendra.Util.ServiceUtil;
import com.esewa_kendra.DBConfig;

@WebServlet("/modifyBooking")
public class ModifyBookingServlet extends HttpServlet {
    ServiceUtil serviceUtil = new ServiceUtil();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String tokenNumber = request.getParameter("tokenNumber");
        String advocateName = request.getParameter("advocateName");
        String enrollmentNumber = request.getParameter("enrollmentNumber");
        String phoneNumber = request.getParameter("phoneNumber");
        String email = request.getParameter("email");
        String status = request.getParameter("status");

        try {
            // Step 1: Get the service table name based on the token number
            String serviceTableName = getServiceTableNameByToken(tokenNumber);

            // Step 2: Fetch booking ID based on the token number
            int bookingId = getBookingIdByToken(tokenNumber);

            if (serviceTableName != null && bookingId != 0) {
                // Step 3: Update the bookings table
                boolean bookingUpdated = updateBookingDetails(tokenNumber, advocateName, enrollmentNumber, phoneNumber,
                        email, status);

                // Step 4: Update the service table with the new details
                boolean serviceUpdated = updateServiceTable(serviceTableName, bookingId, advocateName, enrollmentNumber,
                        phoneNumber, email, status);

                if (bookingUpdated && serviceUpdated) {
                    response.getWriter().write("Booking and service details updated successfully.");
                } else {
                    response.getWriter().write("Failed to update booking or service details.");
                }
            } else {
                response.getWriter().write("Invalid token number.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.getWriter().write("Error occurred while updating details.");
        }
    }

    private String getServiceTableNameByToken(String tokenNumber) throws SQLException, ClassNotFoundException {
        String serviceTableName = null;
        String query = "SELECT s.service_table_name " +
                "FROM bookings b " +
                "JOIN services s ON b.service_id = s.id " +
                "WHERE b.token_number = ?";
        DBConfig dbConfig = new DBConfig();
        try (Connection conn = dbConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, tokenNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                serviceTableName = rs.getString("service_table_name");
            }
        }
        return serviceTableName;
    }

    private int getBookingIdByToken(String tokenNumber) throws SQLException, ClassNotFoundException {
        int bookingId = 0;
        String query = "SELECT id FROM bookings WHERE token_number = ?";
        DBConfig dbConfig = new DBConfig();
        try (Connection conn = dbConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, tokenNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bookingId = rs.getInt("id");
            }
        }
        return bookingId;
    }

    private boolean updateBookingDetails(String tokenNumber, String advocateName, String enrollmentNumber,
            String phoneNumber, String email, String status) throws SQLException, ClassNotFoundException {
        String query = "UPDATE bookings SET advocate_name = ?, enrollment_number = ?, phone_number = ?, email = ?, status = ? WHERE token_number = ?";
        DBConfig dbConfig = new DBConfig();
        try (Connection conn = dbConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, advocateName);
            pstmt.setString(2, enrollmentNumber);
            pstmt.setString(3, phoneNumber);
            pstmt.setString(4, email);
            pstmt.setString(5, status);
            pstmt.setString(6, tokenNumber);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    private boolean updateServiceTable(String serviceTableName, int bookingId, String advocateName,
            String enrollmentNumber, String phoneNumber, String email, String status)
            throws SQLException, ClassNotFoundException {
        String query = "UPDATE " + serviceTableName
                + " SET advocate_name = ?, enrollment_number = ?, phone_number = ?, email = ?, status = ? WHERE booking_id = ?";
        DBConfig dbConfig = new DBConfig();
        try (Connection conn = dbConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, advocateName);
            pstmt.setString(2, enrollmentNumber);
            pstmt.setString(3, phoneNumber);
            pstmt.setString(4, email);
            pstmt.setString(5, status);
            pstmt.setInt(6, bookingId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        }
    }
}

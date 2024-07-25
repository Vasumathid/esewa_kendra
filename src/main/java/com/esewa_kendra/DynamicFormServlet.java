package com.esewa_kendra;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dynamicForm")
public class DynamicFormServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        try (Connection conn = DBConfig.getConnection()) {
            if ("serviceTypes".equals(action)) {
                String query = "SELECT id, name FROM services";
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery();
                out.print("<option value=''>Select Type of Service</option>");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    out.println("<option value='" + id + "'>" + name + "</option>");
                }
            }
            if ("states".equals(action)) {
                String query = "SELECT id, name FROM states";
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery();
                out.print("<option value=''>Select State</option>");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    out.print("<option value='" + id + "'>" + name + "</option>");
                }
            } else if ("districts".equals(action) && idParam != null) {
                String query = "SELECT id, name FROM districts WHERE state_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, Integer.parseInt(idParam));
                ResultSet rs = pstmt.executeQuery();
                out.print("<option value=''>Select District</option>");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    out.print("<option value='" + id + "'>" + name + "</option>");
                }
            } else if ("courtComplexes".equals(action) && idParam != null) {
                String query = "SELECT id, name FROM court_complexes WHERE district_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, Integer.parseInt(idParam));
                ResultSet rs = pstmt.executeQuery();
                out.print("<option value=''>Select Court Complex</option>");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    out.print("<option value='" + id + "'>" + name + "</option>");
                }
            } else if ("sewaKendras".equals(action) && idParam != null) {
                String query = "SELECT id, name FROM sewa_kendras WHERE court_complex_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, Integer.parseInt(idParam));
                ResultSet rs = pstmt.executeQuery();
                out.print("<option value=''>Select E-Sewa Kendra</option>");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    out.print("<option value='" + id + "'>" + name + "</option>");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            out.print("<option value=''>Error loading data</option>");
        }
    }
}

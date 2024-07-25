package com.esewa_kendra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@WebServlet("/generateToken")
public class GenerateTokenServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String tokenNumber = request.getParameter("token");

        try (Connection conn = DBConfig.getConnection()) {
            String bookingQuery = "SELECT * FROM bookings WHERE token_number = ?";
            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingQuery)) {
                bookingStmt.setString(1, tokenNumber);
                try (ResultSet rs = bookingStmt.executeQuery()) {
                    if (rs.next()) {
                        String serviceId = rs.getString("service_id");
                        String stateId = rs.getString("state_id");
                        String districtId = rs.getString("district_id");
                        String courtComplexId = rs.getString("court_complex_id");
                        String kendraId = rs.getString("kendra_id");

                        // Retrieve service-specific details
                        Map<String, String> serviceDetails = getServiceDetails(conn, serviceId, rs.getInt("id"));
                        String stateName = getStateNameById(conn, stateId);
                        String districtName = getDistrictNameById(conn, districtId);
                        String courtComplexName = getCourtComplexNameById(conn, courtComplexId);
                        String kendraName = getKendraNameById(conn, kendraId);

                        // Set response content type to PDF
                        response.setContentType("application/pdf");

                        // Create PDF
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try (OutputStream out = baos) {
                            Document document = new Document();
                            PdfWriter.getInstance(document, out);
                            document.open();

                            // Add header
                            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                            Paragraph header = new Paragraph("High Court of Madras\n", headerFont);
                            header.setAlignment(Element.ALIGN_CENTER);
                            document.add(header);

                            // Add booking details table
                            document.add(new Paragraph("\n"));
                            PdfPTable table = new PdfPTable(2);
                            table.setWidthPercentage(100);
                            table.setSpacingBefore(10f);
                            table.setSpacingAfter(10f);

                            addTableCell(table, "Token Number:", tokenNumber);
                            addTableCell(table, "State:", stateName);
                            addTableCell(table, "District:", districtName);
                            addTableCell(table, "Court Complex:", courtComplexName);
                            addTableCell(table, "E-Sewa Kendra:", kendraName);

                            for (Map.Entry<String, String> entry : serviceDetails.entrySet()) {
                                addTableCell(table, formatColumnName(entry.getKey()) + ":", entry.getValue());
                            }

                            document.add(table);
                            document.close();
                        }
                        

                        // Write the PDF as a byte array
                        byte[] pdfBytes = baos.toByteArray();
                        response.setContentLength(pdfBytes.length);
                        try (OutputStream out = response.getOutputStream()) {
                            out.write(pdfBytes);
                        }
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Token not found");
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new ServletException("Database error", e);
        }catch (DocumentException e) {
            throw new ServletException("Error generating PDF", e);
        }
    }

    private void addTableCell(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(valueCell);
    }

    private Map<String, String> getServiceDetails(Connection conn, String serviceId, int bookingId) throws SQLException {
        Map<String, String> serviceDetails = new LinkedHashMap<>();
        String serviceName = getServiceNameById(conn, serviceId);
        if (serviceName != null) {
            String tableName = getTableNameForService(serviceName);
            if (tableName != null) {
                String query = "SELECT * FROM " + tableName + " WHERE booking_id = ?";
                try (PreparedStatement serviceStmt = conn.prepareStatement(query)) {
                    serviceStmt.setInt(1, bookingId);

                    try (ResultSet rs = serviceStmt.executeQuery()) {
                        if (rs.next()) {
                            ResultSetMetaData metaData = rs.getMetaData();
                            int columnCount = metaData.getColumnCount();

                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = metaData.getColumnName(i);
                                String value = rs.getString(columnName);
                                serviceDetails.put(columnName, value);
                            }
                        }
                    }
                }
            }
        }
        return serviceDetails;
    }

    private String formatColumnName(String columnName) {
        return columnName.replace("_", " ").toLowerCase();
    }

    private String getServiceNameById(Connection conn, String serviceId) throws SQLException {
        String serviceName = null;
        String query = "SELECT name FROM services WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(serviceId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    serviceName = rs.getString("name");
                }
            }
        }
        return serviceName;
    }

    private String getTableNameForService(String serviceName) {
        return serviceName.replace("for", "").replaceAll("\\s+", "_").toLowerCase();
    }

    private String getStateNameById(Connection conn, String stateId) throws SQLException {
        String stateName = null;
        String query = "SELECT name FROM states WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(stateId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stateName = rs.getString("name");
                }
            }
        }
        return stateName;
    }

    private String getDistrictNameById(Connection conn, String districtId) throws SQLException {
        String districtName = null;
        String query = "SELECT name FROM districts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(districtId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    districtName = rs.getString("name");
                }
            }
        }
        return districtName;
    }

    private String getCourtComplexNameById(Connection conn, String courtComplexId) throws SQLException {
        String courtComplexName = null;
        String query = "SELECT name FROM court_complexes WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(courtComplexId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    courtComplexName = rs.getString("name");
                }
            }
        }
        return courtComplexName;
    }

    private String getKendraNameById(Connection conn, String kendraId) throws SQLException {
        String kendraName = null;
        String query = "SELECT name FROM sewa_kendras WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(kendraId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    kendraName = rs.getString("name");
                }
            }
        }
        return kendraName;
    }
}

package com.esewa_kendra;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

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
                        String serviceDetails = getServiceDetails(conn, serviceId, rs.getInt("id"));
                        String stateName = getStateNameById(conn, stateId);
                        String districtName = getDistrictNameById(conn, districtId);
                        String courtComplexName = getCourtComplexNameById(conn, courtComplexId);
                        String kendraName = getKendraNameById(conn, kendraId);

                        // Set response content type to PDF
                        response.setContentType("application/pdf");
                        response.setHeader("Content-Disposition", "attachment; filename=Token_" + tokenNumber + ".pdf");

                        // Create PDF
                        try (OutputStream out = response.getOutputStream()) {
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
                            addTableCell(table, "Service Details:", serviceDetails);

                            document.add(table);
                            document.close();
                        } catch (DocumentException e) {
                            throw new ServletException("Error generating PDF", e);
                        }
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Token not found");
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new ServletException("Database error", e);
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

    private String getServiceDetails(Connection conn, String serviceId, int bookingId) throws SQLException {
        String serviceName = getServiceNameById(conn, serviceId);
        if (serviceName != null) {
            String tableName = getTableNameForService(serviceName);
            if (tableName != null) {
                String query = "SELECT * FROM " + tableName + " WHERE booking_id = ?";
                try (PreparedStatement serviceStmt = conn.prepareStatement(query)) {
                    serviceStmt.setInt(1, bookingId);

                    try (ResultSet rs = serviceStmt.executeQuery()) {
                        if (rs.next()) {
                            // Fetch the column names dynamically
                            ResultSetMetaData metaData = rs.getMetaData();
                            int columnCount = metaData.getColumnCount();
                            StringBuilder details = new StringBuilder();

                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = metaData.getColumnName(i);
                                String value = rs.getString(columnName);

                                // Check for the specific columns
                                if ("date".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("Date: ").append(value).append(", ");
                                } else if ("time_slot".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("Time Slot: ").append(value).append(", ");
                                } else if ("hearing_date".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("Hearing Date: ").append(value).append(", ");
                                }
                                // Add other specific columns based on the service
                                else if ("case_type".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("Case Type: ").append(value).append(", ");
                                } else if ("cnr_number".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("CNR Number: ").append(value).append(", ");
                                } else if ("pages_count".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("Pages Count: ").append(value).append(", ");
                                } else if ("court_fee_amount".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("Court Fee Amount: ").append(value).append(", ");
                                } else if ("case_number".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("Case Number: ").append(value).append(", ");
                                } else if ("court_name".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("Court Name: ").append(value).append(", ");
                                } else if ("district_name".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("District Name: ").append(value).append(", ");
                                } else if ("case_year".equalsIgnoreCase(columnName) && value != null) {
                                    details.append("Case Year: ").append(value).append(", ");
                                }
                            }

                            if (details.length() > 0) {
                                // Remove trailing comma and space
                                details.setLength(details.length() - 2);
                            } else {
                                details.append("No details available");
                            }

                            return details.toString();
                        }
                    }
                }
            }
        }
        return "No details available";
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
        switch (serviceName.toLowerCase()) {
            case "efiling registration":
                return "efiling_registration";
            case "scanning":
                return "scanning";
            case "video conferencing":
                return "video_conferencing";
            case "assistance for filing":
                return "assistance_filing";
            default:
                return null;
        }
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

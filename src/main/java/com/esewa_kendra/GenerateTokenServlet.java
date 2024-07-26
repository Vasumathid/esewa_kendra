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
import com.esewa_kendra.Util.ServiceUtil;

@WebServlet("/generateToken")
public class GenerateTokenServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String tokenNumber = request.getParameter("token");
        ServiceUtil serviceUtil = new ServiceUtil();
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
                        Map<String, String> serviceDetails = serviceUtil.getServiceDetails(conn, serviceId,
                                rs.getInt("id"));
                        String stateName = serviceUtil.getStateNameById(conn, stateId);
                        String districtName = serviceUtil.getDistrictNameById(conn, districtId);
                        String courtComplexName = serviceUtil.getCourtComplexNameById(conn, courtComplexId);
                        String kendraName = serviceUtil.getKendraNameById(conn, kendraId);

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
                                if (!entry.getKey().contains("id"))
                                    addTableCell(table, serviceUtil.formatColumnName(entry.getKey()) + ":",
                                            entry.getValue());
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
        } catch (DocumentException e) {
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

}

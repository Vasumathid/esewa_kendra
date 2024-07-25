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

@WebServlet("/getServices")
public class GetServicesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String serviceIdParam = request.getParameter("serviceId");

        if (serviceIdParam != null) {
            try (Connection conn = DBConfig.getConnection()) {
                int serviceId = Integer.parseInt(serviceIdParam);
                String query = "SELECT column_name, data_type FROM service_columns WHERE service_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, serviceId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String formattedColumnString = formatColumnName(columnName);
                    String dataType = rs.getString("data_type");

                    if (!columnName.equals("booking_id")) {
                        out.print("<div class='form-row mb-3'>");
                        out.print("<div class='col-md-4'>");
                        out.print("<label for='" + columnName + "' class='required'>" + formattedColumnString + ":</label>");
                        out.print("</div>");
                        out.print("<div class='col-md-8'>");

                        if (columnName.equals("case_type")) {
                            out.print("<select name='case_type' id='case_type' class='form-control' required onchange='handleCaseTypeChange(this.value)'>"
                                    + "<option value=''>Select Case Type</option>"
                                    + "<option value='newcase'>New Case</option>"
                                    + "<option value='oldcase'>Old Case</option>"
                                    + "</select>"
                                    + "<div id='newCaseOptions' style='display:none;'>"
                                    + "<label for='civil_or_criminal' class='required'>Civil/Criminal:</label>"
                                    + "<div class='form-check'>"
                                    + "<input type='radio' name='civil_or_criminal' id='civil' value='true' class='form-check-input' />"
                                    + "<label for='civil' class='form-check-label'>Civil</label>"
                                    + "</div>"
                                    + "<div class='form-check'>"
                                    + "<input type='radio' name='civil_or_criminal' id='criminal' value='false' class='form-check-input' />"
                                    + "<label for='criminal' class='form-check-label'>Criminal</label>"
                                    + "</div>"
                                    + "</div>"
                                    + "<div id='oldCaseOptions' style='display:none;'>"
                                    + "<label for='cnr_number' class='required'>CNR Number:</label>"
                                    + "<input type='text' name='cnr_number' id='cnr_number' class='form-control' />"
                                    + "</div>");
                        } else if (columnName.equals("civil_or_criminal")) {
                            out.print("<div class='form-check'>"
                                    + "<input type='radio' name='civil_or_criminal' id='civil' value='true' class='form-check-input' />"
                                    + "<label for='civil' class='form-check-label'>Civil</label>"
                                    + "</div>"
                                    + "<div class='form-check'>"
                                    + "<input type='radio' name='civil_or_criminal' id='criminal' value='false' class='form-check-input' />"
                                    + "<label for='criminal' class='form-check-label'>Criminal</label>"
                                    + "</div>");
                        } else if (columnName.equals("time_slot")) {
                            out.print("<select name='time_slot' id='time_slot' class='form-control' required>"
                                    + "<option value=''>Select Time Slot</option>"
                                    + "<option value='Forenoon'>Forenoon</option>"
                                    + "<option value='Afternoon'>Afternoon</option>"
                                    + "</select>");
                        } else {
                            switch (dataType) {
                                case "VARCHAR":
                                    out.print("<input type='text' name='" + columnName + "' id='" + columnName + "' class='form-control' required />");
                                    break;
                                case "INT":
                                    out.print("<input type='number' name='" + columnName + "' id='" + columnName + "' class='form-control' required />");
                                    break;
                                case "DATE":
                                    out.print("<input type='date' name='" + columnName + "' id='" + columnName + "' class='form-control' required />");
                                    break;
                                case "DECIMAL":
                                    out.print("<input type='number' step='0.01' name='" + columnName + "' id='" + columnName + "' class='form-control' required />");
                                    break;
                                default:
                                    out.print("<input type='text' name='" + columnName + "' id='" + columnName + "' class='form-control' required />");
                                    break;
                            }
                        }
                        out.print("</div>");
                        out.print("</div>"); // End of form row
                        out.print("<br/>");
                    }
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                out.print("<div class='alert alert-danger'>Error loading service form.</div>");
            }
        }
    }

    public static String formatColumnName(String columnName) {
        String[] words = columnName.split("_");
        String formattedName = "";
        for (String word : words) {
            if (!word.isEmpty()) {
                String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                formattedName += capitalizedWord + " ";
            }
        }
        return formattedName.trim();
    }
}

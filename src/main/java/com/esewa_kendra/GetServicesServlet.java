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

                // out.print("<div class='container'>"); // Wrap in container for Bootstrap
                // layout
                // Start of form row

                while (rs.next()) {

                    String columnName = rs.getString("column_name");
                    String dataType = rs.getString("data_type");
                    if (!columnName.equals("booking_id")) {
                        out.print("<div class='form-row mb-3'>");
                        out.print("<div class='col-md-4'>");
                        out.print("<label for='" + columnName + "'class='required'>" + columnName + ":</label>");
                        out.print("</div>");
                        out.print("<div class='col-md-8'>");
                        if (columnName.equals("case_type")) {
                            String[] options = { "New Case", "Old Case" };

                            // Print radio buttons
                            for (String option : options) {
                                out.print("<div class='form-check'>");
                                out.print("<input type='radio' name='" + rs.getString("column_name") + "' id='" + option
                                        + "' value='"
                                        + option + "' class='form-check-input' required />");
                                out.print(
                                        "<label for='" + option + "' class='form-check-label' class='required'>"
                                                + option + "</label>");
                                out.print("</div>");
                            }
                        } else if (columnName.equals("time_slot")) {
                            out.print("<select name='time_slot' id='time_slot' class='form-control' required>" +
                                    "<option value=''>Select Time Slot</option>" +
                                    "<option value='Forenoon'>Forenoon</option>" +
                                    "<option value='Afternoon'>Afternoon</option>" +
                                    "</select>");
                        } else {
                            switch (dataType) {
                                case "VARCHAR":
                                    out.print("<input type='text' name='" + columnName + "' id='" + columnName
                                            + "' class='form-control' required />");
                                    break;
                                case "INT":
                                    out.print("<input type='number' name='" + columnName + "' id='" + columnName
                                            + "' class='form-control' required />");
                                    break;
                                case "DATE":
                                    out.print("<input type='date' name='" + columnName + "' id='" + columnName
                                            + "' class='form-control' required />");
                                    break;
                                case "DECIMAL":
                                    out.print("<input type='number' step='0.01' name='" + columnName + "' id='"
                                            + columnName
                                            + "' class='form-control' required />");
                                    break;
                                default:
                                    out.print("<input type='text' name='" + columnName + "' id='" + columnName
                                            + "' class='form-control' required />");
                                    break;
                            }
                        }
                        out.print("</div>");
                    }
                    out.print("</div>"); // End of form row
                    out.print("<br/>");
                    // out.print("</div>"); // End of container
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                out.print("<div class='alert alert-danger'>Error loading service form.</div>");
            }
        }
    }
}

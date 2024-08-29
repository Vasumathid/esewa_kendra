import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import com.esewa_kendra.DBConfig;

@WebServlet("/getServices")
public class GetServicesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String serviceIdParam = request.getParameter("serviceId");
        String jsonData = request.getParameter("servicedetails");

        if (serviceIdParam != null) {
            // URL-decode and parse the JSON data
            String decodedJsonData = URLDecoder.decode(jsonData, StandardCharsets.UTF_8.name());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode dataNode = objectMapper.readTree(decodedJsonData);

            try (Connection conn = DBConfig.getConnection()) {
                int serviceId = Integer.parseInt(serviceIdParam);
                String query = "SELECT column_name, data_type, isRequired FROM service_columns WHERE service_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, serviceId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String formattedColumnString = formatColumnName(columnName);
                    String dataType = rs.getString("data_type");
                    boolean isRequired = rs.getBoolean("isRequired");

                    String requiredAttribute = isRequired ? "required" : "";
                    String requiredClass = isRequired ? " class='required'" : "";

                    // Fetch the value from JSON data
                    String value = "";
                    if (dataNode.has(columnName)) {
                        value = dataNode.get(columnName).asText();
                    }

                    if (!columnName.equals("booking_id")) {
                        if (columnName.equals("civil_or_criminal") || (columnName.equals("cnr_number"))) {
                            out.print("<div id='" + columnName + "'>");
                        }
                        out.print("<div class='form-row mb-3'>");
                        out.print("<div class='col-md-4'>");
                        out.print("<label for='" + columnName + "'" + requiredClass + ">" + formattedColumnString
                                + ":</label>");
                        out.print("</div>");
                        out.print("<div class='col-md-8'>");

                        if (columnName.equals("case_type_category")) {
                            out.print(
                                    "<select name='case_type' id='case_type' class='form-control' " + requiredAttribute
                                            + ">"
                                            + "<option value=''>Select Case Type </option>"
                                            + "<option value='1'" + ("1".equals(value) ? " selected" : "")
                                            + ">New Case</option>"
                                            + "<option value='0'" + ("0".equals(value) ? " selected" : "")
                                            + ">Old Case</option>"
                                            + "</select>"
                                            + "<div id='newCaseOptions' style='display:none;'>"
                                            + "<label for='civil_or_criminal' class='" + (isRequired ? "required" : "")
                                            + "'>Civil/Criminal:</label>"
                                            + "<div class='form-check'>"
                                            + "<input type='radio' name='civil_or_criminal' id='civil' value='true' class='form-check-input' "
                                            + ("true".equals(value) ? "checked" : "") + " />"
                                            + "<label for='civil' class='form-check-label'>Civil</label>"
                                            + "</div>"
                                            + "<div class='form-check'>"
                                            + "<input type='radio' name='civil_or_criminal' id='criminal' value='false' class='form-check-input' "
                                            + ("false".equals(value) ? "checked" : "") + " />"
                                            + "<label for='criminal' class='form-check-label'>Criminal</label>"
                                            + "</div>"
                                            + "</div>"
                                            + "<div id='oldCaseOptions' style='display:none;'>"
                                            + "<label for='cnr_number' class='" + (isRequired ? "required" : "")
                                            + "'>CNR Number:</label>"
                                            + "<input type='text' name='cnr_number' id='cnr_number' class='form-control' placeholder='Enter CNR Number' value='"
                                            + value + "' />"
                                            + "</div>");
                        } else if (columnName.equals("civil_or_criminal")) {
                            out.print("<div class='form-check'>"
                                    + "<input type='radio' name='civil_or_criminal' id='civil' value='true' class='form-check-input' "
                                    + ("true".equals(value) ? "checked" : "") + " />"
                                    + "<label for='civil' class='form-check-label'>Civil</label>"
                                    + "</div>"
                                    + "<div class='form-check'>"
                                    + "<input type='radio' name='civil_or_criminal' id='criminal' value='false' class='form-check-input' "
                                    + ("false".equals(value) ? "checked" : "") + " />"
                                    + "<label for='criminal' class='form-check-label'>Criminal</label>"
                                    + "</div>");
                        } else if (columnName.equals("time_slot")) {
                            out.print("<select name='time_slot' id='time_slot' class='form-control' "
                                    + requiredAttribute + ">"
                                    + "<option value=''>Select Time Slot</option>"
                                    + "<option value='Forenoon'" + ("Forenoon".equals(value) ? " selected" : "")
                                    + ">Forenoon</option>"
                                    + "<option value='Afternoon'" + ("Afternoon".equals(value) ? " selected" : "")
                                    + ">Afternoon</option>"
                                    + "</select>");
                        } else {
                            String placeholder = "Enter " + formattedColumnString;
                            switch (dataType) {
                                case "VARCHAR":
                                    out.print("<input type='text' name='" + columnName + "' id='" + columnName
                                            + "' class='form-control' placeholder='" + placeholder + "' "
                                            + requiredAttribute + " value='" + value + "' />");
                                    break;
                                case "INT":
                                    out.print("<input type='number' name='" + columnName + "' id='" + columnName
                                            + "' class='form-control' placeholder='" + placeholder + "' "
                                            + requiredAttribute + " value='" + value + "' />");
                                    break;
                                case "DATE":
                                    out.print("<input type='date' name='" + columnName + "' id='" + columnName
                                            + "' class='form-control date-Picker' placeholder='" + placeholder + "' "
                                            + requiredAttribute + " value='" + value + "' />");
                                    break;
                                case "DECIMAL":
                                    out.print("<input type='number' step='0.01' name='" + columnName + "' id='"
                                            + columnName + "' class='form-control' placeholder='" + placeholder + "' "
                                            + requiredAttribute + " value='" + value + "' />");
                                    break;
                                default:
                                    out.print("<input type='text' name='" + columnName + "' id='" + columnName
                                            + "' class='form-control' placeholder='" + placeholder + "' "
                                            + requiredAttribute + " value='" + value + "' />");
                                    break;
                            }
                        }
                        out.print("</div>");
                        out.print("</div>"); // End of form row
                        out.print("<br/>");
                        if (columnName.equals("civil_or_criminal") || (columnName.equals("cnr_number"))) {
                            out.print("</div>");
                        }
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
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                formattedName.append(capitalizedWord).append(" ");
            }
        }
        return formattedName.toString().trim();
    }
}

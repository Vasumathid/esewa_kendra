package com.esewa_kendra;

import com.esewa_kendra.Util.ServiceUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import com.esewa_kendra.service.BookingDetails;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/GetBookingDetailsServlet")
public class GetBookingDetailsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServiceUtil serviceutil = new ServiceUtil();
        String tokenNumber = request.getParameter("token");
        String serviceTableName = null;
        int id = 0;
        try {
            // Database interaction to fetch booking details based on tokenNumber
            BookingDetails bookingDetails = serviceutil.getBookingDetailsByToken(tokenNumber);
            serviceTableName = serviceutil.getTableNameForServiceById(String.valueOf(bookingDetails.getServiceId()));
            id = bookingDetails.getId();
            String serviceTableDetails = serviceutil.getServiceTableDetailsByName(serviceTableName, id);
            Gson gson = new Gson();

            // Convert bookingDetails to JSON
            String bookingDetailsJson = gson.toJson(bookingDetails);

            // Parse JSON strings into JsonObject
            JsonObject bookingDetailsObj = JsonParser.parseString(bookingDetailsJson).getAsJsonObject();
            JsonObject serviceTableDetailsObj = JsonParser.parseString(serviceTableDetails).getAsJsonObject();

            // Combine the two JsonObjects
            JsonObject combinedJson = new JsonObject();
            combinedJson.add("bookingDetails", bookingDetailsObj);
            combinedJson.add("serviceTableDetails", serviceTableDetailsObj);

            // Convert the combined JsonObject to a JSON string
            String jsonResponse = gson.toJson(combinedJson);

            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"success\",\"data\":" + jsonResponse + "}");

        } catch (SQLException e) {
            // Handle SQL exception by sending an error response
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Unable to fetch booking details.\"}");
        }
    }
}

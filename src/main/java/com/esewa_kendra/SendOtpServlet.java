package com.esewa_kendra;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

@WebServlet("/sendOtp")
public class SendOtpServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String mobileNumber = req.getParameter("mobileNumber");
        String otp = generateOtp();

        // Store the OTP in the session for later verification
        req.getSession().setAttribute("generatedOtp", otp);

        // Here you should send the OTP to the mobile number using an SMS gateway
        // For demo purposes, we'll just print it to the console
        System.out.println("Generated OTP: " + otp + " for mobile number: " + mobileNumber);

        // Simulate success/failure response
        boolean otpSent = true; // Assume OTP is sent successfully

        resp.setContentType("application/json");
        resp.getWriter().write("{\"success\": " + otpSent + "}");
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}

package com.esewa_kendra;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/verifyOtp")
public class VerifyOtpServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String inputOtp = req.getParameter("otp");
        String sessionOtp = (String) req.getSession().getAttribute("generatedOtp");

        boolean otpValid = inputOtp != null && inputOtp.equals(sessionOtp);

        resp.setContentType("application/json");
        resp.getWriter().write("{\"success\": " + otpValid + "}");
    }
}

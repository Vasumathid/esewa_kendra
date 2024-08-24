package com.esewa_kendra.service;

import java.util.Date;

public class BookingDetails {
    private int id;
    private int stateId;
    private int districtId;
    private int courtComplexId;
    private int kendraId;
    private int serviceId;
    private String advocateName;
    private String enrollmentNumber;
    private String phoneNumber;
    private String email;
    private String status;
    private String tokenNumber;
    private java.util.Date bookingTime; // Use java.util.Date for DATETIME
    private boolean isAdvocate;

    // Getter and Setter for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter for stateId
    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    // Getter and Setter for districtId
    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }

    // Getter and Setter for courtComplexId
    public int getCourtComplexId() {
        return courtComplexId;
    }

    public void setCourtComplexId(int courtComplexId) {
        this.courtComplexId = courtComplexId;
    }

    // Getter and Setter for kendraId
    public int getKendraId() {
        return kendraId;
    }

    public void setKendraId(int kendraId) {
        this.kendraId = kendraId;
    }

    // Getter and Setter for serviceId
    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    // Getter and Setter for advocateName
    public String getAdvocateName() {
        return advocateName;
    }

    public void setAdvocateName(String advocateName) {
        this.advocateName = advocateName;
    }

    // Getter and Setter for enrollmentNumber
    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    // Getter and Setter for phoneNumber
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Getter and Setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and Setter for status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getter and Setter for tokenNumber
    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    // Getter and Setter for bookingTime
    public java.util.Date getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(java.util.Date bookingTime) {
        this.bookingTime = bookingTime;
    }

    // Getter and Setter for isAdvocate
    public boolean getIsAdvocate() {
        return isAdvocate;
    }

    public void setIsAdvocate(boolean isAdvocate) {
        this.isAdvocate = isAdvocate;
    }
}

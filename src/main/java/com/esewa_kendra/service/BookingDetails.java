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
    private java.sql.Timestamp bookingTime;
    private boolean isAdvocate;
    private java.sql.Date date;
    private String timeSlot;
    private java.sql.Timestamp modifiedTime;
    private String timeRange;

    // Getter and Setter methods
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

    public java.sql.Timestamp getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(java.sql.Timestamp bookingTime) {
        this.bookingTime = bookingTime;
    }

    public boolean isAdvocate() {
        return isAdvocate;
    }

    public void setIsAdvocate(boolean isAdvocate) {
        this.isAdvocate = isAdvocate;
    }

    public java.sql.Date getDate() {
        return date;
    }

    public void setDate(java.sql.Date date) {
        this.date = date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public java.sql.Timestamp getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(java.sql.Timestamp modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }
}

package org.springboot.insurancemanagementsystem.service;

public interface EmailService {
    void sendOtp(String toEmail, String otp);
}

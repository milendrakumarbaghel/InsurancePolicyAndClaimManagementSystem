package org.springboot.insurancemanagementsystem.service;

public interface SmsService {

    void sendOtp(String toPhone, String otp);

}
package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.entitie.User;

public interface OtpService {

    void createAndSendOtp(User user);
    void verifyOtp(User user, String emailOtp, String phoneOtp);
}
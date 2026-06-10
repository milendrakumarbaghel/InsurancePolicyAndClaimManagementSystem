package org.springboot.insurancemanagementsystem.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class SmsService {

    @Value("${app.twilio.account-sid}")
    private String accountSid;

    @Value("${app.twilio.auth-token}")
    private String authToken;

    @Value("${app.twilio.from-phone}")
    private String fromPhone;

    @Value("${app.otp.expiry-minutes:5}")
    private long expiryMinutes;

    public void sendOtp(String toPhone, String otp) {
        if (!StringUtils.hasText(accountSid)
                || !StringUtils.hasText(authToken)
                || !StringUtils.hasText(fromPhone)) {

            log.warn("Twilio not configured. SMS OTP for {} : {}", toPhone, otp);
            return;
        }

        try {
            // Ensure E.164 format for Indian numbers
            String formattedPhone = toPhone.startsWith("+") ? toPhone : "+91" + toPhone;

            Message.creator(
                    new PhoneNumber(formattedPhone),
                    new PhoneNumber(fromPhone),
                    "Your OTP for Insurance Management System is: " + otp
                            + ". Valid for " + expiryMinutes + " minutes. Do not share it."
            ).create();

            log.info("SMS OTP sent successfully to: {}", toPhone);

        } catch (Exception e) {
            log.error("Failed to send SMS OTP to {}: {}", toPhone, e.getMessage());
            throw new RuntimeException("Failed to send SMS OTP");
        }
    }
}
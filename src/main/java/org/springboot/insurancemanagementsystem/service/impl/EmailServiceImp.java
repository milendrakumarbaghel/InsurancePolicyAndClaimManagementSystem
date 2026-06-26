package org.springboot.insurancemanagementsystem.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImp implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.otp.expiry-minutes:5}")
    private long expiryMinutes;

    public void sendOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your OTP - Insurance Management System");
            helper.setText(buildEmailBody(otp), true);

            mailSender.send(message);
            log.info("Email OTP sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email OTP to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email OTP");
        }
    }

    @Override
    public void sendPasswordResetOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset OTP - Insurance Management System");
            helper.setText(buildPasswordResetEmailBody(otp), true);

            mailSender.send(message);
            log.info("Password reset OTP sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset OTP to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset OTP");
        }
    }

    private String buildEmailBody(String otp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 480px; margin: auto; background: white; border-radius: 8px;
                              padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2c3e50;">Email Verification</h2>
                    <p>Your One-Time Password (OTP) is:</p>
                    <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                                color: #2980b9; margin: 20px 0;">%s</div>
                    <p>This OTP is valid for <strong>%d minutes</strong>. Do not share it with anyone.</p>
                    <hr style="border: none; border-top: 1px solid #eee;"/>
                    <p style="font-size: 12px; color: #999;">
                        If you didn't request this, please ignore this email.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(otp, expiryMinutes);
    }

    private String buildPasswordResetEmailBody(String otp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 480px; margin: auto; background: white; border-radius: 8px;
                              padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2c3e50;">Password Reset</h2>
                    <p>Use this OTP to reset your password:</p>
                    <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                                color: #2980b9; margin: 20px 0;">%s</div>
                    <p>This OTP is valid for <strong>%d minutes</strong>. Do not share it with anyone.</p>
                    <hr style="border: none; border-top: 1px solid #eee;"/>
                    <p style="font-size: 12px; color: #999;">
                        If you didn't request a password reset, please ignore this email.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(otp, expiryMinutes);
    }
}

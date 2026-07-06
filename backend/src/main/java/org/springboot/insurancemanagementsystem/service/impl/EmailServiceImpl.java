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
public class EmailServiceImpl implements EmailService {

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

    @Override
    public void sendClaimStatusNotification(String toEmail, String customerName, String claimNumber, String status, String remarks) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Claim " + status + " - " + claimNumber + " | Insurance Management System");
            helper.setText(buildClaimStatusEmailBody(customerName, claimNumber, status, remarks), true);

            mailSender.send(message);
            log.info("Claim status notification sent successfully to: {} for claim: {}", toEmail, claimNumber);

        } catch (MessagingException e) {
            log.error("Failed to send claim status notification to {}: {}", toEmail, e.getMessage());
            // Don't throw — notification failure should not block the claim decision
        }
    }

    private String buildClaimStatusEmailBody(String customerName, String claimNumber, String status, String remarks) {
        String statusColor = "APPROVED".equals(status) ? "#27ae60" : "#e74c3c";
        String statusLabel = "APPROVED".equals(status) ? "Approved ✓" : "Rejected ✗";
        String remarksSection = (remarks != null && !remarks.isBlank())
                ? "<p><strong>Remarks:</strong> " + remarks + "</p>"
                : "";

        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 520px; margin: auto; background: white; border-radius: 8px;
                              padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2c3e50;">Claim Status Update</h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Your insurance claim has been reviewed and a final decision has been made.</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                      <tr>
                        <td style="padding: 10px; border: 1px solid #eee; font-weight: bold;">Claim Number</td>
                        <td style="padding: 10px; border: 1px solid #eee;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 10px; border: 1px solid #eee; font-weight: bold;">Decision</td>
                        <td style="padding: 10px; border: 1px solid #eee;">
                          <span style="color: %s; font-weight: bold; font-size: 16px;">%s</span>
                        </td>
                      </tr>
                    </table>
                    %s
                    <hr style="border: none; border-top: 1px solid #eee;"/>
                    <p style="font-size: 12px; color: #999;">
                        If you have any questions, please contact our support team.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(customerName, claimNumber, statusColor, statusLabel, remarksSection);
    }
}

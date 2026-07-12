package com.wastemanagement.wastesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails via Spring Mail (configured in
 * application.properties under spring.mail.*).
 *
 * Currently handles the OTP email for the Forgot Password flow
 * (AuthService.forgotPassword). Kept as its own dedicated service (rather
 * than inlining JavaMailSender calls directly in AuthService) so:
 * - Email templates/formatting live in one place and can grow (HTML
 *   templates, additional email types like a registration welcome email
 *   or a penalty notice) without touching authentication logic
 * - AuthService and any other future caller only depend on a simple,
 *   intention-revealing method signature (sendOtpEmail) rather than the
 *   full JavaMailSender API
 *
 * Uses SimpleMailMessage (plain text) rather than a MIME/HTML template
 * engine, since an OTP code needs no rich formatting — keeping this
 * proportional to the actual requirement (Rule 18). If a richer HTML
 * template is needed later for other email types, MimeMessageHelper can
 * be introduced without changing this class's public method signatures.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * Sends the password-reset OTP to the user's email.
     *
     * Failures here are logged but intentionally not rethrown as a
     * checked/unchecked exception that would abort AuthService.forgotPassword
     * entirely — a transient SMTP failure shouldn't turn into a 500 error
     * response that could hint to a client whether the email address was
     * valid in the system, reinforcing the same account-enumeration
     * protection already established on ForgotPasswordRequest. The OTP
     * record still exists in the database even if the email send fails,
     * so a retry (re-submitting Forgot Password) will simply issue a
     * fresh OTP and try sending again.
     */
    public void sendOtpEmail(String toEmail, String fullName, String otpCode, long validityMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP - Waste Segregation Monitoring System");
            message.setText(buildOtpEmailBody(fullName, otpCode, validityMinutes));

            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
        }
    }

    private String buildOtpEmailBody(String fullName, String otpCode, long validityMinutes) {
        return "Hello " + fullName + ",\n\n"
                + "You requested to reset your password for the Waste Segregation Monitoring System.\n\n"
                + "Your OTP code is: " + otpCode + "\n\n"
                + "This code is valid for " + validityMinutes + " minutes. "
                + "If you did not request this, please ignore this email.\n\n"
                + "Regards,\n"
                + "Waste Segregation Monitoring System";
    }
}
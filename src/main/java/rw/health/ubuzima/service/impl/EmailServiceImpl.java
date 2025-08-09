package rw.health.ubuzima.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import rw.health.ubuzima.service.EmailService;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ubuzima.rw}")
    private String fromEmail;

    @Value("${ubuzima.app.url:http://localhost:3000}")
    private String appUrl;

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {
        if (mailSender == null) {
            log.warn("Email service not configured. Password reset email not sent to: {}", email);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Ubuzima - Password Reset Request");
            
            String resetLink = appUrl + "/reset-password?token=" + resetToken;
            String emailContent = String.format(
                "Hello,\n\n" +
                "You have requested to reset your password for your Ubuzima account.\n\n" +
                "Click the link below to reset your password:\n" +
                "%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you didn't request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Ubuzima Team",
                resetLink
            );
            
            message.setText(emailContent);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
        }
    }

    @Override
    public void sendEmailVerification(String email, String verificationToken) {
        if (mailSender == null) {
            log.warn("Email service not configured. Email verification not sent to: {}", email);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Ubuzima - Email Verification");
            
            String verificationLink = appUrl + "/verify-email?token=" + verificationToken;
            String emailContent = String.format(
                "Hello,\n\n" +
                "Welcome to Ubuzima! Please verify your email address by clicking the link below:\n\n" +
                "%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Best regards,\n" +
                "Ubuzima Team",
                verificationLink
            );
            
            message.setText(emailContent);
            mailSender.send(message);
            log.info("Email verification sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", email, e);
        }
    }

    @Override
    public void sendWelcomeEmail(String email, String userName) {
        if (mailSender == null) {
            log.warn("Email service not configured. Welcome email not sent to: {}", email);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Welcome to Ubuzima!");
            
            String emailContent = String.format(
                "Hello %s,\n\n" +
                "Welcome to Ubuzima - Your Family Planning Companion!\n\n" +
                "We're excited to have you on board. Here's what you can do with Ubuzima:\n\n" +
                "• Track your health records\n" +
                "• Manage contraception methods\n" +
                "• Book appointments\n" +
                "• Access educational content\n" +
                "• Join support groups\n" +
                "• Get health reminders\n\n" +
                "If you have any questions, feel free to contact our support team.\n\n" +
                "Best regards,\n" +
                "Ubuzima Team",
                userName
            );
            
            message.setText(emailContent);
            mailSender.send(message);
            log.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
        }
    }

    @Override
    public void sendAppointmentReminder(String email, String userName, String appointmentDetails) {
        if (mailSender == null) {
            log.warn("Email service not configured. Appointment reminder not sent to: {}", email);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Ubuzima - Appointment Reminder");
            
            String emailContent = String.format(
                "Hello %s,\n\n" +
                "This is a reminder for your upcoming appointment:\n\n" +
                "%s\n\n" +
                "Please arrive 10 minutes before your scheduled time.\n\n" +
                "Best regards,\n" +
                "Ubuzima Team",
                userName, appointmentDetails
            );
            
            message.setText(emailContent);
            mailSender.send(message);
            log.info("Appointment reminder sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send appointment reminder to: {}", email, e);
        }
    }

    @Override
    public void sendMedicationReminder(String email, String userName, String medicationDetails) {
        if (mailSender == null) {
            log.warn("Email service not configured. Medication reminder not sent to: {}", email);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Ubuzima - Medication Reminder");
            
            String emailContent = String.format(
                "Hello %s,\n\n" +
                "This is a reminder to take your medication:\n\n" +
                "%s\n\n" +
                "Please take your medication as prescribed.\n\n" +
                "Best regards,\n" +
                "Ubuzima Team",
                userName, medicationDetails
            );
            
            message.setText(emailContent);
            mailSender.send(message);
            log.info("Medication reminder sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send medication reminder to: {}", email, e);
        }
    }

    @Override
    public void sendHealthCheckReminder(String email, String userName, String checkType) {
        if (mailSender == null) {
            log.warn("Email service not configured. Health check reminder not sent to: {}", email);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Ubuzima - Health Check Reminder");
            
            String emailContent = String.format(
                "Hello %s,\n\n" +
                "This is a reminder for your scheduled health check:\n\n" +
                "Check Type: %s\n\n" +
                "Please schedule your appointment if you haven't already.\n\n" +
                "Best regards,\n" +
                "Ubuzima Team",
                userName, checkType
            );
            
            message.setText(emailContent);
            mailSender.send(message);
            log.info("Health check reminder sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send health check reminder to: {}", email, e);
        }
    }

    @Override
    public void sendNotification(String email, String subject, String message) {
        if (mailSender == null) {
            log.warn("Email service not configured. Notification email not sent to: {}", email);
            return;
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(email);
            mailMessage.setSubject("Ubuzima - " + subject);
            mailMessage.setText(message);
            
            mailSender.send(mailMessage);
            log.info("Notification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send notification email to: {}", email, e);
        }
    }

    @Override
    public void sendSupportTicketUpdate(String email, String ticketId, String status, String message) {
        if (mailSender == null) {
            log.warn("Email service not configured. Support ticket update not sent to: {}", email);
            return;
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(email);
            mailMessage.setSubject("Ubuzima - Support Ticket Update");
            
            String emailContent = String.format(
                "Hello,\n\n" +
                "Your support ticket #%s has been updated.\n\n" +
                "Status: %s\n" +
                "Message: %s\n\n" +
                "Best regards,\n" +
                "Ubuzima Support Team",
                ticketId, status, message
            );
            
            mailMessage.setText(emailContent);
            mailSender.send(mailMessage);
            log.info("Support ticket update email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send support ticket update email to: {}", email, e);
        }
    }
} 
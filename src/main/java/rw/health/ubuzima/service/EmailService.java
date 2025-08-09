package rw.health.ubuzima.service;

public interface EmailService {

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(String email, String resetToken);

    /**
     * Send email verification
     */
    void sendEmailVerification(String email, String verificationToken);

    /**
     * Send welcome email to new users
     */
    void sendWelcomeEmail(String email, String userName);

    /**
     * Send appointment reminder
     */
    void sendAppointmentReminder(String email, String userName, String appointmentDetails);

    /**
     * Send medication reminder
     */
    void sendMedicationReminder(String email, String userName, String medicationDetails);

    /**
     * Send health check reminder
     */
    void sendHealthCheckReminder(String email, String userName, String checkType);

    /**
     * Send general notification
     */
    void sendNotification(String email, String subject, String message);

    /**
     * Send support ticket update
     */
    void sendSupportTicketUpdate(String email, String ticketId, String status, String message);
} 
package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.health.ubuzima.entity.Appointment;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.AppointmentStatus;
import rw.health.ubuzima.enums.NotificationType;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Professional Appointment Notification Service
 * Handles all appointment-related notifications with proper messaging
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentNotificationService {

    private final InteractiveNotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

    /**
     * Send notification when health worker approves/confirms an appointment
     */
    public void sendAppointmentApprovalNotification(Appointment appointment) {
        try {
            User client = appointment.getUser();
            User healthWorker = appointment.getHealthWorker();
            
            String title = "Appointment Confirmed";
            String message = String.format(
                "Great news! Your %s appointment has been confirmed by %s for %s at %s.",
                appointment.getAppointmentType().toString().toLowerCase().replace("_", " "),
                healthWorker != null ? healthWorker.getName() : "your health worker",
                appointment.getScheduledDate().format(DATE_FORMATTER),
                appointment.getHealthFacility().getName()
            );
            
            // Send in-app notification
            notificationService.sendInfoNotification(
                client.getId(),
                "APPOINTMENT_CONFIRMED",
                message
            );
            
            // Send push notification
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "appointment_confirmed");
            pushData.put("appointmentId", appointment.getId());
            pushData.put("appointmentDate", appointment.getScheduledDate().toString());
            pushData.put("facilityName", appointment.getHealthFacility().getName());
            
            pushNotificationService.sendNotificationToUser(
                client.getId(),
                title,
                message,
                pushData
            );
            
            log.info("Sent appointment approval notification to client {}", client.getId());
            
        } catch (Exception e) {
            log.error("Failed to send appointment approval notification", e);
        }
    }

    /**
     * Send notification when appointment is cancelled
     */
    public void sendAppointmentCancellationNotification(Appointment appointment, String reason) {
        try {
            User client = appointment.getUser();
            
            String title = "Appointment Cancelled";
            String message = String.format(
                "Your %s appointment scheduled for %s has been cancelled.",
                appointment.getAppointmentType().toString().toLowerCase().replace("_", " "),
                appointment.getScheduledDate().format(DATE_FORMATTER)
            );
            
            if (reason != null && !reason.trim().isEmpty()) {
                message += "\n\nReason: " + reason;
            }
            
            message += "\n\nPlease contact us to reschedule your appointment.";
            
            // Send warning notification (cancellation is important)
            notificationService.sendWarningNotification(
                client.getId(),
                "APPOINTMENT_CANCELLED",
                message
            );
            
            // Send push notification
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "appointment_cancelled");
            pushData.put("appointmentId", appointment.getId());
            pushData.put("reason", reason);
            
            pushNotificationService.sendNotificationToUser(
                client.getId(),
                title,
                message,
                pushData
            );
            
            log.info("Sent appointment cancellation notification to client {}", client.getId());
            
        } catch (Exception e) {
            log.error("Failed to send appointment cancellation notification", e);
        }
    }

    /**
     * Send notification when appointment is rescheduled
     */
    public void sendAppointmentRescheduleNotification(Appointment appointment, String reason) {
        try {
            User client = appointment.getUser();
            
            String title = "Appointment Rescheduled";
            String message = String.format(
                "Your %s appointment has been rescheduled to %s at %s.",
                appointment.getAppointmentType().toString().toLowerCase().replace("_", " "),
                appointment.getScheduledDate().format(DATE_FORMATTER),
                appointment.getHealthFacility().getName()
            );
            
            if (reason != null && !reason.trim().isEmpty()) {
                message += "\n\nReason: " + reason;
            }
            
            // Send info notification
            notificationService.sendInfoNotification(
                client.getId(),
                "APPOINTMENT_RESCHEDULED",
                message
            );
            
            // Send push notification
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "appointment_rescheduled");
            pushData.put("appointmentId", appointment.getId());
            pushData.put("newDate", appointment.getScheduledDate().toString());
            pushData.put("reason", reason);
            
            pushNotificationService.sendNotificationToUser(
                client.getId(),
                title,
                message,
                pushData
            );
            
            log.info("Sent appointment reschedule notification to client {}", client.getId());
            
        } catch (Exception e) {
            log.error("Failed to send appointment reschedule notification", e);
        }
    }

    /**
     * Send notification when new appointment is booked
     */
    public void sendNewAppointmentNotification(Appointment appointment) {
        try {
            User healthWorker = appointment.getHealthWorker();
            
            if (healthWorker != null) {
                String title = "New Appointment Request";
                String message = String.format(
                    "New %s appointment request from %s for %s. Please review and confirm.",
                    appointment.getAppointmentType().toString().toLowerCase().replace("_", " "),
                    appointment.getUser().getName(),
                    appointment.getScheduledDate().format(DATE_FORMATTER)
                );
                
                // Send info notification to health worker
                notificationService.sendInfoNotification(
                    healthWorker.getId(),
                    "NEW_APPOINTMENT_REQUEST",
                    message
                );
                
                // Send push notification
                Map<String, Object> pushData = new HashMap<>();
                pushData.put("type", "new_appointment_request");
                pushData.put("appointmentId", appointment.getId());
                pushData.put("clientName", appointment.getUser().getName());
                pushData.put("appointmentDate", appointment.getScheduledDate().toString());
                
                pushNotificationService.sendNotificationToUser(
                    healthWorker.getId(),
                    title,
                    message,
                    pushData
                );
                
                log.info("Sent new appointment notification to health worker {}", healthWorker.getId());
            }
            
            // Send confirmation to client
            sendAppointmentBookingConfirmation(appointment);
            
        } catch (Exception e) {
            log.error("Failed to send new appointment notification", e);
        }
    }

    /**
     * Send booking confirmation to client
     */
    private void sendAppointmentBookingConfirmation(Appointment appointment) {
        try {
            User client = appointment.getUser();
            
            String title = "Appointment Booked";
            String message = String.format(
                "Your %s appointment has been successfully booked for %s at %s. " +
                "Your health worker will confirm this appointment soon.",
                appointment.getAppointmentType().toString().toLowerCase().replace("_", " "),
                appointment.getScheduledDate().format(DATE_FORMATTER),
                appointment.getHealthFacility().getName()
            );
            
            // Send info notification
            notificationService.sendInfoNotification(
                client.getId(),
                "APPOINTMENT_BOOKED",
                message
            );
            
            // Send push notification
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "appointment_booked");
            pushData.put("appointmentId", appointment.getId());
            pushData.put("status", "pending_confirmation");
            
            pushNotificationService.sendNotificationToUser(
                client.getId(),
                title,
                message,
                pushData
            );
            
            log.info("Sent appointment booking confirmation to client {}", client.getId());
            
        } catch (Exception e) {
            log.error("Failed to send appointment booking confirmation", e);
        }
    }

    /**
     * Send reminder notification before appointment
     */
    public void sendAppointmentReminderNotification(Appointment appointment, String timeframe) {
        try {
            User client = appointment.getUser();
            
            String title = "Appointment Reminder";
            String message = String.format(
                "Reminder: You have a %s appointment in %s at %s with %s.",
                appointment.getAppointmentType().toString().toLowerCase().replace("_", " "),
                timeframe,
                appointment.getScheduledDate().format(DATE_FORMATTER),
                appointment.getHealthWorker() != null ? 
                    appointment.getHealthWorker().getName() : "your health worker"
            );
            
            message += "\n\nPlease arrive 15 minutes early for check-in.";
            
            // Send info notification
            notificationService.sendInfoNotification(
                client.getId(),
                "APPOINTMENT_REMINDER",
                message
            );
            
            // Send push notification
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "appointment_reminder");
            pushData.put("appointmentId", appointment.getId());
            pushData.put("timeframe", timeframe);
            pushData.put("facilityName", appointment.getHealthFacility().getName());
            
            pushNotificationService.sendNotificationToUser(
                client.getId(),
                title,
                message,
                pushData
            );
            
            log.info("Sent {} appointment reminder to client {}", timeframe, client.getId());
            
        } catch (Exception e) {
            log.error("Failed to send appointment reminder notification", e);
        }
    }

    /**
     * Send notification when appointment status changes
     */
    public void sendAppointmentStatusChangeNotification(Appointment appointment, 
                                                       AppointmentStatus oldStatus, 
                                                       AppointmentStatus newStatus) {
        try {
            String title = "Appointment Update";
            String message = getStatusChangeMessage(appointment, oldStatus, newStatus);
            
            // Send to client
            notificationService.sendInfoNotification(
                appointment.getUser().getId(),
                "APPOINTMENT_STATUS_CHANGED",
                message
            );
            
            // Send push notification
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "appointment_status_changed");
            pushData.put("appointmentId", appointment.getId());
            pushData.put("oldStatus", oldStatus.toString());
            pushData.put("newStatus", newStatus.toString());
            
            pushNotificationService.sendNotificationToUser(
                appointment.getUser().getId(),
                title,
                message,
                pushData
            );
            
            log.info("Sent status change notification for appointment {} from {} to {}", 
                appointment.getId(), oldStatus, newStatus);
            
        } catch (Exception e) {
            log.error("Failed to send appointment status change notification", e);
        }
    }

    /**
     * Get appropriate message for status change
     */
    private String getStatusChangeMessage(Appointment appointment, 
                                        AppointmentStatus oldStatus, 
                                        AppointmentStatus newStatus) {
        return switch (newStatus) {
            case CONFIRMED -> String.format(
                "Your %s appointment for %s has been confirmed by your health worker.",
                appointment.getAppointmentType().toString().toLowerCase().replace("_", " "),
                appointment.getScheduledDate().format(DATE_FORMATTER)
            );
            case IN_PROGRESS -> "Your appointment is now in progress.";
            case COMPLETED -> String.format(
                "Your %s appointment has been completed. Thank you for visiting us!",
                appointment.getAppointmentType().toString().toLowerCase().replace("_", " ")
            );
            case CANCELLED -> String.format(
                "Your %s appointment for %s has been cancelled. Please contact us to reschedule.",
                appointment.getAppointmentType().toString().toLowerCase().replace("_", " "),
                appointment.getScheduledDate().format(DATE_FORMATTER)
            );
            case NO_SHOW -> "You missed your scheduled appointment. Please contact us to reschedule.";
            case RESCHEDULED -> String.format(
                "Your appointment has been rescheduled to %s.",
                appointment.getScheduledDate().format(DATE_FORMATTER)
            );
            default -> String.format(
                "Your appointment status has been updated to %s.",
                newStatus.toString().toLowerCase().replace("_", " ")
            );
        };
    }
}

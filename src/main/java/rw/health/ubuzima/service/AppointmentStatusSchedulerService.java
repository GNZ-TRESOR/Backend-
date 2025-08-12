package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.health.ubuzima.entity.Appointment;
import rw.health.ubuzima.entity.Notification;
import rw.health.ubuzima.enums.AppointmentStatus;
import rw.health.ubuzima.enums.NotificationType;
import rw.health.ubuzima.repository.AppointmentRepository;
import rw.health.ubuzima.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Professional Appointment Status Management Service
 * Automatically updates appointment statuses based on date/time and business rules
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentStatusSchedulerService {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final InteractiveNotificationService notificationService;
    private final PushNotificationService pushNotificationService;

    /**
     * Scheduled task to update appointment statuses
     * Runs every 15 minutes to check for status updates
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    @Transactional
    public void updateAppointmentStatuses() {
        log.info("Starting scheduled appointment status update...");

        try {
            LocalDateTime now = LocalDateTime.now();

            // Update overdue scheduled appointments to MISSED/NO_SHOW
            updateOverdueAppointments(now);

            // Update confirmed appointments to IN_PROGRESS if they're starting
            updateAppointmentsToInProgress(now);

            // Update in-progress appointments to COMPLETED if they're past end time
            updateAppointmentsToCompleted(now);

            // Send appointment reminders
            sendAppointmentReminders(now);

            log.info("Completed scheduled appointment status update");

        } catch (Exception e) {
            log.error("Error during scheduled appointment status update", e);
            // Don't rethrow to prevent scheduler from stopping
        }
    }

    /**
     * Update overdue scheduled and confirmed appointments to NO_SHOW
     */
    private void updateOverdueAppointments(LocalDateTime now) {
        // Find SCHEDULED appointments that are past their scheduled time (immediate)
        List<Appointment> overdueScheduled = appointmentRepository
            .findByStatusAndScheduledDateBefore(AppointmentStatus.SCHEDULED, now);

        // Find CONFIRMED appointments that are 15 minutes past their scheduled time
        LocalDateTime confirmedOverdueThreshold = now.minusMinutes(15);
        List<Appointment> overdueConfirmed = appointmentRepository
            .findByStatusAndScheduledDateBefore(AppointmentStatus.CONFIRMED, confirmedOverdueThreshold);

        // Process SCHEDULED appointments (immediate no-show)
        for (Appointment appointment : overdueScheduled) {
            appointment.setStatus(AppointmentStatus.NO_SHOW);
            appointmentRepository.save(appointment);

            // Notify health worker about no-show (with error handling)
            try {
                notifyHealthWorkerAboutNoShow(appointment);
            } catch (Exception e) {
                log.warn("Failed to send no-show notification for appointment {}: {}",
                    appointment.getId(), e.getMessage());
            }

            log.info("Updated SCHEDULED appointment {} to NO_SHOW status (immediate)", appointment.getId());
        }

        // Process CONFIRMED appointments (15 minutes grace period)
        for (Appointment appointment : overdueConfirmed) {
            appointment.setStatus(AppointmentStatus.NO_SHOW);
            appointmentRepository.save(appointment);

            // Notify health worker about no-show (with error handling)
            try {
                notifyHealthWorkerAboutNoShow(appointment);
            } catch (Exception e) {
                log.warn("Failed to send no-show notification for appointment {}: {}",
                    appointment.getId(), e.getMessage());
            }

            log.info("Updated CONFIRMED appointment {} to NO_SHOW status (15min grace)", appointment.getId());
        }

        int totalUpdated = overdueScheduled.size() + overdueConfirmed.size();
        log.info("Updated {} overdue appointments to NO_SHOW ({} scheduled, {} confirmed)",
            totalUpdated, overdueScheduled.size(), overdueConfirmed.size());
    }

    /**
     * Update confirmed appointments to IN_PROGRESS if they're starting
     */
    private void updateAppointmentsToInProgress(LocalDateTime now) {
        // Find confirmed appointments that should be starting (within 5 minutes of scheduled time)
        LocalDateTime startThreshold = now.minusMinutes(5);
        LocalDateTime endThreshold = now.plusMinutes(5);
        
        List<Appointment> startingAppointments = appointmentRepository
            .findByStatusAndScheduledDateBetween(
                AppointmentStatus.CONFIRMED, 
                startThreshold, 
                endThreshold
            );
        
        for (Appointment appointment : startingAppointments) {
            appointment.setStatus(AppointmentStatus.IN_PROGRESS);
            appointmentRepository.save(appointment);
            
            // Notify both client and health worker (with error handling)
            try {
                notifyAppointmentStarted(appointment);
            } catch (Exception e) {
                log.warn("Failed to send appointment started notification for appointment {}: {}",
                    appointment.getId(), e.getMessage());
            }
            
            log.info("Updated appointment {} to IN_PROGRESS status", appointment.getId());
        }
        
        log.info("Updated {} appointments to IN_PROGRESS", startingAppointments.size());
    }

    /**
     * Update in-progress appointments to COMPLETED if they're past end time
     */
    private void updateAppointmentsToCompleted(LocalDateTime now) {
        // Find in-progress appointments that are past their expected end time
        List<Appointment> inProgressAppointments = appointmentRepository
            .findByStatus(AppointmentStatus.IN_PROGRESS);
        
        int completedCount = 0;
        for (Appointment appointment : inProgressAppointments) {
            LocalDateTime expectedEndTime = appointment.getScheduledDate()
                .plusMinutes(appointment.getDurationMinutes() != null ? 
                    appointment.getDurationMinutes() : 30);
            
            if (now.isAfter(expectedEndTime.plusMinutes(15))) { // 15 minutes grace period
                appointment.setStatus(AppointmentStatus.COMPLETED);
                appointment.setCompletedAt(now);
                appointmentRepository.save(appointment);
                
                // Notify about completion (with error handling)
                try {
                    notifyAppointmentCompleted(appointment);
                } catch (Exception e) {
                    log.warn("Failed to send appointment completed notification for appointment {}: {}",
                        appointment.getId(), e.getMessage());
                }
                
                completedCount++;
                log.info("Auto-completed appointment {} after duration", appointment.getId());
            }
        }
        
        log.info("Auto-completed {} appointments", completedCount);
    }

    /**
     * Send appointment reminders
     */
    private void sendAppointmentReminders(LocalDateTime now) {
        // Send reminders 24 hours before appointment
        LocalDateTime reminderTime24h = now.plusHours(24);
        List<Appointment> appointmentsIn24h = appointmentRepository
            .findAppointmentsForReminder(reminderTime24h.minusMinutes(15), reminderTime24h.plusMinutes(15));
        
        for (Appointment appointment : appointmentsIn24h) {
            if (!Boolean.TRUE.equals(appointment.getReminderSent())) {
                sendAppointmentReminder(appointment, "24 hours");
                appointment.setReminderSent(true);
                appointmentRepository.save(appointment);
            }
        }
        
        // Send reminders 2 hours before appointment
        LocalDateTime reminderTime2h = now.plusHours(2);
        List<Appointment> appointmentsIn2h = appointmentRepository
            .findAppointmentsForReminder(reminderTime2h.minusMinutes(15), reminderTime2h.plusMinutes(15));
        
        for (Appointment appointment : appointmentsIn2h) {
            sendAppointmentReminder(appointment, "2 hours");
        }
        
        log.info("Sent {} 24-hour reminders and {} 2-hour reminders", 
            appointmentsIn24h.size(), appointmentsIn2h.size());
    }

    /**
     * Notify health worker about no-show appointment
     */
    private void notifyHealthWorkerAboutNoShow(Appointment appointment) {
        if (appointment.getHealthWorker() != null) {
            String message = String.format(
                "Client %s did not show up for their %s appointment scheduled at %s",
                appointment.getUser().getName(),
                appointment.getAppointmentType().toString().toLowerCase(),
                appointment.getScheduledDate().toString()
            );
            
            notificationService.sendWarningNotification(
                appointment.getHealthWorker().getId(),
                "APPOINTMENT_NO_SHOW",
                message
            );
            
            // Send push notification
            pushNotificationService.sendNotificationToUser(
                appointment.getHealthWorker().getId(),
                "Appointment No-Show",
                message,
                java.util.Map.of("appointmentId", appointment.getId(), "type", "no_show")
            );
        }
    }

    /**
     * Notify about appointment starting
     */
    private void notifyAppointmentStarted(Appointment appointment) {
        // Notify client
        String clientMessage = String.format(
            "Your %s appointment with %s is now starting",
            appointment.getAppointmentType().toString().toLowerCase(),
            appointment.getHealthWorker() != null ? 
                appointment.getHealthWorker().getName() : "your health worker"
        );
        
        notificationService.sendInfoNotification(
            appointment.getUser().getId(),
            "APPOINTMENT_STARTED",
            clientMessage
        );
        
        // Notify health worker
        if (appointment.getHealthWorker() != null) {
            String hwMessage = String.format(
                "Appointment with %s is now starting",
                appointment.getUser().getName()
            );
            
            notificationService.sendInfoNotification(
                appointment.getHealthWorker().getId(),
                "APPOINTMENT_STARTED",
                hwMessage
            );
        }
    }

    /**
     * Notify about appointment completion
     */
    private void notifyAppointmentCompleted(Appointment appointment) {
        // Notify client
        String clientMessage = String.format(
            "Your %s appointment has been completed. Thank you for visiting us!",
            appointment.getAppointmentType().toString().toLowerCase()
        );
        
        notificationService.sendInfoNotification(
            appointment.getUser().getId(),
            "APPOINTMENT_COMPLETED",
            clientMessage
        );
        
        // Notify health worker
        if (appointment.getHealthWorker() != null) {
            String hwMessage = String.format(
                "Appointment with %s has been marked as completed",
                appointment.getUser().getName()
            );
            
            notificationService.sendInfoNotification(
                appointment.getHealthWorker().getId(),
                "APPOINTMENT_COMPLETED",
                hwMessage
            );
        }
    }

    /**
     * Send appointment reminder
     */
    private void sendAppointmentReminder(Appointment appointment, String timeframe) {
        String message = String.format(
            "Reminder: You have a %s appointment in %s at %s",
            appointment.getAppointmentType().toString().toLowerCase(),
            timeframe,
            appointment.getScheduledDate().toString()
        );
        
        // Send to client
        notificationService.sendInfoNotification(
            appointment.getUser().getId(),
            "APPOINTMENT_REMINDER",
            message
        );
        
        pushNotificationService.sendAppointmentReminder(
            appointment.getUser().getId(),
            message
        );
        
        log.info("Sent {} reminder for appointment {}", timeframe, appointment.getId());
    }

    /**
     * Manual method to update a specific appointment status
     */
    public void updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(newStatus);
        
        // Set completion time if completed
        if (newStatus == AppointmentStatus.COMPLETED) {
            appointment.setCompletedAt(LocalDateTime.now());
        }
        
        // Set cancellation details if cancelled
        if (newStatus == AppointmentStatus.CANCELLED) {
            appointment.setCancelledAt(LocalDateTime.now());
            appointment.setCancellationReason(reason);
        }
        
        appointmentRepository.save(appointment);
        
        // Send notifications about status change
        notifyStatusChange(appointment, oldStatus, newStatus, reason);
        
        log.info("Manually updated appointment {} from {} to {}", 
            appointmentId, oldStatus, newStatus);
    }

    /**
     * Notify about manual status changes
     */
    private void notifyStatusChange(Appointment appointment, AppointmentStatus oldStatus, 
                                  AppointmentStatus newStatus, String reason) {
        String statusMessage = getStatusChangeMessage(appointment, oldStatus, newStatus, reason);
        
        // Notify client
        notificationService.sendInfoNotification(
            appointment.getUser().getId(),
            "APPOINTMENT_STATUS_CHANGED",
            statusMessage
        );
        
        // Notify health worker if different from the one making the change
        if (appointment.getHealthWorker() != null) {
            notificationService.sendInfoNotification(
                appointment.getHealthWorker().getId(),
                "APPOINTMENT_STATUS_CHANGED",
                statusMessage
            );
        }
        
        // Send push notification to client
        pushNotificationService.sendNotificationToUser(
            appointment.getUser().getId(),
            "Appointment Update",
            statusMessage,
            java.util.Map.of(
                "appointmentId", appointment.getId(),
                "oldStatus", oldStatus.toString(),
                "newStatus", newStatus.toString()
            )
        );
    }

    /**
     * Get appropriate message for status change
     */
    private String getStatusChangeMessage(Appointment appointment, AppointmentStatus oldStatus, 
                                        AppointmentStatus newStatus, String reason) {
        return switch (newStatus) {
            case CONFIRMED -> "Your appointment has been confirmed by your health worker";
            case CANCELLED -> "Your appointment has been cancelled" + 
                (reason != null ? ": " + reason : "");
            case COMPLETED -> "Your appointment has been completed";
            case RESCHEDULED -> "Your appointment has been rescheduled" + 
                (reason != null ? ": " + reason : "");
            case NO_SHOW -> "You missed your scheduled appointment";
            default -> String.format("Your appointment status has been updated to %s", 
                newStatus.toString().toLowerCase());
        };
    }
}

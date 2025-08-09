package rw.health.ubuzima.service;

import rw.health.ubuzima.entity.Notification;

import java.util.List;
import java.util.Map;

public interface PushNotificationService {

    /**
     * Send push notification to a specific user
     */
    void sendNotificationToUser(Long userId, String title, String message, Map<String, Object> data);

    /**
     * Send push notification to multiple users
     */
    void sendNotificationToUsers(List<Long> userIds, String title, String message, Map<String, Object> data);

    /**
     * Send push notification to all users
     */
    void sendNotificationToAll(String title, String message, Map<String, Object> data);

    /**
     * Send appointment reminder notification
     */
    void sendAppointmentReminder(Long userId, String appointmentDetails);

    /**
     * Send medication reminder notification
     */
    void sendMedicationReminder(Long userId, String medicationDetails);

    /**
     * Send health check reminder notification
     */
    void sendHealthCheckReminder(Long userId, String checkType);

    /**
     * Send support ticket update notification
     */
    void sendSupportTicketUpdate(Long userId, String ticketId, String status);

    /**
     * Send educational content notification
     */
    void sendEducationalContentNotification(Long userId, String lessonTitle);

    /**
     * Send community event notification
     */
    void sendCommunityEventNotification(Long userId, String eventTitle);

    /**
     * Register device token for a user
     */
    void registerDeviceToken(Long userId, String deviceToken, String platform);

    /**
     * Unregister device token for a user
     */
    void unregisterDeviceToken(Long userId, String deviceToken);

    /**
     * Get notification statistics
     */
    Map<String, Object> getNotificationStats();

    /**
     * Send notification from Notification entity
     */
    void sendNotification(Notification notification);
} 
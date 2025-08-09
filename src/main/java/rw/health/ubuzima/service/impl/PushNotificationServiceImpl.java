package rw.health.ubuzima.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.NotificationType;
import rw.health.ubuzima.repository.NotificationRepository;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.service.PushNotificationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    @Autowired(required = false)
    private FirebaseMessaging firebaseMessaging;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void sendNotificationToUser(Long userId, String title, String message, Map<String, Object> data) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Save notification to database
            rw.health.ubuzima.entity.Notification notification = new rw.health.ubuzima.entity.Notification();
            notification.setUser(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(NotificationType.PUSH);
            notification.setIsRead(false);
            notificationRepository.save(notification);

            // Send push notification if device token exists and Firebase is configured
            if (firebaseMessaging != null && user.getDeviceToken() != null && !user.getDeviceToken().isEmpty()) {
                sendFirebaseNotification(user.getDeviceToken(), title, message, data);
            } else if (firebaseMessaging == null) {
                log.warn("Firebase messaging not configured. Push notification not sent to user: {}", userId);
            }

            log.info("Push notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send push notification to user: {}", userId, e);
        }
    }

    @Override
    public void sendNotificationToUsers(List<Long> userIds, String title, String message, Map<String, Object> data) {
        userIds.forEach(userId -> {
            CompletableFuture.runAsync(() -> {
                sendNotificationToUser(userId, title, message, data);
            });
        });
    }

    @Override
    public void sendNotificationToAll(String title, String message, Map<String, Object> data) {
        try {
            List<User> users = userRepository.findByDeviceTokenIsNotNull();
            users.forEach(user -> {
                CompletableFuture.runAsync(() -> {
                    sendNotificationToUser(user.getId(), title, message, data);
                });
            });
        } catch (Exception e) {
            log.error("Failed to send notification to all users", e);
        }
    }

    @Override
    public void sendAppointmentReminder(Long userId, String appointmentDetails) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "appointment_reminder");
        data.put("appointmentDetails", appointmentDetails);
        
        sendNotificationToUser(userId, "Appointment Reminder", 
            "You have an upcoming appointment: " + appointmentDetails, data);
    }

    @Override
    public void sendMedicationReminder(Long userId, String medicationDetails) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "medication_reminder");
        data.put("medicationDetails", medicationDetails);
        
        sendNotificationToUser(userId, "Medication Reminder", 
            "Time to take your medication: " + medicationDetails, data);
    }

    @Override
    public void sendHealthCheckReminder(Long userId, String checkType) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "health_check_reminder");
        data.put("checkType", checkType);
        
        sendNotificationToUser(userId, "Health Check Reminder", 
            "Scheduled health check: " + checkType, data);
    }

    @Override
    public void sendSupportTicketUpdate(Long userId, String ticketId, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "support_ticket_update");
        data.put("ticketId", ticketId);
        data.put("status", status);
        
        sendNotificationToUser(userId, "Support Ticket Update", 
            "Your support ticket #" + ticketId + " status: " + status, data);
    }

    @Override
    public void sendEducationalContentNotification(Long userId, String lessonTitle) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "educational_content");
        data.put("lessonTitle", lessonTitle);
        
        sendNotificationToUser(userId, "New Educational Content", 
            "New lesson available: " + lessonTitle, data);
    }

    @Override
    public void sendCommunityEventNotification(Long userId, String eventTitle) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "community_event");
        data.put("eventTitle", eventTitle);
        
        sendNotificationToUser(userId, "Community Event", 
            "New community event: " + eventTitle, data);
    }

    @Override
    public void registerDeviceToken(Long userId, String deviceToken, String platform) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setDeviceToken(deviceToken);
            user.setPlatform(platform);
            userRepository.save(user);
            
            log.info("Device token registered for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to register device token for user: {}", userId, e);
        }
    }

    @Override
    public void unregisterDeviceToken(Long userId, String deviceToken) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (deviceToken.equals(user.getDeviceToken())) {
                user.setDeviceToken(null);
                user.setPlatform(null);
                userRepository.save(user);
            }
            
            log.info("Device token unregistered for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to unregister device token for user: {}", userId, e);
        }
    }

    @Override
    public Map<String, Object> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalNotifications = notificationRepository.count();
            long unreadNotifications = notificationRepository.countByIsReadFalse();
            long todayNotifications = notificationRepository.countByCreatedAtAfter(LocalDateTime.now().withHour(0).withMinute(0));
            
            stats.put("totalNotifications", totalNotifications);
            stats.put("unreadNotifications", unreadNotifications);
            stats.put("todayNotifications", todayNotifications);
            
        } catch (Exception e) {
            log.error("Failed to get notification stats", e);
        }
        
        return stats;
    }

    @Override
    public void sendNotification(rw.health.ubuzima.entity.Notification notification) {
        try {
            User user = notification.getUser();
            
            Map<String, Object> data = new HashMap<>();
            data.put("type", "general");
            data.put("notificationId", notification.getId());
            
            if (firebaseMessaging != null && user.getDeviceToken() != null && !user.getDeviceToken().isEmpty()) {
                sendFirebaseNotification(user.getDeviceToken(),
                    notification.getTitle(), notification.getMessage(), data);
            } else if (firebaseMessaging == null) {
                log.warn("Firebase messaging not configured. Push notification not sent for notification: {}", notification.getId());
            }
            
            log.info("Notification sent from entity: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send notification from entity: {}", notification.getId(), e);
        }
    }

    private void sendFirebaseNotification(String deviceToken, String title, String message, Map<String, Object> data) {
        if (firebaseMessaging == null) {
            log.warn("Firebase messaging not configured. Cannot send push notification.");
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(message)
                            .build());

            // Add custom data
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data.entrySet().stream()
                        .collect(HashMap::new, (map, entry) -> 
                            map.put(entry.getKey(), entry.getValue().toString()), HashMap::putAll));
            }

            String response = firebaseMessaging.send(messageBuilder.build());
            log.info("Firebase notification sent successfully: {}", response);
            
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send Firebase notification", e);
        }
    }
} 
package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.health.ubuzima.constants.NotificationConstants;
import rw.health.ubuzima.dto.response.ApiResponse;
import rw.health.ubuzima.entity.Notification;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.NotificationType;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.repository.NotificationRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InteractiveNotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    // Send success notification to user
    public void sendSuccessNotification(Long userId, String operation, String message) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;
            
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(getSuccessTitle(user.getRole(), operation));
            notification.setMessage(message);
            notification.setType(NotificationType.SUCCESS);
            notification.setPriority(NotificationConstants.PRIORITY_LOW);
            notification.setIcon(NotificationConstants.ICON_SUCCESS);
            notification.setIsRead(false);
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to send success notification: " + e.getMessage());
        }
    }
    
    // Send error notification to user
    public void sendErrorNotification(Long userId, String operation, String errorMessage, String errorCode) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;
            
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(getErrorTitle(user.getRole(), operation));
            notification.setMessage(errorMessage);
            notification.setType(NotificationType.ERROR);
            notification.setPriority(NotificationConstants.PRIORITY_HIGH);
            notification.setIcon(NotificationConstants.ICON_ERROR);
            notification.setIsRead(false);
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to send error notification: " + e.getMessage());
        }
    }
    
    // Send warning notification to user
    public void sendWarningNotification(Long userId, String operation, String warningMessage) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;
            
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(getWarningTitle(user.getRole(), operation));
            notification.setMessage(warningMessage);
            notification.setType(NotificationType.WARNING);
            notification.setPriority(NotificationConstants.PRIORITY_MEDIUM);
            notification.setIcon(NotificationConstants.ICON_WARNING);
            notification.setIsRead(false);
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to send warning notification: " + e.getMessage());
        }
    }
    
    // Send info notification to user
    public void sendInfoNotification(Long userId, String operation, String infoMessage) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;
            
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(getInfoTitle(user.getRole(), operation));
            notification.setMessage(infoMessage);
            notification.setType(NotificationType.INFO);
            notification.setPriority(NotificationConstants.PRIORITY_LOW);
            notification.setIcon(NotificationConstants.ICON_INFO);
            notification.setIsRead(false);
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to send info notification: " + e.getMessage());
        }
    }
    
    private String getSuccessTitle(UserRole role, String operation) {
        return switch (role) {
            case ADMIN -> "Admin: Operation Successful";
            case HEALTH_WORKER -> "Health Worker: Operation Successful";
            case CLIENT -> "Operation Successful";
        };
    }
    
    private String getErrorTitle(UserRole role, String operation) {
        return switch (role) {
            case ADMIN -> "Admin: Operation Failed";
            case HEALTH_WORKER -> "Health Worker: Operation Failed";
            case CLIENT -> "Operation Failed";
        };
    }
    
    private String getWarningTitle(UserRole role, String operation) {
        return switch (role) {
            case ADMIN -> "Admin: Warning";
            case HEALTH_WORKER -> "Health Worker: Warning";
            case CLIENT -> "Warning";
        };
    }
    
    private String getInfoTitle(UserRole role, String operation) {
        return switch (role) {
            case ADMIN -> "Admin: Information";
            case HEALTH_WORKER -> "Health Worker: Information";
            case CLIENT -> "Information";
        };
    }
    
    // Send real-time notification (for WebSocket implementation)
    public void sendRealTimeNotification(Long userId, ApiResponse<?> response) {
        // This would integrate with WebSocket or Server-Sent Events
        // For now, we'll create a database notification
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;
            
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(response.isSuccess() ? "Operation Completed" : "Operation Failed");
            notification.setMessage(response.getUserMessage());
            notification.setType(response.isSuccess() ? NotificationType.SUCCESS : NotificationType.ERROR);
            notification.setPriority(response.isSuccess() ? NotificationConstants.PRIORITY_LOW : NotificationConstants.PRIORITY_HIGH);
            notification.setIcon(response.isSuccess() ? NotificationConstants.ICON_SUCCESS : NotificationConstants.ICON_ERROR);
            notification.setIsRead(false);
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            System.err.println("Failed to send real-time notification: " + e.getMessage());
        }
    }
}

package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.Notification;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.NotificationType;
import rw.health.ubuzima.repository.NotificationRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            Pageable pageable = PageRequest.of(page, limit);
            Page<Notification> notificationsPage = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "notifications", notificationsPage.getContent(),
                "total", notificationsPage.getTotalElements(),
                "page", page,
                "totalPages", notificationsPage.getTotalPages()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch notifications: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(@PathVariable Long id) {
        try {
            Notification notification = notificationRepository.findById(id).orElse(null);
            
            if (notification == null) {
                return ResponseEntity.notFound().build();
            }

            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification marked as read"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to mark notification as read: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
            Long unreadCount = notificationRepository.countUnreadNotifications(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "unreadNotifications", unreadNotifications,
                "unreadCount", unreadCount
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch unread notifications: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(request.get("title").toString());
            notification.setMessage(request.get("message").toString());
            
            if (request.get("type") != null) {
                notification.setType(NotificationType.valueOf(request.get("type").toString().toUpperCase()));
            }
            
            if (request.get("priority") != null) {
                notification.setPriority(Integer.valueOf(request.get("priority").toString()));
            }
            
            if (request.get("actionUrl") != null) {
                notification.setActionUrl(request.get("actionUrl").toString());
            }
            
            if (request.get("icon") != null) {
                notification.setIcon(request.get("icon").toString());
            }
            
            if (request.get("scheduledFor") != null) {
                notification.setScheduledFor(LocalDateTime.parse(request.get("scheduledFor").toString()));
            }

            Notification savedNotification = notificationRepository.save(notification);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification created successfully",
                "notification", savedNotification
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create notification: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/register-device")
    public ResponseEntity<Map<String, Object>> registerDevice(@RequestBody Map<String, Object> request) {
        try {
            // This would typically register a device token for push notifications
            // For now, we'll just return success
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Device registered successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to register device: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/types/{type}")
    public ResponseEntity<Map<String, Object>> getNotificationsByType(
            @PathVariable String type,
            @RequestParam Long userId) {
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
            List<Notification> notifications = notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(user, notificationType);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "notifications", notifications
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch notifications by type: " + e.getMessage()
            ));
        }
    }
}

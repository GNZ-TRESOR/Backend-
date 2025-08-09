package rw.health.ubuzima.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.Notification;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    Long countUnreadNotifications(@Param("user") User user);
    
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);
    
    @Query("SELECT n FROM Notification n WHERE n.scheduledFor <= :now AND n.sentAt IS NULL ORDER BY n.scheduledFor ASC")
    List<Notification> findPendingNotifications(@Param("now") LocalDateTime now);
    
    List<Notification> findByUserAndPriorityGreaterThanEqualOrderByCreatedAtDesc(User user, Integer priority);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsBetweenDates(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Admin-specific methods
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type, Pageable pageable);

    Page<Notification> findByTitleContainingIgnoreCaseOrMessageContainingIgnoreCaseOrderByCreatedAtDesc(
        String titleSearch, String messageSearch, Pageable pageable);

    // Additional methods for PushNotificationService
    long countByIsReadFalse();

    long countByCreatedAtAfter(LocalDateTime dateTime);

    // Analytics methods needed by AnalyticsServiceImpl
    long countByIsRead(boolean isRead);
}

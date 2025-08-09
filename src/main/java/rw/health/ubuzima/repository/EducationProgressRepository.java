package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.EducationProgress;
import rw.health.ubuzima.entity.EducationLesson;
import rw.health.ubuzima.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EducationProgressRepository extends JpaRepository<EducationProgress, Long> {
    
    @Query("SELECT ep FROM EducationProgress ep JOIN FETCH ep.lesson WHERE ep.user = :user ORDER BY ep.lastAccessedAt DESC")
    List<EducationProgress> findByUserOrderByLastAccessedAtDesc(@Param("user") User user);
    
    @Query("SELECT ep FROM EducationProgress ep JOIN FETCH ep.lesson WHERE ep.user = :user AND ep.lesson = :lesson")
    Optional<EducationProgress> findByUserAndLesson(@Param("user") User user, @Param("lesson") EducationLesson lesson);
    
    @Query("SELECT ep FROM EducationProgress ep JOIN FETCH ep.lesson WHERE ep.user = :user AND ep.isCompleted = true")
    List<EducationProgress> findByUserAndIsCompletedTrue(@Param("user") User user);
    
    List<EducationProgress> findByUserAndIsCompletedFalse(User user);
    
    @Query("SELECT AVG(ep.progressPercentage) FROM EducationProgress ep WHERE ep.user = :user")
    Double calculateAverageProgress(@Param("user") User user);
    
    @Query("SELECT COUNT(ep) FROM EducationProgress ep WHERE ep.user = :user AND ep.isCompleted = true")
    Long countCompletedLessons(@Param("user") User user);
    
    @Query("SELECT SUM(ep.timeSpentMinutes) FROM EducationProgress ep WHERE ep.user = :user")
    Long calculateTotalTimeSpent(@Param("user") User user);
    
    @Query("SELECT ep FROM EducationProgress ep JOIN FETCH ep.lesson WHERE ep.user = :user AND ep.progressPercentage > 0 AND ep.isCompleted = false ORDER BY ep.lastAccessedAt DESC")
    List<EducationProgress> findInProgressLessons(@Param("user") User user);

    @Query("SELECT ep FROM EducationProgress ep JOIN FETCH ep.lesson WHERE ep.user = :user AND ep.isCompleted = true ORDER BY ep.completedAt DESC")
    List<EducationProgress> findCompletedLessons(@Param("user") User user);

    // Additional methods for admin functionality
    List<EducationProgress> findByLesson(EducationLesson lesson);

    @Query("SELECT COUNT(ep) FROM EducationProgress ep WHERE ep.isCompleted = true")
    Long countByIsCompletedTrue();

    // Analytics methods needed by AnalyticsServiceImpl
    long countByIsCompleted(boolean isCompleted);
    long countByUserIdAndUpdatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}

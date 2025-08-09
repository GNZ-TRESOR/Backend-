package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.HealthRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    // Find health record by user ID (one record per user approach)
    Optional<HealthRecord> findByUserId(Long userId);

    // Check if user has health record
    boolean existsByUserId(Long userId);

    // Delete by user ID
    void deleteByUserId(Long userId);

    // Find all records ordered by last updated
    List<HealthRecord> findAllByOrderByLastUpdatedDesc();

    // Find by health status
    List<HealthRecord> findByHealthStatus(String healthStatus);

    // Find records with complete vitals (all main metrics present)
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.heartRateValue IS NOT NULL AND hr.bpValue IS NOT NULL AND hr.kgValue IS NOT NULL AND hr.tempValue IS NOT NULL")
    List<HealthRecord> findRecordsWithCompleteVitals();

    // Search records by user name or email
    @Query("SELECT hr FROM HealthRecord hr WHERE LOWER(hr.user.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(hr.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<HealthRecord> searchRecords(@Param("searchTerm") String searchTerm);

    // Find records by date range
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.lastUpdated BETWEEN :startDate AND :endDate ORDER BY hr.lastUpdated DESC")
    List<HealthRecord> findByDateRangeOrderByLastUpdatedDesc(@Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);

    // Find records by user and date range
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.user.id = :userId AND hr.lastUpdated BETWEEN :startDate AND :endDate ORDER BY hr.lastUpdated DESC")
    List<HealthRecord> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Find records by health worker
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.assignedHealthWorker.id = :healthWorkerId ORDER BY hr.lastUpdated DESC")
    List<HealthRecord> findByAssignedHealthWorkerId(@Param("healthWorkerId") Long healthWorkerId);

    // Find unverified records
    List<HealthRecord> findByIsVerifiedFalseOrderByLastUpdatedDesc();

    // Find verified records
    List<HealthRecord> findByIsVerifiedTrueOrderByLastUpdatedDesc();

    // Legacy method for backward compatibility (maps to findByUserId)
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.user.id = :userId ORDER BY hr.lastUpdated DESC")
    List<HealthRecord> findByUserIdOrderByRecordedAtDesc(@Param("userId") Long userId);

    // Analytics methods needed by AnalyticsServiceImpl
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}

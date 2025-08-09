package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.SideEffectReport;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.entity.ContraceptionMethod;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SideEffectReportRepository extends JpaRepository<SideEffectReport, Long> {
    
    // Find reports by user
    List<SideEffectReport> findByUserOrderByReportedAtDesc(User user);
    
    // Find reports by contraception method
    List<SideEffectReport> findByContraceptionMethodOrderByReportedAtDesc(ContraceptionMethod contraceptionMethod);
    
    // Find reports by contraception method ID
    @Query("SELECT ser FROM SideEffectReport ser WHERE ser.contraceptionMethod.id = :methodId ORDER BY ser.reportedAt DESC")
    List<SideEffectReport> findByContraceptionMethodIdOrderByReportedAtDesc(@Param("methodId") Long methodId);
    
    // Count reports by contraception method ID
    @Query("SELECT COUNT(ser) FROM SideEffectReport ser WHERE ser.contraceptionMethod.id = :methodId")
    long countByContraceptionMethodId(@Param("methodId") Long methodId);
    
    // Find reports by user and contraception method
    List<SideEffectReport> findByUserAndContraceptionMethodOrderByReportedAtDesc(User user, ContraceptionMethod contraceptionMethod);
    
    // Find reports by side effect type
    List<SideEffectReport> findBySideEffectTypeOrderByReportedAtDesc(String sideEffectType);
    
    // Find reports by severity
    List<SideEffectReport> findBySeverityOrderByReportedAtDesc(SideEffectReport.SideEffectSeverity severity);
    
    // Find ongoing side effects
    @Query("SELECT ser FROM SideEffectReport ser WHERE ser.isOngoing = true ORDER BY ser.reportedAt DESC")
    List<SideEffectReport> findOngoingSideEffects();
    
    // Find reports within date range
    @Query("SELECT ser FROM SideEffectReport ser WHERE ser.createdAt BETWEEN :startDate AND :endDate ORDER BY ser.reportedAt DESC")
    List<SideEffectReport> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find reports after a specific date
    @Query("SELECT ser FROM SideEffectReport ser WHERE ser.createdAt >= :date ORDER BY ser.reportedAt DESC")
    List<SideEffectReport> findByCreatedAtAfter(@Param("date") LocalDateTime date);
    
    // Find reports by user ID
    @Query("SELECT ser FROM SideEffectReport ser WHERE ser.user.id = :userId ORDER BY ser.reportedAt DESC")
    List<SideEffectReport> findByUserIdOrderByReportedAtDesc(@Param("userId") Long userId);
    
    // Get side effect statistics
    @Query("SELECT ser.sideEffectType, COUNT(ser) FROM SideEffectReport ser GROUP BY ser.sideEffectType ORDER BY COUNT(ser) DESC")
    List<Object[]> getSideEffectTypeStatistics();
    
    @Query("SELECT ser.severity, COUNT(ser) FROM SideEffectReport ser GROUP BY ser.severity ORDER BY COUNT(ser) DESC")
    List<Object[]> getSeverityStatistics();
    
    // Find reports by contraception type
    @Query("SELECT ser FROM SideEffectReport ser WHERE ser.contraceptionMethod.type = :contraceptionType ORDER BY ser.reportedAt DESC")
    List<SideEffectReport> findByContraceptionType(@Param("contraceptionType") String contraceptionType);
    
    // Count total reports
    @Query("SELECT COUNT(ser) FROM SideEffectReport ser")
    long countAllReports();
    
    // Count reports by user
    @Query("SELECT COUNT(ser) FROM SideEffectReport ser WHERE ser.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    // Find recent reports for dashboard
    @Query("SELECT ser FROM SideEffectReport ser WHERE ser.createdAt >= :date ORDER BY ser.reportedAt DESC LIMIT 10")
    List<SideEffectReport> findRecentReports(@Param("date") LocalDateTime date);

    // Additional methods needed by ContraceptionServiceImpl
    List<SideEffectReport> findByContraceptionMethodId(Long contraceptionMethodId);

    List<SideEffectReport> findByUserId(Long userId);

    List<SideEffectReport> findBySeverity(SideEffectReport.SideEffectSeverity severity);

    List<SideEffectReport> findByUserIdAndIsOngoingTrue(Long userId);
}

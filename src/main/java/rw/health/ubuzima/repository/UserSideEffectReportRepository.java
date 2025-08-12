package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.UserSideEffectReport;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.entity.ContraceptionMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserSideEffectReportRepository extends JpaRepository<UserSideEffectReport, Long> {
    
    // Find reports by user
    List<UserSideEffectReport> findByUserOrderByDateReportedDesc(User user);
    
    // Find reports by user ID
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.user.id = :userId ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findByUserIdOrderByDateReportedDesc(@Param("userId") Long userId);
    
    // Find reports by contraception method
    List<UserSideEffectReport> findByContraceptionMethodOrderByDateReportedDesc(ContraceptionMethod contraceptionMethod);
    
    // Find reports by contraception method ID
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.contraceptionMethod.id = :methodId ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findByContraceptionMethodIdOrderByDateReportedDesc(@Param("methodId") Long methodId);
    
    // Find reports by user and contraception method
    List<UserSideEffectReport> findByUserAndContraceptionMethodOrderByDateReportedDesc(User user, ContraceptionMethod contraceptionMethod);
    
    // Find reports by side effect name
    List<UserSideEffectReport> findBySideEffectNameOrderByDateReportedDesc(String sideEffectName);
    
    // Find reports by severity
    List<UserSideEffectReport> findBySeverityOrderByDateReportedDesc(UserSideEffectReport.SideEffectSeverity severity);
    
    // Find reports by frequency
    List<UserSideEffectReport> findByFrequencyOrderByDateReportedDesc(UserSideEffectReport.SideEffectFrequency frequency);
    
    // Find active (unresolved) reports
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.isResolved = false ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findActiveReportsOrderByDateReportedDesc();
    
    // Find resolved reports
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.isResolved = true ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findResolvedReportsOrderByDateReportedDesc();
    
    // Find active reports by user
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.user.id = :userId AND r.isResolved = false ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findActiveReportsByUserIdOrderByDateReportedDesc(@Param("userId") Long userId);
    
    // Find resolved reports by user
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.user.id = :userId AND r.isResolved = true ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findResolvedReportsByUserIdOrderByDateReportedDesc(@Param("userId") Long userId);
    
    // Count reports by user
    @Query("SELECT COUNT(r) FROM UserSideEffectReport r WHERE r.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    // Count active reports by user
    @Query("SELECT COUNT(r) FROM UserSideEffectReport r WHERE r.user.id = :userId AND r.isResolved = false")
    long countActiveReportsByUserId(@Param("userId") Long userId);
    
    // Count reports by contraception method
    @Query("SELECT COUNT(r) FROM UserSideEffectReport r WHERE r.contraceptionMethod.id = :methodId")
    long countByContraceptionMethodId(@Param("methodId") Long methodId);
    
    // Get side effect statistics
    @Query("SELECT r.sideEffectName, COUNT(r) FROM UserSideEffectReport r GROUP BY r.sideEffectName ORDER BY COUNT(r) DESC")
    List<Object[]> getSideEffectNameStatistics();
    
    @Query("SELECT r.severity, COUNT(r) FROM UserSideEffectReport r GROUP BY r.severity ORDER BY COUNT(r) DESC")
    List<Object[]> getSeverityStatistics();
    
    @Query("SELECT r.frequency, COUNT(r) FROM UserSideEffectReport r GROUP BY r.frequency ORDER BY COUNT(r) DESC")
    List<Object[]> getFrequencyStatistics();
    
    // Find reports by contraception type
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.contraceptionMethod.type = :contraceptionType ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findByContraceptionType(@Param("contraceptionType") String contraceptionType);
    
    // Find recent reports for dashboard
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.createdAt >= :date ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findRecentReports(@Param("date") LocalDateTime date);
    
    // Find reports by date range
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.dateReported BETWEEN :startDate AND :endDate ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findByDateReportedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Find reports by health worker's clients
    @Query("SELECT r FROM UserSideEffectReport r WHERE r.user.village = :village ORDER BY r.dateReported DESC")
    List<UserSideEffectReport> findByUserVillageOrderByDateReportedDesc(@Param("village") String village);
    
    // Count total reports
    @Query("SELECT COUNT(r) FROM UserSideEffectReport r")
    long countAllReports();
    
    // Count active reports
    @Query("SELECT COUNT(r) FROM UserSideEffectReport r WHERE r.isResolved = false")
    long countActiveReports();
    
    // Count resolved reports
    @Query("SELECT COUNT(r) FROM UserSideEffectReport r WHERE r.isResolved = true")
    long countResolvedReports();
}

package rw.health.ubuzima.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    /**
     * Get user registration statistics
     */
    Map<String, Object> getUserRegistrationStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get health records statistics
     */
    Map<String, Object> getHealthRecordsStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get contraception usage statistics
     */
    Map<String, Object> getContraceptionStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get appointment statistics
     */
    Map<String, Object> getAppointmentStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get education progress statistics
     */
    Map<String, Object> getEducationStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get community engagement statistics
     */
    Map<String, Object> getCommunityStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get support ticket statistics
     */
    Map<String, Object> getSupportTicketStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get notification statistics
     */
    Map<String, Object> getNotificationStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get overall platform statistics
     */
    Map<String, Object> getPlatformStats();

    /**
     * Get user activity statistics
     */
    Map<String, Object> getUserActivityStats(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Get health facility statistics
     */
    Map<String, Object> getHealthFacilityStats(Long facilityId, LocalDate startDate, LocalDate endDate);

    /**
     * Get health worker performance statistics
     */
    Map<String, Object> getHealthWorkerStats(Long workerId, LocalDate startDate, LocalDate endDate);

    /**
     * Get demographic statistics
     */
    Map<String, Object> getDemographicStats();

    /**
     * Get geographic statistics
     */
    Map<String, Object> getGeographicStats();

    /**
     * Get trend analysis
     */
    Map<String, Object> getTrendAnalysis(String metric, LocalDate startDate, LocalDate endDate);

    /**
     * Generate comprehensive report
     */
    Map<String, Object> generateComprehensiveReport(LocalDate startDate, LocalDate endDate);

    /**
     * Export analytics data
     */
    byte[] exportAnalyticsData(String format, LocalDate startDate, LocalDate endDate);
} 
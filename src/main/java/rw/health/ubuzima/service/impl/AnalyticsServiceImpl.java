package rw.health.ubuzima.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rw.health.ubuzima.repository.*;
import rw.health.ubuzima.service.AnalyticsService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final UserRepository userRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final ContraceptionMethodRepository contraceptionMethodRepository;
    private final AppointmentRepository appointmentRepository;
    private final EducationProgressRepository educationProgressRepository;
    private final CommunityEventRepository communityEventRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final NotificationRepository notificationRepository;
    private final HealthFacilityRepository healthFacilityRepository;

    @Override
    public Map<String, Object> getUserRegistrationStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalUsers = userRepository.count();
            long newUsers = userRepository.countByCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
            long activeUsers = userRepository.countByStatus(rw.health.ubuzima.enums.UserStatus.ACTIVE);
            
            stats.put("totalUsers", totalUsers);
            stats.put("newUsers", newUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", totalUsers - activeUsers);
            
            // User role distribution
            stats.put("adminCount", userRepository.countByRole(rw.health.ubuzima.enums.UserRole.ADMIN));
            stats.put("healthWorkerCount", userRepository.countByRole(rw.health.ubuzima.enums.UserRole.HEALTH_WORKER));
            stats.put("clientCount", userRepository.countByRoleAndEmailVerified(rw.health.ubuzima.enums.UserRole.CLIENT, true));
            
        } catch (Exception e) {
            log.error("Error getting user registration stats", e);
            stats.put("error", "Failed to retrieve user registration statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getHealthRecordsStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalRecords = healthRecordRepository.count();
            long newRecords = healthRecordRepository.countByCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
            
            stats.put("totalHealthRecords", totalRecords);
            stats.put("newHealthRecords", newRecords);
            stats.put("averageRecordsPerUser", totalRecords > 0 ? (double) totalRecords / userRepository.count() : 0);
            
        } catch (Exception e) {
            log.error("Error getting health records stats", e);
            stats.put("error", "Failed to retrieve health records statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getContraceptionStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalMethods = contraceptionMethodRepository.count();
            long activeMethods = contraceptionMethodRepository.countByIsActive(true);
            
            stats.put("totalContraceptionMethods", totalMethods);
            stats.put("activeContraceptionMethods", activeMethods);
            stats.put("inactiveContraceptionMethods", totalMethods - activeMethods);
            
            // Method type distribution
            for (rw.health.ubuzima.enums.ContraceptionType type : rw.health.ubuzima.enums.ContraceptionType.values()) {
                stats.put(type.name().toLowerCase() + "Count", contraceptionMethodRepository.countByType(type));
            }
            
        } catch (Exception e) {
            log.error("Error getting contraception stats", e);
            stats.put("error", "Failed to retrieve contraception statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getAppointmentStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalAppointments = appointmentRepository.count();
            long scheduledAppointments = appointmentRepository.countByStatus(rw.health.ubuzima.enums.AppointmentStatus.SCHEDULED);
            long completedAppointments = appointmentRepository.countByStatus(rw.health.ubuzima.enums.AppointmentStatus.COMPLETED);
            long cancelledAppointments = appointmentRepository.countByStatus(rw.health.ubuzima.enums.AppointmentStatus.CANCELLED);
            
            stats.put("totalAppointments", totalAppointments);
            stats.put("scheduledAppointments", scheduledAppointments);
            stats.put("completedAppointments", completedAppointments);
            stats.put("cancelledAppointments", cancelledAppointments);
            stats.put("completionRate", totalAppointments > 0 ? (double) completedAppointments / totalAppointments * 100 : 0);
            
        } catch (Exception e) {
            log.error("Error getting appointment stats", e);
            stats.put("error", "Failed to retrieve appointment statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getEducationStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalProgress = educationProgressRepository.count();
            long completedLessons = educationProgressRepository.countByIsCompleted(true);
            
            stats.put("totalEducationProgress", totalProgress);
            stats.put("completedLessons", completedLessons);
            stats.put("completionRate", totalProgress > 0 ? (double) completedLessons / totalProgress * 100 : 0);
            
        } catch (Exception e) {
            log.error("Error getting education stats", e);
            stats.put("error", "Failed to retrieve education statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getCommunityStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalEvents = communityEventRepository.count();
            long upcomingEvents = communityEventRepository.countByEventDateAfter(LocalDate.now());
            
            stats.put("totalCommunityEvents", totalEvents);
            stats.put("upcomingEvents", upcomingEvents);
            stats.put("pastEvents", totalEvents - upcomingEvents);
            
        } catch (Exception e) {
            log.error("Error getting community stats", e);
            stats.put("error", "Failed to retrieve community statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getSupportTicketStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalTickets = supportTicketRepository.count();
            long openTickets = supportTicketRepository.countByStatus(rw.health.ubuzima.enums.TicketStatus.OPEN);
            long closedTickets = supportTicketRepository.countByStatus(rw.health.ubuzima.enums.TicketStatus.CLOSED);
            
            stats.put("totalSupportTickets", totalTickets);
            stats.put("openTickets", openTickets);
            stats.put("closedTickets", closedTickets);
            stats.put("resolutionRate", totalTickets > 0 ? (double) closedTickets / totalTickets * 100 : 0);
            
        } catch (Exception e) {
            log.error("Error getting support ticket stats", e);
            stats.put("error", "Failed to retrieve support ticket statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getNotificationStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalNotifications = notificationRepository.count();
            long readNotifications = notificationRepository.countByIsRead(true);
            
            stats.put("totalNotifications", totalNotifications);
            stats.put("readNotifications", readNotifications);
            stats.put("unreadNotifications", totalNotifications - readNotifications);
            stats.put("readRate", totalNotifications > 0 ? (double) readNotifications / totalNotifications * 100 : 0);
            
        } catch (Exception e) {
            log.error("Error getting notification stats", e);
            stats.put("error", "Failed to retrieve notification statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.putAll(getUserRegistrationStats(LocalDate.now().minusMonths(1), LocalDate.now()));
            stats.putAll(getHealthRecordsStats(LocalDate.now().minusMonths(1), LocalDate.now()));
            stats.putAll(getContraceptionStats(LocalDate.now().minusMonths(1), LocalDate.now()));
            stats.putAll(getAppointmentStats(LocalDate.now().minusMonths(1), LocalDate.now()));
            stats.putAll(getEducationStats(LocalDate.now().minusMonths(1), LocalDate.now()));
            stats.putAll(getCommunityStats(LocalDate.now().minusMonths(1), LocalDate.now()));
            stats.putAll(getSupportTicketStats(LocalDate.now().minusMonths(1), LocalDate.now()));
            stats.putAll(getNotificationStats(LocalDate.now().minusMonths(1), LocalDate.now()));
            
            stats.put("totalHealthFacilities", healthFacilityRepository.count());
            stats.put("activeHealthFacilities", healthFacilityRepository.countByIsActive(true));
            
        } catch (Exception e) {
            log.error("Error getting platform stats", e);
            stats.put("error", "Failed to retrieve platform statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getUserActivityStats(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("userId", userId);
            stats.put("appointmentCount", appointmentRepository.countByUserIdAndScheduledDateBetween(userId, startDate, endDate));
            stats.put("healthRecordCount", healthRecordRepository.countByUserIdAndCreatedAtBetween(userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
            stats.put("educationProgressCount", educationProgressRepository.countByUserIdAndUpdatedAtBetween(userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
            
        } catch (Exception e) {
            log.error("Error getting user activity stats for user: {}", userId, e);
            stats.put("error", "Failed to retrieve user activity statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getHealthFacilityStats(Long facilityId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("facilityId", facilityId);
            stats.put("appointmentCount", appointmentRepository.countByHealthFacilityIdAndScheduledDateBetween(facilityId, startDate, endDate));
            stats.put("healthWorkerCount", userRepository.countByFacilityIdAndRole(facilityId.toString(), rw.health.ubuzima.enums.UserRole.HEALTH_WORKER));
            
        } catch (Exception e) {
            log.error("Error getting health facility stats for facility: {}", facilityId, e);
            stats.put("error", "Failed to retrieve health facility statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getHealthWorkerStats(Long workerId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("workerId", workerId);
            stats.put("appointmentCount", appointmentRepository.countByHealthWorkerIdAndScheduledDateBetween(workerId, startDate, endDate));
            // Count only verified clients for this health worker (simplified approach)
            stats.put("clientCount", userRepository.countByRoleAndEmailVerified(rw.health.ubuzima.enums.UserRole.CLIENT, true));
            
        } catch (Exception e) {
            log.error("Error getting health worker stats for worker: {}", workerId, e);
            stats.put("error", "Failed to retrieve health worker statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getDemographicStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Gender distribution
            stats.put("maleCount", userRepository.countByGender(rw.health.ubuzima.enums.Gender.MALE));
            stats.put("femaleCount", userRepository.countByGender(rw.health.ubuzima.enums.Gender.FEMALE));
            
            // Age group distribution (simplified)
            stats.put("totalUsers", userRepository.count());
            
        } catch (Exception e) {
            log.error("Error getting demographic stats", e);
            stats.put("error", "Failed to retrieve demographic statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getGeographicStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // District distribution
            stats.put("kigaliUsers", userRepository.countByDistrict("Kigali"));
            stats.put("totalUsers", userRepository.count());
            
        } catch (Exception e) {
            log.error("Error getting geographic stats", e);
            stats.put("error", "Failed to retrieve geographic statistics");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getTrendAnalysis(String metric, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("metric", metric);
            stats.put("startDate", startDate);
            stats.put("endDate", endDate);
            stats.put("message", "Trend analysis implementation pending");
            
        } catch (Exception e) {
            log.error("Error getting trend analysis for metric: {}", metric, e);
            stats.put("error", "Failed to retrieve trend analysis");
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> generateComprehensiveReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        try {
            report.put("reportPeriod", Map.of("startDate", startDate, "endDate", endDate));
            report.put("userStats", getUserRegistrationStats(startDate, endDate));
            report.put("healthStats", getHealthRecordsStats(startDate, endDate));
            report.put("contraceptionStats", getContraceptionStats(startDate, endDate));
            report.put("appointmentStats", getAppointmentStats(startDate, endDate));
            report.put("educationStats", getEducationStats(startDate, endDate));
            report.put("communityStats", getCommunityStats(startDate, endDate));
            report.put("supportStats", getSupportTicketStats(startDate, endDate));
            report.put("notificationStats", getNotificationStats(startDate, endDate));
            report.put("demographicStats", getDemographicStats());
            report.put("geographicStats", getGeographicStats());
            
        } catch (Exception e) {
            log.error("Error generating comprehensive report", e);
            report.put("error", "Failed to generate comprehensive report");
        }
        
        return report;
    }

    @Override
    public byte[] exportAnalyticsData(String format, LocalDate startDate, LocalDate endDate) {
        try {
            Map<String, Object> data = generateComprehensiveReport(startDate, endDate);
            
            if ("json".equalsIgnoreCase(format)) {
                return data.toString().getBytes();
            } else if ("csv".equalsIgnoreCase(format)) {
                // Simple CSV export implementation
                StringBuilder csv = new StringBuilder();
                csv.append("Metric,Value\n");
                data.forEach((key, value) -> csv.append(key).append(",").append(value).append("\n"));
                return csv.toString().getBytes();
            }
            
            return "Unsupported format".getBytes();
            
        } catch (Exception e) {
            log.error("Error exporting analytics data", e);
            return "Export failed".getBytes();
        }
    }
}

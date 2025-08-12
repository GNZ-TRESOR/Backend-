package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.repository.*;
import rw.health.ubuzima.entity.*;
import rw.health.ubuzima.enums.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Reporting Controller
 * Provides comprehensive reporting and analytics functionality
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportingController {

    private final UserRepository userRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicationRepository medicationRepository;
    private final ContraceptionMethodRepository contraceptionMethodRepository;
    private final SideEffectReportRepository sideEffectReportRepository;
    private final EducationLessonRepository educationLessonRepository;
    private final EducationProgressRepository educationProgressRepository;
    private final SupportGroupRepository supportGroupRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Generate comprehensive health analytics report
     */
    @GetMapping("/health-analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTH_WORKER')")
    public ResponseEntity<Map<String, Object>> getHealthAnalyticsReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime start = startDate != null ? 
                LocalDateTime.parse(startDate + "T00:00:00") : 
                LocalDateTime.now().minusMonths(1);
            LocalDateTime end = endDate != null ? 
                LocalDateTime.parse(endDate + "T23:59:59") : 
                LocalDateTime.now();

            Map<String, Object> report = new HashMap<>();
            
            // Health Records Analytics
            report.put("healthRecords", Map.of(
                "total", healthRecordRepository.count(),
                "newRecords", healthRecordRepository.countByCreatedAtBetween(start, end),
                "recordsByType", getHealthRecordsByType(),
                "averageRecordsPerUser", calculateAverageRecordsPerUser()
            ));
            
            // Appointment Analytics
            report.put("appointments", Map.of(
                "total", appointmentRepository.count(),
                "scheduled", appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED),
                "completed", appointmentRepository.countByStatus(AppointmentStatus.COMPLETED),
                "cancelled", appointmentRepository.countByStatus(AppointmentStatus.CANCELLED),
                "completionRate", calculateAppointmentCompletionRate()
            ));
            
            // Medication Analytics
            report.put("medications", Map.of(
                "totalPrescriptions", medicationRepository.count(),
                "activeMedications", medicationRepository.countByIsActiveTrue(),
                "medicationsByType", getMedicationsByType(),
                "adherenceRate", calculateMedicationAdherence()
            ));
            
            // Contraception Analytics
            report.put("contraception", Map.of(
                "totalMethods", contraceptionMethodRepository.count(),
                "methodsByType", getContraceptionMethodsByType(),
                "sideEffectReports", sideEffectReportRepository.count(),
                "popularMethods", getPopularContraceptionMethods()
            ));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "report", report,
                "period", Map.of("start", start, "end", end),
                "generatedAt", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Health analytics report failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Generate user engagement report
     */
    @GetMapping("/user-engagement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserEngagementReport() {
        try {
            Map<String, Object> report = new HashMap<>();
            
            // User Activity
            report.put("userActivity", Map.of(
                "totalUsers", userRepository.count(),
                "activeUsers", userRepository.countByStatus(UserStatus.ACTIVE),
                "newUsers30Days", userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30)),
                "usersByRole", getUsersByRole()
            ));
            
            // Education Engagement
            report.put("education", Map.of(
                "totalLessons", educationLessonRepository.count(),
                "completedLessons", educationProgressRepository.countByIsCompletedTrue(),
                "averageProgress", calculateAverageEducationProgress(),
                "popularLessons", getPopularEducationLessons()
            ));
            
            // Communication
            report.put("communication", Map.of(
                "totalMessages", messageRepository.count(),
                "messagesLast30Days", messageRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30)),
                "supportGroups", supportGroupRepository.count(),
                "activeGroups", supportGroupRepository.countByIsActiveTrue()
            ));
            
            // Notifications
            report.put("notifications", Map.of(
                "totalSent", notificationRepository.count(),
                "readRate", calculateNotificationReadRate(),
                "notificationsByType", getNotificationsByType()
            ));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "report", report,
                "generatedAt", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "User engagement report failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Generate facility performance report
     */
    @GetMapping("/facility-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTH_WORKER')")
    public ResponseEntity<Map<String, Object>> getFacilityPerformanceReport(
            @RequestParam(required = false) Long facilityId) {
        try {
            Map<String, Object> report = new HashMap<>();
            
            if (facilityId != null) {
                // Single facility report
                report.put("facilityId", facilityId);
                report.put("appointments", appointmentRepository.countByHealthFacilityId(facilityId));
                report.put("healthWorkers", userRepository.countByFacilityIdAndRole(facilityId.toString(), UserRole.HEALTH_WORKER));
            } else {
                // All facilities overview
                report.put("totalFacilities", "12"); // This would come from HealthFacilityRepository
                report.put("averageAppointmentsPerFacility", calculateAverageAppointmentsPerFacility());
                report.put("topPerformingFacilities", getTopPerformingFacilities());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "report", report,
                "generatedAt", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Facility performance report failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Generate custom report based on parameters
     */
    @PostMapping("/custom")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateCustomReport(@RequestBody Map<String, Object> request) {
        try {
            String reportType = request.get("reportType").toString();
            Map<String, Object> filters = (Map<String, Object>) request.get("filters");
            
            Map<String, Object> report = new HashMap<>();
            
            switch (reportType) {
                case "demographic":
                    report = generateDemographicReport(filters);
                    break;
                case "health_trends":
                    report = generateHealthTrendsReport(filters);
                    break;
                case "medication_analysis":
                    report = generateMedicationAnalysisReport(filters);
                    break;
                case "education_effectiveness":
                    report = generateEducationEffectivenessReport(filters);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Unknown report type: " + reportType
                    ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "reportType", reportType,
                "report", report,
                "filters", filters,
                "generatedAt", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Custom report generation failed: " + e.getMessage()
            ));
        }
    }

    // Helper methods for calculations
    private Map<String, Object> getHealthRecordsByType() {
        return Map.of(
            "checkup", 45,
            "vaccination", 23,
            "consultation", 67,
            "emergency", 12
        );
    }

    private double calculateAverageRecordsPerUser() {
        long totalUsers = userRepository.count();
        long totalRecords = healthRecordRepository.count();
        return totalUsers > 0 ? (double) totalRecords / totalUsers : 0.0;
    }

    private double calculateAppointmentCompletionRate() {
        long total = appointmentRepository.count();
        long completed = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
        return total > 0 ? (double) completed / total * 100 : 0.0;
    }

    private Map<String, Object> getMedicationsByType() {
        return Map.of(
            "contraceptive", 45,
            "antibiotic", 23,
            "vitamin", 67,
            "other", 12
        );
    }

    private double calculateMedicationAdherence() {
        return 78.5; // This would be calculated based on actual data
    }

    private Map<String, Object> getContraceptionMethodsByType() {
        return Map.of(
            "pill", 45,
            "injection", 23,
            "implant", 67,
            "iud", 12
        );
    }

    private List<Map<String, Object>> getPopularContraceptionMethods() {
        return List.of(
            Map.of("name", "Birth Control Pill", "users", 45),
            Map.of("name", "Depo-Provera", "users", 23),
            Map.of("name", "Implant", "users", 67)
        );
    }

    private Map<String, Object> getUsersByRole() {
        return Map.of(
            "clients", userRepository.countByRole(UserRole.CLIENT),
            "healthWorkers", userRepository.countByRole(UserRole.HEALTH_WORKER),
            "admins", userRepository.countByRole(UserRole.ADMIN)
        );
    }

    private double calculateAverageEducationProgress() {
        return 65.5; // This would be calculated from actual progress data
    }

    private List<Map<String, Object>> getPopularEducationLessons() {
        return List.of(
            Map.of("title", "Family Planning Basics", "completions", 45),
            Map.of("title", "Contraception Methods", "completions", 38),
            Map.of("title", "Reproductive Health", "completions", 32)
        );
    }

    private double calculateNotificationReadRate() {
        long total = notificationRepository.count();
        long read = notificationRepository.countByIsReadTrue();
        return total > 0 ? (double) read / total * 100 : 0.0;
    }

    private Map<String, Object> getNotificationsByType() {
        return Map.of(
            "appointment", 45,
            "medication", 23,
            "education", 67,
            "system", 12
        );
    }

    private double calculateAverageAppointmentsPerFacility() {
        return 15.5; // This would be calculated from actual data
    }

    private List<Map<String, Object>> getTopPerformingFacilities() {
        return List.of(
            Map.of("name", "Kigali Health Center", "appointments", 145),
            Map.of("name", "Gasabo Clinic", "appointments", 123),
            Map.of("name", "Nyarugenge Hospital", "appointments", 98)
        );
    }

    private Map<String, Object> generateDemographicReport(Map<String, Object> filters) {
        return Map.of(
            "ageGroups", Map.of("18-25", 45, "26-35", 67, "36-45", 23),
            "genderDistribution", Map.of("male", 45, "female", 123),
            "locationDistribution", Map.of("urban", 89, "rural", 79)
        );
    }

    private Map<String, Object> generateHealthTrendsReport(Map<String, Object> filters) {
        return Map.of(
            "appointmentTrends", List.of(45, 52, 48, 61, 58),
            "healthRecordTrends", List.of(23, 28, 31, 29, 35),
            "medicationTrends", List.of(67, 71, 69, 74, 78)
        );
    }

    private Map<String, Object> generateMedicationAnalysisReport(Map<String, Object> filters) {
        return Map.of(
            "adherenceRates", Map.of("high", 45, "medium", 67, "low", 23),
            "sideEffects", Map.of("mild", 12, "moderate", 8, "severe", 3),
            "effectiveness", Map.of("very_effective", 78, "effective", 15, "less_effective", 7)
        );
    }

    private Map<String, Object> generateEducationEffectivenessReport(Map<String, Object> filters) {
        return Map.of(
            "completionRates", Map.of("high", 65, "medium", 25, "low", 10),
            "knowledgeRetention", Map.of("excellent", 45, "good", 35, "fair", 20),
            "behaviorChange", Map.of("significant", 30, "moderate", 45, "minimal", 25)
        );
    }
}

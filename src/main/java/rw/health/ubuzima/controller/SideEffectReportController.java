package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.repository.UserSideEffectReportRepository;
import rw.health.ubuzima.repository.ContraceptionMethodRepository;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.entity.UserSideEffectReport;
import rw.health.ubuzima.entity.ContraceptionMethod;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/side-effect-reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SideEffectReportController {

    private final UserRepository userRepository;
    private final UserSideEffectReportRepository sideEffectReportRepository;
    private final ContraceptionMethodRepository contraceptionMethodRepository;

    // Create side effect report
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSideEffectReport(
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long contraceptionMethodId = Long.valueOf(request.get("contraceptionMethodId").toString());

            User user = userRepository.findById(userId).orElse(null);
            ContraceptionMethod contraceptionMethod = contraceptionMethodRepository.findById(contraceptionMethodId).orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            if (contraceptionMethod == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Contraception method not found"
                ));
            }

            UserSideEffectReport report = UserSideEffectReport.builder()
                .user(user)
                .contraceptionMethod(contraceptionMethod)
                .sideEffectName(request.get("sideEffectName").toString())
                .severity(UserSideEffectReport.SideEffectSeverity.valueOf(request.get("severity").toString().toUpperCase()))
                .frequency(UserSideEffectReport.SideEffectFrequency.valueOf(request.get("frequency").toString().toUpperCase()))
                .description(request.get("description") != null ? request.get("description").toString() : null)
                .dateReported(LocalDate.now())
                .isResolved(false)
                .build();

            UserSideEffectReport savedReport = sideEffectReportRepository.save(report);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Side effect report created successfully",
                "report", convertToMap(savedReport)
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create side effect report: " + e.getMessage()
            ));
        }
    }

    // Get all side effect reports
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSideEffectReports() {
        try {
            List<UserSideEffectReport> reports = sideEffectReportRepository.findAll();
            List<Map<String, Object>> reportMaps = reports.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "reports", reportMaps
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch side effect reports: " + e.getMessage()
            ));
        }
    }

    // Get side effect reports by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getSideEffectReportsByUser(@PathVariable Long userId) {
        try {
            List<UserSideEffectReport> reports = sideEffectReportRepository.findByUserIdOrderByDateReportedDesc(userId);
            List<Map<String, Object>> reportMaps = reports.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "reports", reportMaps
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch user side effect reports: " + e.getMessage()
            ));
        }
    }

    // Update side effect report
    @PutMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> updateSideEffectReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> request) {
        try {
            UserSideEffectReport report = sideEffectReportRepository.findById(reportId).orElse(null);

            if (report == null) {
                return ResponseEntity.notFound().build();
            }

            // Update fields if provided
            if (request.containsKey("sideEffectName")) {
                report.setSideEffectName(request.get("sideEffectName").toString());
            }
            if (request.containsKey("severity")) {
                report.setSeverity(UserSideEffectReport.SideEffectSeverity.valueOf(request.get("severity").toString().toUpperCase()));
            }
            if (request.containsKey("frequency")) {
                report.setFrequency(UserSideEffectReport.SideEffectFrequency.valueOf(request.get("frequency").toString().toUpperCase()));
            }
            if (request.containsKey("description")) {
                report.setDescription(request.get("description").toString());
            }
            if (request.containsKey("isResolved")) {
                report.setIsResolved((Boolean) request.get("isResolved"));
            }
            if (request.containsKey("resolutionNotes")) {
                report.setResolutionNotes(request.get("resolutionNotes").toString());
            }

            UserSideEffectReport updatedReport = sideEffectReportRepository.save(report);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Side effect report updated successfully",
                "report", convertToMap(updatedReport)
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update side effect report: " + e.getMessage()
            ));
        }
    }

    // Delete side effect report
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> deleteSideEffectReport(@PathVariable Long reportId) {
        try {
            if (!sideEffectReportRepository.existsById(reportId)) {
                return ResponseEntity.notFound().build();
            }

            sideEffectReportRepository.deleteById(reportId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Side effect report deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete side effect report: " + e.getMessage()
            ));
        }
    }

    // Helper method to convert entity to map
    private Map<String, Object> convertToMap(UserSideEffectReport report) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", report.getId());
        map.put("userId", report.getUser().getId());
        map.put("userName", report.getUser().getName());
        map.put("contraceptionMethodId", report.getContraceptionMethod().getId());
        map.put("contraceptionMethodName", report.getContraceptionMethod().getName());
        map.put("sideEffectName", report.getSideEffectName());
        map.put("severity", report.getSeverity().toString());
        map.put("frequency", report.getFrequency().toString());
        map.put("description", report.getDescription());
        map.put("dateReported", report.getDateReported().toString());
        map.put("isResolved", report.getIsResolved());
        map.put("resolutionNotes", report.getResolutionNotes());
        map.put("createdAt", report.getCreatedAt());
        map.put("updatedAt", report.getUpdatedAt());
        return map;
    }
}

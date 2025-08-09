package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.entity.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/side-effect-reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SideEffectReportController {

    private final UserRepository userRepository;

    // Create side effect report
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSideEffectReport(
            @RequestBody Map<String, Object> request) {
        try {
            // For now, return a success response
            // In a real implementation, you would save to a side effect reports table
            
            Long clientId = Long.valueOf(request.get("clientId").toString());
            User client = userRepository.findById(clientId).orElse(null);
            
            if (client == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            // Mock side effect report creation
            Map<String, Object> report = new HashMap<>();
            report.put("id", System.currentTimeMillis());
            report.put("clientId", clientId);
            report.put("clientName", client.getName());
            report.put("contraceptiveMethod", request.get("contraceptiveMethod"));
            report.put("symptoms", request.get("symptoms"));
            report.put("severity", request.get("severity"));
            report.put("category", request.get("category"));
            report.put("reportedDate", LocalDate.now().toString());
            report.put("status", "Under Review");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Side effect report created successfully",
                "report", report
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
            // Return mock data for now
            List<Map<String, Object>> mockReports = List.of(
                Map.of(
                    "id", 1,
                    "clientName", "Jane Doe",
                    "contraceptiveMethod", "Birth Control Pills",
                    "symptoms", "Nausea, headache",
                    "severity", "Mild",
                    "category", "Gastrointestinal",
                    "reportedDate", LocalDate.now().toString(),
                    "status", "Under Review"
                ),
                Map.of(
                    "id", 2,
                    "clientName", "Mary Smith",
                    "contraceptiveMethod", "IUD",
                    "symptoms", "Irregular bleeding",
                    "severity", "Moderate",
                    "category", "Menstrual",
                    "reportedDate", LocalDate.now().minusDays(2).toString(),
                    "status", "Resolved"
                )
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "reports", mockReports
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch side effect reports: " + e.getMessage()
            ));
        }
    }

    // Update side effect report
    @PutMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> updateSideEffectReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> request) {
        try {
            // Mock update for now
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Side effect report updated successfully"
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
            // Mock delete for now
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
}

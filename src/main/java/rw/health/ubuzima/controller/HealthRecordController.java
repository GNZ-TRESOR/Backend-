package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.HealthRecord;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.repository.HealthRecordRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/health-records")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HealthRecordController {

    private final HealthRecordRepository healthRecordRepository;
    private final UserRepository userRepository;

    /**
     * Get all health records with pagination
     * GET /api/v1/health-records?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllHealthRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String healthStatus,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) String search) {
        
        try {
            log.info("Fetching health records - page: {}, size: {}, healthStatus: {}, verified: {}, search: {}", 
                    page, size, healthStatus, verified, search);

            Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdated").descending());
            List<HealthRecord> healthRecords;

            // Apply filters
            if (search != null && !search.trim().isEmpty()) {
                healthRecords = healthRecordRepository.searchRecords(search.trim());
            } else if (healthStatus != null && !healthStatus.trim().isEmpty()) {
                healthRecords = healthRecordRepository.findByHealthStatus(healthStatus);
            } else if (verified != null) {
                if (verified) {
                    healthRecords = healthRecordRepository.findByIsVerifiedTrueOrderByLastUpdatedDesc();
                } else {
                    healthRecords = healthRecordRepository.findByIsVerifiedFalseOrderByLastUpdatedDesc();
                }
            } else {
                healthRecords = healthRecordRepository.findAllByOrderByLastUpdatedDesc();
            }

            // Manual pagination for filtered results
            int start = page * size;
            int end = Math.min(start + size, healthRecords.size());
            List<HealthRecord> paginatedRecords = healthRecords.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", paginatedRecords);
            response.put("totalElements", healthRecords.size());
            response.put("totalPages", (int) Math.ceil((double) healthRecords.size() / size));
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("message", "Health records retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching health records", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch health records: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Get health record by ID
     * GET /api/v1/health-records/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getHealthRecordById(@PathVariable Long id) {
        try {
            HealthRecord healthRecord = healthRecordRepository.findById(id).orElse(null);
            
            if (healthRecord == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", healthRecord,
                "message", "Health record retrieved successfully"
            ));

        } catch (Exception e) {
            log.error("Error fetching health record by ID: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch health record: " + e.getMessage()
            ));
        }
    }

    /**
     * Get health records by user ID
     * GET /api/v1/health-records/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getHealthRecordsByUserId(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<HealthRecord> healthRecords = healthRecordRepository.findByUserIdOrderByRecordedAtDesc(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", healthRecords,
                "user", Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail()
                ),
                "message", "Health records retrieved successfully"
            ));

        } catch (Exception e) {
            log.error("Error fetching health records for user: {}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch health records: " + e.getMessage()
            ));
        }
    }

    /**
     * Create health record
     * POST /api/v1/health-records
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createHealthRecord(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Get existing health record or create new one
            HealthRecord healthRecord = healthRecordRepository.findByUserId(userId).orElse(new HealthRecord());
            
            if (healthRecord.getUser() == null) {
                healthRecord.setUser(user);
            }

            // Update fields from request
            if (request.containsKey("heartRateValue")) {
                healthRecord.setHeartRateValue(Integer.valueOf(request.get("heartRateValue").toString()));
            }
            if (request.containsKey("bpValue")) {
                healthRecord.setBpValue(request.get("bpValue").toString());
            }
            if (request.containsKey("kgValue")) {
                healthRecord.setKgValue(new java.math.BigDecimal(request.get("kgValue").toString()));
            }
            if (request.containsKey("tempValue")) {
                healthRecord.setTempValue(new java.math.BigDecimal(request.get("tempValue").toString()));
            }
            if (request.containsKey("heightValue")) {
                healthRecord.setHeightValue(new java.math.BigDecimal(request.get("heightValue").toString()));
            }
            if (request.containsKey("notes")) {
                healthRecord.setNotes(request.get("notes").toString());
            }
            if (request.containsKey("healthStatus")) {
                healthRecord.setHealthStatus(request.get("healthStatus").toString());
            }
            if (request.containsKey("recordedBy")) {
                healthRecord.setRecordedBy(request.get("recordedBy").toString());
            }
            if (request.containsKey("isVerified")) {
                healthRecord.setIsVerified(Boolean.valueOf(request.get("isVerified").toString()));
            }
            if (request.containsKey("assignedHealthWorkerId")) {
                Long healthWorkerId = request.get("assignedHealthWorkerId") != null ?
                    Long.valueOf(request.get("assignedHealthWorkerId").toString()) : null;
                if (healthWorkerId != null) {
                    User healthWorker = userRepository.findById(healthWorkerId).orElse(null);
                    healthRecord.setAssignedHealthWorker(healthWorker);
                }
            }

            healthRecord.setLastUpdated(LocalDateTime.now());
            HealthRecord savedRecord = healthRecordRepository.save(healthRecord);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", savedRecord,
                "message", "Health record saved successfully"
            ));

        } catch (Exception e) {
            log.error("Error creating/updating health record", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to save health record: " + e.getMessage()
            ));
        }
    }

    /**
     * Update health record
     * PUT /api/v1/health-records/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateHealthRecord(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            // Find existing health record
            HealthRecord existingRecord = healthRecordRepository.findById(id).orElse(null);
            if (existingRecord == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Health record not found"));
            }

            // Update fields from request
            if (request.containsKey("heartRateValue")) {
                existingRecord.setHeartRateValue(request.get("heartRateValue") != null ?
                    Integer.valueOf(request.get("heartRateValue").toString()) : null);
            }
            if (request.containsKey("heartRateUnit")) {
                existingRecord.setHeartRateUnit((String) request.get("heartRateUnit"));
            }
            if (request.containsKey("bpValue")) {
                existingRecord.setBpValue((String) request.get("bpValue"));
            }
            if (request.containsKey("bpUnit")) {
                existingRecord.setBpUnit((String) request.get("bpUnit"));
            }
            if (request.containsKey("kgValue")) {
                existingRecord.setKgValue(request.get("kgValue") != null ?
                    new BigDecimal(request.get("kgValue").toString()) : null);
            }
            if (request.containsKey("kgUnit")) {
                existingRecord.setKgUnit((String) request.get("kgUnit"));
            }
            if (request.containsKey("tempValue")) {
                existingRecord.setTempValue(request.get("tempValue") != null ?
                    new BigDecimal(request.get("tempValue").toString()) : null);
            }
            if (request.containsKey("tempUnit")) {
                existingRecord.setTempUnit((String) request.get("tempUnit"));
            }
            if (request.containsKey("heightValue")) {
                existingRecord.setHeightValue(request.get("heightValue") != null ?
                    new BigDecimal(request.get("heightValue").toString()) : null);
            }
            if (request.containsKey("heightUnit")) {
                existingRecord.setHeightUnit((String) request.get("heightUnit"));
            }
            if (request.containsKey("bmi")) {
                existingRecord.setBmi(request.get("bmi") != null ?
                    new BigDecimal(request.get("bmi").toString()) : null);
            }
            if (request.containsKey("healthStatus")) {
                existingRecord.setHealthStatus((String) request.get("healthStatus"));
            }
            if (request.containsKey("notes")) {
                existingRecord.setNotes((String) request.get("notes"));
            }
            if (request.containsKey("recordedBy")) {
                existingRecord.setRecordedBy((String) request.get("recordedBy"));
            }
            if (request.containsKey("isVerified")) {
                existingRecord.setIsVerified(Boolean.valueOf(request.get("isVerified").toString()));
            }
            if (request.containsKey("assignedHealthWorkerId")) {
                Long healthWorkerId = request.get("assignedHealthWorkerId") != null ?
                    Long.valueOf(request.get("assignedHealthWorkerId").toString()) : null;
                if (healthWorkerId != null) {
                    User healthWorker = userRepository.findById(healthWorkerId).orElse(null);
                    existingRecord.setAssignedHealthWorker(healthWorker);
                } else {
                    existingRecord.setAssignedHealthWorker(null);
                }
            }

            existingRecord.setLastUpdated(LocalDateTime.now());
            existingRecord.setUpdatedAt(LocalDateTime.now());

            HealthRecord savedRecord = healthRecordRepository.save(existingRecord);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Health record updated successfully",
                    "data", savedRecord
            ));

        } catch (Exception e) {
            log.error("Error updating health record", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update health record: " + e.getMessage()
            ));
        }
    }

    /**
     * Get available health workers
     * GET /api/v1/health-records/health-workers
     */
    @GetMapping("/health-workers")
    public ResponseEntity<Map<String, Object>> getAvailableHealthWorkers() {
        try {
            List<User> healthWorkers = userRepository.findByRole(UserRole.HEALTH_WORKER);

            List<Map<String, Object>> healthWorkerList = healthWorkers.stream()
                .map(worker -> {
                    Map<String, Object> workerMap = new HashMap<>();
                    workerMap.put("id", worker.getId());
                    workerMap.put("name", worker.getName());
                    workerMap.put("email", worker.getEmail());
                    workerMap.put("phone", worker.getPhone() != null ? worker.getPhone() : "");
                    return workerMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", healthWorkerList,
                "message", "Health workers retrieved successfully"
            ));

        } catch (Exception e) {
            log.error("Error retrieving health workers", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to retrieve health workers: " + e.getMessage()
            ));
        }
    }

    /**
     * Delete health record
     * DELETE /api/v1/health-records/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteHealthRecord(@PathVariable Long id) {
        try {
            HealthRecord healthRecord = healthRecordRepository.findById(id).orElse(null);
            
            if (healthRecord == null) {
                return ResponseEntity.notFound().build();
            }

            healthRecordRepository.delete(healthRecord);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Health record deleted successfully"
            ));

        } catch (Exception e) {
            log.error("Error deleting health record: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete health record: " + e.getMessage()
            ));
        }
    }

    /**
     * Get health records statistics
     * GET /api/v1/health-records/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getHealthRecordsStats() {
        try {
            long totalRecords = healthRecordRepository.count();
            long verifiedRecords = healthRecordRepository.findByIsVerifiedTrueOrderByLastUpdatedDesc().size();
            long unverifiedRecords = healthRecordRepository.findByIsVerifiedFalseOrderByLastUpdatedDesc().size();
            long completeVitalsRecords = healthRecordRepository.findRecordsWithCompleteVitals().size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRecords", totalRecords);
            stats.put("verifiedRecords", verifiedRecords);
            stats.put("unverifiedRecords", unverifiedRecords);
            stats.put("completeVitalsRecords", completeVitalsRecords);
            stats.put("verificationRate", totalRecords > 0 ? (double) verifiedRecords / totalRecords * 100 : 0);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats,
                "message", "Health records statistics retrieved successfully"
            ));

        } catch (Exception e) {
            log.error("Error fetching health records statistics", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch statistics: " + e.getMessage()
            ));
        }
    }
}

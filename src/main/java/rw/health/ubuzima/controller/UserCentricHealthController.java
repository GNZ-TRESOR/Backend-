package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.HealthRecord;
import rw.health.ubuzima.service.UserCentricHealthService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user-centric-health")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserCentricHealthController {

    private final UserCentricHealthService userCentricHealthService;

    /**
     * Get user's health record (user-centric format)
     * Example: GET /api/user-centric-health/record/5
     * Returns: { user_id: 5, heart_rate_value: 80, bp_value: "120/80", kg_value: 64, temp_value: 36.0 }
     */
    @GetMapping("/record/{userId}")
    public ResponseEntity<Map<String, Object>> getUserHealthRecord(@PathVariable Long userId) {
        try {
            HealthRecord healthRecord = userCentricHealthService.getUserHealthRecord(userId);
            
            if (healthRecord == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Create a safe DTO to avoid serialization issues
            Map<String, Object> healthRecordDto = new HashMap<>();
            healthRecordDto.put("id", healthRecord.getId());
            healthRecordDto.put("userId", healthRecord.getUser().getId());
            healthRecordDto.put("heartRateValue", healthRecord.getHeartRateValue() != null ? healthRecord.getHeartRateValue() : 0);
            healthRecordDto.put("heartRateUnit", healthRecord.getHeartRateUnit() != null ? healthRecord.getHeartRateUnit() : "bpm");
            healthRecordDto.put("bpValue", healthRecord.getBpValue() != null ? healthRecord.getBpValue() : "");
            healthRecordDto.put("bpUnit", healthRecord.getBpUnit() != null ? healthRecord.getBpUnit() : "mmHg");
            healthRecordDto.put("kgValue", healthRecord.getKgValue() != null ? healthRecord.getKgValue() : 0);
            healthRecordDto.put("kgUnit", healthRecord.getKgUnit() != null ? healthRecord.getKgUnit() : "kg");
            healthRecordDto.put("heightValue", healthRecord.getHeightValue() != null ? healthRecord.getHeightValue() : 0);
            healthRecordDto.put("heightUnit", healthRecord.getHeightUnit() != null ? healthRecord.getHeightUnit() : "cm");
            healthRecordDto.put("tempValue", healthRecord.getTempValue() != null ? healthRecord.getTempValue() : 0);
            healthRecordDto.put("tempUnit", healthRecord.getTempUnit() != null ? healthRecord.getTempUnit() : "°C");
            healthRecordDto.put("bmi", healthRecord.getBmi() != null ? healthRecord.getBmi() : 0);
            healthRecordDto.put("healthStatus", healthRecord.getHealthStatus() != null ? healthRecord.getHealthStatus() : "normal");
            healthRecordDto.put("notes", healthRecord.getNotes() != null ? healthRecord.getNotes() : "");
            healthRecordDto.put("lastUpdated", healthRecord.getLastUpdated() != null ? healthRecord.getLastUpdated().toString() : "");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", healthRecordDto
            ));
        } catch (Exception e) {
            log.error("Error getting health record for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get health record: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Update heart rate for a user
     * Example: POST /api/user-centric-health/heart-rate
     * Body: { "userId": 5, "heartRateValue": 80, "unit": "bpm" }
     */
    @PostMapping("/heart-rate")
    public ResponseEntity<Map<String, Object>> updateHeartRate(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer heartRateValue = Integer.valueOf(request.get("heartRateValue").toString());
            String unit = request.get("unit") != null ? request.get("unit").toString() : "bpm";

            HealthRecord updatedRecord = userCentricHealthService.updateHeartRate(userId, heartRateValue, unit);

            // Create a safe DTO to avoid serialization issues
            Map<String, Object> healthRecordDto = new HashMap<>();
            healthRecordDto.put("id", updatedRecord.getId());
            healthRecordDto.put("userId", updatedRecord.getUser().getId());
            healthRecordDto.put("heartRateValue", updatedRecord.getHeartRateValue() != null ? updatedRecord.getHeartRateValue() : 0);
            healthRecordDto.put("heartRateUnit", updatedRecord.getHeartRateUnit() != null ? updatedRecord.getHeartRateUnit() : "bpm");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Heart rate updated successfully",
                "data", healthRecordDto
            ));
        } catch (Exception e) {
            log.error("Error updating heart rate: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update heart rate: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Update blood pressure for a user
     * Example: POST /api/user-centric-health/blood-pressure
     * Body: { "userId": 5, "systolic": 120, "diastolic": 80, "unit": "mmHg" }
     * OR: { "userId": 5, "bpValue": "120/80", "unit": "mmHg" }
     */
    @PostMapping("/blood-pressure")
    public ResponseEntity<Map<String, Object>> updateBloodPressure(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String unit = request.get("unit") != null ? request.get("unit").toString() : "mmHg";

            HealthRecord updatedRecord;
            
            if (request.containsKey("systolic") && request.containsKey("diastolic")) {
                int systolic = Integer.parseInt(request.get("systolic").toString());
                int diastolic = Integer.parseInt(request.get("diastolic").toString());
                updatedRecord = userCentricHealthService.updateBloodPressure(userId, systolic, diastolic, unit);
            } else if (request.containsKey("bpValue")) {
                String bpValue = request.get("bpValue").toString();
                updatedRecord = userCentricHealthService.updateBloodPressure(userId, bpValue, unit);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Either provide systolic/diastolic or bpValue"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Blood pressure updated successfully",
                "data", updatedRecord
            ));
        } catch (Exception e) {
            log.error("Error updating blood pressure: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update blood pressure: " + e.getMessage()
            ));
        }
    }

    /**
     * Update weight for a user
     * Example: POST /api/user-centric-health/weight
     * Body: { "userId": 5, "kgValue": 64.5, "unit": "kg" }
     */
    @PostMapping("/weight")
    public ResponseEntity<Map<String, Object>> updateWeight(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal kgValue = new BigDecimal(request.get("kgValue").toString());
            String unit = request.get("unit") != null ? request.get("unit").toString() : "kg";

            HealthRecord updatedRecord = userCentricHealthService.updateWeight(userId, kgValue, unit);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Weight updated successfully",
                "data", updatedRecord
            ));
        } catch (Exception e) {
            log.error("Error updating weight: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update weight: " + e.getMessage()
            ));
        }
    }

    /**
     * Update temperature for a user
     * Example: POST /api/user-centric-health/temperature
     * Body: { "userId": 5, "tempValue": 36.5, "unit": "°C" }
     */
    @PostMapping("/temperature")
    public ResponseEntity<Map<String, Object>> updateTemperature(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal tempValue = new BigDecimal(request.get("tempValue").toString());
            String unit = request.get("unit") != null ? request.get("unit").toString() : "°C";

            HealthRecord updatedRecord = userCentricHealthService.updateTemperature(userId, tempValue, unit);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Temperature updated successfully",
                "data", updatedRecord
            ));
        } catch (Exception e) {
            log.error("Error updating temperature: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update temperature: " + e.getMessage()
            ));
        }
    }

    /**
     * Update height for a user
     * Example: POST /api/health-records/height
     * Body: { "userId": 5, "heightValue": 170.5, "unit": "cm" }
     */
    @PostMapping("/height")
    public ResponseEntity<Map<String, Object>> updateHeight(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal heightValue = new BigDecimal(request.get("heightValue").toString());
            String unit = request.get("unit") != null ? request.get("unit").toString() : "cm";

            HealthRecord updatedRecord = userCentricHealthService.updateHeight(userId, heightValue, unit);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Height updated successfully",
                "data", updatedRecord
            ));
        } catch (Exception e) {
            log.error("Error updating height: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update height: " + e.getMessage()
            ));
        }
    }

    /**
     * Update multiple health metrics at once
     * Example: POST /api/health-records/multiple
     * Body: {
     *   "userId": 5,
     *   "heartRate": 80,
     *   "bloodPressure": "120/80",
     *   "weight": 64.5,
     *   "temperature": 36.5,
     *   "height": 170.5
     * }
     */
    @PostMapping("/multiple")
    public ResponseEntity<Map<String, Object>> updateMultipleMetrics(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());

            Integer heartRate = request.get("heartRate") != null ?
                Integer.valueOf(request.get("heartRate").toString()) : null;
            String heartRateUnit = request.get("heartRateUnit") != null ?
                request.get("heartRateUnit").toString() : "bpm";

            String bloodPressure = request.get("bloodPressure") != null ?
                request.get("bloodPressure").toString() : null;
            String bpUnit = request.get("bpUnit") != null ?
                request.get("bpUnit").toString() : "mmHg";

            BigDecimal weight = request.get("weight") != null ?
                new BigDecimal(request.get("weight").toString()) : null;
            String weightUnit = request.get("weightUnit") != null ?
                request.get("weightUnit").toString() : "kg";

            BigDecimal temperature = request.get("temperature") != null ?
                new BigDecimal(request.get("temperature").toString()) : null;
            String tempUnit = request.get("tempUnit") != null ?
                request.get("tempUnit").toString() : "°C";

            BigDecimal height = request.get("height") != null ?
                new BigDecimal(request.get("height").toString()) : null;
            String heightUnit = request.get("heightUnit") != null ?
                request.get("heightUnit").toString() : "cm";

            HealthRecord updatedRecord = userCentricHealthService.updateMultipleMetrics(
                userId, heartRate, heartRateUnit, bloodPressure, bpUnit,
                weight, weightUnit, temperature, tempUnit, height, heightUnit
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Health metrics updated successfully",
                "data", updatedRecord
            ));
        } catch (Exception e) {
            log.error("Error updating multiple metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update metrics: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all users' health data
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUsersHealthData() {
        try {
            List<HealthRecord> allRecords = userCentricHealthService.getAllUsersHealthData();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", allRecords.size(),
                "data", allRecords
            ));
        } catch (Exception e) {
            log.error("Error getting all health data: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get health data: " + e.getMessage()
            ));
        }
    }

    /**
     * Get users with critical health status
     */
    @GetMapping("/critical")
    public ResponseEntity<Map<String, Object>> getCriticalUsers() {
        try {
            List<HealthRecord> criticalRecords = userCentricHealthService.getCriticalHealthUsers();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", criticalRecords.size(),
                "data", criticalRecords
            ));
        } catch (Exception e) {
            log.error("Error getting critical users: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get critical users: " + e.getMessage()
            ));
        }
    }
}

package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.entity.Appointment;
import rw.health.ubuzima.entity.HealthRecord;
import rw.health.ubuzima.entity.HealthFacility;
import rw.health.ubuzima.enums.AppointmentStatus;
import rw.health.ubuzima.enums.AppointmentType;


import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.repository.AppointmentRepository;
import rw.health.ubuzima.repository.EducationLessonRepository;
import rw.health.ubuzima.repository.EducationProgressRepository;
import rw.health.ubuzima.repository.HealthRecordRepository;
import rw.health.ubuzima.repository.HealthFacilityRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClientController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final EducationLessonRepository educationLessonRepository;
    private final EducationProgressRepository educationProgressRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthFacilityRepository healthFacilityRepository;

    // Get client profile
    @GetMapping("/{clientId}/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long clientId) {
        try {
            User client = userRepository.findById(clientId).orElse(null);
            
            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "client", client
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch profile: " + e.getMessage()
            ));
        }
    }

    // Get client appointments
    @GetMapping("/{clientId}/appointments")
    public ResponseEntity<Map<String, Object>> getAppointments(@PathVariable Long clientId) {
        try {
            User client = userRepository.findById(clientId).orElse(null);
            
            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            List<Appointment> appointments = appointmentRepository.findByUserOrderByScheduledDateDesc(client);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "appointments", appointments
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch appointments: " + e.getMessage()
            ));
        }
    }

    // Book new appointment
    @PostMapping("/{clientId}/appointments")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @PathVariable Long clientId,
            @RequestBody Map<String, Object> request) {
        
        try {
            User client = userRepository.findById(clientId).orElse(null);
            
            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            // Get facility
            Long facilityId = Long.valueOf(request.get("facilityId").toString());
            HealthFacility facility = healthFacilityRepository.findById(facilityId).orElse(null);
            
            if (facility == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health facility not found"
                ));
            }

            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setUser(client);
            appointment.setHealthFacility(facility);
            appointment.setAppointmentType(AppointmentType.valueOf(request.get("type").toString().toUpperCase()));
            appointment.setScheduledDate(LocalDateTime.parse(request.get("scheduledDate").toString()));
            appointment.setReason(request.get("reason").toString());
            appointment.setStatus(AppointmentStatus.SCHEDULED);

            Appointment savedAppointment = appointmentRepository.save(appointment);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment booked successfully",
                "appointment", savedAppointment
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to book appointment: " + e.getMessage()
            ));
        }
    }

    // Get client health records
    @GetMapping("/{clientId}/health-records")
    public ResponseEntity<Map<String, Object>> getHealthRecords(@PathVariable Long clientId) {
        try {
            User client = userRepository.findById(clientId).orElse(null);
            
            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            // Get all health records for the client (original approach)
            List<HealthRecord> healthRecords = healthRecordRepository.findByUserIdOrderByRecordedAtDesc(clientId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "healthRecords", healthRecords
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch health records: " + e.getMessage()
            ));
        }
    }

    // Add health record
    @PostMapping("/{clientId}/health-records")
    public ResponseEntity<Map<String, Object>> addHealthRecord(
            @PathVariable Long clientId,
            @RequestBody Map<String, Object> request) {
        
        try {
            User client = userRepository.findById(clientId).orElse(null);
            
            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            // Create or update health record (user-centric approach)
            HealthRecord healthRecord = healthRecordRepository.findByUserId(clientId).orElse(null);

            if (healthRecord == null) {
                healthRecord = new HealthRecord();
                healthRecord.setUser(client);
                healthRecord.setHealthStatus("normal");
                healthRecord.setIsVerified(false);
            }

            // Update specific health metrics based on request
            String metricType = request.get("metricType") != null ? request.get("metricType").toString() : null;
            String value = request.get("value").toString();
            String unit = request.get("unit") != null ? request.get("unit").toString() : null;
            String notes = request.get("notes") != null ? request.get("notes").toString() : null;

            // Update the appropriate field based on metric type
            if ("HEART_RATE".equals(metricType)) {
                healthRecord.setHeartRateValue(Integer.valueOf(value));
                healthRecord.setHeartRateUnit(unit != null ? unit : "bpm");
            } else if ("BLOOD_PRESSURE".equals(metricType)) {
                healthRecord.setBpValue(value);
                healthRecord.setBpUnit(unit != null ? unit : "mmHg");
            } else if ("WEIGHT".equals(metricType)) {
                healthRecord.setKgValue(new java.math.BigDecimal(value));
                healthRecord.setKgUnit(unit != null ? unit : "kg");
            } else if ("TEMPERATURE".equals(metricType)) {
                healthRecord.setTempValue(new java.math.BigDecimal(value));
                healthRecord.setTempUnit(unit != null ? unit : "°C");
            }

            healthRecord.setNotes(notes);
            healthRecord.setRecordedBy("CLIENT");

            HealthRecord savedRecord = healthRecordRepository.save(healthRecord);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Health record added successfully",
                "healthRecord", savedRecord
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to add health record: " + e.getMessage()
            ));
        }
    }

    // Get nearby health facilities
    @GetMapping("/{clientId}/nearby-facilities")
    public ResponseEntity<Map<String, Object>> getNearbyFacilities(
            @PathVariable Long clientId,
            @RequestParam(required = false, defaultValue = "10.0") Double radius) {
        
        try {
            User client = userRepository.findById(clientId).orElse(null);
            
            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            // For now, return all active facilities
            // In real implementation, use client's location to find nearby facilities
            List<HealthFacility> facilities = healthFacilityRepository.findByIsActiveTrue();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "facilities", facilities
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch facilities: " + e.getMessage()
            ));
        }
    }

    // Get client dashboard stats
    @GetMapping("/{clientId}/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@PathVariable Long clientId) {
        try {
            User client = userRepository.findById(clientId).orElse(null);
            
            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            List<Appointment> appointments = appointmentRepository.findByUser(client);
            List<HealthRecord> healthRecords = healthRecordRepository.findByUserIdOrderByRecordedAtDesc(clientId);

            long totalAppointments = appointments.size();
            long upcomingAppointments = appointments.stream()
                .filter(apt -> apt.getScheduledDate().isAfter(LocalDateTime.now()) &&
                              apt.getStatus() == AppointmentStatus.SCHEDULED)
                .count();
            long completedAppointments = appointments.stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.COMPLETED)
                .count();

            // Get lesson statistics
            Long totalLessons = educationLessonRepository.countByIsPublishedTrue();
            Long completedLessons = educationProgressRepository.countCompletedLessons(client);
            if (totalLessons == null) totalLessons = 0L;
            if (completedLessons == null) completedLessons = 0L;

            // Get facility count
            Long totalFacilities = healthFacilityRepository.count();

            // Count health records (original approach)
            long totalHealthRecords = healthRecords.size();

            // Count different types of health metrics recorded
            long recordedMetrics = 0;
            if (!healthRecords.isEmpty()) {
                HealthRecord record = healthRecords.get(0);
                if (record.getHeartRateValue() != null) recordedMetrics++;
                if (record.getBpValue() != null) recordedMetrics++;
                if (record.getKgValue() != null) recordedMetrics++;
                if (record.getTempValue() != null) recordedMetrics++;
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAppointments", totalAppointments);
            stats.put("upcomingAppointments", upcomingAppointments);
            stats.put("completedAppointments", completedAppointments);
            stats.put("totalHealthRecords", totalHealthRecords);
            stats.put("recordedMetrics", recordedMetrics);
            stats.put("hasHealthRecord", !healthRecords.isEmpty());

            // Add lesson statistics
            stats.put("totalLessons", totalLessons);
            stats.put("completedLessons", completedLessons);
            stats.put("availableLessons", totalLessons - completedLessons);

            // Add facility statistics
            stats.put("totalFacilities", totalFacilities);

            // Add latest health record info if available
            if (!healthRecords.isEmpty()) {
                HealthRecord latestRecord = healthRecords.get(0);
                stats.put("healthStatus", latestRecord.getHealthStatus());
                stats.put("lastUpdated", latestRecord.getLastUpdated());
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch dashboard stats: " + e.getMessage()
            ));
        }
    }
}

package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.dto.response.UserResponse;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.entity.Appointment;
import rw.health.ubuzima.entity.HealthRecord;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.enums.AppointmentStatus;

import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.repository.AppointmentRepository;
import rw.health.ubuzima.repository.HealthRecordRepository;
import rw.health.ubuzima.repository.TimeSlotRepository;
import rw.health.ubuzima.repository.StiTestRecordRepository;
import rw.health.ubuzima.repository.SupportGroupRepository;
import rw.health.ubuzima.repository.SupportTicketRepository;
import rw.health.ubuzima.repository.CommunityEventRepository;
import rw.health.ubuzima.entity.TimeSlot;
import rw.health.ubuzima.entity.StiTestRecord;
import rw.health.ubuzima.entity.SupportGroup;
import rw.health.ubuzima.entity.SupportTicket;
import rw.health.ubuzima.entity.CommunityEvent;
import rw.health.ubuzima.enums.StiTestType;
import rw.health.ubuzima.enums.TestResultStatus;
import rw.health.ubuzima.enums.TicketStatus;
import rw.health.ubuzima.enums.TicketPriority;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/health-worker")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HealthWorkerController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final StiTestRecordRepository stiTestRecordRepository;
    private final SupportGroupRepository supportGroupRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final CommunityEventRepository communityEventRepository;

    // Get assigned clients
    @GetMapping("/{healthWorkerId}/clients")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEALTH_WORKER')")
    public ResponseEntity<Map<String, Object>> getAssignedClients(@PathVariable Long healthWorkerId) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);
            
            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            // Get clients from the same facility as the health worker
            List<User> clients = userRepository.findByFacilityIdAndRole(healthWorker.getFacilityId(), UserRole.CLIENT);

            List<UserResponse> clientResponses = clients.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "clients", clientResponses
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch clients: " + e.getMessage()
            ));
        }
    }

    // Get appointments for health worker
    @GetMapping("/{healthWorkerId}/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEALTH_WORKER')")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable Long healthWorkerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date) {
        
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);
            
            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            List<Appointment> appointments;
            
            if (status != null) {
                AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
                appointments = appointmentRepository.findByHealthWorker(healthWorker)
                    .stream()
                    .filter(apt -> apt.getStatus() == appointmentStatus)
                    .collect(Collectors.toList());
            } else {
                appointments = appointmentRepository.findByHealthWorker(healthWorker);
            }

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

    // Update appointment status
    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<Map<String, Object>> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> request) {
        
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }

            String statusStr = request.get("status");
            AppointmentStatus status = AppointmentStatus.valueOf(statusStr.toUpperCase());
            
            appointment.setStatus(status);
            
            if (status == AppointmentStatus.COMPLETED) {
                appointment.setCompletedAt(LocalDateTime.now());
            } else if (status == AppointmentStatus.CANCELLED) {
                appointment.setCancelledAt(LocalDateTime.now());
                appointment.setCancellationReason(request.get("reason"));
            }

            appointmentRepository.save(appointment);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment status updated successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update appointment: " + e.getMessage()
            ));
        }
    }

    // Get client health records
    @GetMapping("/clients/{clientId}/health-records")
    public ResponseEntity<Map<String, Object>> getClientHealthRecords(@PathVariable Long clientId) {
        try {
            User client = userRepository.findById(clientId).orElse(null);
            
            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

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

    // Add health record for client
    @PostMapping("/clients/{clientId}/health-records")
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

            // Get or create health record for client (user-centric approach)
            HealthRecord healthRecord = healthRecordRepository.findByUserId(clientId).orElse(null);

            if (healthRecord == null) {
                healthRecord = new HealthRecord();
                healthRecord.setUser(client);
                healthRecord.setHealthStatus("normal");
                healthRecord.setIsVerified(false);
            }

            // Set health metrics from request
            // healthRecord.setHeartRateValue(...);
            // healthRecord.setBpValue(...);
            // etc.

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

    // Get health worker dashboard stats
    @GetMapping("/{healthWorkerId}/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@PathVariable Long healthWorkerId) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);
            
            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            List<Appointment> allAppointments = appointmentRepository.findByHealthWorker(healthWorker);
            long totalAppointments = allAppointments.size();
            long todayAppointments = allAppointments.stream()
                .filter(apt -> apt.getScheduledDate().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();
            long completedAppointments = allAppointments.stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.COMPLETED)
                .count();
            long totalClients = allAppointments.stream()
                .map(Appointment::getUser)
                .distinct()
                .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAppointments", totalAppointments);
            stats.put("todayAppointments", todayAppointments);
            stats.put("completedAppointments", completedAppointments);
            stats.put("totalClients", totalClients);

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

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(Long.valueOf(user.getId().toString()));
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setFacilityId(user.getFacilityId());
        response.setDistrict(user.getDistrict());
        response.setSector(user.getSector());
        response.setCell(user.getCell());
        response.setVillage(user.getVillage());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setActive(user.isActive());
        response.setProfileImageUrl(user.getProfilePictureUrl());
        return response;
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<Map<String, Object>> getClientDetails(@PathVariable Long clientId) {
        try {
            User client = userRepository.findById(clientId).orElse(null);

            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            // Get client's health records (original approach)
            List<HealthRecord> healthRecords = healthRecordRepository.findByUserIdOrderByRecordedAtDesc(clientId);

            // Get client's appointments
            List<Appointment> appointments = appointmentRepository.findByUser(client);

            Map<String, Object> clientDetails = new HashMap<>();
            clientDetails.put("id", client.getId());
            clientDetails.put("name", client.getName());
            clientDetails.put("email", client.getEmail());
            clientDetails.put("phone", client.getPhone());
            clientDetails.put("district", client.getDistrict());
            clientDetails.put("sector", client.getSector());
            clientDetails.put("cell", client.getCell());
            clientDetails.put("village", client.getVillage());
            clientDetails.put("healthRecords", healthRecords);
            clientDetails.put("appointments", appointments);
            clientDetails.put("totalHealthRecords", healthRecords.size());
            clientDetails.put("totalAppointments", appointments.size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "clientDetails", clientDetails
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch client details: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/consultations")
    public ResponseEntity<Map<String, Object>> createConsultation(@RequestBody Map<String, Object> request) {
        try {
            Long clientId = Long.valueOf(request.get("clientId").toString());
            Long healthWorkerId = Long.valueOf(request.get("healthWorkerId").toString());

            User client = userRepository.findById(clientId).orElse(null);
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            // Update consultation notes in health record (user-centric approach)
            HealthRecord consultation = healthRecordRepository.findByUserId(clientId).orElse(null);

            if (consultation == null) {
                consultation = new HealthRecord();
                consultation.setUser(client);
                consultation.setHealthStatus("normal");
                consultation.setIsVerified(true);
            }

            consultation.setNotes(request.get("notes").toString());
            consultation.setRecordedBy(healthWorker.getName());
            consultation.setIsVerified(true);

            HealthRecord savedConsultation = healthRecordRepository.save(consultation);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Consultation recorded successfully",
                "consultation", savedConsultation
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create consultation: " + e.getMessage()
            ));
        }
    }

    // ==================== TIME SLOTS ENDPOINTS ====================

    // Get time slots for health worker
    @GetMapping("/{healthWorkerId}/time-slots")
    public ResponseEntity<Map<String, Object>> getTimeSlots(
            @PathVariable Long healthWorkerId,
            @RequestParam(required = false) String date) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            List<TimeSlot> timeSlots;
            if (date != null) {
                LocalDate targetDate = LocalDate.parse(date);
                timeSlots = timeSlotRepository.findByHealthWorkerAndDate(healthWorker, targetDate);
            } else {
                timeSlots = timeSlotRepository.findByHealthWorker(healthWorker);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "timeSlots", timeSlots
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch time slots: " + e.getMessage()
            ));
        }
    }

    // Create time slot
    @PostMapping("/{healthWorkerId}/time-slots")
    public ResponseEntity<Map<String, Object>> createTimeSlot(
            @PathVariable Long healthWorkerId,
            @RequestBody Map<String, Object> request) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setHealthWorker(healthWorker);
            // Note: TimeSlot entity might need a facilityId field instead of HealthFacility object
            // timeSlot.setHealthFacility(healthWorker.getHealthFacility());

            // Parse date and time
            String dateStr = request.get("date").toString();
            String startTimeStr = request.get("startTime").toString();
            String endTimeStr = request.get("endTime").toString();

            LocalDate date = LocalDate.parse(dateStr);
            LocalDateTime startTime = LocalDateTime.of(date, java.time.LocalTime.parse(startTimeStr));
            LocalDateTime endTime = LocalDateTime.of(date, java.time.LocalTime.parse(endTimeStr));

            timeSlot.setStartTime(startTime);
            timeSlot.setEndTime(endTime);
            timeSlot.setIsAvailable(true);
            timeSlot.setMaxAppointments((Integer) request.getOrDefault("maxPatients", 1));
            timeSlot.setReason(request.get("reason") != null ? request.get("reason").toString() : null);

            TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot created successfully",
                "timeSlot", savedTimeSlot
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create time slot: " + e.getMessage()
            ));
        }
    }

    // Create bulk time slots
    @PostMapping("/{healthWorkerId}/time-slots/bulk")
    public ResponseEntity<Map<String, Object>> createBulkTimeSlots(
            @PathVariable Long healthWorkerId,
            @RequestBody Map<String, Object> request) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            String dateStr = request.get("date").toString();
            String startTimeStr = request.get("startTime").toString();
            String endTimeStr = request.get("endTime").toString();
            Integer slotDuration = (Integer) request.getOrDefault("slotDuration", 30);
            Integer maxPatients = (Integer) request.getOrDefault("maxPatients", 1);

            LocalDate date = LocalDate.parse(dateStr);
            java.time.LocalTime startTime = java.time.LocalTime.parse(startTimeStr);
            java.time.LocalTime endTime = java.time.LocalTime.parse(endTimeStr);

            List<TimeSlot> createdSlots = new ArrayList<>();
            java.time.LocalTime currentTime = startTime;

            while (currentTime.isBefore(endTime)) {
                java.time.LocalTime slotEndTime = currentTime.plusMinutes(slotDuration);
                if (slotEndTime.isAfter(endTime)) break;

                TimeSlot timeSlot = new TimeSlot();
                timeSlot.setHealthWorker(healthWorker);
                // Note: TimeSlot entity might need a facilityId field instead of HealthFacility object
            // timeSlot.setHealthFacility(healthWorker.getHealthFacility());
                timeSlot.setStartTime(LocalDateTime.of(date, currentTime));
                timeSlot.setEndTime(LocalDateTime.of(date, slotEndTime));
                timeSlot.setIsAvailable(true);
                timeSlot.setMaxAppointments(maxPatients);

                createdSlots.add(timeSlotRepository.save(timeSlot));
                currentTime = slotEndTime;
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bulk time slots created successfully",
                "createdSlots", createdSlots.size(),
                "timeSlots", createdSlots
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create bulk time slots: " + e.getMessage()
            ));
        }
    }

    // Update time slot
    @PutMapping("/{healthWorkerId}/time-slots/{timeSlotId}")
    public ResponseEntity<Map<String, Object>> updateTimeSlot(
            @PathVariable Long healthWorkerId,
            @PathVariable Long timeSlotId,
            @RequestBody Map<String, Object> request) {
        try {
            TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId).orElse(null);

            if (timeSlot == null || !timeSlot.getHealthWorker().getId().equals(healthWorkerId)) {
                return ResponseEntity.notFound().build();
            }

            if (request.containsKey("startTime")) {
                String startTimeStr = request.get("startTime").toString();
                timeSlot.setStartTime(LocalDateTime.parse(startTimeStr));
            }

            if (request.containsKey("endTime")) {
                String endTimeStr = request.get("endTime").toString();
                timeSlot.setEndTime(LocalDateTime.parse(endTimeStr));
            }

            if (request.containsKey("maxPatients")) {
                timeSlot.setMaxAppointments((Integer) request.get("maxPatients"));
            }

            if (request.containsKey("isAvailable")) {
                timeSlot.setIsAvailable((Boolean) request.get("isAvailable"));
            }

            TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot updated successfully",
                "timeSlot", updatedTimeSlot
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update time slot: " + e.getMessage()
            ));
        }
    }

    // Delete time slot
    @DeleteMapping("/{healthWorkerId}/time-slots/{timeSlotId}")
    public ResponseEntity<Map<String, Object>> deleteTimeSlot(
            @PathVariable Long healthWorkerId,
            @PathVariable Long timeSlotId) {
        try {
            TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId).orElse(null);

            if (timeSlot == null || !timeSlot.getHealthWorker().getId().equals(healthWorkerId)) {
                return ResponseEntity.notFound().build();
            }

            timeSlotRepository.delete(timeSlot);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete time slot: " + e.getMessage()
            ));
        }
    }

    // Book time slot
    @PostMapping("/{healthWorkerId}/time-slots/{timeSlotId}/book")
    public ResponseEntity<Map<String, Object>> bookTimeSlot(
            @PathVariable Long healthWorkerId,
            @PathVariable Long timeSlotId) {
        try {
            TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId).orElse(null);

            if (timeSlot == null || !timeSlot.getHealthWorker().getId().equals(healthWorkerId)) {
                return ResponseEntity.notFound().build();
            }

            if (!timeSlot.getIsAvailable()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Time slot is not available"
                ));
            }

            // Mark as booked (you might want to create an appointment here instead)
            timeSlot.setIsAvailable(false);
            timeSlotRepository.save(timeSlot);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot booked successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to book time slot: " + e.getMessage()
            ));
        }
    }

    // ==================== STI TEST RECORDS ENDPOINTS ====================

    // Get STI tests for health worker
    @GetMapping("/{healthWorkerId}/sti-tests")
    public ResponseEntity<Map<String, Object>> getSTITests(@PathVariable Long healthWorkerId) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            // Get all STI tests for clients assigned to this health worker
            List<Appointment> appointments = appointmentRepository.findByHealthWorker(healthWorker);
            List<User> clients = appointments.stream()
                .map(Appointment::getUser)
                .distinct()
                .collect(Collectors.toList());

            List<StiTestRecord> allTests = new ArrayList<>();
            for (User client : clients) {
                List<StiTestRecord> clientTests = stiTestRecordRepository.findByUser(client);
                allTests.addAll(clientTests);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "tests", allTests
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch STI tests: " + e.getMessage()
            ));
        }
    }

    // Get STI tests for specific client
    @GetMapping("/clients/{clientId}/sti-tests")
    public ResponseEntity<Map<String, Object>> getClientSTITests(@PathVariable Long clientId) {
        try {
            User client = userRepository.findById(clientId).orElse(null);

            if (client == null || !client.isClient()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Client not found"
                ));
            }

            List<StiTestRecord> tests = stiTestRecordRepository.findByUser(client);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "tests", tests
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch client STI tests: " + e.getMessage()
            ));
        }
    }

    // Create STI test record
    @PostMapping("/clients/{clientId}/sti-tests")
    public ResponseEntity<Map<String, Object>> createSTITest(
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

            StiTestRecord test = new StiTestRecord();
            test.setUser(client);
            test.setTestType(StiTestType.valueOf(request.get("testType").toString().toUpperCase()));
            test.setTestDate(LocalDate.parse(request.get("testDate").toString()));
            test.setTestLocation(request.get("testLocation") != null ? request.get("testLocation").toString() : null);
            // Note: StiTestRecord doesn't have a priority field
            // test.setPriority(request.get("priority") != null ? request.get("priority").toString() : "NORMAL");
            test.setNotes(request.get("notes") != null ? request.get("notes").toString() : null);
            test.setResultStatus(TestResultStatus.PENDING);

            StiTestRecord savedTest = stiTestRecordRepository.save(test);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "STI test record created successfully",
                "test", savedTest
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create STI test: " + e.getMessage()
            ));
        }
    }

    // Update STI test record
    @PutMapping("/sti-tests/{testId}")
    public ResponseEntity<Map<String, Object>> updateSTITest(
            @PathVariable Long testId,
            @RequestBody Map<String, Object> request) {
        try {
            StiTestRecord test = stiTestRecordRepository.findById(testId).orElse(null);

            if (test == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.containsKey("resultStatus")) {
                test.setResultStatus(TestResultStatus.valueOf(request.get("resultStatus").toString().toUpperCase()));
            }

            if (request.containsKey("resultDate")) {
                test.setResultDate(LocalDate.parse(request.get("resultDate").toString()));
            }

            if (request.containsKey("notes")) {
                test.setNotes(request.get("notes").toString());
            }

            StiTestRecord updatedTest = stiTestRecordRepository.save(test);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "STI test updated successfully",
                "test", updatedTest
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update STI test: " + e.getMessage()
            ));
        }
    }

    // ==================== SUPPORT GROUPS ENDPOINTS ====================

    // Get support groups managed by health worker
    @GetMapping("/{healthWorkerId}/support-groups")
    public ResponseEntity<Map<String, Object>> getSupportGroups(@PathVariable Long healthWorkerId) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            List<SupportGroup> groups = supportGroupRepository.findByCreatorAndIsActiveTrueOrderByCreatedAtDesc(healthWorker);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "supportGroups", groups
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch support groups: " + e.getMessage()
            ));
        }
    }

    // Get tickets assigned to health worker
    @GetMapping("/{healthWorkerId}/tickets")
    public ResponseEntity<Map<String, Object>> getTickets(@PathVariable Long healthWorkerId) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            List<SupportTicket> tickets = supportTicketRepository.findByAssignedTo(healthWorker);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", tickets
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch tickets: " + e.getMessage()
            ));
        }
    }

    // Create support group
    @PostMapping("/{healthWorkerId}/support-groups")
    public ResponseEntity<Map<String, Object>> createSupportGroup(
            @PathVariable Long healthWorkerId,
            @RequestBody Map<String, Object> request) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            SupportGroup group = new SupportGroup();
            group.setName(request.get("name").toString());
            group.setDescription(request.get("description").toString());
            group.setCreator(healthWorker);
            group.setMaxMembers((Integer) request.getOrDefault("maxMembers", 20));
            group.setIsActive(true);

            SupportGroup savedGroup = supportGroupRepository.save(group);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support group created successfully",
                "supportGroup", savedGroup
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create support group: " + e.getMessage()
            ));
        }
    }

    // Create support ticket
    @PostMapping("/{healthWorkerId}/tickets")
    public ResponseEntity<Map<String, Object>> createTicket(
            @PathVariable Long healthWorkerId,
            @RequestBody Map<String, Object> request) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            SupportTicket ticket = new SupportTicket();
            ticket.setSubject(request.get("title").toString());
            ticket.setDescription(request.get("description").toString());
            ticket.setUser(healthWorker);
            ticket.setAssignedTo(healthWorker);
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setPriority(TicketPriority.valueOf(
                request.getOrDefault("priority", "MEDIUM").toString().toUpperCase()
            ));

            SupportTicket savedTicket = supportTicketRepository.save(ticket);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support ticket created successfully",
                "ticket", savedTicket
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create ticket: " + e.getMessage()
            ));
        }
    }

    // ==================== COMMUNITY EVENTS ENDPOINTS ====================

    // Get community events organized by health worker
    @GetMapping("/{healthWorkerId}/community-events")
    public ResponseEntity<Map<String, Object>> getCommunityEvents(@PathVariable Long healthWorkerId) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            List<CommunityEvent> events = communityEventRepository.findByOrganizer(healthWorker);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "events", events
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch community events: " + e.getMessage()
            ));
        }
    }

    // Create community event
    @PostMapping("/{healthWorkerId}/community-events")
    public ResponseEntity<Map<String, Object>> createCommunityEvent(
            @PathVariable Long healthWorkerId,
            @RequestBody Map<String, Object> request) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            CommunityEvent event = new CommunityEvent();
            event.setTitle(request.get("title").toString());
            event.setDescription(request.get("description").toString());
            event.setOrganizer(healthWorker);
            event.setType(request.getOrDefault("eventType", "HEALTH_EDUCATION").toString());
            event.setEventDate(LocalDateTime.parse(request.get("eventDate").toString()));
            event.setLocation(request.get("location") != null ? request.get("location").toString() : null);
            event.setMaxParticipants((Integer) request.getOrDefault("maxParticipants", 50));
            event.setIsActive(true);

            CommunityEvent savedEvent = communityEventRepository.save(event);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Community event created successfully",
                "event", savedEvent
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create community event: " + e.getMessage()
            ));
        }
    }

    // ==================== SIDE EFFECT REPORTS ENDPOINTS ====================

    // Get side effect reports for health worker's clients
    @GetMapping("/{healthWorkerId}/side-effect-reports")
    public ResponseEntity<Map<String, Object>> getSideEffectReports(@PathVariable Long healthWorkerId) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            // For now, return mock data since we don't have a side effect entity yet
            // In a real implementation, you would query the side effect repository
            List<Map<String, Object>> mockReports = List.of(
                Map.of(
                    "id", 1,
                    "clientName", "Jane Doe",
                    "contraceptiveMethod", "Birth Control Pills",
                    "symptoms", "Nausea, headache",
                    "severity", "Mild",
                    "reportedDate", LocalDate.now().toString(),
                    "status", "Under Review"
                ),
                Map.of(
                    "id", 2,
                    "clientName", "Mary Smith",
                    "contraceptiveMethod", "IUD",
                    "symptoms", "Irregular bleeding",
                    "severity", "Moderate",
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

    // ==================== REPORTS ENDPOINTS ====================

    // Get user activity report
    @GetMapping("/{healthWorkerId}/reports/user-activity")
    public ResponseEntity<Map<String, Object>> getUserActivityReport(
            @PathVariable Long healthWorkerId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            // Get clients assigned to this health worker
            List<Appointment> appointments = appointmentRepository.findByHealthWorker(healthWorker);
            List<User> clients = appointments.stream()
                .map(Appointment::getUser)
                .distinct()
                .collect(Collectors.toList());

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("totalClients", clients.size());
            reportData.put("activeClients", clients.stream().filter(User::isActive).count());
            reportData.put("newRegistrations", clients.stream()
                .filter(c -> c.getCreatedAt().isAfter(LocalDateTime.parse(startDate + "T00:00:00")))
                .count());
            reportData.put("clientGrowthRate", 12.5);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reportData
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to generate user activity report: " + e.getMessage()
            ));
        }
    }

    // Get health records report
    @GetMapping("/{healthWorkerId}/reports/health-records")
    public ResponseEntity<Map<String, Object>> getHealthRecordsReport(
            @PathVariable Long healthWorkerId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            // Get health records for clients assigned to this health worker
            List<Appointment> appointments = appointmentRepository.findByHealthWorker(healthWorker);
            List<User> clients = appointments.stream()
                .map(Appointment::getUser)
                .distinct()
                .collect(Collectors.toList());

            List<HealthRecord> allRecords = new ArrayList<>();
            for (User client : clients) {
                List<HealthRecord> clientRecords = healthRecordRepository.findByUserIdOrderByRecordedAtDesc(client.getId());
                allRecords.addAll(clientRecords);
            }

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("totalRecords", allRecords.size());
            reportData.put("newRecords", allRecords.stream()
                .filter(r -> r.getCreatedAt().isAfter(LocalDateTime.parse(startDate + "T00:00:00")))
                .count());
            reportData.put("recordsGrowthRate", 8.9);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reportData
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to generate health records report: " + e.getMessage()
            ));
        }
    }

    // Get appointments report
    @GetMapping("/{healthWorkerId}/reports/appointments")
    public ResponseEntity<Map<String, Object>> getAppointmentsReport(
            @PathVariable Long healthWorkerId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            List<Appointment> appointments = appointmentRepository.findByHealthWorker(healthWorker);

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("totalAppointments", appointments.size());
            reportData.put("completedAppointments", appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count());
            reportData.put("cancelledAppointments", appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                .count());
            reportData.put("pendingAppointments", appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .count());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reportData
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to generate appointments report: " + e.getMessage()
            ));
        }
    }

    // Export reports
    @GetMapping("/{healthWorkerId}/reports/export/{format}")
    public ResponseEntity<Map<String, Object>> exportReport(
            @PathVariable Long healthWorkerId,
            @PathVariable String format,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId).orElse(null);

            if (healthWorker == null || !healthWorker.isHealthWorker()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health worker not found"
                ));
            }

            // For now, return a success message
            // In a real implementation, you would generate the actual file
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Report export initiated",
                "format", format,
                "downloadUrl", "/downloads/health-worker-report-" + healthWorkerId + "." + format
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to export report: " + e.getMessage()
            ));
        }
    }
}

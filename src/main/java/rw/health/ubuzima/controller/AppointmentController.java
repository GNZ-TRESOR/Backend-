package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.Appointment;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.entity.HealthFacility;
import rw.health.ubuzima.entity.TimeSlot;
import rw.health.ubuzima.enums.AppointmentStatus;
import rw.health.ubuzima.enums.AppointmentType;
import rw.health.ubuzima.repository.AppointmentRepository;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.repository.HealthFacilityRepository;
import rw.health.ubuzima.repository.TimeSlotRepository;
import rw.health.ubuzima.constants.ErrorCodes;
import rw.health.ubuzima.dto.response.ApiResponse;
import rw.health.ubuzima.enums.UserRole;

import java.util.HashMap;
import java.util.Map;
import rw.health.ubuzima.service.InteractiveNotificationService;
import rw.health.ubuzima.service.UserMessageService;
import rw.health.ubuzima.util.JwtUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final HealthFacilityRepository healthFacilityRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Extract JWT token from Authorization header
            String token = authHeader.replace("Bearer ", "");

            // Extract user ID from JWT token
            Long userId = jwtUtil.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid token - unable to extract user ID"
                ));
            }

            // Find user by ID from token
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Get appointments based on user role
            List<Appointment> appointments;

            if (user.isAdmin()) {
                // Admin users get all appointments in the system
                appointments = appointmentRepository.findAll();
            } else {
                // Regular users get only their own appointments
                appointments = appointmentRepository.findByUserOrderByScheduledDateDesc(user);
            }

            if (status != null) {
                AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
                appointments = appointments.stream()
                    .filter(apt -> apt.getStatus() == appointmentStatus)
                    .toList();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", appointments,
                "total", appointments.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch appointments: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long facilityId = Long.valueOf(request.get("facilityId").toString());
            
            User user = userRepository.findById(userId).orElse(null);
            HealthFacility facility = healthFacilityRepository.findById(facilityId).orElse(null);
            
            if (user == null || facility == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User or facility not found"
                ));
            }

            // Check for duplicate appointments (same user, facility, date, and time)
            LocalDateTime scheduledDateTime = LocalDateTime.parse(request.get("scheduledDate").toString());
            List<Appointment> existingAppointments = appointmentRepository.findByUserAndHealthFacilityAndScheduledDate(
                user, facility, scheduledDateTime
            );

            if (!existingAppointments.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "An appointment already exists for this date and time"
                ));
            }

            Appointment appointment = new Appointment();
            appointment.setUser(user);
            appointment.setHealthFacility(facility);
            appointment.setScheduledDate(LocalDateTime.parse(request.get("scheduledDate").toString()));
            appointment.setAppointmentType(AppointmentType.valueOf(request.get("appointmentType").toString().toUpperCase()));
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            
            if (request.get("reason") != null) {
                appointment.setReason(request.get("reason").toString());
            }
            
            if (request.get("notes") != null) {
                appointment.setNotes(request.get("notes").toString());
            }

            if (request.get("durationMinutes") != null) {
                appointment.setDurationMinutes(Integer.valueOf(request.get("durationMinutes").toString()));
            }

            if (request.get("healthWorkerId") != null) {
                Long healthWorkerId = Long.valueOf(request.get("healthWorkerId").toString());
                User healthWorker = userRepository.findById(healthWorkerId).orElse(null);
                appointment.setHealthWorker(healthWorker);
            }

            Appointment savedAppointment = appointmentRepository.save(appointment);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment created successfully",
                "appointment", savedAppointment
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create appointment: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentRepository.findById(id).orElse(null);

            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }

            // Build comprehensive appointment details
            Map<String, Object> appointmentDetails = buildAppointmentDetails(appointment);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "appointment", appointmentDetails
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch appointment: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAppointment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        try {
            Appointment appointment = appointmentRepository.findById(id).orElse(null);
            
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.get("scheduledDate") != null) {
                appointment.setScheduledDate(LocalDateTime.parse(request.get("scheduledDate").toString()));
            }
            
            if (request.get("status") != null) {
                appointment.setStatus(AppointmentStatus.valueOf(request.get("status").toString().toUpperCase()));
            }
            
            if (request.get("notes") != null) {
                appointment.setNotes(request.get("notes").toString());
            }

            Appointment updatedAppointment = appointmentRepository.save(appointment);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment updated successfully",
                "appointment", updatedAppointment
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update appointment: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> request) {
        
        try {
            Appointment appointment = appointmentRepository.findById(id).orElse(null);
            
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }

            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setCancelledAt(LocalDateTime.now());

            if (request != null && request.get("reason") != null) {
                appointment.setCancellationReason(request.get("reason").toString());
            }

            appointmentRepository.save(appointment);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment cancelled successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to cancel appointment: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(
            @RequestParam String facilityId,
            @RequestParam(required = false) String healthWorkerId,
            @RequestParam String date) {
        
        try {
            HealthFacility facility = healthFacilityRepository.findById(Long.valueOf(facilityId)).orElse(null);
            
            if (facility == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Facility not found"
                ));
            }

            LocalDate requestedDate = LocalDate.parse(date);
            LocalDateTime startOfDay = requestedDate.atStartOfDay();
            LocalDateTime endOfDay = requestedDate.atTime(LocalTime.MAX);

            List<TimeSlot> availableSlots;
            
            if (healthWorkerId != null) {
                User healthWorker = userRepository.findById(Long.valueOf(healthWorkerId)).orElse(null);
                if (healthWorker == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Health worker not found"
                    ));
                }
                availableSlots = timeSlotRepository.findAvailableSlots(facility, healthWorker, startOfDay, endOfDay);
            } else {
                availableSlots = timeSlotRepository.findAvailableSlotsByFacility(facility, startOfDay, endOfDay);
            }

            // If no time slots exist, generate default ones
            if (availableSlots.isEmpty()) {
                availableSlots = generateDefaultTimeSlots(facility, requestedDate);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "timeSlots", availableSlots
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch available slots: " + e.getMessage()
            ));
        }
    }

    private List<TimeSlot> generateDefaultTimeSlots(HealthFacility facility, LocalDate date) {
        List<TimeSlot> slots = new ArrayList<>();
        
        // Generate slots from 8 AM to 5 PM, every 30 minutes
        for (int hour = 8; hour < 17; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                LocalDateTime startTime = date.atTime(hour, minute);
                LocalDateTime endTime = startTime.plusMinutes(30);
                
                TimeSlot slot = new TimeSlot();
                slot.setHealthFacility(facility);
                slot.setStartTime(startTime);
                slot.setEndTime(endTime);
                slot.setIsAvailable(true);
                
                slots.add(slot);
            }
        }
        
        return slots;
    }



    /**
     * Build comprehensive appointment details
     */
    private Map<String, Object> buildAppointmentDetails(Appointment appointment) {
        Map<String, Object> details = new HashMap<>();

        // Basic appointment information
        details.put("id", appointment.getId());
        details.put("appointmentType", appointment.getAppointmentType());
        details.put("status", appointment.getStatus());
        details.put("scheduledDate", appointment.getScheduledDate());
        details.put("durationMinutes", appointment.getDurationMinutes());
        details.put("reason", appointment.getReason());
        details.put("notes", appointment.getNotes());
        details.put("reminderSent", appointment.getReminderSent());
        details.put("completedAt", appointment.getCompletedAt());
        details.put("cancelledAt", appointment.getCancelledAt());
        details.put("cancellationReason", appointment.getCancellationReason());
        details.put("createdAt", appointment.getCreatedAt());
        details.put("updatedAt", appointment.getUpdatedAt());

        // Client information
        if (appointment.getUser() != null) {
            Map<String, Object> clientInfo = new HashMap<>();
            clientInfo.put("id", appointment.getUser().getId());
            clientInfo.put("name", appointment.getUser().getName());
            clientInfo.put("email", appointment.getUser().getEmail());
            clientInfo.put("phone", appointment.getUser().getPhone());
            clientInfo.put("dateOfBirth", appointment.getUser().getDateOfBirth());
            clientInfo.put("gender", appointment.getUser().getGender());
            clientInfo.put("village", appointment.getUser().getVillage());
            details.put("client", clientInfo);
        }

        // Health worker information
        if (appointment.getHealthWorker() != null) {
            Map<String, Object> healthWorkerInfo = new HashMap<>();
            healthWorkerInfo.put("id", appointment.getHealthWorker().getId());
            healthWorkerInfo.put("name", appointment.getHealthWorker().getName());
            healthWorkerInfo.put("email", appointment.getHealthWorker().getEmail());
            healthWorkerInfo.put("phone", appointment.getHealthWorker().getPhone());
            // Note: Specialization field not available in User entity, using role instead
            healthWorkerInfo.put("specialization", appointment.getHealthWorker().getRole().toString());
            details.put("healthWorker", healthWorkerInfo);
        }

        // Health facility information
        if (appointment.getHealthFacility() != null) {
            Map<String, Object> facilityInfo = new HashMap<>();
            facilityInfo.put("id", appointment.getHealthFacility().getId());
            facilityInfo.put("name", appointment.getHealthFacility().getName());
            facilityInfo.put("address", appointment.getHealthFacility().getAddress());
            facilityInfo.put("phone", appointment.getHealthFacility().getPhoneNumber());
            facilityInfo.put("email", appointment.getHealthFacility().getEmail());
            facilityInfo.put("type", appointment.getHealthFacility().getFacilityType());
            details.put("facility", facilityInfo);
        }

        // Status information
        details.put("statusInfo", getStatusInfo(appointment));

        // Timeline information
        details.put("timeline", getAppointmentTimeline(appointment));

        return details;
    }

    /**
     * Get status-specific information
     */
    private Map<String, Object> getStatusInfo(Appointment appointment) {
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("current", appointment.getStatus());
        statusInfo.put("canCancel", canCancelAppointment(appointment));
        statusInfo.put("canReschedule", canRescheduleAppointment(appointment));
        statusInfo.put("canComplete", canCompleteAppointment(appointment));
        statusInfo.put("canConfirm", canConfirmAppointment(appointment));

        return statusInfo;
    }

    /**
     * Get appointment timeline
     */
    private List<Map<String, Object>> getAppointmentTimeline(Appointment appointment) {
        List<Map<String, Object>> timeline = new ArrayList<>();

        // Created
        timeline.add(Map.of(
            "event", "Appointment Created",
            "timestamp", appointment.getCreatedAt(),
            "status", "SCHEDULED",
            "description", "Appointment was booked"
        ));

        // Confirmed (if applicable)
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            timeline.add(Map.of(
                "event", "Status Changed",
                "timestamp", appointment.getUpdatedAt(),
                "status", appointment.getStatus(),
                "description", getStatusDescription(appointment.getStatus())
            ));
        }

        // Completed (if applicable)
        if (appointment.getCompletedAt() != null) {
            timeline.add(Map.of(
                "event", "Appointment Completed",
                "timestamp", appointment.getCompletedAt(),
                "status", "COMPLETED",
                "description", "Appointment was completed successfully"
            ));
        }

        // Cancelled (if applicable)
        if (appointment.getCancelledAt() != null) {
            timeline.add(Map.of(
                "event", "Appointment Cancelled",
                "timestamp", appointment.getCancelledAt(),
                "status", "CANCELLED",
                "description", appointment.getCancellationReason() != null ?
                    appointment.getCancellationReason() : "Appointment was cancelled"
            ));
        }

        return timeline;
    }

    private String getStatusDescription(AppointmentStatus status) {
        return switch (status) {
            case CONFIRMED -> "Appointment was confirmed by health worker";
            case IN_PROGRESS -> "Appointment is currently in progress";
            case COMPLETED -> "Appointment was completed successfully";
            case CANCELLED -> "Appointment was cancelled";
            case NO_SHOW -> "Client did not show up for appointment";
            case RESCHEDULED -> "Appointment was rescheduled";
            default -> "Appointment status updated";
        };
    }

    private boolean canCancelAppointment(Appointment appointment) {
        return appointment.getStatus() == AppointmentStatus.SCHEDULED ||
               appointment.getStatus() == AppointmentStatus.CONFIRMED;
    }

    private boolean canRescheduleAppointment(Appointment appointment) {
        return appointment.getStatus() == AppointmentStatus.SCHEDULED ||
               appointment.getStatus() == AppointmentStatus.CONFIRMED;
    }

    private boolean canCompleteAppointment(Appointment appointment) {
        return appointment.getStatus() == AppointmentStatus.CONFIRMED ||
               appointment.getStatus() == AppointmentStatus.IN_PROGRESS;
    }

    private boolean canConfirmAppointment(Appointment appointment) {
        return appointment.getStatus() == AppointmentStatus.SCHEDULED;
    }
}

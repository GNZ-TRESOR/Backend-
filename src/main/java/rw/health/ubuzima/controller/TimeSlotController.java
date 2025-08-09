package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.TimeSlot;
import rw.health.ubuzima.entity.HealthFacility;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.repository.TimeSlotRepository;
import rw.health.ubuzima.repository.HealthFacilityRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/time-slots")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TimeSlotController {

    private final TimeSlotRepository timeSlotRepository;
    private final HealthFacilityRepository healthFacilityRepository;
    private final UserRepository userRepository;

    // ============ GET TIME SLOTS ============
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTimeSlots(
            @RequestParam(required = false) Integer healthWorkerId,
            @RequestParam(required = false) Integer healthFacilityId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            System.out.println("🔍 Getting time slots with params:");
            System.out.println("  - healthWorkerId: " + healthWorkerId);
            System.out.println("  - healthFacilityId: " + healthFacilityId);
            System.out.println("  - date: " + date);
            System.out.println("  - isAvailable: " + isAvailable);

            List<TimeSlot> timeSlots = new ArrayList<>();

            if (date != null) {
                LocalDate requestedDate = LocalDate.parse(date);
                LocalDateTime startOfDay = requestedDate.atStartOfDay();
                LocalDateTime endOfDay = requestedDate.atTime(LocalTime.MAX);

                if (healthWorkerId != null && healthFacilityId != null) {
                    // Get slots for specific health worker and facility
                    Optional<User> healthWorkerOpt = userRepository.findById(Long.valueOf(healthWorkerId));
                    Optional<HealthFacility> facilityOpt = healthFacilityRepository.findById(Long.valueOf(healthFacilityId));
                    
                    if (healthWorkerOpt.isPresent() && facilityOpt.isPresent()) {
                        timeSlots = timeSlotRepository.findAvailableSlots(
                            facilityOpt.get(), 
                            healthWorkerOpt.get(), 
                            startOfDay, 
                            endOfDay
                        );
                    }
                } else if (healthFacilityId != null) {
                    // Get slots for facility
                    Optional<HealthFacility> facilityOpt = healthFacilityRepository.findById(Long.valueOf(healthFacilityId));
                    if (facilityOpt.isPresent()) {
                        timeSlots = timeSlotRepository.findByHealthFacilityAndStartTimeBetween(
                            facilityOpt.get(), 
                            startOfDay, 
                            endOfDay
                        );
                    }
                } else if (healthWorkerId != null) {
                    // Get slots for health worker
                    Optional<User> healthWorkerOpt = userRepository.findById(Long.valueOf(healthWorkerId));
                    if (healthWorkerOpt.isPresent()) {
                        timeSlots = timeSlotRepository.findByHealthWorkerAndStartTimeBetween(
                            healthWorkerOpt.get(), 
                            startOfDay, 
                            endOfDay
                        );
                    }
                }
            } else {
                // Get all time slots (with pagination)
                timeSlots = timeSlotRepository.findAll();
            }

            // Filter by availability if specified
            if (isAvailable != null) {
                timeSlots = timeSlots.stream()
                    .filter(slot -> slot.getIsAvailable().equals(isAvailable))
                    .toList();
            }

            System.out.println("📊 Found " + timeSlots.size() + " time slots");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "timeSlots", timeSlots,
                "totalElements", timeSlots.size(),
                "page", page,
                "size", size
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in getTimeSlots: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load time slots: " + e.getMessage()
            ));
        }
    }

    // ============ CREATE TIME SLOT ============
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTimeSlot(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("➕ Creating time slot with data: " + request);

            // Extract required fields
            Integer healthFacilityId = (Integer) request.get("healthFacilityId");
            Integer healthWorkerId = (Integer) request.get("healthWorkerId");
            String startTimeStr = (String) request.get("startTime");
            String endTimeStr = (String) request.get("endTime");

            if (healthFacilityId == null || healthWorkerId == null || startTimeStr == null || endTimeStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing required fields: healthFacilityId, healthWorkerId, startTime, endTime"
                ));
            }

            // Get entities
            Optional<HealthFacility> facilityOpt = healthFacilityRepository.findById(Long.valueOf(healthFacilityId));
            Optional<User> healthWorkerOpt = userRepository.findById(Long.valueOf(healthWorkerId));

            if (facilityOpt.isEmpty() || healthWorkerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Health facility or health worker not found"
                ));
            }

            // Parse times
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);

            // Create time slot
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setHealthFacility(facilityOpt.get());
            timeSlot.setHealthWorker(healthWorkerOpt.get());
            timeSlot.setStartTime(startTime);
            timeSlot.setEndTime(endTime);
            timeSlot.setIsAvailable((Boolean) request.getOrDefault("isAvailable", true));
            timeSlot.setReason((String) request.get("reason"));
            timeSlot.setMaxAppointments((Integer) request.getOrDefault("maxAppointments", 1));

            TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);

            System.out.println("✅ Time slot created successfully with ID: " + savedTimeSlot.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot created successfully",
                "timeSlot", savedTimeSlot
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in createTimeSlot: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create time slot: " + e.getMessage()
            ));
        }
    }

    // ============ UPDATE TIME SLOT ============
    
    @PutMapping("/{timeSlotId}")
    public ResponseEntity<Map<String, Object>> updateTimeSlot(
            @PathVariable Long timeSlotId,
            @RequestBody Map<String, Object> request) {
        try {
            System.out.println("✏️ Updating time slot " + timeSlotId + " with data: " + request);

            Optional<TimeSlot> timeSlotOpt = timeSlotRepository.findById(timeSlotId);
            if (timeSlotOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            TimeSlot timeSlot = timeSlotOpt.get();

            // Update fields if provided
            if (request.containsKey("startTime")) {
                timeSlot.setStartTime(LocalDateTime.parse((String) request.get("startTime")));
            }
            if (request.containsKey("endTime")) {
                timeSlot.setEndTime(LocalDateTime.parse((String) request.get("endTime")));
            }
            if (request.containsKey("isAvailable")) {
                timeSlot.setIsAvailable((Boolean) request.get("isAvailable"));
            }
            if (request.containsKey("reason")) {
                timeSlot.setReason((String) request.get("reason"));
            }
            if (request.containsKey("maxAppointments")) {
                timeSlot.setMaxAppointments((Integer) request.get("maxAppointments"));
            }

            TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);

            System.out.println("✅ Time slot updated successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot updated successfully",
                "timeSlot", updatedTimeSlot
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in updateTimeSlot: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update time slot: " + e.getMessage()
            ));
        }
    }

    // ============ DELETE TIME SLOT ============
    
    @DeleteMapping("/{timeSlotId}")
    public ResponseEntity<Map<String, Object>> deleteTimeSlot(@PathVariable Long timeSlotId) {
        try {
            System.out.println("🗑️ Deleting time slot " + timeSlotId);

            Optional<TimeSlot> timeSlotOpt = timeSlotRepository.findById(timeSlotId);
            if (timeSlotOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            timeSlotRepository.deleteById(timeSlotId);

            System.out.println("✅ Time slot deleted successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot deleted successfully"
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in deleteTimeSlot: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete time slot: " + e.getMessage()
            ));
        }
    }
}

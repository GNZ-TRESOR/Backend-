package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.MenstrualCycle;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.repository.MenstrualCycleRepository;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.util.JwtUtil;
import rw.health.ubuzima.dto.response.ApiResponse;
import rw.health.ubuzima.util.ResponseUtil;
import rw.health.ubuzima.constants.ErrorCodes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/menstrual-cycles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenstrualCycleController {

    private final MenstrualCycleRepository menstrualCycleRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMenstrualCycles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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

            // Get menstrual cycles for the authenticated user
            List<MenstrualCycle> cycles = menstrualCycleRepository.findByUserOrderByStartDateDesc(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", cycles,
                "total", cycles.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch menstrual cycles: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createMenstrualCycle(
            @RequestBody Map<String, Object> request,
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

            MenstrualCycle cycle = new MenstrualCycle();
            cycle.setUser(user);
            cycle.setStartDate(LocalDate.parse(request.get("startDate").toString()));
            
            if (request.get("endDate") != null) {
                cycle.setEndDate(LocalDate.parse(request.get("endDate").toString()));
            }
            
            if (request.get("cycleLength") != null) {
                cycle.setCycleLength(Integer.valueOf(request.get("cycleLength").toString()));
            }
            
            if (request.get("flowDuration") != null) {
                cycle.setFlowDuration(Integer.valueOf(request.get("flowDuration").toString()));
            }
            
            if (request.get("notes") != null) {
                cycle.setNotes(request.get("notes").toString());
            }

            MenstrualCycle savedCycle = menstrualCycleRepository.save(cycle);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Menstrual cycle created successfully",
                "data", savedCycle
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create menstrual cycle: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMenstrualCycle(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
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

            MenstrualCycle cycle = menstrualCycleRepository.findById(id).orElse(null);

            if (cycle == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Menstrual cycle not found"
                ));
            }

            // Ensure the cycle belongs to the authenticated user
            if (!cycle.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied - cycle does not belong to user"
                ));
            }

            if (request.get("endDate") != null) {
                cycle.setEndDate(LocalDate.parse(request.get("endDate").toString()));
            }
            
            if (request.get("cycleLength") != null) {
                cycle.setCycleLength(Integer.valueOf(request.get("cycleLength").toString()));
            }
            
            if (request.get("flowDuration") != null) {
                cycle.setFlowDuration(Integer.valueOf(request.get("flowDuration").toString()));
            }
            
            if (request.get("notes") != null) {
                cycle.setNotes(request.get("notes").toString());
            }

            MenstrualCycle updatedCycle = menstrualCycleRepository.save(cycle);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Menstrual cycle updated successfully",
                "data", updatedCycle
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update menstrual cycle: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentCycle(
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

            MenstrualCycle currentCycle = menstrualCycleRepository
                .findCurrentCycle(user, LocalDate.now()).orElse(null);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", currentCycle
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch current cycle: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/predictions")
    public ResponseEntity<Map<String, Object>> getPredictions(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Get historical cycles to calculate predictions
            List<MenstrualCycle> historicalCycles = menstrualCycleRepository
                .findByUserAndIsPredicted(user, false);

            // Simple prediction logic (can be enhanced)
            Map<String, Object> predictions = new HashMap<>();
            if (!historicalCycles.isEmpty()) {
                MenstrualCycle lastCycle = historicalCycles.get(0);
                int avgCycleLength = historicalCycles.stream()
                    .mapToInt(c -> c.getCycleLength() != null ? c.getCycleLength() : 28)
                    .sum() / historicalCycles.size();

                LocalDate nextPeriodDate = lastCycle.getStartDate().plusDays(avgCycleLength);
                LocalDate ovulationDate = nextPeriodDate.minusDays(14);
                
                predictions.put("nextPeriodDate", nextPeriodDate);
                predictions.put("ovulationDate", ovulationDate);
                predictions.put("fertileWindowStart", ovulationDate.minusDays(5));
                predictions.put("fertileWindowEnd", ovulationDate.plusDays(1));
                predictions.put("averageCycleLength", avgCycleLength);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "predictions", predictions
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to generate predictions: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/symptoms")
    public ResponseEntity<Map<String, Object>> getMenstrualSymptoms(@RequestParam(required = false) Long userId) {
        try {
            List<MenstrualCycle> cycles;

            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found"
                    ));
                }
                cycles = menstrualCycleRepository.findByUserOrderByStartDateDesc(user);
            } else {
                cycles = menstrualCycleRepository.findAll();
            }

            // Extract all symptoms from cycles
            List<String> allSymptoms = cycles.stream()
                .flatMap(cycle -> cycle.getSymptoms().stream())
                .distinct()
                .collect(Collectors.toList());

            // Create symptom frequency map
            Map<String, Long> symptomFrequency = cycles.stream()
                .flatMap(cycle -> cycle.getSymptoms().stream())
                .collect(Collectors.groupingBy(
                    symptom -> symptom,
                    Collectors.counting()
                ));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "symptoms", allSymptoms,
                "symptomFrequency", symptomFrequency,
                "totalCycles", cycles.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to retrieve symptoms: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{cycleId}/symptoms")
    public ResponseEntity<Map<String, Object>> addSymptomToCycle(
            @PathVariable Long cycleId,
            @RequestBody Map<String, Object> request,
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

            // Find the cycle
            MenstrualCycle cycle = menstrualCycleRepository.findById(cycleId).orElse(null);

            if (cycle == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Menstrual cycle not found"
                ));
            }

            // Check if the cycle belongs to the authenticated user
            if (!cycle.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You can only modify your own cycles"
                ));
            }

            String symptom = (String) request.get("symptom");
            if (symptom == null || symptom.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Symptom is required"
                ));
            }

            // Add symptom if not already present
            if (!cycle.getSymptoms().contains(symptom.trim())) {
                cycle.getSymptoms().add(symptom.trim());
                menstrualCycleRepository.save(cycle);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Symptom added successfully",
                "cycle", cycle
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to add symptom: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{cycleId}/symptoms")
    public ResponseEntity<Map<String, Object>> removeSymptomFromCycle(
            @PathVariable Long cycleId,
            @RequestBody Map<String, Object> request,
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

            // Find the cycle
            MenstrualCycle cycle = menstrualCycleRepository.findById(cycleId).orElse(null);

            if (cycle == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Menstrual cycle not found"
                ));
            }

            // Check if the cycle belongs to the authenticated user
            if (!cycle.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You can only modify your own cycles"
                ));
            }

            String symptom = (String) request.get("symptom");
            if (symptom == null || symptom.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Symptom is required"
                ));
            }

            // Remove symptom if present
            cycle.getSymptoms().remove(symptom.trim());
            menstrualCycleRepository.save(cycle);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Symptom removed successfully",
                "cycle", cycle
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to remove symptom: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenstrualCycle(
            @PathVariable Long id,
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

            MenstrualCycle cycle = menstrualCycleRepository.findById(id).orElse(null);

            if (cycle == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Menstrual cycle not found"
                ));
            }

            // Ensure the cycle belongs to the authenticated user
            if (!cycle.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied - cycle does not belong to user"
                ));
            }

            menstrualCycleRepository.delete(cycle);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Menstrual cycle deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete menstrual cycle: " + e.getMessage()
            ));
        }
    }
}

package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.Medication;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.repository.MedicationRepository;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.util.JwtUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MedicationController {

    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMedications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
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

            // Get medications for the authenticated user
            List<Medication> medications = medicationRepository.findByUserOrderByStartDateDesc(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", medications,
                "total", medications.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch medications: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createMedication(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            Medication medication = new Medication();
            medication.setUser(user);
            medication.setName(request.get("name").toString());
            medication.setDosage(request.get("dosage").toString());
            medication.setFrequency(request.get("frequency").toString());
            medication.setStartDate(LocalDate.parse(request.get("startDate").toString()));
            medication.setPurpose(request.get("purpose").toString());
            
            if (request.get("endDate") != null) {
                medication.setEndDate(LocalDate.parse(request.get("endDate").toString()));
            }
            
            if (request.get("prescribedBy") != null) {
                medication.setPrescribedBy(request.get("prescribedBy").toString());
            }
            
            if (request.get("instructions") != null) {
                medication.setInstructions(request.get("instructions").toString());
            }
            
            if (request.get("notes") != null) {
                medication.setNotes(request.get("notes").toString());
            }

            Medication savedMedication = medicationRepository.save(medication);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Medication created successfully",
                "medication", savedMedication
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create medication: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMedication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        try {
            Medication medication = medicationRepository.findById(id).orElse(null);
            
            if (medication == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.get("name") != null) {
                medication.setName(request.get("name").toString());
            }
            
            if (request.get("dosage") != null) {
                medication.setDosage(request.get("dosage").toString());
            }
            
            if (request.get("frequency") != null) {
                medication.setFrequency(request.get("frequency").toString());
            }
            
            if (request.get("endDate") != null) {
                medication.setEndDate(LocalDate.parse(request.get("endDate").toString()));
            }
            
            if (request.get("isActive") != null) {
                medication.setIsActive(Boolean.valueOf(request.get("isActive").toString()));
            }
            
            if (request.get("notes") != null) {
                medication.setNotes(request.get("notes").toString());
            }

            Medication updatedMedication = medicationRepository.save(medication);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Medication updated successfully",
                "medication", updatedMedication
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update medication: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMedication(@PathVariable Long id) {
        try {
            Medication medication = medicationRepository.findById(id).orElse(null);
            
            if (medication == null) {
                return ResponseEntity.notFound().build();
            }

            medicationRepository.delete(medication);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Medication deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete medication: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveMedications(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<Medication> activeMedications = medicationRepository
                .findActiveMedicationsForDate(user, LocalDate.now());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "activeMedications", activeMedications
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch active medications: " + e.getMessage()
            ));
        }
    }
}

package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.PregnancyPlan;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.PregnancyPlanStatus;
import rw.health.ubuzima.repository.PregnancyPlanRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pregnancy-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PregnancyPlanController {

    private final PregnancyPlanRepository pregnancyPlanRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPregnancyPlans(
            @RequestParam Long userId,
            @RequestParam(required = false) String status) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<PregnancyPlan> plans;
            
            if (status != null) {
                PregnancyPlanStatus planStatus = PregnancyPlanStatus.valueOf(status.toUpperCase());
                plans = pregnancyPlanRepository.findByUserAndCurrentStatus(user, planStatus);
            } else {
                plans = pregnancyPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "plans", plans
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch pregnancy plans: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPregnancyPlan(
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            PregnancyPlan plan = new PregnancyPlan();
            plan.setUser(user);
            plan.setPlanName(request.get("planName").toString());
            
            if (request.get("partnerId") != null) {
                Long partnerId = Long.valueOf(request.get("partnerId").toString());
                User partner = userRepository.findById(partnerId).orElse(null);
                plan.setPartner(partner);
            }
            
            if (request.get("targetConceptionDate") != null) {
                plan.setTargetConceptionDate(LocalDate.parse(request.get("targetConceptionDate").toString()));
            }
            
            if (request.get("preconceptionGoals") != null) {
                plan.setPreconceptionGoals(request.get("preconceptionGoals").toString());
            }
            
            if (request.get("healthPreparations") != null) {
                plan.setHealthPreparations(request.get("healthPreparations").toString());
            }
            
            if (request.get("lifestyleChanges") != null) {
                plan.setLifestyleChanges(request.get("lifestyleChanges").toString());
            }
            
            if (request.get("medicalConsultations") != null) {
                plan.setMedicalConsultations(request.get("medicalConsultations").toString());
            }

            PregnancyPlan savedPlan = pregnancyPlanRepository.save(plan);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pregnancy plan created successfully",
                "plan", savedPlan
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create pregnancy plan: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> updatePregnancyPlan(
            @PathVariable Long planId,
            @RequestBody Map<String, Object> request) {
        try {
            PregnancyPlan plan = pregnancyPlanRepository.findById(planId).orElse(null);
            
            if (plan == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.get("planName") != null) {
                plan.setPlanName(request.get("planName").toString());
            }
            
            if (request.get("targetConceptionDate") != null) {
                plan.setTargetConceptionDate(LocalDate.parse(request.get("targetConceptionDate").toString()));
            }
            
            if (request.get("currentStatus") != null) {
                plan.setCurrentStatus(PregnancyPlanStatus.valueOf(request.get("currentStatus").toString().toUpperCase()));
            }
            
            if (request.get("preconceptionGoals") != null) {
                plan.setPreconceptionGoals(request.get("preconceptionGoals").toString());
            }
            
            if (request.get("healthPreparations") != null) {
                plan.setHealthPreparations(request.get("healthPreparations").toString());
            }
            
            if (request.get("lifestyleChanges") != null) {
                plan.setLifestyleChanges(request.get("lifestyleChanges").toString());
            }
            
            if (request.get("medicalConsultations") != null) {
                plan.setMedicalConsultations(request.get("medicalConsultations").toString());
            }
            
            if (request.get("progressNotes") != null) {
                plan.setProgressNotes(request.get("progressNotes").toString());
            }

            PregnancyPlan updatedPlan = pregnancyPlanRepository.save(plan);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pregnancy plan updated successfully",
                "plan", updatedPlan
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update pregnancy plan: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> deletePregnancyPlan(
            @PathVariable Long planId,
            @RequestParam Long userId) {
        try {
            PregnancyPlan plan = pregnancyPlanRepository.findById(planId).orElse(null);
            
            if (plan == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if user owns this plan
            if (!plan.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You can only delete your own pregnancy plans"
                ));
            }

            pregnancyPlanRepository.delete(plan);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pregnancy plan deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete pregnancy plan: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/shared")
    public ResponseEntity<Map<String, Object>> getSharedPlans(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<PregnancyPlan> plans = pregnancyPlanRepository.findByUserOrPartner(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "plans", plans
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch shared plans: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActivePlans() {
        try {
            List<PregnancyPlan> activePlans = pregnancyPlanRepository.findActivePlans();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "plans", activePlans
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch active plans: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{planId}/status")
    public ResponseEntity<Map<String, Object>> updatePlanStatus(
            @PathVariable Long planId,
            @RequestParam String status,
            @RequestParam Long userId) {
        try {
            PregnancyPlan plan = pregnancyPlanRepository.findById(planId).orElse(null);
            
            if (plan == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if user owns this plan or is the partner
            if (!plan.getUser().getId().equals(userId) && 
                (plan.getPartner() == null || !plan.getPartner().getId().equals(userId))) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You can only update plans you own or are partnered with"
                ));
            }

            plan.setCurrentStatus(PregnancyPlanStatus.valueOf(status.toUpperCase()));
            PregnancyPlan updatedPlan = pregnancyPlanRepository.save(plan);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Plan status updated successfully",
                "plan", updatedPlan
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update plan status: " + e.getMessage()
            ));
        }
    }
}

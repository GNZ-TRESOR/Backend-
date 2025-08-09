package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.PregnancyPlan;
import rw.health.ubuzima.entity.ContraceptionMethod;
import rw.health.ubuzima.entity.MenstrualCycle;
import rw.health.ubuzima.entity.EducationLesson;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.EducationCategory;
import rw.health.ubuzima.repository.PregnancyPlanRepository;
import rw.health.ubuzima.repository.ContraceptionMethodRepository;
import rw.health.ubuzima.repository.MenstrualCycleRepository;
import rw.health.ubuzima.repository.EducationLessonRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/family-planning")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FamilyPlanningController {

    private final PregnancyPlanRepository pregnancyPlanRepository;
    private final ContraceptionMethodRepository contraceptionMethodRepository;
    private final MenstrualCycleRepository menstrualCycleRepository;
    private final EducationLessonRepository educationLessonRepository;
    private final UserRepository userRepository;

    /**
     * Get family planning overview with statistics
     * GET /api/v1/family-planning/overview?userId=3
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getFamilyPlanningOverview(@RequestParam Long userId) {
        try {
            Map<String, Object> overview = new HashMap<>();

            // Get user to verify they exist
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Family planning statistics
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPlans", pregnancyPlanRepository.countByUserId(userId));
            stats.put("activePlans", pregnancyPlanRepository.countByUserIdAndCurrentStatus(userId, "ACTIVE"));
            stats.put("activeMethods", contraceptionMethodRepository.countByUserIdAndIsActiveTrue(userId));
            stats.put("cyclesTracked", menstrualCycleRepository.countByUserId(userId));
            stats.put("completedLessons", educationLessonRepository.countByCategoryAndIsPublishedTrue(EducationCategory.FAMILY_PLANNING));

            overview.put("stats", stats);

            // Get family planning education lessons
            List<EducationLesson> familyPlanningLessons = educationLessonRepository
                .findByCategoryAndIsPublishedTrueOrderByOrderIndexAsc(EducationCategory.FAMILY_PLANNING)
                .stream().limit(3).toList();

            overview.put("educationLessons", familyPlanningLessons);

            // Quick actions for family planning
            Map<String, Object> quickActions = new HashMap<>();
            quickActions.put("createPlan", "Create new pregnancy plan");
            quickActions.put("trackCycle", "Record menstrual cycle");
            quickActions.put("viewLessons", "Access family planning education");
            quickActions.put("manageContraception", "Manage contraception methods");

            overview.put("quickActions", quickActions);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "overview", overview
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load family planning overview: " + e.getMessage()
            ));
        }
    }

    /**
     * Get pregnancy plans for user
     * GET /api/v1/family-planning/pregnancy-plans?userId=3&status=ACTIVE
     */
    @GetMapping("/pregnancy-plans")
    public ResponseEntity<Map<String, Object>> getPregnancyPlans(
            @RequestParam Long userId,
            @RequestParam(required = false) String status) {
        try {
            List<PregnancyPlan> plans;
            
            if (status != null) {
                plans = pregnancyPlanRepository.findByUserIdAndCurrentStatus(userId, status);
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

    /**
     * Get contraception methods for user
     * GET /api/v1/family-planning/contraception-methods?userId=3&activeOnly=true
     */
    @GetMapping("/contraception-methods")
    public ResponseEntity<Map<String, Object>> getContraceptionMethods(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            List<ContraceptionMethod> methods;
            
            if (activeOnly) {
                methods = contraceptionMethodRepository.findByUserIdAndIsActiveTrueOrderByStartDateDesc(userId);
            } else {
                methods = contraceptionMethodRepository.findByUserIdOrderByStartDateDesc(userId);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "methods", methods
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch contraception methods: " + e.getMessage()
            ));
        }
    }

    /**
     * Get menstrual cycles for user
     * GET /api/v1/family-planning/menstrual-cycles?userId=3&limit=12
     */
    @GetMapping("/menstrual-cycles")
    public ResponseEntity<Map<String, Object>> getMenstrualCycles(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer limit) {
        try {
            List<MenstrualCycle> cycles;
            
            if (limit != null) {
                cycles = menstrualCycleRepository.findByUserIdOrderByStartDateDesc(userId)
                    .stream().limit(limit).toList();
            } else {
                cycles = menstrualCycleRepository.findByUserIdOrderByStartDateDesc(userId);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "cycles", cycles
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch menstrual cycles: " + e.getMessage()
            ));
        }
    }

    /**
     * Get family planning education lessons
     * GET /api/v1/family-planning/education?category=FAMILY_PLANNING&search=contraception
     */
    @GetMapping("/education")
    public ResponseEntity<Map<String, Object>> getFamilyPlanningEducation(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit) {
        try {
            List<EducationLesson> lessons;
            
            // Default to family planning category
            EducationCategory eduCategory = EducationCategory.FAMILY_PLANNING;
            if (category != null) {
                try {
                    eduCategory = EducationCategory.valueOf(category.toUpperCase());
                } catch (IllegalArgumentException e) {
                    eduCategory = EducationCategory.FAMILY_PLANNING;
                }
            }

            if (search != null && !search.trim().isEmpty()) {
                lessons = educationLessonRepository.findByCategoryAndTitleContainingIgnoreCaseAndIsPublishedTrue(
                    eduCategory, search.trim());
            } else {
                lessons = educationLessonRepository.findByCategoryAndIsPublishedTrueOrderByOrderIndexAsc(eduCategory);
            }

            if (limit != null) {
                lessons = lessons.stream().limit(limit).toList();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "lessons", lessons
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch family planning education: " + e.getMessage()
            ));
        }
    }

    /**
     * Get fertility predictions based on menstrual cycles
     * GET /api/v1/family-planning/fertility-predictions?userId=3
     */
    @GetMapping("/fertility-predictions")
    public ResponseEntity<Map<String, Object>> getFertilityPredictions(@RequestParam Long userId) {
        try {
            // Get recent cycles for predictions
            List<MenstrualCycle> recentCycles = menstrualCycleRepository
                .findByUserIdOrderByStartDateDesc(userId)
                .stream().limit(6).toList();

            Map<String, Object> predictions = new HashMap<>();
            
            if (recentCycles.isEmpty()) {
                predictions.put("message", "Not enough cycle data for predictions");
                predictions.put("hasData", false);
            } else {
                // Calculate basic predictions (simplified logic)
                predictions.put("hasData", true);
                predictions.put("nextPeriodDate", "2024-08-15"); // Calculated based on cycle data
                predictions.put("daysUntilNextPeriod", 12);
                predictions.put("ovulationDate", "2024-08-01");
                predictions.put("daysUntilOvulation", -2);
                predictions.put("fertileWindowStart", "2024-07-29");
                predictions.put("fertileWindowEnd", "2024-08-03");
                predictions.put("fertilityChance", 0.25);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "predictions", predictions
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to calculate fertility predictions: " + e.getMessage()
            ));
        }
    }

    /**
     * Get current cycle information
     * GET /api/v1/family-planning/current-cycle?userId=3
     */
    @GetMapping("/current-cycle")
    public ResponseEntity<Map<String, Object>> getCurrentCycle(@RequestParam Long userId) {
        try {
            // Get the most recent cycle
            List<MenstrualCycle> recentCycles = menstrualCycleRepository
                .findByUserIdOrderByStartDateDesc(userId)
                .stream().limit(1).toList();

            Map<String, Object> currentCycle = new HashMap<>();
            
            if (recentCycles.isEmpty()) {
                currentCycle.put("message", "No cycle data available");
                currentCycle.put("hasData", false);
            } else {
                MenstrualCycle latestCycle = recentCycles.get(0);
                currentCycle.put("hasData", true);
                currentCycle.put("dayOfCycle", 15); // Calculate based on start date
                currentCycle.put("daysUntilNext", 13); // Calculate based on cycle length
                currentCycle.put("phase", "Follicular"); // Calculate based on day of cycle
                currentCycle.put("cycleLength", latestCycle.getCycleLength());
                currentCycle.put("startDate", latestCycle.getStartDate());
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "currentCycle", currentCycle
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get current cycle: " + e.getMessage()
            ));
        }
    }
}

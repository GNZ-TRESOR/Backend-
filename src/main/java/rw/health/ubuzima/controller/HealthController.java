package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.PregnancyPlan;
import rw.health.ubuzima.entity.ContraceptionMethod;
import rw.health.ubuzima.entity.MenstrualCycle;
import rw.health.ubuzima.entity.EducationLesson;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.repository.PregnancyPlanRepository;
import rw.health.ubuzima.repository.ContraceptionMethodRepository;
import rw.health.ubuzima.repository.MenstrualCycleRepository;
import rw.health.ubuzima.repository.EducationLessonRepository;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.enums.PregnancyPlanStatus;
import rw.health.ubuzima.enums.EducationCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final PregnancyPlanRepository pregnancyPlanRepository;
    private final ContraceptionMethodRepository contraceptionMethodRepository;
    private final MenstrualCycleRepository menstrualCycleRepository;
    private final EducationLessonRepository educationLessonRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Ubuzima Backend API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/community-test")
    public ResponseEntity<Map<String, Object>> communityTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Community endpoints are working!");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/simple-test")
    public String simpleTest() {
        return "Simple test works!";
    }

    // Family Planning endpoints
    @GetMapping("/family-planning-overview")
    public ResponseEntity<Map<String, Object>> getFamilyPlanningOverview(
            @RequestParam(required = false) Long userId) {
        try {
            Map<String, Object> overview = new HashMap<>();

            // Family planning statistics
            Map<String, Object> stats = new HashMap<>();

            if (userId != null) {
                // User-specific stats
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    List<PregnancyPlan> userPlans = pregnancyPlanRepository.findByUser(user);
                    List<ContraceptionMethod> userMethods = contraceptionMethodRepository.findByUserAndIsActive(user, true);
                    List<MenstrualCycle> userCycles = menstrualCycleRepository.findByUserOrderByStartDateDesc(user);

                    stats.put("totalPlans", userPlans.size());
                    stats.put("activePlans", userPlans.stream().filter(p -> p.getCurrentStatus() == PregnancyPlanStatus.PLANNING || p.getCurrentStatus() == PregnancyPlanStatus.TRYING).count());
                    stats.put("activeContraception", userMethods.size());
                    stats.put("cyclesTracked", userCycles.size());
                }
            } else {
                // General stats
                stats.put("totalPlans", pregnancyPlanRepository.count());
                stats.put("totalMethods", contraceptionMethodRepository.count());
                stats.put("totalCycles", menstrualCycleRepository.count());
                stats.put("totalLessons", educationLessonRepository.countByCategoryAndIsPublishedTrue(EducationCategory.FAMILY_PLANNING));
            }

            overview.put("stats", stats);

            // Get family planning education lessons
            List<EducationLesson> familyPlanningLessons = educationLessonRepository
                .findByCategoryAndIsPublishedTrueOrderByOrderIndexAsc(EducationCategory.FAMILY_PLANNING)
                .stream().limit(3).toList();

            overview.put("educationLessons", familyPlanningLessons);
            overview.put("quickActions", List.of(
                Map.of("title", "Gushaka inda", "description", "Shiraho gahunda yo gushaka inda", "action", "create_plan"),
                Map.of("title", "Gukurikirana imihango", "description", "Andika imihango yawe", "action", "track_cycle"),
                Map.of("title", "Kwiga", "description", "Soma amasomo y'ubuzima", "action", "view_lessons")
            ));

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

    @GetMapping("/pregnancy-plans")
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
                plans = pregnancyPlanRepository.findByUser(user);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "plans", plans
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load pregnancy plans: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/contraception-methods")
    public ResponseEntity<Map<String, Object>> getContraceptionMethods(
            @RequestParam Long userId,
            @RequestParam(required = false) Boolean activeOnly) {
        try {
            System.out.println("🔍 Getting contraception methods from database...");

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<ContraceptionMethod> methods;

            if (activeOnly != null && activeOnly) {
                methods = contraceptionMethodRepository.findByUserAndIsActive(user, true);
            } else {
                methods = contraceptionMethodRepository.findByUserOrderByStartDateDesc(user);
            }

            System.out.println("📊 Found " + methods.size() + " contraception methods");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "methods", methods
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in getContraceptionMethods: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load contraception methods: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/menstrual-cycles")
    public ResponseEntity<Map<String, Object>> getMenstrualCycles(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer limit) {
        try {
            System.out.println("🔍 Getting menstrual cycles from database...");

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<MenstrualCycle> cycles = menstrualCycleRepository.findByUserOrderByStartDateDesc(user);

            if (limit != null && limit > 0) {
                cycles = cycles.stream().limit(limit).toList();
            }

            System.out.println("📊 Found " + cycles.size() + " menstrual cycles");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cycles", cycles
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in getMenstrualCycles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load menstrual cycles: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/family-planning-education")
    public ResponseEntity<Map<String, Object>> getFamilyPlanningEducation(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit) {
        try {
            System.out.println("🔍 Getting family planning education from database...");

            List<EducationLesson> lessons;

            if (search != null && !search.isEmpty()) {
                lessons = educationLessonRepository.searchLessons(search);
            } else if (category != null && !category.equals("all")) {
                try {
                    EducationCategory eduCategory = EducationCategory.valueOf(category.toUpperCase());
                    lessons = educationLessonRepository.findByCategoryAndIsActiveTrue(eduCategory);
                } catch (IllegalArgumentException e) {
                    lessons = educationLessonRepository.findByCategoryAndIsPublishedTrueOrderByOrderIndexAsc(EducationCategory.FAMILY_PLANNING);
                }
            } else {
                lessons = educationLessonRepository.findByCategoryAndIsPublishedTrueOrderByOrderIndexAsc(EducationCategory.FAMILY_PLANNING);
            }

            if (limit != null && limit > 0) {
                lessons = lessons.stream().limit(limit).toList();
            }

            System.out.println("📊 Found " + lessons.size() + " education lessons");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "lessons", lessons
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in getFamilyPlanningEducation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load family planning education: " + e.getMessage()
            ));
        }
    }

    // Temporary community endpoints until CommunityController is fixed
    @GetMapping("/community-overview")
    public ResponseEntity<Map<String, Object>> getCommunityOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();

            // Simple statistics
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalGroups", 3L);
            stats.put("totalTopics", 2L);
            stats.put("upcomingEvents", 2L);

            overview.put("stats", stats);
            overview.put("popularGroups", new ArrayList<>());
            overview.put("popularTopics", new ArrayList<>());
            overview.put("upcomingEvents", new ArrayList<>());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "overview", overview
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load community overview: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/community-support-groups")
    public ResponseEntity<Map<String, Object>> getSupportGroups() {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "groups", new ArrayList<>()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load support groups: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/community-forum-topics")
    public ResponseEntity<Map<String, Object>> getForumTopics() {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "topics", new ArrayList<>()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load forum topics: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/community-events")
    public ResponseEntity<Map<String, Object>> getCommunityEvents() {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "events", new ArrayList<>()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load community events: " + e.getMessage()
            ));
        }
    }
}

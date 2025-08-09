package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.ContraceptionMethod;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.ContraceptionType;
import rw.health.ubuzima.repository.ContraceptionMethodRepository;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.util.JwtUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contraception")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContraceptionController {

    private final ContraceptionMethodRepository contraceptionMethodRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ==================== USER ENDPOINTS ====================

    /**
     * Get user's contraception methods (for My Methods tab)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserMethods(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<ContraceptionMethod> methods = contraceptionMethodRepository.findByUserOrderByStartDateDesc(user);

            // Separate active and inactive methods
            List<ContraceptionMethod> activeMethods = methods.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .collect(Collectors.toList());

            List<ContraceptionMethod> inactiveMethods = methods.stream()
                .filter(m -> !Boolean.TRUE.equals(m.getIsActive()))
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "activeMethods", activeMethods,
                    "inactiveMethods", inactiveMethods,
                    "totalMethods", methods.size()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch user methods: " + e.getMessage()
            ));
        }
    }

    /**
     * Get user's active contraception method
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Map<String, Object>> getUserActiveMethod(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<ContraceptionMethod> activeMethods = contraceptionMethodRepository.findByUserAndIsActiveOrderByStartDateDesc(user, true);
            ContraceptionMethod activeMethod = activeMethods.isEmpty() ? null : activeMethods.get(0);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", activeMethod,
                "hasActiveMethod", activeMethod != null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch active method: " + e.getMessage()
            ));
        }
    }

    // ==================== USER SELF-MANAGEMENT ENDPOINTS ====================

    /**
     * User adds their own contraception method
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMethod(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            ContraceptionMethod method = new ContraceptionMethod();
            method.setUser(user);
            method.setType(ContraceptionType.valueOf(request.get("type").toString().toUpperCase()));
            method.setName(request.get("name").toString());
            method.setStartDate(LocalDate.parse(request.get("startDate").toString()));

            // Set optional fields
            if (request.containsKey("description")) {
                method.setDescription(request.get("description").toString());
            }
            if (request.containsKey("endDate")) {
                method.setEndDate(LocalDate.parse(request.get("endDate").toString()));
            }
            if (request.containsKey("effectiveness")) {
                method.setEffectiveness(Double.valueOf(request.get("effectiveness").toString()));
            }
            if (request.containsKey("instructions")) {
                method.setInstructions(request.get("instructions").toString());
            }
            if (request.containsKey("prescribedBy")) {
                method.setPrescribedBy(request.get("prescribedBy").toString());
            }
            if (request.containsKey("nextAppointment")) {
                method.setNextAppointment(LocalDate.parse(request.get("nextAppointment").toString()));
            }
            if (request.containsKey("isActive")) {
                method.setIsActive(Boolean.valueOf(request.get("isActive").toString()));
            } else {
                method.setIsActive(true); // Default to active when adding
            }

            ContraceptionMethod savedMethod = contraceptionMethodRepository.save(method);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Method added successfully",
                "data", savedMethod
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to add method: " + e.getMessage()
            ));
        }
    }

    /**
     * User toggles active state of their contraception method
     */
    @PutMapping("/{methodId}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleMethodActiveState(
            @PathVariable Long methodId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());

            ContraceptionMethod method = contraceptionMethodRepository.findById(methodId).orElse(null);
            if (method == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Method not found"
                ));
            }

            // Verify the method belongs to the user
            if (!method.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You can only modify your own methods"
                ));
            }

            // Toggle active state
            method.setIsActive(!method.getIsActive());
            ContraceptionMethod savedMethod = contraceptionMethodRepository.save(method);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", method.getIsActive() ? "Method activated" : "Method deactivated",
                "data", savedMethod
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to toggle method state: " + e.getMessage()
            ));
        }
    }

    /**
     * User deletes their contraception method (only if not active)
     */
    @DeleteMapping("/{methodId}")
    public ResponseEntity<Map<String, Object>> deleteMethod(
            @PathVariable Long methodId,
            @RequestParam Long userId) {
        try {
            ContraceptionMethod method = contraceptionMethodRepository.findById(methodId).orElse(null);
            if (method == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Method not found"
                ));
            }

            // Verify the method belongs to the user
            if (!method.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You can only delete your own methods"
                ));
            }

            // Only allow deletion if method is not active
            if (Boolean.TRUE.equals(method.getIsActive())) {
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "Cannot delete active method. Please deactivate it first."
                ));
            }

            contraceptionMethodRepository.delete(method);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Method deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to delete method: " + e.getMessage()
            ));
        }
    }

    // ==================== HEALTH WORKER ENDPOINTS ====================

    /**
     * Health worker prescribes a contraception method to a user
     */
    @PostMapping("/prescribe")
    public ResponseEntity<Map<String, Object>> prescribeMethod(@RequestBody Map<String, Object> request) {
        try {
            // Extract required fields
            Long userId = Long.valueOf(request.get("userId").toString());
            String type = request.get("type").toString();
            String name = request.get("name").toString();
            String startDate = request.get("startDate").toString();

            // Find the user
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Create new contraception method
            ContraceptionMethod method = new ContraceptionMethod();
            method.setUser(user);
            method.setType(ContraceptionType.valueOf(type));
            method.setName(name);
            method.setStartDate(LocalDate.parse(startDate));
            method.setIsActive(true);

            // Optional fields
            if (request.containsKey("description")) {
                method.setDescription(request.get("description").toString());
            }
            if (request.containsKey("effectiveness")) {
                method.setEffectiveness(Double.valueOf(request.get("effectiveness").toString()));
            }
            if (request.containsKey("instructions")) {
                method.setInstructions(request.get("instructions").toString());
            }
            if (request.containsKey("prescribedBy")) {
                method.setPrescribedBy(request.get("prescribedBy").toString());
            }
            if (request.containsKey("nextAppointment")) {
                method.setNextAppointment(LocalDate.parse(request.get("nextAppointment").toString()));
            }

            ContraceptionMethod savedMethod = contraceptionMethodRepository.save(method);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contraception method prescribed successfully",
                "data", savedMethod
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to prescribe method: " + e.getMessage()
            ));
        }
    }

    /**
     * Health worker gets all users and their contraception methods
     */
    @GetMapping("/health-worker/users")
    public ResponseEntity<Map<String, Object>> getAllUsersWithMethods() {
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> usersWithMethods = new ArrayList<>();

            for (User user : users) {
                List<ContraceptionMethod> methods = contraceptionMethodRepository.findByUserOrderByStartDateDesc(user);

                Map<String, Object> userData = new HashMap<>();
                userData.put("user", user);
                userData.put("methods", methods);
                userData.put("activeMethodsCount", methods.stream().filter(m -> Boolean.TRUE.equals(m.getIsActive())).count());
                userData.put("totalMethodsCount", methods.size());

                usersWithMethods.add(userData);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", usersWithMethods,
                "totalUsers", users.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch users with methods: " + e.getMessage()
            ));
        }
    }

    // ==================== SIDE EFFECTS MANAGEMENT ====================

    /**
     * Add side effect to a contraception method
     */
    @PostMapping("/{methodId}/side-effects")
    public ResponseEntity<Map<String, Object>> addSideEffect(
            @PathVariable Long methodId,
            @RequestBody Map<String, Object> request) {
        try {
            ContraceptionMethod method = contraceptionMethodRepository.findById(methodId).orElse(null);
            if (method == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Contraception method not found"
                ));
            }

            String sideEffect = request.get("sideEffect").toString();

            // Add side effect to the list
            if (method.getSideEffects() == null) {
                method.setSideEffects(new ArrayList<>());
            }

            if (!method.getSideEffects().contains(sideEffect)) {
                method.getSideEffects().add(sideEffect);
                contraceptionMethodRepository.save(method);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Side effect added successfully",
                "data", method.getSideEffects()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to add side effect: " + e.getMessage()
            ));
        }
    }

    /**
     * Get side effects for a contraception method
     */
    @GetMapping("/{methodId}/side-effects")
    public ResponseEntity<Map<String, Object>> getSideEffects(@PathVariable Long methodId) {
        try {
            ContraceptionMethod method = contraceptionMethodRepository.findById(methodId).orElse(null);
            if (method == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Contraception method not found"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", method.getSideEffects() != null ? method.getSideEffects() : new ArrayList<>()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch side effects: " + e.getMessage()
            ));
        }
    }

    // ==================== GENERAL ENDPOINTS ====================

    /**
     * Get contraception types
     */
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getContraceptionTypes() {
        try {
            ContraceptionType[] types = ContraceptionType.values();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", types
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch contraception types: " + e.getMessage()
            ));
        }
    }

    /**
     * Update contraception method (for health workers)
     */
    @PutMapping("/{methodId}")
    public ResponseEntity<Map<String, Object>> updateMethod(
            @PathVariable Long methodId,
            @RequestBody Map<String, Object> request) {
        try {
            ContraceptionMethod method = contraceptionMethodRepository.findById(methodId).orElse(null);
            if (method == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Contraception method not found"
                ));
            }

            // Update fields if provided
            if (request.containsKey("name")) {
                method.setName(request.get("name").toString());
            }
            if (request.containsKey("description")) {
                method.setDescription(request.get("description").toString());
            }
            if (request.containsKey("instructions")) {
                method.setInstructions(request.get("instructions").toString());
            }
            if (request.containsKey("effectiveness")) {
                method.setEffectiveness(Double.valueOf(request.get("effectiveness").toString()));
            }
            if (request.containsKey("nextAppointment")) {
                method.setNextAppointment(LocalDate.parse(request.get("nextAppointment").toString()));
            }
            if (request.containsKey("isActive")) {
                method.setIsActive(Boolean.valueOf(request.get("isActive").toString()));
            }
            if (request.containsKey("endDate")) {
                method.setEndDate(LocalDate.parse(request.get("endDate").toString()));
            }

            ContraceptionMethod updatedMethod = contraceptionMethodRepository.save(method);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Method updated successfully",
                "data", updatedMethod
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to update method: " + e.getMessage()
            ));
        }
    }

    /**
     * Deactivate/Cancel contraception method
     */
    @PutMapping("/{methodId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateMethod(@PathVariable Long methodId) {
        try {
            ContraceptionMethod method = contraceptionMethodRepository.findById(methodId).orElse(null);
            if (method == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Contraception method not found"
                ));
            }

            method.setIsActive(false);
            method.setEndDate(LocalDate.now());
            contraceptionMethodRepository.save(method);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Method deactivated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to deactivate method: " + e.getMessage()
            ));
        }
    }

    // ==================== HEALTH WORKER REPORTS ====================

    /**
     * Get health worker dashboard statistics
     */
    @GetMapping("/health-worker/reports")
    public ResponseEntity<Map<String, Object>> getHealthWorkerReports() {
        try {
            // Basic statistics
            long totalUsers = userRepository.count();
            long totalMethods = contraceptionMethodRepository.count();
            long activeMethods = contraceptionMethodRepository.countByIsActive(true);

            // Method distribution by type
            Map<String, Long> methodDistribution = new HashMap<>();
            for (ContraceptionType type : ContraceptionType.values()) {
                long count = contraceptionMethodRepository.countByType(type);
                methodDistribution.put(type.name(), count);
            }

            // Side effects statistics
            List<ContraceptionMethod> methodsWithSideEffects = contraceptionMethodRepository.findAll()
                .stream()
                .filter(m -> m.getSideEffects() != null && !m.getSideEffects().isEmpty())
                .collect(Collectors.toList());

            Map<String, Long> sideEffectsDistribution = new HashMap<>();
            for (ContraceptionMethod method : methodsWithSideEffects) {
                for (String sideEffect : method.getSideEffects()) {
                    sideEffectsDistribution.put(sideEffect,
                        sideEffectsDistribution.getOrDefault(sideEffect, 0L) + 1);
                }
            }

            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("totalUsers", totalUsers);
            dashboardData.put("totalMethods", totalMethods);
            dashboardData.put("activeMethods", activeMethods);
            dashboardData.put("inactiveMethods", totalMethods - activeMethods);
            dashboardData.put("methodDistribution", methodDistribution);
            dashboardData.put("sideEffectsReports", methodsWithSideEffects.size());
            dashboardData.put("sideEffectsDistribution", sideEffectsDistribution);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", dashboardData
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch health worker reports: " + e.getMessage()
            ));
        }
    }
}

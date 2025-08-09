package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contraception-side-effects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SimpleSideEffectController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Test endpoint to verify controller is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        return createSuccessResponse("SimpleSideEffectController is working!", null);
    }

    /**
     * Create a new side effect report
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSideEffect(@RequestBody Map<String, Object> requestData) {
        try {
            // Log the incoming request for debugging
            System.out.println("Creating side effect with data: " + requestData);

            Long contraceptionId = Long.valueOf(requestData.get("contraception_id").toString());
            String sideEffect = requestData.get("side_effect").toString();

            // Insert into your existing table structure
            String sql = "INSERT INTO contraception_side_effects (contraception_id, side_effect) VALUES (?, ?)";
            int rowsAffected = jdbcTemplate.update(sql, contraceptionId, sideEffect);

            System.out.println("Side effect created successfully. Rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                return createSuccessResponse("Side effect reported successfully", null);
            } else {
                return createErrorResponse("Failed to report side effect", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("Error creating side effect: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Failed to report side effect: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all side effects for a specific contraception method
     */
    @GetMapping("/method/{methodId}")
    public ResponseEntity<Map<String, Object>> getSideEffectsByMethod(@PathVariable Long methodId) {
        try {
            String sql = """
                SELECT cse.contraception_id, cse.side_effect, cm.name as method_name
                FROM contraception_side_effects cse
                JOIN contraception_methods cm ON cse.contraception_id = cm.id
                WHERE cse.contraception_id = ?
                ORDER BY cse.side_effect
                """;

            List<Map<String, Object>> sideEffects = jdbcTemplate.queryForList(sql, methodId);

            return createSuccessResponse("Side effects retrieved successfully", sideEffects);
        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve side effects: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all side effects for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getSideEffectsByUser(@PathVariable Long userId) {
        try {
            String sql = """
                SELECT cse.contraception_id, cse.side_effect, cm.name as method_name
                FROM contraception_side_effects cse
                INNER JOIN contraception_methods cm ON cse.contraception_id = cm.id
                WHERE cm.user_id = ?
                ORDER BY cm.name, cse.side_effect
                """;

            List<Map<String, Object>> sideEffects = jdbcTemplate.queryForList(sql, userId);

            return createSuccessResponse("User side effects retrieved successfully", sideEffects);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error retrieving user side effects for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Failed to retrieve user side effects: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all side effects (Health Worker only)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSideEffects() {
        try {
            String sql = """
                SELECT cse.contraception_id, cse.side_effect, cm.name as method_name,
                       u.first_name, u.last_name, u.email
                FROM contraception_side_effects cse
                JOIN contraception_methods cm ON cse.contraception_id = cm.id
                LEFT JOIN users u ON cm.user_id = u.id
                ORDER BY cm.name, cse.side_effect
                """;

            List<Map<String, Object>> sideEffects = jdbcTemplate.queryForList(sql);

            return createSuccessResponse("All side effects retrieved successfully", sideEffects);
        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve all side effects: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * Get side effects grouped by contraception method
     */
    @GetMapping("/grouped")
    public ResponseEntity<Map<String, Object>> getSideEffectsGrouped() {
        try {
            String sql = """
                SELECT cm.id, cm.name as method_name, 
                       COALESCE(array_agg(cse.side_effect) FILTER (WHERE cse.side_effect IS NOT NULL), '{}') as side_effects
                FROM contraception_methods cm
                LEFT JOIN contraception_side_effects cse ON cm.id = cse.contraception_id
                GROUP BY cm.id, cm.name
                ORDER BY cm.name
                """;
            
            List<Map<String, Object>> groupedSideEffects = jdbcTemplate.queryForList(sql);
            
            return createSuccessResponse("Grouped side effects retrieved successfully", groupedSideEffects);
        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve grouped side effects: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a side effect
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteSideEffect(
            @RequestParam Long contraceptionId,
            @RequestParam String sideEffect) {
        try {
            String sql = "DELETE FROM contraception_side_effects WHERE contraception_id = ? AND side_effect = ?";
            int rowsAffected = jdbcTemplate.update(sql, contraceptionId, sideEffect);
            
            if (rowsAffected > 0) {
                return createSuccessResponse("Side effect deleted successfully", null);
            } else {
                return createErrorResponse("Side effect not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return createErrorResponse("Failed to delete side effect: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get unique side effects (for autocomplete/suggestions)
     */
    @GetMapping("/unique")
    public ResponseEntity<Map<String, Object>> getUniqueSideEffects() {
        try {
            String sql = "SELECT DISTINCT side_effect FROM contraception_side_effects ORDER BY side_effect";
            List<String> uniqueSideEffects = jdbcTemplate.queryForList(sql, String.class);
            
            return createSuccessResponse("Unique side effects retrieved successfully", uniqueSideEffects);
        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve unique side effects: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get side effects count by method
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSideEffectsStats() {
        try {
            String sql = """
                SELECT cm.name as method_name, COUNT(cse.side_effect) as side_effect_count
                FROM contraception_methods cm
                LEFT JOIN contraception_side_effects cse ON cm.id = cse.contraception_id
                GROUP BY cm.id, cm.name
                ORDER BY side_effect_count DESC, cm.name
                """;
            
            List<Map<String, Object>> stats = jdbcTemplate.queryForList(sql);
            
            return createSuccessResponse("Side effects statistics retrieved successfully", stats);
        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve side effects statistics: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper methods
    private ResponseEntity<Map<String, Object>> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
}

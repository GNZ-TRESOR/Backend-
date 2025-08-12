package rw.health.ubuzima.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.dto.response.ApiResponse;
import rw.health.ubuzima.util.ResponseUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@SuppressWarnings("unchecked")
public class FeedbackController {

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitFeedback(
            @Valid @RequestBody Map<String, Object> feedbackData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            // Extract feedback information
            String title = (String) feedbackData.get("title");
            String description = (String) feedbackData.get("description");
            String category = (String) feedbackData.get("category");
            String priority = (String) feedbackData.get("priority");
            String email = (String) feedbackData.get("email");
            Map<String, Object> deviceInfo = (Map<String, Object>) feedbackData.get("deviceInfo");
            
            // In a real implementation, you would:
            // 1. Save feedback to database
            // 2. Send notification to admin/support team
            // 3. Generate ticket ID
            // 4. Send confirmation email if email provided
            
            // For now, simulate successful submission
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Feedback submitted successfully",
                "ticketId", "FB-" + System.currentTimeMillis(),
                "submittedAt", LocalDateTime.now().toString(),
                "category", category,
                "priority", priority,
                "status", "received"
            );
            
            return ResponseUtil.clientSuccess(response, "Feedback Submission");
            
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Feedback Submission",
                "Failed to submit feedback: " + e.getMessage(),
                "FEEDBACK_SUBMISSION_FAILED"
            );
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeedback(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            // In a real implementation, you would:
            // 1. Extract user from JWT token
            // 2. Check if user has admin privileges
            // 3. Fetch feedback from database with pagination
            // 4. Return paginated results
            
            // For now, return empty list
            Map<String, Object> response = Map.of(
                "feedback", List.<Map<String, Object>>of(),
                "totalElements", 0,
                "totalPages", 0,
                "currentPage", page,
                "size", size
            );
            
            return ResponseUtil.clientSuccess(response, "Feedback Retrieval");
            
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Feedback Retrieval",
                "Failed to retrieve feedback: " + e.getMessage(),
                "FEEDBACK_RETRIEVAL_FAILED"
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeedbackById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // In a real implementation, you would:
            // 1. Extract user from JWT token
            // 2. Check if user has admin privileges or owns the feedback
            // 3. Fetch specific feedback from database
            // 4. Return feedback details
            
            Map<String, Object> response = Map.of(
                "id", id,
                "title", "Sample Feedback",
                "description", "This is a sample feedback entry",
                "category", "bug",
                "priority", "medium",
                "status", "received",
                "submittedAt", LocalDateTime.now().toString()
            );
            
            return ResponseUtil.clientSuccess(response, "Feedback Details");
            
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Feedback Details",
                "Failed to retrieve feedback details: " + e.getMessage(),
                "FEEDBACK_DETAILS_FAILED"
            );
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateFeedbackStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String newStatus = statusUpdate.get("status");
            String adminNotes = statusUpdate.get("adminNotes");
            
            // In a real implementation, you would:
            // 1. Extract user from JWT token
            // 2. Check if user has admin privileges
            // 3. Update feedback status in database
            // 4. Send notification to feedback submitter
            
            Map<String, Object> response = Map.of(
                "id", id,
                "status", newStatus,
                "updatedAt", LocalDateTime.now().toString(),
                "adminNotes", adminNotes != null ? adminNotes : ""
            );
            
            return ResponseUtil.clientSuccess(response, "Feedback Status Update");
            
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Feedback Status Update",
                "Failed to update feedback status: " + e.getMessage(),
                "FEEDBACK_STATUS_UPDATE_FAILED"
            );
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getFeedbackCategories() {

        try {
            List<Map<String, String>> categories = List.of(
                Map.of("id", "bug", "name", "Bug Report", "description", "Report technical issues"),
                Map.of("id", "ui", "name", "UI/UX", "description", "User interface feedback"),
                Map.of("id", "feature", "name", "Feature Request", "description", "Suggest new features"),
                Map.of("id", "performance", "name", "Performance", "description", "Speed and responsiveness"),
                Map.of("id", "usability", "name", "Usability", "description", "Ease of use feedback"),
                Map.of("id", "content", "name", "Content", "description", "Educational content feedback"),
                Map.of("id", "accessibility", "name", "Accessibility", "description", "Accessibility improvements"),
                Map.of("id", "other", "name", "Other", "description", "General feedback")
            );
            
            return ResponseUtil.clientSuccess(categories, "Feedback Categories");
            
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Feedback Categories",
                "Failed to retrieve feedback categories: " + e.getMessage(),
                "FEEDBACK_CATEGORIES_FAILED"
            );
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeedbackStats(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // In a real implementation, you would:
            // 1. Extract user from JWT token
            // 2. Check if user has admin privileges
            // 3. Calculate feedback statistics from database
            
            Map<String, Object> stats = Map.of(
                "totalFeedback", 0,
                "pendingFeedback", 0,
                "resolvedFeedback", 0,
                "averageRating", 0.0,
                "categoryBreakdown", Map.of(
                    "bug", 0,
                    "feature", 0,
                    "ui", 0,
                    "other", 0
                ),
                "priorityBreakdown", Map.of(
                    "low", 0,
                    "medium", 0,
                    "high", 0,
                    "critical", 0
                )
            );
            
            return ResponseUtil.clientSuccess(stats, "Feedback Statistics");
            
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Feedback Statistics",
                "Failed to retrieve feedback statistics: " + e.getMessage(),
                "FEEDBACK_STATS_FAILED"
            );
        }
    }
}

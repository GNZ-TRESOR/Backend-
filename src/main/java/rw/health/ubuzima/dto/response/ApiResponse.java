package rw.health.ubuzima.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private String userMessage; // User-friendly message
    private String errorCode;
    private T data;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private String userRole; // ADMIN, HEALTH_WORKER, CLIENT
    
    // Success response builders
    public static <T> ApiResponse<T> success(T data, String message, String userMessage, String userRole) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .userMessage(userMessage)
                .userRole(userRole)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String userMessage, String userRole) {
        return success(data, "Operation completed successfully", userMessage, userRole);
    }
    
    // Error response builders
    public static <T> ApiResponse<T> error(String message, String userMessage, String errorCode, String userRole) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .userMessage(userMessage)
                .errorCode(errorCode)
                .userRole(userRole)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String userMessage, String errorCode, String userRole) {
        return error("Operation failed", userMessage, errorCode, userRole);
    }
    
    // Role-specific success messages
    public static <T> ApiResponse<T> adminSuccess(T data, String operation) {
        String userMessage = String.format("✅ Admin: %s completed successfully", operation);
        return success(data, userMessage, "ADMIN");
    }
    
    public static <T> ApiResponse<T> healthWorkerSuccess(T data, String operation) {
        String userMessage = String.format("✅ Health Worker: %s completed successfully", operation);
        return success(data, userMessage, "HEALTH_WORKER");
    }
    
    public static <T> ApiResponse<T> clientSuccess(T data, String operation) {
        String userMessage = String.format("✅ %s completed successfully", operation);
        return success(data, userMessage, "CLIENT");
    }
    
    // Role-specific error messages
    public static <T> ApiResponse<T> adminError(String operation, String reason, String errorCode) {
        String userMessage = String.format("❌ Admin: %s failed - %s", operation, reason);
        return error(userMessage, errorCode, "ADMIN");
    }
    
    public static <T> ApiResponse<T> healthWorkerError(String operation, String reason, String errorCode) {
        String userMessage = String.format("❌ Health Worker: %s failed - %s", operation, reason);
        return error(userMessage, errorCode, "HEALTH_WORKER");
    }
    
    public static <T> ApiResponse<T> clientError(String operation, String reason, String errorCode) {
        String userMessage = String.format("❌ %s failed - %s", operation, reason);
        return error(userMessage, errorCode, "CLIENT");
    }
}

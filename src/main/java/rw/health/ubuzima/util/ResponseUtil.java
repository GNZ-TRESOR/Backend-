package rw.health.ubuzima.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rw.health.ubuzima.dto.response.ApiResponse;

public class ResponseUtil {
    
    // Success responses
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String userMessage, String userRole) {
        return ResponseEntity.ok(ApiResponse.success(data, userMessage, userRole));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String userMessage, String userRole) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, userMessage, userRole));
    }
    
    // Error responses
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String userMessage, String errorCode, String userRole) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(userMessage, errorCode, userRole));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String userMessage, String errorCode, String userRole) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(userMessage, errorCode, userRole));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String userMessage, String errorCode, String userRole) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(userMessage, errorCode, userRole));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> notFound(String userMessage, String errorCode, String userRole) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(userMessage, errorCode, userRole));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> conflict(String userMessage, String errorCode, String userRole) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(userMessage, errorCode, userRole));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String userMessage, String errorCode, String userRole) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(userMessage, errorCode, userRole));
    }
    
    // Role-specific success responses
    public static <T> ResponseEntity<ApiResponse<T>> adminSuccess(T data, String operation) {
        return ResponseEntity.ok(ApiResponse.adminSuccess(data, operation));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> healthWorkerSuccess(T data, String operation) {
        return ResponseEntity.ok(ApiResponse.healthWorkerSuccess(data, operation));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> clientSuccess(T data, String operation) {
        return ResponseEntity.ok(ApiResponse.clientSuccess(data, operation));
    }
    
    // Role-specific error responses
    public static <T> ResponseEntity<ApiResponse<T>> adminError(String operation, String reason, String errorCode, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.adminError(operation, reason, errorCode));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> healthWorkerError(String operation, String reason, String errorCode, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.healthWorkerError(operation, reason, errorCode));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> clientError(String operation, String reason, String errorCode, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.clientError(operation, reason, errorCode));
    }
    
    // Common error shortcuts
    public static <T> ResponseEntity<ApiResponse<T>> adminNotFound(String operation, String reason) {
        return adminError(operation, reason, "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> healthWorkerNotFound(String operation, String reason) {
        return healthWorkerError(operation, reason, "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> clientNotFound(String operation, String reason) {
        return clientError(operation, reason, "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> adminBadRequest(String operation, String reason, String errorCode) {
        return adminError(operation, reason, errorCode, HttpStatus.BAD_REQUEST);
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> healthWorkerBadRequest(String operation, String reason, String errorCode) {
        return healthWorkerError(operation, reason, errorCode, HttpStatus.BAD_REQUEST);
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> clientBadRequest(String operation, String reason, String errorCode) {
        return clientError(operation, reason, errorCode, HttpStatus.BAD_REQUEST);
    }
}

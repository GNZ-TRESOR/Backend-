package rw.health.ubuzima.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String error;
    
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(true, "Operation completed successfully", data, LocalDateTime.now(), null);
    }
    
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data, LocalDateTime.now(), null);
    }
    
    public static <T> ApiResponseDto<T> success(String message) {
        return new ApiResponseDto<>(true, message, null, LocalDateTime.now(), null);
    }
    
    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, message, null, LocalDateTime.now(), "Bad Request");
    }
    
    public static <T> ApiResponseDto<T> error(String message, String error) {
        return new ApiResponseDto<>(false, message, null, LocalDateTime.now(), error);
    }
}

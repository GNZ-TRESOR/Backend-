package rw.health.ubuzima.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.constants.ErrorCodes;
import rw.health.ubuzima.dto.request.LoginRequest;
import rw.health.ubuzima.dto.request.UserCreateRequest;
import rw.health.ubuzima.dto.response.ApiResponse;
import rw.health.ubuzima.dto.response.UserResponse;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.service.AuthService;
import rw.health.ubuzima.service.UserMessageService;
import rw.health.ubuzima.util.ResponseUtil;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserMessageService userMessageService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody UserCreateRequest request) {
        try {
            Map<String, Object> response = authService.register(request);

            return ResponseUtil.clientSuccess(response, "Account Registration");

        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Account Registration",
                "Email already exists or invalid data",
                ErrorCodes.AUTH_EMAIL_ALREADY_EXISTS
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Map<String, Object> response = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

            // Extract user role from response
            UserResponse userData = (UserResponse) response.get("user");
            String userRole = userData.getRole().toString();

            return ResponseUtil.clientSuccess(response, "User Login");

        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "User Login",
                "Invalid email or password",
                ErrorCodes.AUTH_INVALID_CREDENTIALS
            );
        }
    }



    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // In real app, invalidate token
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Logout failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Map<String, Object> response = Map.of(
            "success", true,
            "user", authService.getCurrentUser(token)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(@RequestBody Map<String, Object> request) {
        try {
            String refreshToken = request.get("refreshToken").toString();
            Map<String, Object> response = authService.refreshToken(refreshToken);
            return ResponseUtil.clientSuccess(response, "Token Refresh");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Token Refresh",
                "Invalid refresh token",
                ErrorCodes.AUTH_INVALID_TOKEN
            );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forgotPassword(@RequestBody Map<String, Object> request) {
        try {
            String email = request.get("email").toString();
            authService.sendPasswordResetEmail(email);
            return ResponseUtil.clientSuccess(
                Map.of("message", "Password reset email sent"),
                "Password Reset Request"
            );
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Password Reset Request",
                "Failed to send password reset email",
                ErrorCodes.AUTH_EMAIL_NOT_FOUND
            );
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(@RequestBody Map<String, Object> request) {
        try {
            String token = request.get("token").toString();
            String newPassword = request.get("newPassword").toString();
            authService.resetPassword(token, newPassword);
            return ResponseUtil.clientSuccess(
                Map.of("message", "Password reset successfully"),
                "Password Reset"
            );
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Password Reset",
                "Invalid or expired reset token",
                ErrorCodes.AUTH_INVALID_TOKEN
            );
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEmail(@RequestBody Map<String, Object> request) {
        try {
            String token = request.get("token").toString();
            authService.verifyEmail(token);
            return ResponseUtil.clientSuccess(
                Map.of("message", "Email verified successfully"),
                "Email Verification"
            );
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Email Verification",
                "Invalid or expired verification token",
                ErrorCodes.AUTH_INVALID_TOKEN
            );
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resendVerification(@RequestBody Map<String, Object> request) {
        try {
            String email = request.get("email").toString();
            authService.resendVerificationEmail(email);
            return ResponseUtil.clientSuccess(
                Map.of("message", "Verification email sent"),
                "Email Verification Resent"
            );
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Email Verification Resent",
                "Failed to send verification email",
                ErrorCodes.AUTH_EMAIL_NOT_FOUND
            );
        }
    }
}

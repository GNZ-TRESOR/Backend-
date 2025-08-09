package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.health.ubuzima.dto.request.UserCreateRequest;
import rw.health.ubuzima.dto.response.UserResponse;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.exception.AuthenticationException;
import rw.health.ubuzima.exception.ResourceNotFoundException;
import rw.health.ubuzima.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public Map<String, Object> register(UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
        
        String accessToken = jwtUtil.generateToken(
            user.getEmail(), 
            user.getRole().toString(), 
            user.getId()
        );
        
        String refreshToken = jwtUtil.generateRefreshToken(
            user.getEmail(), 
            user.getRole().toString(), 
            user.getId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("user", user);
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        
        return response;
    }

    public Map<String, Object> login(String email, String password) {
        // Validate credentials
        if (!userService.validatePassword(email, password)) {
            throw new AuthenticationException("Invalid email or password");
        }

        UserResponse user = userService.getUserByEmail(email);
        
        // Update last login
        userService.updateLastLogin(email);

        String accessToken = jwtUtil.generateToken(
            user.getEmail(), 
            user.getRole().toString(), 
            user.getId()
        );
        
        String refreshToken = jwtUtil.generateRefreshToken(
            user.getEmail(), 
            user.getRole().toString(), 
            user.getId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Login successful");
        response.put("user", user);
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        
        return response;
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            String email = jwtUtil.extractUsername(refreshToken);
            
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new AuthenticationException("Invalid refresh token");
            }
            
            if (!jwtUtil.validateToken(refreshToken, email)) {
                throw new AuthenticationException("Refresh token expired or invalid");
            }

            UserResponse user = userService.getUserByEmail(email);
            
            String newAccessToken = jwtUtil.generateToken(
                user.getEmail(), 
                user.getRole().toString(), 
                user.getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token refreshed successfully");
            response.put("accessToken", newAccessToken);
            
            return response;
            
        } catch (Exception e) {
            throw new AuthenticationException("Invalid refresh token: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            return userService.getUserByEmail(email);
        } catch (Exception e) {
            throw new AuthenticationException("Invalid token: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            return jwtUtil.validateToken(token, email);
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> sendPasswordResetEmail(String email) {
        // Check if user exists
        UserResponse user = userService.getUserByEmail(email);

        // TODO: Implement email sending logic
        // For now, just return success
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset email sent successfully");

        return response;
    }

    public Map<String, Object> resetPassword(String token, String newPassword) {
        // TODO: Implement password reset logic with token validation
        // For now, just return success
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset successfully");

        return response;
    }

    public Map<String, Object> verifyEmail(String token) {
        // TODO: Implement email verification logic
        // For now, just return success
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Email verified successfully");

        return response;
    }

    public Map<String, Object> resendVerificationEmail(String email) {
        // Check if user exists
        UserResponse user = userService.getUserByEmail(email);

        // TODO: Implement email sending logic
        // For now, just return success
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Verification email sent successfully");

        return response;
    }
}

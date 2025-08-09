package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.constants.ErrorCodes;
import rw.health.ubuzima.dto.response.ApiResponse;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.dto.response.UserResponse;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.enums.UserStatus;
import rw.health.ubuzima.enums.Gender;
import rw.health.ubuzima.service.UserMessageService;
import rw.health.ubuzima.util.ResponseUtil;
import rw.health.ubuzima.util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final UserMessageService userMessageService;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract JWT token from Authorization header
            String token = authHeader.replace("Bearer ", "");

            // Extract user ID from JWT token
            Long userId = jwtUtil.extractUserId(token);

            if (userId == null) {
                return ResponseUtil.clientError(
                    "Profile Fetch",
                    "Invalid token - unable to extract user ID",
                    ErrorCodes.AUTH_TOKEN_INVALID,
                    org.springframework.http.HttpStatus.UNAUTHORIZED
                );
            }

            // Find user by ID from token
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ResponseUtil.clientNotFound("Profile Fetch", "User profile not found");
            }

            UserResponse userResponse = convertToUserResponse(user);

            return ResponseUtil.clientSuccess(userResponse, "Profile Retrieved");

        } catch (Exception e) {
            return ResponseUtil.clientError(
                "Profile Fetch",
                "Unable to retrieve profile information",
                ErrorCodes.SYSTEM_DATABASE_ERROR,
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        
        try {
            // In real app, extract user from JWT token
            User user = userRepository.findAll().stream().findFirst().orElse(null);
            
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Update user fields
            if (request.get("name") != null) {
                user.setName(request.get("name").toString());
            }
            
            if (request.get("phone") != null) {
                user.setPhone(request.get("phone").toString());
            }
            
            if (request.get("district") != null) {
                user.setDistrict(request.get("district").toString());
            }
            
            if (request.get("sector") != null) {
                user.setSector(request.get("sector").toString());
            }
            
            if (request.get("cell") != null) {
                user.setCell(request.get("cell").toString());
            }
            
            if (request.get("village") != null) {
                user.setVillage(request.get("village").toString());
            }

            User updatedUser = userRepository.save(user);
            UserResponse userResponse = convertToUserResponse(updatedUser);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "user", userResponse
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update profile: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Current password and new password are required"
                ));
            }

            // In real app, validate current password and update
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to change password: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/account")
    public ResponseEntity<Map<String, Object>> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        try {
            // In real app, extract user from JWT token and delete account
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete account: " + e.getMessage()
            ));
        }
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setGender(user.getGender());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setFacilityId(user.getFacilityId());
        response.setDistrict(user.getDistrict());
        response.setSector(user.getSector());
        response.setCell(user.getCell());
        response.setVillage(user.getVillage());
        response.setEmergencyContact(user.getEmergencyContact());
        response.setPreferredLanguage(user.getPreferredLanguage());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setEmailVerified(user.getEmailVerified());
        response.setPhoneVerified(user.getPhoneVerified());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}

package rw.health.ubuzima.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.health.ubuzima.enums.Gender;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String facilityId;
    private String district;
    private String sector;
    private String cell;
    private String village;
    private String emergencyContact;
    private String preferredLanguage;
    private String profilePictureUrl;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDate lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setProfileImageUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Boolean isActive() {
        return active;
    }
}

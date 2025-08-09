
package rw.health.ubuzima.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.health.ubuzima.enums.Gender;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.enums.UserStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "appointments"})
public class User extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone", unique = true, nullable = false)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.CLIENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "facility_id")
    private String facilityId;

    @Column(name = "district")
    private String district;

    @Column(name = "sector")
    private String sector;

    @Column(name = "cell")
    private String cell;

    @Column(name = "village")
    private String village;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "preferred_language")
    private String preferredLanguage = "rw";

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    @Column(name = "last_login_at")
    private LocalDate lastLoginAt;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "platform")
    private String platform;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HealthRecord healthRecord;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    // Helper methods
    public String getFullName() {
        return name;
    }

    public String getFullLocation() {
        StringBuilder location = new StringBuilder();
        if (village != null && !village.isEmpty()) location.append(village);
        if (cell != null && !cell.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(cell);
        }
        if (sector != null && !sector.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(sector);
        }
        if (district != null && !district.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(district);
        }
        return location.toString();
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isClient() {
        return role == UserRole.CLIENT;
    }

    public boolean isHealthWorker() {
        return role == UserRole.HEALTH_WORKER;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}

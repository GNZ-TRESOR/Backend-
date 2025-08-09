package rw.health.ubuzima.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.health.ubuzima.enums.Gender;
import rw.health.ubuzima.enums.UserRole;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+250[0-9]{9}$", message = "Phone should be in format +250XXXXXXXXX")
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;

    private UserRole role = UserRole.CLIENT;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String facilityId;
    private String district;
    private String sector;
    private String cell;
    private String village;
    private String emergencyContact;
    private String preferredLanguage = "rw";
}


package rw.health.ubuzima.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.health.ubuzima.enums.FacilityType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "health_facilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "appointments"})
public class HealthFacility extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", nullable = false)
    private FacilityType facilityType;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "services_offered", columnDefinition = "TEXT")
    private String servicesOffered;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @OneToMany(mappedBy = "healthFacility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();
}

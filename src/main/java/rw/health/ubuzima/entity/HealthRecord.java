package rw.health.ubuzima.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"healthRecord", "medications", "appointments", "messages", "password", "passwordHash", "hibernateLazyInitializer", "handler"})
    private User user;

    // Heart rate columns (as shown in your image)
    @Column(name = "heart_rate_value")
    private Integer heartRateValue;

    @Column(name = "heart_rate_unit", length = 10)
    private String heartRateUnit = "bpm";

    // Blood pressure columns (bp_value, bp_unit as shown in image)
    @Column(name = "bp_value", length = 20)
    private String bpValue;

    @Column(name = "bp_unit", length = 10)
    private String bpUnit = "mmHg";

    // Weight columns (kg_value, kg_unit as shown in image)
    @Column(name = "kg_value", precision = 5, scale = 2)
    private BigDecimal kgValue;

    @Column(name = "kg_unit", length = 10)
    private String kgUnit = "kg";

    // Temperature columns (temp_value, temp_unit)
    @Column(name = "temp_value", precision = 4, scale = 1)
    private BigDecimal tempValue;

    @Column(name = "temp_unit", length = 10)
    private String tempUnit = "°C";

    // Additional useful columns
    @Column(name = "height_value", precision = 5, scale = 2)
    private BigDecimal heightValue;

    @Column(name = "height_unit", length = 10)
    private String heightUnit = "cm";

    // Computed fields
    @Column(name = "bmi", precision = 4, scale = 1)
    private BigDecimal bmi;

    @Column(name = "health_status", length = 20)
    private String healthStatus = "normal";

    // Metadata
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_health_worker_id")
    @JsonIgnoreProperties({"healthRecord", "medications", "appointments", "messages", "password", "passwordHash", "hibernateLazyInitializer", "handler"})
    private User assignedHealthWorker;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
        updateComputedFields();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
        updateComputedFields();
    }

    private void updateComputedFields() {
        // Calculate BMI if both weight and height are available
        if (kgValue != null && heightValue != null && heightValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightInMeters = heightValue.divide(new BigDecimal("100"));
            bmi = kgValue.divide(heightInMeters.multiply(heightInMeters), 1, RoundingMode.HALF_UP);
        }

        // Update health status based on vital signs
        updateHealthStatus();
    }

    private void updateHealthStatus() {
        boolean isCritical = false;
        boolean isConcerning = false;

        // Check blood pressure
        if (bpValue != null && bpValue.contains("/")) {
            try {
                String[] bpParts = bpValue.split("/");
                int systolic = Integer.parseInt(bpParts[0]);
                int diastolic = Integer.parseInt(bpParts[1]);

                if (systolic >= 180 || diastolic >= 120) {
                    isCritical = true;
                } else if (systolic >= 140 || diastolic >= 90) {
                    isConcerning = true;
                }
            } catch (NumberFormatException e) {
                // Invalid blood pressure format, skip
            }
        }

        // Check heart rate
        if (heartRateValue != null) {
            if (heartRateValue < 40 || heartRateValue > 150) {
                isCritical = true;
            } else if (heartRateValue < 50 || heartRateValue > 120) {
                isConcerning = true;
            }
        }

        // Check temperature
        if (tempValue != null) {
            if (tempValue.compareTo(new BigDecimal("40.0")) >= 0 ||
                tempValue.compareTo(new BigDecimal("34.0")) <= 0) {
                isCritical = true;
            } else if (tempValue.compareTo(new BigDecimal("39.0")) >= 0 ||
                       tempValue.compareTo(new BigDecimal("35.0")) <= 0) {
                isConcerning = true;
            }
        }

        if (isCritical) {
            healthStatus = "critical";
        } else if (isConcerning) {
            healthStatus = "concerning";
        } else {
            healthStatus = "normal";
        }
    }

    // Convenience methods for easier access
    public Integer getSystolic() {
        if (bpValue != null && bpValue.contains("/")) {
            try {
                return Integer.parseInt(bpValue.split("/")[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public Integer getDiastolic() {
        if (bpValue != null && bpValue.contains("/")) {
            try {
                return Integer.parseInt(bpValue.split("/")[1]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public void setBloodPressure(int systolic, int diastolic) {
        this.bpValue = systolic + "/" + diastolic;
    }
}

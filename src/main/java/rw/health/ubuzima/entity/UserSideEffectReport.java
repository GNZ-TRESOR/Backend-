package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Table(name = "user_side_effect_reports")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSideEffectReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contraception_method_id", nullable = false)
    private ContraceptionMethod contraceptionMethod;

    @Column(name = "side_effect_name", nullable = false)
    private String sideEffectName;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private SideEffectSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private SideEffectFrequency frequency;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_reported", nullable = false)
    private LocalDate dateReported;

    @Column(name = "is_resolved")
    @Builder.Default
    private Boolean isResolved = false;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    public enum SideEffectSeverity {
        MILD, MODERATE, SEVERE
    }

    public enum SideEffectFrequency {
        RARE, OCCASIONAL, COMMON, FREQUENT
    }

    // Helper methods
    public String getSeverityDisplayName() {
        return switch (severity) {
            case MILD -> "Mild";
            case MODERATE -> "Moderate";
            case SEVERE -> "Severe";
        };
    }

    public String getFrequencyDisplayName() {
        return switch (frequency) {
            case RARE -> "Rare";
            case OCCASIONAL -> "Occasional";
            case COMMON -> "Common";
            case FREQUENT -> "Frequent";
        };
    }

    public boolean isActive() {
        return !Boolean.TRUE.equals(isResolved);
    }

    public void markAsResolved(String resolutionNotes) {
        this.isResolved = true;
        this.resolutionNotes = resolutionNotes;
    }

    public void markAsUnresolved() {
        this.isResolved = false;
        this.resolutionNotes = null;
    }
}

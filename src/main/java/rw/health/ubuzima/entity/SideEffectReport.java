package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "side_effect_reports")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SideEffectReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contraception_method_id", nullable = false)
    private ContraceptionMethod contraceptionMethod;

    @Column(name = "side_effect_type", nullable = false)
    private String sideEffectType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private SideEffectSeverity severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "started_at", nullable = false)
    private LocalDate startedAt;

    @Column(name = "ended_at")
    private LocalDate endedAt;

    @Column(name = "is_ongoing")
    @Builder.Default
    private Boolean isOngoing = true;

    @Column(name = "reported_at")
    @Builder.Default
    private LocalDateTime reportedAt = LocalDateTime.now();

    public enum SideEffectSeverity {
        MILD, MODERATE, SEVERE
    }

    // Common side effect types as constants
    public static class SideEffectTypes {
        public static final String NAUSEA = "NAUSEA";
        public static final String HEADACHE = "HEADACHE";
        public static final String MOOD_CHANGES = "MOOD_CHANGES";
        public static final String WEIGHT_GAIN = "WEIGHT_GAIN";
        public static final String WEIGHT_LOSS = "WEIGHT_LOSS";
        public static final String BREAST_TENDERNESS = "BREAST_TENDERNESS";
        public static final String IRREGULAR_BLEEDING = "IRREGULAR_BLEEDING";
        public static final String ACNE = "ACNE";
        public static final String DECREASED_LIBIDO = "DECREASED_LIBIDO";
        public static final String FATIGUE = "FATIGUE";
        public static final String DIZZINESS = "DIZZINESS";
        public static final String CRAMPING = "CRAMPING";
        public static final String ALLERGIC_REACTION = "ALLERGIC_REACTION";
        public static final String OTHER = "OTHER";
    }
}

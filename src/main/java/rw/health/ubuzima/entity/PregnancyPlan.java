package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rw.health.ubuzima.enums.PregnancyPlanStatus;

import java.time.LocalDate;

@Entity
@Table(name = "pregnancy_plans")
@Data
@EqualsAndHashCode(callSuper = true)
public class PregnancyPlan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private User partner;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "target_conception_date")
    private LocalDate targetConceptionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    private PregnancyPlanStatus currentStatus = PregnancyPlanStatus.PLANNING;

    @Column(name = "preconception_goals", columnDefinition = "TEXT")
    private String preconceptionGoals;

    @Column(name = "health_preparations", columnDefinition = "TEXT")
    private String healthPreparations;

    @Column(name = "lifestyle_changes", columnDefinition = "TEXT")
    private String lifestyleChanges;

    @Column(name = "medical_consultations", columnDefinition = "TEXT")
    private String medicalConsultations;

    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;

    // Constructors
    public PregnancyPlan() {}

    public PregnancyPlan(User user, String planName) {
        this.user = user;
        this.planName = planName;
    }
}

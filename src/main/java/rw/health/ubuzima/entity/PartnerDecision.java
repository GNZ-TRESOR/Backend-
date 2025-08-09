package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rw.health.ubuzima.enums.DecisionType;
import rw.health.ubuzima.enums.DecisionStatus;

import java.time.LocalDate;

@Entity
@Table(name = "partner_decisions")
@Data
@EqualsAndHashCode(callSuper = true)
public class PartnerDecision extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private User partner;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false)
    private DecisionType decisionType;

    @Column(name = "decision_title", nullable = false)
    private String decisionTitle;

    @Column(name = "decision_description", columnDefinition = "TEXT")
    private String decisionDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_status")
    private DecisionStatus decisionStatus = DecisionStatus.PROPOSED;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Constructors
    public PartnerDecision() {}

    public PartnerDecision(User user, DecisionType decisionType, String title, String description) {
        this.user = user;
        this.decisionType = decisionType;
        this.decisionTitle = title;
        this.decisionDescription = description;
    }
}

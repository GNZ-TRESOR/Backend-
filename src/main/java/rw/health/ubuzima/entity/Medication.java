package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Medication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "dosage", nullable = false)
    private String dosage;

    @Column(name = "frequency", nullable = false)
    private String frequency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "prescribed_by")
    private String prescribedBy;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @ElementCollection
    @CollectionTable(name = "medication_side_effects", joinColumns = @JoinColumn(name = "medication_id"))
    @Column(name = "side_effect")
    private List<String> sideEffects = new ArrayList<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

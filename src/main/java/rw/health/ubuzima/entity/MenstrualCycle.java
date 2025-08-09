package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.health.ubuzima.enums.FlowIntensity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menstrual_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenstrualCycle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "cycle_length")
    private Integer cycleLength;

    @Column(name = "flow_duration")
    private Integer flowDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow_intensity")
    private FlowIntensity flowIntensity;

    @ElementCollection
    @CollectionTable(name = "menstrual_symptoms", joinColumns = @JoinColumn(name = "cycle_id"))
    @Column(name = "symptom")
    private List<String> symptoms = new ArrayList<>();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_predicted")
    private Boolean isPredicted = false;

    @Column(name = "ovulation_date")
    private LocalDate ovulationDate;

    @Column(name = "fertile_window_start")
    private LocalDate fertileWindowStart;

    @Column(name = "fertile_window_end")
    private LocalDate fertileWindowEnd;
}

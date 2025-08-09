
package rw.health.ubuzima.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.health.ubuzima.enums.ContraceptionType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contraception_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContraceptionMethod extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"appointments"})
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "contraception_type", nullable = false)
    private ContraceptionType type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "effectiveness")
    private Double effectiveness;

    @ElementCollection
    @CollectionTable(name = "contraception_side_effects", joinColumns = @JoinColumn(name = "contraception_id"))
    @Column(name = "side_effect")
    private List<String> sideEffects = new ArrayList<>();

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "next_appointment")
    private LocalDate nextAppointment;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "prescribed_by")
    private String prescribedBy;

    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData; // JSON string for flexible data
}

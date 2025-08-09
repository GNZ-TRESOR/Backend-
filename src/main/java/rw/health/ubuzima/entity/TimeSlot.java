package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "time_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_facility_id")
    private HealthFacility healthFacility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_worker_id")
    private User healthWorker;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "reason")
    private String reason;

    @Column(name = "max_appointments")
    private Integer maxAppointments = 1;

    @Column(name = "current_appointments")
    private Integer currentAppointments = 0;
}

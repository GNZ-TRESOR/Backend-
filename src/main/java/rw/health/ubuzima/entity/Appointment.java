package rw.health.ubuzima.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.health.ubuzima.enums.AppointmentStatus;
import rw.health.ubuzima.enums.AppointmentType;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"appointments", "healthRecords", "medications", "cycleTrackings", "messages", "notifications"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_facility_id", nullable = false)
    @JsonIgnoreProperties({"appointments", "healthWorkers", "services"})
    private HealthFacility healthFacility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_worker_id")
    @JsonIgnoreProperties({"appointments", "healthRecords", "medications", "cycleTrackings", "messages", "notifications"})
    private User healthWorker;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;
}

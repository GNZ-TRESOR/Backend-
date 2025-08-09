package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rw.health.ubuzima.enums.TicketType;
import rw.health.ubuzima.enums.TicketPriority;
import rw.health.ubuzima.enums.TicketStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
@Data
@EqualsAndHashCode(callSuper = true)
public class SupportTicket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false)
    private TicketType ticketType;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TicketStatus status = TicketStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_phone", length = 20)
    private String userPhone;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Constructors
    public SupportTicket() {}

    public SupportTicket(TicketType ticketType, String subject, String description) {
        this.ticketType = ticketType;
        this.subject = subject;
        this.description = description;
    }

    // Helper methods
    public boolean isResolved() {
        return status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED;
    }

    public void resolve(String resolutionNotes) {
        this.status = TicketStatus.RESOLVED;
        this.resolutionNotes = resolutionNotes;
        this.resolvedAt = LocalDateTime.now();
    }
}

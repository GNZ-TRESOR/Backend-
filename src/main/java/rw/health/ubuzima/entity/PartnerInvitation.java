package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rw.health.ubuzima.enums.InvitationType;
import rw.health.ubuzima.enums.InvitationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "partner_invitations")
@Data
@EqualsAndHashCode(callSuper = true)
public class PartnerInvitation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_type")
    private InvitationType invitationType = InvitationType.PARTNER_LINK;

    @Column(name = "invitation_message", columnDefinition = "TEXT")
    private String invitationMessage;

    @Column(name = "invitation_code", unique = true, nullable = false, length = 50)
    private String invitationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvitationStatus status = InvitationStatus.SENT;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    // Constructors
    public PartnerInvitation() {}

    public PartnerInvitation(User sender, String recipientEmail, String invitationCode, LocalDateTime expiresAt) {
        this.sender = sender;
        this.recipientEmail = recipientEmail;
        this.invitationCode = invitationCode;
        this.expiresAt = expiresAt;
    }

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canBeAccepted() {
        return status == InvitationStatus.SENT && !isExpired();
    }
}

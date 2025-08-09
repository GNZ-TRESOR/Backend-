package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.PartnerInvitation;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.InvitationStatus;
import rw.health.ubuzima.enums.InvitationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerInvitationRepository extends JpaRepository<PartnerInvitation, Long> {

    List<PartnerInvitation> findBySender(User sender);

    List<PartnerInvitation> findByRecipientEmail(String recipientEmail);

    Optional<PartnerInvitation> findByInvitationCode(String invitationCode);

    List<PartnerInvitation> findBySenderAndStatus(User sender, InvitationStatus status);

    List<PartnerInvitation> findByRecipientEmailAndStatus(String recipientEmail, InvitationStatus status);

    @Query("SELECT pi FROM PartnerInvitation pi WHERE pi.expiresAt < :now AND pi.status = 'SENT'")
    List<PartnerInvitation> findExpiredInvitations(@Param("now") LocalDateTime now);

    @Query("SELECT pi FROM PartnerInvitation pi WHERE pi.sender.id = :senderId ORDER BY pi.createdAt DESC")
    List<PartnerInvitation> findBySenderIdOrderByCreatedAtDesc(@Param("senderId") Long senderId);

    List<PartnerInvitation> findBySenderAndInvitationType(User sender, InvitationType invitationType);

    boolean existsByInvitationCode(String invitationCode);
}

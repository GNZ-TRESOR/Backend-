package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.PartnerInvitation;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.InvitationType;
import rw.health.ubuzima.enums.InvitationStatus;
import rw.health.ubuzima.repository.PartnerInvitationRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/partner-invitations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartnerInvitationController {

    private final PartnerInvitationRepository partnerInvitationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPartnerInvitations(
            @RequestParam Long userId,
            @RequestParam(required = false) String status) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<PartnerInvitation> invitations;
            
            if (status != null) {
                InvitationStatus invitationStatus = InvitationStatus.valueOf(status.toUpperCase());
                invitations = partnerInvitationRepository.findBySenderAndStatus(user, invitationStatus);
            } else {
                invitations = partnerInvitationRepository.findBySenderIdOrderByCreatedAtDesc(userId);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "invitations", invitations
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch invitations: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendPartnerInvitation(
            @RequestBody Map<String, Object> request) {
        try {
            Long senderId = Long.valueOf(request.get("senderId").toString());
            User sender = userRepository.findById(senderId).orElse(null);
            
            if (sender == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Sender not found"
                ));
            }

            String recipientEmail = request.get("recipientEmail").toString();
            
            // Generate unique invitation code
            String invitationCode;
            do {
                invitationCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            } while (partnerInvitationRepository.existsByInvitationCode(invitationCode));

            PartnerInvitation invitation = new PartnerInvitation();
            invitation.setSender(sender);
            invitation.setRecipientEmail(recipientEmail);
            invitation.setInvitationCode(invitationCode);
            invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // Expires in 7 days
            
            if (request.get("recipientPhone") != null) {
                invitation.setRecipientPhone(request.get("recipientPhone").toString());
            }
            
            if (request.get("invitationType") != null) {
                invitation.setInvitationType(InvitationType.valueOf(request.get("invitationType").toString().toUpperCase()));
            }
            
            if (request.get("invitationMessage") != null) {
                invitation.setInvitationMessage(request.get("invitationMessage").toString());
            }

            PartnerInvitation savedInvitation = partnerInvitationRepository.save(invitation);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Partner invitation sent successfully",
                "invitation", savedInvitation,
                "invitationCode", invitationCode
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to send invitation: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/accept/{invitationCode}")
    public ResponseEntity<Map<String, Object>> acceptPartnerInvitation(
            @PathVariable String invitationCode,
            @RequestParam Long userId) {
        try {
            PartnerInvitation invitation = partnerInvitationRepository.findByInvitationCode(invitationCode).orElse(null);
            
            if (invitation == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid invitation code"
                ));
            }

            if (!invitation.canBeAccepted()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invitation has expired or already been processed"
                ));
            }

            User acceptingUser = userRepository.findById(userId).orElse(null);
            if (acceptingUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Check if the accepting user's email matches the invitation
            if (!acceptingUser.getEmail().equalsIgnoreCase(invitation.getRecipientEmail())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "This invitation is not for your email address"
                ));
            }

            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(LocalDateTime.now());
            
            PartnerInvitation updatedInvitation = partnerInvitationRepository.save(invitation);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Partner invitation accepted successfully",
                "invitation", updatedInvitation
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to accept invitation: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/decline/{invitationCode}")
    public ResponseEntity<Map<String, Object>> declinePartnerInvitation(
            @PathVariable String invitationCode) {
        try {
            PartnerInvitation invitation = partnerInvitationRepository.findByInvitationCode(invitationCode).orElse(null);
            
            if (invitation == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid invitation code"
                ));
            }

            if (!invitation.canBeAccepted()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invitation has expired or already been processed"
                ));
            }

            invitation.setStatus(InvitationStatus.DECLINED);
            
            PartnerInvitation updatedInvitation = partnerInvitationRepository.save(invitation);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Partner invitation declined",
                "invitation", updatedInvitation
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to decline invitation: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/received")
    public ResponseEntity<Map<String, Object>> getReceivedInvitations(@RequestParam String email) {
        try {
            List<PartnerInvitation> invitations = partnerInvitationRepository.findByRecipientEmail(email);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "invitations", invitations
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch received invitations: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/verify/{invitationCode}")
    public ResponseEntity<Map<String, Object>> verifyInvitationCode(@PathVariable String invitationCode) {
        try {
            PartnerInvitation invitation = partnerInvitationRepository.findByInvitationCode(invitationCode).orElse(null);
            
            if (invitation == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid invitation code"
                ));
            }

            boolean isValid = invitation.canBeAccepted();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "valid", isValid,
                "invitation", invitation,
                "expired", invitation.isExpired()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to verify invitation: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Map<String, Object>> cancelInvitation(
            @PathVariable Long invitationId,
            @RequestParam Long userId) {
        try {
            PartnerInvitation invitation = partnerInvitationRepository.findById(invitationId).orElse(null);
            
            if (invitation == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if user owns this invitation
            if (!invitation.getSender().getId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You can only cancel your own invitations"
                ));
            }

            partnerInvitationRepository.delete(invitation);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invitation cancelled successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to cancel invitation: " + e.getMessage()
            ));
        }
    }
}

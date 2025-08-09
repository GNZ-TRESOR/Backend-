package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.PartnerDecision;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.DecisionType;
import rw.health.ubuzima.enums.DecisionStatus;
import rw.health.ubuzima.repository.PartnerDecisionRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/partner-decisions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartnerDecisionController {

    private final PartnerDecisionRepository partnerDecisionRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPartnerDecisions(
            @RequestParam Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<PartnerDecision> decisions;
            
            if (type != null && status != null) {
                DecisionType decisionType = DecisionType.valueOf(type.toUpperCase());
                DecisionStatus decisionStatus = DecisionStatus.valueOf(status.toUpperCase());
                decisions = partnerDecisionRepository.findByUserAndDecisionType(user, decisionType)
                    .stream()
                    .filter(d -> d.getDecisionStatus() == decisionStatus)
                    .toList();
            } else if (type != null) {
                DecisionType decisionType = DecisionType.valueOf(type.toUpperCase());
                decisions = partnerDecisionRepository.findByUserAndDecisionType(user, decisionType);
            } else if (status != null) {
                DecisionStatus decisionStatus = DecisionStatus.valueOf(status.toUpperCase());
                decisions = partnerDecisionRepository.findByUserAndDecisionStatus(user, decisionStatus);
            } else {
                decisions = partnerDecisionRepository.findByUserOrPartner(user);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "decisions", decisions
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch decisions: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPartnerDecision(
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            PartnerDecision decision = new PartnerDecision();
            decision.setUser(user);
            decision.setDecisionType(DecisionType.valueOf(request.get("decisionType").toString().toUpperCase()));
            decision.setDecisionTitle(request.get("decisionTitle").toString());
            
            if (request.get("decisionDescription") != null) {
                decision.setDecisionDescription(request.get("decisionDescription").toString());
            }
            
            if (request.get("partnerId") != null) {
                Long partnerId = Long.valueOf(request.get("partnerId").toString());
                User partner = userRepository.findById(partnerId).orElse(null);
                decision.setPartner(partner);
            }
            
            if (request.get("targetDate") != null) {
                decision.setTargetDate(LocalDate.parse(request.get("targetDate").toString()));
            }
            
            if (request.get("notes") != null) {
                decision.setNotes(request.get("notes").toString());
            }

            PartnerDecision savedDecision = partnerDecisionRepository.save(decision);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Partner decision created successfully",
                "decision", savedDecision
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create decision: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{decisionId}")
    public ResponseEntity<Map<String, Object>> updatePartnerDecision(
            @PathVariable Long decisionId,
            @RequestBody Map<String, Object> request) {
        try {
            PartnerDecision decision = partnerDecisionRepository.findById(decisionId).orElse(null);
            
            if (decision == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.get("decisionTitle") != null) {
                decision.setDecisionTitle(request.get("decisionTitle").toString());
            }
            
            if (request.get("decisionDescription") != null) {
                decision.setDecisionDescription(request.get("decisionDescription").toString());
            }
            
            if (request.get("decisionStatus") != null) {
                decision.setDecisionStatus(DecisionStatus.valueOf(request.get("decisionStatus").toString().toUpperCase()));
            }
            
            if (request.get("targetDate") != null) {
                decision.setTargetDate(LocalDate.parse(request.get("targetDate").toString()));
            }
            
            if (request.get("notes") != null) {
                decision.setNotes(request.get("notes").toString());
            }

            PartnerDecision updatedDecision = partnerDecisionRepository.save(decision);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Decision updated successfully",
                "decision", updatedDecision
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update decision: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{decisionId}")
    public ResponseEntity<Map<String, Object>> deletePartnerDecision(
            @PathVariable Long decisionId,
            @RequestParam Long userId) {
        try {
            PartnerDecision decision = partnerDecisionRepository.findById(decisionId).orElse(null);
            
            if (decision == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if user owns this decision
            if (!decision.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You can only delete your own decisions"
                ));
            }

            partnerDecisionRepository.delete(decision);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Decision deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete decision: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/shared")
    public ResponseEntity<Map<String, Object>> getSharedDecisions(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<PartnerDecision> decisions = partnerDecisionRepository.findByUserOrPartner(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "decisions", decisions
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch shared decisions: " + e.getMessage()
            ));
        }
    }
}

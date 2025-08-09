package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.SupportTicket;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.TicketType;
import rw.health.ubuzima.enums.TicketStatus;
import rw.health.ubuzima.enums.TicketPriority;
import rw.health.ubuzima.repository.SupportTicketRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/support-tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SupportTicketController {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSupportTickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority) {
        try {
            List<SupportTicket> tickets;
            
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found"
                    ));
                }
                
                if (status != null) {
                    TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
                    tickets = supportTicketRepository.findByUserAndStatus(user, ticketStatus);
                } else {
                    tickets = supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId);
                }
            } else {
                if (status != null) {
                    TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
                    tickets = supportTicketRepository.findByStatus(ticketStatus);
                } else if (type != null) {
                    TicketType ticketType = TicketType.valueOf(type.toUpperCase());
                    tickets = supportTicketRepository.findByTicketType(ticketType);
                } else if (priority != null) {
                    TicketPriority ticketPriority = TicketPriority.valueOf(priority.toUpperCase());
                    tickets = supportTicketRepository.findByPriority(ticketPriority);
                } else {
                    tickets = supportTicketRepository.findOpenTicketsOrderByPriorityAndCreatedAt();
                }
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", tickets
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch support tickets: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSupportTicket(
            @RequestBody Map<String, Object> request) {
        try {
            SupportTicket ticket = new SupportTicket();
            ticket.setTicketType(TicketType.valueOf(request.get("ticketType").toString().toUpperCase()));
            ticket.setSubject(request.get("subject").toString());
            ticket.setDescription(request.get("description").toString());
            
            if (request.get("userId") != null) {
                Long userId = Long.valueOf(request.get("userId").toString());
                User user = userRepository.findById(userId).orElse(null);
                ticket.setUser(user);
            }
            
            if (request.get("priority") != null) {
                ticket.setPriority(TicketPriority.valueOf(request.get("priority").toString().toUpperCase()));
            }
            
            if (request.get("userEmail") != null) {
                ticket.setUserEmail(request.get("userEmail").toString());
            }
            
            if (request.get("userPhone") != null) {
                ticket.setUserPhone(request.get("userPhone").toString());
            }

            SupportTicket savedTicket = supportTicketRepository.save(ticket);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support ticket created successfully",
                "ticket", savedTicket
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create support ticket: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> updateSupportTicket(
            @PathVariable Long ticketId,
            @RequestBody Map<String, Object> request) {
        try {
            SupportTicket ticket = supportTicketRepository.findById(ticketId).orElse(null);
            
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.get("status") != null) {
                ticket.setStatus(TicketStatus.valueOf(request.get("status").toString().toUpperCase()));
            }
            
            if (request.get("priority") != null) {
                ticket.setPriority(TicketPriority.valueOf(request.get("priority").toString().toUpperCase()));
            }
            
            if (request.get("assignedTo") != null) {
                Long assigneeId = Long.valueOf(request.get("assignedTo").toString());
                User assignee = userRepository.findById(assigneeId).orElse(null);
                ticket.setAssignedTo(assignee);
            }
            
            if (request.get("resolutionNotes") != null) {
                ticket.setResolutionNotes(request.get("resolutionNotes").toString());
            }

            SupportTicket updatedTicket = supportTicketRepository.save(ticket);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support ticket updated successfully",
                "ticket", updatedTicket
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update support ticket: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{ticketId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveSupportTicket(
            @PathVariable Long ticketId,
            @RequestBody Map<String, Object> request) {
        try {
            SupportTicket ticket = supportTicketRepository.findById(ticketId).orElse(null);
            
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            String resolutionNotes = request.get("resolutionNotes").toString();
            ticket.resolve(resolutionNotes);

            SupportTicket resolvedTicket = supportTicketRepository.save(ticket);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support ticket resolved successfully",
                "ticket", resolvedTicket
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to resolve support ticket: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{ticketId}/assign")
    public ResponseEntity<Map<String, Object>> assignSupportTicket(
            @PathVariable Long ticketId,
            @RequestParam Long assigneeId) {
        try {
            SupportTicket ticket = supportTicketRepository.findById(ticketId).orElse(null);
            
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            User assignee = userRepository.findById(assigneeId).orElse(null);
            if (assignee == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Assignee not found"
                ));
            }

            ticket.setAssignedTo(assignee);
            ticket.setStatus(TicketStatus.IN_PROGRESS);

            SupportTicket assignedTicket = supportTicketRepository.save(ticket);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support ticket assigned successfully",
                "ticket", assignedTicket
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to assign support ticket: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/assigned/{assigneeId}")
    public ResponseEntity<Map<String, Object>> getAssignedTickets(@PathVariable Long assigneeId) {
        try {
            List<SupportTicket> tickets = supportTicketRepository.findByAssigneeIdOrderByPriorityAndCreatedAt(assigneeId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", tickets
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch assigned tickets: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTicketStats() {
        try {
            long openTickets = supportTicketRepository.countByStatus(TicketStatus.OPEN);
            long inProgressTickets = supportTicketRepository.countByStatus(TicketStatus.IN_PROGRESS);
            long resolvedTickets = supportTicketRepository.countByStatus(TicketStatus.RESOLVED);
            long closedTickets = supportTicketRepository.countByStatus(TicketStatus.CLOSED);

            Map<String, Object> stats = Map.of(
                "open", openTickets,
                "inProgress", inProgressTickets,
                "resolved", resolvedTickets,
                "closed", closedTickets,
                "total", openTickets + inProgressTickets + resolvedTickets + closedTickets
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch ticket stats: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> deleteSupportTicket(
            @PathVariable Long ticketId,
            @RequestParam Long userId) {
        try {
            SupportTicket ticket = supportTicketRepository.findById(ticketId).orElse(null);
            
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if user owns this ticket
            if (ticket.getUser() != null && !ticket.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You can only delete your own support tickets"
                ));
            }

            supportTicketRepository.delete(ticket);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support ticket deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete support ticket: " + e.getMessage()
            ));
        }
    }

    // Alternative endpoint for tickets with different field names
    @PostMapping("/tickets")
    public ResponseEntity<Map<String, Object>> createTicket(
            @RequestBody Map<String, Object> request) {
        try {
            SupportTicket ticket = new SupportTicket();

            // Support both title/subject for compatibility
            if (request.get("title") != null) {
                ticket.setSubject(request.get("title").toString());
            } else if (request.get("subject") != null) {
                ticket.setSubject(request.get("subject").toString());
            }

            ticket.setDescription(request.get("description").toString());

            // Support both createdBy and userId
            if (request.get("createdBy") != null) {
                Long userId = Long.valueOf(request.get("createdBy").toString());
                User user = userRepository.findById(userId).orElse(null);
                ticket.setUser(user);
            } else if (request.get("userId") != null) {
                Long userId = Long.valueOf(request.get("userId").toString());
                User user = userRepository.findById(userId).orElse(null);
                ticket.setUser(user);
            }

            if (request.get("priority") != null) {
                ticket.setPriority(TicketPriority.valueOf(request.get("priority").toString().toUpperCase()));
            } else {
                ticket.setPriority(TicketPriority.MEDIUM);
            }

            // Set default ticket type if not provided
            if (request.get("ticketType") != null) {
                ticket.setTicketType(TicketType.valueOf(request.get("ticketType").toString().toUpperCase()));
            } else {
                ticket.setTicketType(TicketType.GENERAL);
            }

            SupportTicket savedTicket = supportTicketRepository.save(ticket);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket created successfully",
                "ticket", savedTicket
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create ticket: " + e.getMessage()
            ));
        }
    }
}

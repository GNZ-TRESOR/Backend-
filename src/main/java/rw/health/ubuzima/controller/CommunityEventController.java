package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.dto.response.ApiResponse;
import rw.health.ubuzima.service.CommunityEventService;
import rw.health.ubuzima.util.ResponseUtil;
import rw.health.ubuzima.repository.CommunityEventRepository;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.entity.CommunityEvent;
import rw.health.ubuzima.entity.User;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/community-events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommunityEventController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CommunityEventController.class);

    private final CommunityEventService communityEventService;
    private final CommunityEventRepository communityEventRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCommunityEvents() {
        logger.info("Received request to get all community events");
        try {
            List<CommunityEvent> events = communityEventRepository.findByIsActiveTrueAndIsCancelledFalseOrderByEventDateAsc();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "events", events
            ));
        } catch (Exception e) {
            logger.error("Error retrieving community events", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load community events: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/created")
    @PreAuthorize("hasRole('HEALTH_WORKER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCreatedEvents() {
        try {
            var events = communityEventService.getCreatedEvents();
            return ResponseUtil.clientSuccess(events, "Created Events Retrieved");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Created Events Retrieval",
                e.getMessage(),
                "CREATED_EVENTS_RETRIEVAL_ERROR"
            );
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCommunityEvent(@RequestBody Map<String, Object> request) {
        try {
            CommunityEvent event = new CommunityEvent();
            event.setTitle(request.get("title").toString());
            event.setDescription(request.get("description").toString());
            event.setEventDate(LocalDateTime.parse(request.get("eventDate").toString()));
            event.setLocation(request.get("location") != null ? request.get("location").toString() : null);
            event.setType(request.getOrDefault("eventType", "HEALTH_EDUCATION").toString());
            event.setMaxParticipants((Integer) request.getOrDefault("maxParticipants", 50));
            event.setIsActive(true);
            event.setIsCancelled(false);

            // Set organizer if provided
            if (request.get("organizerId") != null) {
                Long organizerId = Long.valueOf(request.get("organizerId").toString());
                User organizer = userRepository.findById(organizerId).orElse(null);
                event.setOrganizer(organizer);
            }

            CommunityEvent savedEvent = communityEventRepository.save(event);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Community event created successfully",
                "event", savedEvent
            ));
        } catch (Exception e) {
            logger.error("Error creating community event", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create community event: " + e.getMessage()
            ));
        }
    }

    /**
     * Get user's events (events they created or are attending)
     */
    @GetMapping("/my-events")
    public ResponseEntity<Map<String, Object>> getMyEvents() {
        try {
            // For now, return empty list - this can be enhanced later with actual user events
            List<CommunityEvent> myEvents = List.of();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "events", myEvents,
                "message", "User events retrieved successfully"
            ));
        } catch (Exception e) {
            logger.error("Error retrieving user events", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to retrieve user events: " + e.getMessage()
            ));
        }
    }
}

package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import rw.health.ubuzima.entity.CommunityEvent;
import rw.health.ubuzima.repository.CommunityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityEventService {
    private static final Logger logger = LoggerFactory.getLogger(CommunityEventService.class);

    private final CommunityEventRepository eventRepository;
    private final rw.health.ubuzima.repository.UserRepository userRepository;

    public Map<String, Object> getAllEvents() {
        logger.debug("Fetching all active community events");
        List<CommunityEvent> events = eventRepository.findByIsActiveAndIsCancelledOrderByEventDateAsc(true, false);
        
        Map<String, Object> response = new HashMap<>();
        response.put("events", events);
        response.put("total", events.size());
        return response;
    }

    public Map<String, Object> getCreatedEvents() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Fetching events created by user: {}", principal);

        List<CommunityEvent> events;
        try {
            // Try to parse as userId (Long)
            Long userId = null;
            try {
                userId = Long.valueOf(principal);
            } catch (NumberFormatException e) {
                // Not a userId, treat as email
            }

            if (userId != null) {
                events = eventRepository.findByOrganizerIdOrderByEventDateDesc(String.valueOf(userId));
            } else {
                // Try to resolve email to userId using injected userRepository
                rw.health.ubuzima.entity.User user = null;
                try {
                    user = userRepository.findByEmail(principal).orElse(null);
                } catch (Exception ignored) {}
                if (user != null) {
                    events = eventRepository.findByOrganizerIdOrderByEventDateDesc(String.valueOf(user.getId()));
                } else {
                    events = List.of();
                }
            }
        } catch (Exception ex) {
            logger.error("Error resolving organizer for created events", ex);
            events = List.of();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("events", events);
        response.put("total", events.size());
        return response;
    }
}

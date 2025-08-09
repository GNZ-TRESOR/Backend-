package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.SupportGroup;
import rw.health.ubuzima.entity.ForumTopic;
import rw.health.ubuzima.entity.CommunityEvent;
import rw.health.ubuzima.entity.SupportGroupMember;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.repository.SupportGroupRepository;
import rw.health.ubuzima.repository.ForumTopicRepository;
import rw.health.ubuzima.repository.CommunityEventRepository;
import rw.health.ubuzima.repository.SupportGroupMemberRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommunityController {

    private final SupportGroupRepository supportGroupRepository;
    private final ForumTopicRepository forumTopicRepository;
    private final CommunityEventRepository communityEventRepository;
    private final SupportGroupMemberRepository supportGroupMemberRepository;
    private final UserRepository userRepository;

    // ============ TEST ENDPOINT ============
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testCommunity() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Community controller is working!",
            "timestamp", LocalDateTime.now()
        ));
    }
    
    // ============ COMMUNITY OVERVIEW ============
    
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getCommunityOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();

            // Real community statistics from database
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalGroups", supportGroupRepository.countActiveGroups());
            stats.put("totalTopics", forumTopicRepository.countActiveTopics());
            stats.put("totalEvents", communityEventRepository.countActiveEvents());
            stats.put("upcomingEvents", communityEventRepository.countUpcomingEvents(LocalDateTime.now()));

            overview.put("stats", stats);

            // Get real data for highlights
            List<SupportGroup> popularGroups = supportGroupRepository.findPopularGroups().stream().limit(3).toList();
            List<ForumTopic> popularTopics = forumTopicRepository.findPopularTopics().stream().limit(3).toList();
            List<CommunityEvent> upcomingEvents = communityEventRepository.findUpcomingEvents(LocalDateTime.now()).stream().limit(3).toList();

            overview.put("popularGroups", popularGroups);
            overview.put("popularTopics", popularTopics);
            overview.put("upcomingEvents", upcomingEvents);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "overview", overview
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load community overview: " + e.getMessage()
            ));
        }
    }

    // ============ SUPPORT GROUPS ============
    
    @GetMapping("/support-groups")
    public ResponseEntity<Map<String, Object>> getSupportGroups(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        try {
            System.out.println("🔍 Getting support groups from database...");

            List<SupportGroup> groups;

            if (search != null && !search.isEmpty()) {
                groups = supportGroupRepository.searchActiveGroups(search);
            } else if (category != null && !category.equals("all")) {
                groups = supportGroupRepository.findByCategoryAndIsActiveTrueOrderByMemberCountDesc(category);
            } else {
                groups = supportGroupRepository.findByIsActiveTrueOrderByCreatedAtDesc();
            }

            System.out.println("📊 Found " + groups.size() + " groups");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "groups", groups
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in getSupportGroups: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load support groups: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/support-groups")
    public ResponseEntity<Map<String, Object>> createSupportGroup(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("➕ Creating support group with data: " + request);

            // Extract required fields
            String name = (String) request.get("name");
            String category = (String) request.get("category");
            Integer creatorId = (Integer) request.get("creatorId");

            if (name == null || name.trim().isEmpty() || category == null || creatorId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing required fields: name, category, creatorId"
                ));
            }

            // Get creator user
            Optional<User> creatorOpt = userRepository.findById(Long.valueOf(creatorId));
            if (creatorOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Creator user not found"
                ));
            }

            // Create support group
            SupportGroup group = new SupportGroup();
            group.setName(name.trim());
            group.setCategory(category);
            group.setCreator(creatorOpt.get());
            group.setDescription((String) request.get("description"));
            group.setContactInfo((String) request.get("contactInfo"));
            group.setMeetingLocation((String) request.get("meetingLocation"));
            group.setMeetingSchedule((String) request.get("meetingSchedule"));
            group.setMaxMembers((Integer) request.get("maxMembers"));
            group.setIsPrivate((Boolean) request.getOrDefault("isPrivate", false));
            group.setIsActive((Boolean) request.getOrDefault("isActive", true));
            group.setMemberCount(1); // Creator is first member

            // Handle tags
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) request.get("tags");
            if (tags != null && !tags.isEmpty()) {
                group.setTags(tags);
            }

            SupportGroup savedGroup = supportGroupRepository.save(group);

            // Create membership for creator
            SupportGroupMember creatorMembership = new SupportGroupMember();
            creatorMembership.setGroup(savedGroup);
            creatorMembership.setUser(creatorOpt.get());
            creatorMembership.setRole("ADMIN");
            creatorMembership.setIsActive(true);
            creatorMembership.setJoinedAt(LocalDateTime.now());
            creatorMembership.setLastActivityAt(LocalDateTime.now());
            supportGroupMemberRepository.save(creatorMembership);

            System.out.println("✅ Support group created successfully with ID: " + savedGroup.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support group created successfully",
                "group", savedGroup
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in createSupportGroup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create support group: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/support-groups/{groupId}")
    public ResponseEntity<Map<String, Object>> updateSupportGroup(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        try {
            System.out.println("✏️ Updating support group " + groupId + " with data: " + request);

            Optional<SupportGroup> groupOpt = supportGroupRepository.findById(groupId);
            if (groupOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SupportGroup group = groupOpt.get();

            // Update fields if provided
            if (request.containsKey("name")) {
                group.setName((String) request.get("name"));
            }
            if (request.containsKey("description")) {
                group.setDescription((String) request.get("description"));
            }
            if (request.containsKey("category")) {
                group.setCategory((String) request.get("category"));
            }
            if (request.containsKey("contactInfo")) {
                group.setContactInfo((String) request.get("contactInfo"));
            }
            if (request.containsKey("meetingLocation")) {
                group.setMeetingLocation((String) request.get("meetingLocation"));
            }
            if (request.containsKey("meetingSchedule")) {
                group.setMeetingSchedule((String) request.get("meetingSchedule"));
            }
            if (request.containsKey("maxMembers")) {
                group.setMaxMembers((Integer) request.get("maxMembers"));
            }
            if (request.containsKey("isPrivate")) {
                group.setIsPrivate((Boolean) request.get("isPrivate"));
            }
            if (request.containsKey("isActive")) {
                group.setIsActive((Boolean) request.get("isActive"));
            }
            if (request.containsKey("tags")) {
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) request.get("tags");
                group.setTags(tags);
            }

            SupportGroup updatedGroup = supportGroupRepository.save(group);

            System.out.println("✅ Support group updated successfully");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support group updated successfully",
                "group", updatedGroup
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in updateSupportGroup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update support group: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/support-groups/{groupId}")
    public ResponseEntity<Map<String, Object>> deleteSupportGroup(@PathVariable Long groupId) {
        try {
            System.out.println("🗑️ Deleting support group " + groupId);

            Optional<SupportGroup> groupOpt = supportGroupRepository.findById(groupId);
            if (groupOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SupportGroup group = groupOpt.get();

            // Soft delete - set as inactive
            group.setIsActive(false);
            supportGroupRepository.save(group);

            System.out.println("✅ Support group deleted successfully");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support group deleted successfully"
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in deleteSupportGroup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete support group: " + e.getMessage()
            ));
        }
    }

    // ============ FORUM TOPICS ============
    
    @GetMapping("/forum/topics")
    public ResponseEntity<Map<String, Object>> getForumTopics(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter) {
        try {
            System.out.println("🔍 Getting forum topics from database...");

            List<ForumTopic> topics;

            if (search != null && !search.isEmpty()) {
                topics = forumTopicRepository.searchActiveTopics(search);
            } else if (category != null && !category.equals("all")) {
                topics = forumTopicRepository.findByCategoryAndIsActiveTrueOrderByLastActivityAtDesc(category);
            } else if ("popular".equals(filter)) {
                topics = forumTopicRepository.findPopularTopics();
            } else if ("pinned".equals(filter)) {
                topics = forumTopicRepository.findPinnedTopics();
            } else {
                topics = forumTopicRepository.findByIsActiveTrueOrderByLastActivityAtDesc();
            }

            System.out.println("📊 Found " + topics.size() + " topics");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "topics", topics
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in getForumTopics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load forum topics: " + e.getMessage()
            ));
        }
    }
    
    // ============ COMMUNITY EVENTS ============
    
    @GetMapping("/events")
    public ResponseEntity<Map<String, Object>> getCommunityEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search) {
        try {
            System.out.println("🔍 Getting community events from database...");

            List<CommunityEvent> events;

            if (search != null && !search.isEmpty()) {
                events = communityEventRepository.searchEvents(search);
            } else if (type != null && !type.equals("all")) {
                events = communityEventRepository.findByTypeAndActive(type);
            } else if ("upcoming".equals(filter)) {
                events = communityEventRepository.findUpcomingEvents(LocalDateTime.now());
            } else if ("past".equals(filter)) {
                events = communityEventRepository.findPastEvents(LocalDateTime.now());
            } else if ("registration".equals(filter)) {
                events = communityEventRepository.findEventsOpenForRegistration(LocalDateTime.now());
            } else {
                events = communityEventRepository.findByIsActiveTrueAndIsCancelledFalseOrderByEventDateAsc();
            }

            System.out.println("📊 Found " + events.size() + " events");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "events", events
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in getCommunityEvents: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load community events: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/events/my-events")
    public ResponseEntity<Map<String, Object>> getMyEvents() {
        try {
            System.out.println("🔍 Getting user's registered events...");

            // For now, return empty list - will be implemented with user authentication
            List<CommunityEvent> myEvents = new ArrayList<>();

            System.out.println("📊 Found " + myEvents.size() + " registered events");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "events", myEvents
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in getMyEvents: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load my events: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/events/{eventId}/register")
    public ResponseEntity<Map<String, Object>> registerForEvent(@PathVariable Long eventId) {
        try {
            System.out.println("🎯 Registering for event ID: " + eventId);

            // For now, just return success - will be implemented with user authentication
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully registered for event"
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in registerForEvent: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to register for event: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/events/{eventId}/register")
    public ResponseEntity<Map<String, Object>> cancelEventRegistration(@PathVariable Long eventId) {
        try {
            System.out.println("🚫 Canceling registration for event ID: " + eventId);

            // For now, just return success - will be implemented with user authentication
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully canceled event registration"
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in cancelEventRegistration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to cancel event registration: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Map<String, Object> eventData) {
        try {
            System.out.println("➕ Creating new event: " + eventData.get("title"));

            // For now, just return success - will be implemented with proper event creation
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Event created successfully",
                "eventId", 1L
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in createEvent: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create event: " + e.getMessage()
            ));
        }
    }

    // ============ MOCK DATA METHODS ============
    
    private List<Map<String, Object>> getMockSupportGroups() {
        List<Map<String, Object>> groups = new ArrayList<>();
        
        Map<String, Object> group1 = new HashMap<>();
        group1.put("id", "1");
        group1.put("name", "Itsinda ry'abagore biga kubana n'ubwiyunge");
        group1.put("description", "Support Group for Women Family Planning");
        group1.put("category", "Family Planning");
        group1.put("creatorName", "Dr. Marie Uwimana");
        group1.put("memberCount", 156);
        group1.put("maxMembers", 200);
        group1.put("isPrivate", false);
        group1.put("isActive", true);
        group1.put("meetingSchedule", "Ku wa gatatu buri cyumweru saa kumi n'ebyiri");
        group1.put("meetingLocation", "Kimisagara Health Center");
        group1.put("contactInfo", "+250788111222");
        group1.put("tags", new ArrayList<>());
        group1.put("createdAt", LocalDateTime.now().minusDays(30));
        group1.put("updatedAt", LocalDateTime.now().minusDays(1));
        groups.add(group1);
        
        Map<String, Object> group2 = new HashMap<>();
        group2.put("id", "2");
        group2.put("name", "Ikiganiro cy'ubuzima");
        group2.put("description", "Health Discussion Forum");
        group2.put("category", "Health");
        group2.put("creatorName", "System Administrator");
        group2.put("memberCount", 89);
        group2.put("maxMembers", 150);
        group2.put("isPrivate", false);
        group2.put("isActive", true);
        group2.put("meetingSchedule", "Ku wa kane buri cyumweru saa kumi");
        group2.put("meetingLocation", "Online Platform");
        group2.put("contactInfo", "ubuzima@forum.rw");
        group2.put("tags", new ArrayList<>());
        group2.put("createdAt", LocalDateTime.now().minusDays(35));
        group2.put("updatedAt", LocalDateTime.now().minusDays(2));
        groups.add(group2);
        
        Map<String, Object> group3 = new HashMap<>();
        group3.put("id", "3");
        group3.put("name", "Abana b'urubyiruko");
        group3.put("description", "Youth Health Group");
        group3.put("category", "Youth Health");
        group3.put("creatorName", "Grace Mukamana");
        group3.put("memberCount", 67);
        group3.put("maxMembers", 100);
        group3.put("isPrivate", false);
        group3.put("isActive", true);
        group3.put("meetingSchedule", "Ku wa mbere buri cyumweru saa kumi n'ebyiri");
        group3.put("meetingLocation", "Nyarugenge Health Center");
        group3.put("contactInfo", "+250788555666");
        group3.put("tags", new ArrayList<>());
        group3.put("createdAt", LocalDateTime.now().minusDays(40));
        group3.put("updatedAt", LocalDateTime.now().minusDays(3));
        groups.add(group3);
        
        return groups;
    }

    private List<Map<String, Object>> getMockForumTopics() {
        List<Map<String, Object>> topics = new ArrayList<>();

        Map<String, Object> topic1 = new HashMap<>();
        topic1.put("id", "1");
        topic1.put("title", "Itsinda ry'abagore biga kubana n'ubwiyunge");
        topic1.put("content", "Itsinda ry'abagore biga kubana n'ubwiyunge");
        topic1.put("category", "Family Planning");
        topic1.put("authorName", "Dr. Marie Uwimana");
        topic1.put("viewCount", 245);
        topic1.put("replyCount", 23);
        topic1.put("isPinned", true);
        topic1.put("isLocked", false);
        topic1.put("isActive", true);
        topic1.put("tags", new ArrayList<>());
        topic1.put("lastActivityAt", LocalDateTime.now().minusHours(2));
        topic1.put("lastReplyByName", "Grace Mukamana");
        topic1.put("createdAt", LocalDateTime.now().minusDays(10));
        topic1.put("updatedAt", LocalDateTime.now().minusHours(2));
        topics.add(topic1);

        Map<String, Object> topic2 = new HashMap<>();
        topic2.put("id", "2");
        topic2.put("title", "Ikiganiro cy'ubuzima bw'imyororokere");
        topic2.put("content", "Ikiganiro gishya kuri ubuzima bw'imyororokere");
        topic2.put("category", "Health");
        topic2.put("authorName", "System Administrator");
        topic2.put("viewCount", 189);
        topic2.put("replyCount", 15);
        topic2.put("isPinned", false);
        topic2.put("isLocked", false);
        topic2.put("isActive", true);
        topic2.put("tags", new ArrayList<>());
        topic2.put("lastActivityAt", LocalDateTime.now().minusHours(4));
        topic2.put("lastReplyByName", "Dr. Marie Uwimana");
        topic2.put("createdAt", LocalDateTime.now().minusDays(8));
        topic2.put("updatedAt", LocalDateTime.now().minusHours(4));
        topics.add(topic2);

        return topics;
    }

    private List<Map<String, Object>> getMockCommunityEvents() {
        List<Map<String, Object>> events = new ArrayList<>();

        Map<String, Object> event1 = new HashMap<>();
        event1.put("id", "1");
        event1.put("title", "Ubwiyunge bw'urubyiruko");
        event1.put("description", "Youth Family Planning Workshop");
        event1.put("type", "WORKSHOP");
        event1.put("organizerName", "Dr. Marie Uwimana");
        event1.put("eventDate", LocalDateTime.now().plusDays(7));
        event1.put("endDate", LocalDateTime.now().plusDays(7).plusHours(3));
        event1.put("location", "Kimisagara Health Center");
        event1.put("maxParticipants", 50);
        event1.put("currentParticipants", 8);
        event1.put("registrationRequired", true);
        event1.put("registrationDeadline", LocalDateTime.now().plusDays(5));
        event1.put("isVirtual", false);
        event1.put("virtualLink", null);
        event1.put("isActive", true);
        event1.put("isCancelled", false);
        event1.put("contactInfo", "+250788111222");
        event1.put("createdAt", LocalDateTime.now().minusDays(7));
        event1.put("updatedAt", LocalDateTime.now().minusDays(1));
        events.add(event1);

        Map<String, Object> event2 = new HashMap<>();
        event2.put("id", "2");
        event2.put("title", "Inama y'ubuzima");
        event2.put("description", "Health Education Workshop");
        event2.put("type", "SEMINAR");
        event2.put("organizerName", "System Administrator");
        event2.put("eventDate", LocalDateTime.now().plusDays(14));
        event2.put("endDate", LocalDateTime.now().plusDays(14).plusHours(4));
        event2.put("location", "Nyarugenge Health Center");
        event2.put("maxParticipants", 100);
        event2.put("currentParticipants", 23);
        event2.put("registrationRequired", true);
        event2.put("registrationDeadline", LocalDateTime.now().plusDays(10));
        event2.put("isVirtual", false);
        event2.put("virtualLink", null);
        event2.put("isActive", true);
        event2.put("isCancelled", false);
        event2.put("contactInfo", "+250788555666");
        event2.put("createdAt", LocalDateTime.now().minusDays(14));
        event2.put("updatedAt", LocalDateTime.now().minusDays(2));
        events.add(event2);

        return events;
    }

    // ============ SUPPORT GROUP MEMBERS ============

    @GetMapping("/support-groups/{groupId}/members")
    public ResponseEntity<Map<String, Object>> getSupportGroupMembers(@PathVariable Long groupId) {
        try {
            System.out.println("🔍 Getting members for support group " + groupId);

            Optional<SupportGroup> groupOpt = supportGroupRepository.findById(groupId);
            if (groupOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SupportGroup group = groupOpt.get();
            List<SupportGroupMember> members = supportGroupMemberRepository
                .findByGroupAndIsActiveTrueOrderByJoinedAtAsc(group);

            System.out.println("📊 Found " + members.size() + " members");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "members", members
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in getSupportGroupMembers: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load group members: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/support-groups/{groupId}/members")
    public ResponseEntity<Map<String, Object>> addSupportGroupMember(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        try {
            System.out.println("➕ Adding member to support group " + groupId);

            Long userId = Long.valueOf(request.get("userId").toString());

            Optional<SupportGroup> groupOpt = supportGroupRepository.findById(groupId);
            Optional<User> userOpt = userRepository.findById(userId);

            if (groupOpt.isEmpty() || userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Group or user not found"
                ));
            }

            SupportGroup group = groupOpt.get();
            User user = userOpt.get();

            // Check if user is already a member
            if (supportGroupMemberRepository.existsByGroupAndUserAndIsActiveTrue(group, user)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User is already a member of this group"
                ));
            }

            // Create new membership
            SupportGroupMember member = new SupportGroupMember();
            member.setGroup(group);
            member.setUser(user);
            member.setRole("MEMBER");
            member.setIsActive(true);
            member.setJoinedAt(LocalDateTime.now());
            member.setLastActivityAt(LocalDateTime.now());

            supportGroupMemberRepository.save(member);

            System.out.println("✅ Member added successfully");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Member added successfully",
                "member", member
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in addSupportGroupMember: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to add member: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/support-groups/{groupId}/members/{userId}")
    public ResponseEntity<Map<String, Object>> removeSupportGroupMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        try {
            System.out.println("➖ Removing member " + userId + " from support group " + groupId);

            Optional<SupportGroup> groupOpt = supportGroupRepository.findById(groupId);
            Optional<User> userOpt = userRepository.findById(userId);

            if (groupOpt.isEmpty() || userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SupportGroup group = groupOpt.get();
            User user = userOpt.get();

            Optional<SupportGroupMember> memberOpt = supportGroupMemberRepository
                .findByGroupAndUserAndIsActiveTrue(group, user);

            if (memberOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User is not a member of this group"
                ));
            }

            SupportGroupMember member = memberOpt.get();
            member.setIsActive(false);
            supportGroupMemberRepository.save(member);

            System.out.println("✅ Member removed successfully");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Member removed successfully"
            ));

        } catch (Exception e) {
            System.out.println("❌ Error in removeSupportGroupMember: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to remove member: " + e.getMessage()
            ));
        }
    }
}

// Additional controller for direct endpoints that frontend expects
@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
class DirectCommunityController {

    private final SupportGroupRepository supportGroupRepository;
    private final UserRepository userRepository;

    // Direct endpoint for support groups
    @GetMapping("/support-groups")
    public ResponseEntity<Map<String, Object>> getSupportGroups() {
        try {
            List<SupportGroup> groups = supportGroupRepository.findByIsActiveTrueOrderByCreatedAtDesc();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "supportGroups", groups
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load support groups: " + e.getMessage()
            ));
        }
    }

    // Direct endpoint for creating support groups
    @PostMapping("/support-groups")
    public ResponseEntity<Map<String, Object>> createSupportGroup(@RequestBody Map<String, Object> request) {
        try {
            SupportGroup group = new SupportGroup();
            group.setName(request.get("name").toString());
            group.setDescription(request.get("description").toString());
            group.setCategory(request.get("category") != null ? request.get("category").toString() : "General");
            group.setMaxMembers((Integer) request.getOrDefault("maxMembers", 20));
            group.setIsActive(true);

            // Set creator if provided
            if (request.get("facilitatorId") != null) {
                Long facilitatorId = Long.valueOf(request.get("facilitatorId").toString());
                User facilitator = userRepository.findById(facilitatorId).orElse(null);
                group.setCreator(facilitator);
            }

            SupportGroup savedGroup = supportGroupRepository.save(group);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Support group created successfully",
                "supportGroup", savedGroup
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create support group: " + e.getMessage()
            ));
        }
    }

    // Direct endpoint for tickets
    @PostMapping("/tickets")
    public ResponseEntity<Map<String, Object>> createTicket(@RequestBody Map<String, Object> request) {
        try {
            // This will be handled by the SupportTicketController's new endpoint
            // Just return success for now
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket creation endpoint available at /support-tickets/tickets"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create ticket: " + e.getMessage()
            ));
        }
    }
}

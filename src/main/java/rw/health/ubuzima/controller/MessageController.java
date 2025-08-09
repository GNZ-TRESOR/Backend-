package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.Message;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.MessageType;
import rw.health.ubuzima.enums.MessagePriority;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.repository.MessageRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String conversationId,
            @RequestParam(required = false) Long userId) {
        
        try {
            if (conversationId != null) {
                List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "messages", messages
                ));
            }

            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found"
                    ));
                }

                Pageable pageable = PageRequest.of(page, limit);
                Page<Message> messagesPage = messageRepository.findMessagesByUser(user, pageable);

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "messages", messagesPage.getContent(),
                    "total", messagesPage.getTotalElements(),
                    "page", page,
                    "totalPages", messagesPage.getTotalPages()
                ));
            }

            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Either conversationId or userId is required"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch messages: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            Long senderId = Long.valueOf(request.get("senderId").toString());
            Long receiverId = Long.valueOf(request.get("receiverId").toString());
            
            User sender = userRepository.findById(senderId).orElse(null);
            User receiver = userRepository.findById(receiverId).orElse(null);
            
            if (sender == null || receiver == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Sender or receiver not found"
                ));
            }

            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(request.get("content").toString());
            
            // Generate conversation ID if not provided
            String conversationId = request.get("conversationId") != null 
                ? request.get("conversationId").toString()
                : generateConversationId(senderId, receiverId);
            message.setConversationId(conversationId);
            
            if (request.get("messageType") != null) {
                message.setMessageType(MessageType.valueOf(request.get("messageType").toString().toUpperCase()));
            }
            
            if (request.get("priority") != null) {
                message.setPriority(MessagePriority.valueOf(request.get("priority").toString().toUpperCase()));
            }
            
            if (request.get("isEmergency") != null) {
                message.setIsEmergency(Boolean.valueOf(request.get("isEmergency").toString()));
            }

            Message savedMessage = messageRepository.save(message);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message sent successfully",
                "messageData", savedMessage
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to send message: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markMessageAsRead(@PathVariable Long id) {
        try {
            Message message = messageRepository.findById(id).orElse(null);
            
            if (message == null) {
                return ResponseEntity.notFound().build();
            }

            message.setIsRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message marked as read"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to mark message as read: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    public ResponseEntity<Map<String, Object>> getConversationMessages(
            @PathVariable Long userId1,
            @PathVariable Long userId2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            User user1 = userRepository.findById(userId1).orElse(null);
            User user2 = userRepository.findById(userId2).orElse(null);

            if (user1 == null || user2 == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "One or both users not found"
                ));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messagesPage = messageRepository.findConversationMessages(user1, user2, pageable);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "messages", messagesPage.getContent(),
                "totalPages", messagesPage.getTotalPages(),
                "totalElements", messagesPage.getTotalElements(),
                "currentPage", page
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch conversation: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> updateMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, Object> updateData) {
        try {
            Message message = messageRepository.findById(messageId).orElse(null);

            if (message == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Message not found"
                ));
            }

            // Only sender can edit message
            Long requesterId = Long.valueOf(updateData.get("requesterId").toString());
            if (!message.getSender().getId().equals(requesterId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You can only edit your own messages"
                ));
            }

            if (updateData.containsKey("content")) {
                message.setContent(updateData.get("content").toString());
                message.setUpdatedAt(LocalDateTime.now());
            }

            Message updatedMessage = messageRepository.save(message);

            // Fetch the updated message with eager loading to avoid lazy loading issues
            Message freshMessage = messageRepository.findByIdWithUsers(messageId);
            if (freshMessage == null) {
                freshMessage = updatedMessage;
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message updated successfully",
                "data", freshMessage
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update message: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long requesterId) {
        try {
            Message message = messageRepository.findById(messageId).orElse(null);

            if (message == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Message not found"
                ));
            }

            // Only sender can delete message
            if (!message.getSender().getId().equals(requesterId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You can only delete your own messages"
                ));
            }

            messageRepository.delete(message);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete message: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<Map<String, Object>> getConversations(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Get existing conversation partners
            List<User> conversationPartners = messageRepository.findConversationPartners(user);

            // Enhance conversation data with metadata
            List<Map<String, Object>> enrichedConversations = conversationPartners.stream()
                .map(partner -> {
                    Map<String, Object> conversation = new HashMap<>();

                    // Basic user info
                    conversation.put("id", partner.getId());
                    conversation.put("name", partner.getName());
                    conversation.put("email", partner.getEmail());
                    conversation.put("phone", partner.getPhone());
                    conversation.put("role", partner.getRole());
                    conversation.put("status", partner.getStatus());
                    conversation.put("district", partner.getDistrict());
                    conversation.put("sector", partner.getSector());
                    conversation.put("cell", partner.getCell());
                    conversation.put("village", partner.getVillage());

                    // Get conversation metadata
                    List<Message> conversationMessages = messageRepository.findConversationBetweenUsers(user, partner);

                    if (!conversationMessages.isEmpty()) {
                        // Get last message
                        Message lastMessage = conversationMessages.get(conversationMessages.size() - 1);
                        conversation.put("lastMessage", lastMessage.getContent());
                        conversation.put("lastMessageTime", lastMessage.getCreatedAt().toString());
                        conversation.put("lastMessageType", lastMessage.getMessageType());

                        // Count unread messages from this partner
                        long unreadCount = conversationMessages.stream()
                            .filter(msg -> msg.getSender().equals(partner) && !msg.getIsRead())
                            .count();
                        conversation.put("unreadCount", unreadCount);
                    } else {
                        conversation.put("lastMessage", "");
                        conversation.put("lastMessageTime", null);
                        conversation.put("lastMessageType", null);
                        conversation.put("unreadCount", 0);
                    }

                    // Add online status (placeholder for now)
                    conversation.put("isOnline", false);

                    return conversation;
                })
                .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "conversations", enrichedConversations
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch conversations: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/available-users")
    public ResponseEntity<Map<String, Object>> getAvailableUsers(@RequestParam Long userId) {
        try {
            User currentUser = userRepository.findById(userId).orElse(null);

            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Get all users except current user and admins
            List<User> availableUsers = userRepository.findByRoleNotAndIdNot(UserRole.ADMIN, userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "users", availableUsers
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch available users: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadMessages(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<Message> unreadMessages = messageRepository.findUnreadMessages(user);
            Long unreadCount = messageRepository.countUnreadMessages(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "unreadMessages", unreadMessages,
                "unreadCount", unreadCount
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch unread messages: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/emergency")
    public ResponseEntity<Map<String, Object>> getEmergencyMessages(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<Message> emergencyMessages = messageRepository.findEmergencyMessages(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "emergencyMessages", emergencyMessages
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch emergency messages: " + e.getMessage()
            ));
        }
    }

    private String generateConversationId(Long userId1, Long userId2) {
        // Create consistent conversation ID regardless of order
        Long smaller = Math.min(userId1, userId2);
        Long larger = Math.max(userId1, userId2);
        return "conv_" + smaller + "_" + larger;
    }
}

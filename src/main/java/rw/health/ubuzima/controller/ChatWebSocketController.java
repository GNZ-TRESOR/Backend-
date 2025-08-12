package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import rw.health.ubuzima.entity.Message;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.MessageType;
import rw.health.ubuzima.repository.MessageRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket controller for real-time chat features
 * Handles typing indicators, message delivery, and real-time updates
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Handle real-time message sending
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Map<String, Object> messageData) {
        try {
            Long senderId = Long.valueOf(messageData.get("senderId").toString());
            Long receiverId = Long.valueOf(messageData.get("receiverId").toString());
            String content = messageData.get("content").toString();
            String conversationId = messageData.get("conversationId").toString();

            User sender = userRepository.findById(senderId).orElse(null);
            User receiver = userRepository.findById(receiverId).orElse(null);

            if (sender == null || receiver == null) {
                log.error("Invalid sender or receiver for real-time message");
                return;
            }

            // Create and save message
            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(content);
            message.setConversationId(conversationId);
            message.setMessageType(MessageType.TEXT);
            message.setMessageStatus("SENT");

            Message savedMessage = messageRepository.save(message);

            // Prepare message data for real-time delivery
            Map<String, Object> realTimeMessage = Map.of(
                "id", savedMessage.getId(),
                "senderId", senderId,
                "receiverId", receiverId,
                "content", content,
                "conversationId", conversationId,
                "messageType", "TEXT",
                "status", "SENT",
                "timestamp", savedMessage.getCreatedAt().toString(),
                "senderName", sender.getName()
            );

            // Send to specific conversation
            messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId, 
                realTimeMessage
            );

            // Send to receiver's personal queue
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                realTimeMessage
            );

            log.info("Real-time message sent: {} -> {}", senderId, receiverId);

        } catch (Exception e) {
            log.error("Error sending real-time message", e);
        }
    }

    /**
     * Handle typing indicators
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> typingData) {
        try {
            String conversationId = typingData.get("conversationId").toString();
            Long userId = Long.valueOf(typingData.get("userId").toString());
            Boolean isTyping = Boolean.valueOf(typingData.get("isTyping").toString());
            String userName = typingData.get("userName").toString();

            Map<String, Object> typingIndicator = Map.of(
                "userId", userId,
                "userName", userName,
                "isTyping", isTyping,
                "conversationId", conversationId,
                "timestamp", LocalDateTime.now().toString()
            );

            // Broadcast typing indicator to conversation
            messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId + "/typing",
                typingIndicator
            );

            log.debug("Typing indicator: {} is {} in conversation {}", 
                userName, isTyping ? "typing" : "stopped typing", conversationId);

        } catch (Exception e) {
            log.error("Error handling typing indicator", e);
        }
    }

    /**
     * Handle message status updates (delivered, read)
     */
    @MessageMapping("/chat.messageStatus")
    public void updateMessageStatus(@Payload Map<String, Object> statusData) {
        try {
            Long messageId = Long.valueOf(statusData.get("messageId").toString());
            String status = statusData.get("status").toString();
            Long userId = Long.valueOf(statusData.get("userId").toString());

            Message message = messageRepository.findById(messageId).orElse(null);
            if (message == null) {
                log.error("Message not found for status update: {}", messageId);
                return;
            }

            // Update message status
            message.setMessageStatus(status.toUpperCase());
            if ("DELIVERED".equals(status.toUpperCase())) {
                message.setDeliveredAt(LocalDateTime.now());
            } else if ("READ".equals(status.toUpperCase())) {
                message.setIsRead(true);
                message.setReadAt(LocalDateTime.now());
            }

            messageRepository.save(message);

            // Notify sender about status change
            Map<String, Object> statusUpdate = Map.of(
                "messageId", messageId,
                "status", status.toUpperCase(),
                "timestamp", LocalDateTime.now().toString(),
                "conversationId", message.getConversationId()
            );

            messagingTemplate.convertAndSendToUser(
                message.getSender().getId().toString(),
                "/queue/status",
                statusUpdate
            );

            log.debug("Message status updated: {} -> {}", messageId, status);

        } catch (Exception e) {
            log.error("Error updating message status", e);
        }
    }

    /**
     * Handle user online/offline status
     */
    @MessageMapping("/chat.userStatus")
    public void updateUserStatus(@Payload Map<String, Object> statusData) {
        try {
            Long userId = Long.valueOf(statusData.get("userId").toString());
            Boolean isOnline = Boolean.valueOf(statusData.get("isOnline").toString());
            String userName = statusData.get("userName").toString();

            Map<String, Object> userStatus = Map.of(
                "userId", userId,
                "userName", userName,
                "isOnline", isOnline,
                "lastSeen", LocalDateTime.now().toString()
            );

            // Broadcast user status to all conversations they're part of
            messagingTemplate.convertAndSend("/topic/userStatus", userStatus);

            log.debug("User status updated: {} is {}", userName, isOnline ? "online" : "offline");

        } catch (Exception e) {
            log.error("Error updating user status", e);
        }
    }

    /**
     * Handle message reactions
     */
    @MessageMapping("/chat.reaction")
    public void addReaction(@Payload Map<String, Object> reactionData) {
        try {
            Long messageId = Long.valueOf(reactionData.get("messageId").toString());
            String reaction = reactionData.get("reaction").toString();
            Long userId = Long.valueOf(reactionData.get("userId").toString());

            Message message = messageRepository.findById(messageId).orElse(null);
            if (message == null) {
                log.error("Message not found for reaction: {}", messageId);
                return;
            }

            // Update message with reaction
            message.setReaction(reaction);
            messageRepository.save(message);

            // Broadcast reaction to conversation
            Map<String, Object> reactionUpdate = Map.of(
                "messageId", messageId,
                "reaction", reaction,
                "userId", userId,
                "conversationId", message.getConversationId(),
                "timestamp", LocalDateTime.now().toString()
            );

            messagingTemplate.convertAndSend(
                "/topic/conversation/" + message.getConversationId() + "/reactions",
                reactionUpdate
            );

            log.debug("Reaction added: {} to message {}", reaction, messageId);

        } catch (Exception e) {
            log.error("Error adding reaction", e);
        }
    }

    /**
     * Handle voice message notifications
     */
    @MessageMapping("/chat.voiceMessage")
    public void handleVoiceMessage(@Payload Map<String, Object> voiceData) {
        try {
            String conversationId = voiceData.get("conversationId").toString();
            Long senderId = Long.valueOf(voiceData.get("senderId").toString());
            Long receiverId = Long.valueOf(voiceData.get("receiverId").toString());
            String audioUrl = voiceData.get("audioUrl").toString();
            Integer duration = Integer.valueOf(voiceData.get("duration").toString());

            Map<String, Object> voiceMessage = Map.of(
                "senderId", senderId,
                "receiverId", receiverId,
                "conversationId", conversationId,
                "messageType", "AUDIO",
                "audioUrl", audioUrl,
                "duration", duration,
                "timestamp", LocalDateTime.now().toString()
            );

            // Send to conversation
            messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                voiceMessage
            );

            // Send to receiver
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                voiceMessage
            );

            log.info("Voice message notification sent: {} -> {}", senderId, receiverId);

        } catch (Exception e) {
            log.error("Error handling voice message notification", e);
        }
    }
}

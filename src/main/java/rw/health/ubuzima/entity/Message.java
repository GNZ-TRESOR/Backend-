package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import rw.health.ubuzima.enums.MessageType;
import rw.health.ubuzima.enums.MessagePriority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "healthRecord", "appointments", "passwordHash"})
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "healthRecord", "appointments", "passwordHash"})
    private User receiver;

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private MessagePriority priority = MessagePriority.NORMAL;

    @ElementCollection
    @CollectionTable(name = "message_attachments", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "attachment_url")
    private List<String> attachments = new ArrayList<>();

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_emergency")
    private Boolean isEmergency = false;

    @Column(name = "reply_to_id")
    private Long replyToId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data
}

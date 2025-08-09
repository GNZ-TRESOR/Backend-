package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "forum_topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumTopic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false)
    private String category; // Family Planning, Health, General, etc.
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;
    
    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;
    
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @ElementCollection
    @CollectionTable(name = "forum_topic_tags", joinColumns = @JoinColumn(name = "topic_id"))
    @Column(name = "tag")
    private List<String> tags;
    
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_reply_by")
    private User lastReplyBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

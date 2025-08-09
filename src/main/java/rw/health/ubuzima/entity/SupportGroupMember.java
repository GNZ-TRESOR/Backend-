package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_group_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportGroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SupportGroup group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String role = "MEMBER"; // MEMBER, MODERATOR, ADMIN
    
    @Column(name = "joined_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime joinedAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;
}

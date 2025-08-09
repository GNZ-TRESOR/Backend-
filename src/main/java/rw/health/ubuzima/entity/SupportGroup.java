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
@Table(name = "support_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String category; // Family Planning, Youth Health, Parenting, Mental Health
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
    
    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 0;
    
    @Column(name = "max_members")
    private Integer maxMembers;
    
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "meeting_schedule")
    private String meetingSchedule; // e.g., "Weekly on Wednesdays at 7 PM"
    
    @Column(name = "meeting_location")
    private String meetingLocation;
    
    @Column(name = "contact_info")
    private String contactInfo;
    
    @ElementCollection
    @CollectionTable(name = "support_group_tags", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "tag")
    private List<String> tags;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

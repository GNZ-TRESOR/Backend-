package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String type; // WORKSHOP, SEMINAR, SUPPORT_MEETING, HEALTH_CAMP, etc.
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(nullable = false)
    private String location;
    
    @Column(name = "max_participants")
    private Integer maxParticipants;
    
    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;
    
    @Column(name = "registration_required", nullable = false)
    private Boolean registrationRequired = true;
    
    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;
    
    @Column(name = "is_virtual", nullable = false)
    private Boolean isVirtual = false;
    
    @Column(name = "virtual_link")
    private String virtualLink;
    
    @Column(name = "contact_info")
    private String contactInfo;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_cancelled", nullable = false)
    private Boolean isCancelled = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

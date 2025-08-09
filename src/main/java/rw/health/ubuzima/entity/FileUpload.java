package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_uploads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUpload {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(nullable = false)
    private String originalFilename;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String fileType;
    
    private String mimeType;
    
    @Column(nullable = false)
    private Long size;
    
    private String userId;

    // Education-specific fields
    private Long lessonId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string for additional metadata

    private String thumbnailUrl;

    private Integer duration; // For videos/audio in seconds

    private String resolution; // For videos (e.g., "1920x1080")

    private String category; // education, profile, health_document, etc.

    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}

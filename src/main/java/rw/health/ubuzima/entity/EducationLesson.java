package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.health.ubuzima.enums.EducationCategory;
import rw.health.ubuzima.enums.EducationLevel;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "education_lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EducationLesson extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private EducationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private EducationLevel level;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @ElementCollection
    @CollectionTable(name = "lesson_tags", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "audio_url")
    private String audioUrl;

    @ElementCollection
    @CollectionTable(name = "lesson_images", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "is_published")
    private Boolean isPublished = true;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "language")
    private String language = "rw"; // Default to Kinyarwanda

    @Column(name = "author")
    private String author;

    @Column(name = "order_index")
    private Integer orderIndex = 0;
}

package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "education_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EducationProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private EducationLesson lesson;

    @Column(name = "progress_percentage")
    private Double progressPercentage = 0.0;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes = 0;

    @Column(name = "quiz_score")
    private Double quizScore;

    @Column(name = "quiz_attempts")
    private Integer quizAttempts = 0;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

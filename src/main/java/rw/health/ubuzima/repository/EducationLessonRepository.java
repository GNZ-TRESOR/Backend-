package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.EducationLesson;
import rw.health.ubuzima.enums.EducationCategory;
import rw.health.ubuzima.enums.EducationLevel;

import java.util.List;

@Repository
public interface EducationLessonRepository extends JpaRepository<EducationLesson, Long> {
    
    List<EducationLesson> findByIsPublishedTrueOrderByOrderIndexAsc();
    
    List<EducationLesson> findByCategoryAndIsPublishedTrueOrderByOrderIndexAsc(EducationCategory category);
    
    List<EducationLesson> findByLevelAndIsPublishedTrueOrderByOrderIndexAsc(EducationLevel level);
    
    List<EducationLesson> findByCategoryAndLevelAndIsPublishedTrueOrderByOrderIndexAsc(EducationCategory category, EducationLevel level);
    
    List<EducationLesson> findByLanguageAndIsPublishedTrueOrderByOrderIndexAsc(String language);
    
    @Query("SELECT l FROM EducationLesson l WHERE l.isPublished = true AND (l.title LIKE %:searchTerm% OR l.description LIKE %:searchTerm%) ORDER BY l.orderIndex ASC")
    List<EducationLesson> searchLessons(@Param("searchTerm") String searchTerm);

    @Query("SELECT l FROM EducationLesson l WHERE l.isPublished = true AND " +
           "(l.title LIKE %:searchTerm% OR l.description LIKE %:searchTerm%) AND " +
           "(:category IS NULL OR l.category = :category) AND " +
           "(:level IS NULL OR l.level = :level) AND " +
           "(:language IS NULL OR l.language = :language) " +
           "ORDER BY l.orderIndex ASC")
    List<EducationLesson> searchLessonsWithFilters(
        @Param("searchTerm") String searchTerm,
        @Param("category") EducationCategory category,
        @Param("level") EducationLevel level,
        @Param("language") String language);
    
    @Query("SELECT l FROM EducationLesson l WHERE l.isPublished = true ORDER BY l.viewCount DESC")
    List<EducationLesson> findMostPopularLessons();
    
    List<EducationLesson> findByAuthorAndIsPublishedTrueOrderByCreatedAtDesc(String author);

    @Query("SELECT l FROM EducationLesson l WHERE l.isPublished = true AND l.id NOT IN :completedLessonIds ORDER BY l.viewCount DESC, l.createdAt DESC")
    List<EducationLesson> findRecommendedLessons(@Param("completedLessonIds") List<Long> completedLessonIds);

    Long countByIsPublishedTrue();

    Long countByCategoryAndIsPublishedTrue(EducationCategory category);

    @Query("SELECT l FROM EducationLesson l WHERE l.category = :category AND l.isPublished = true ORDER BY l.orderIndex ASC")
    List<EducationLesson> findByCategoryAndIsActiveTrue(@Param("category") EducationCategory category);

    // Additional method for FamilyPlanningController
    @Query("SELECT l FROM EducationLesson l WHERE l.category = :category AND l.title LIKE %:title% AND l.isPublished = true ORDER BY l.orderIndex ASC")
    List<EducationLesson> findByCategoryAndTitleContainingIgnoreCaseAndIsPublishedTrue(@Param("category") EducationCategory category, @Param("title") String title);

    // Admin repository methods
    List<EducationLesson> findAllByOrderByOrderIndexAsc();
    List<EducationLesson> findByCategoryOrderByOrderIndexAsc(EducationCategory category);
    List<EducationLesson> findByLevelOrderByOrderIndexAsc(EducationLevel level);
    List<EducationLesson> findByCategoryAndLevelOrderByOrderIndexAsc(EducationCategory category, EducationLevel level);
    List<EducationLesson> findByLanguageOrderByOrderIndexAsc(String language);
    List<EducationLesson> findByIsPublishedFalseOrderByOrderIndexAsc();
    List<EducationLesson> findByCategoryAndIsPublishedFalseOrderByOrderIndexAsc(EducationCategory category);

    Long countByLevelAndIsPublishedTrue(EducationLevel level);
}

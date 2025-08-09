package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.ForumTopic;
import rw.health.ubuzima.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ForumTopicRepository extends JpaRepository<ForumTopic, Long> {
    
    List<ForumTopic> findByIsActiveTrueOrderByLastActivityAtDesc();
    
    List<ForumTopic> findByCategoryAndIsActiveTrueOrderByLastActivityAtDesc(String category);
    
    List<ForumTopic> findByAuthorAndIsActiveTrueOrderByCreatedAtDesc(User author);
    
    @Query("SELECT ft FROM ForumTopic ft WHERE ft.isActive = true AND ft.isPinned = true " +
           "ORDER BY ft.createdAt DESC")
    List<ForumTopic> findPinnedTopics();
    
    @Query("SELECT ft FROM ForumTopic ft WHERE ft.isActive = true " +
           "ORDER BY ft.viewCount DESC, ft.replyCount DESC")
    List<ForumTopic> findPopularTopics();
    
    @Query("SELECT ft FROM ForumTopic ft WHERE ft.isActive = true AND " +
           "(LOWER(ft.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ft.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY ft.lastActivityAt DESC")
    List<ForumTopic> searchTopics(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT ft FROM ForumTopic ft WHERE ft.isActive = true AND " +
           "ft.lastActivityAt >= :since ORDER BY ft.lastActivityAt DESC")
    List<ForumTopic> findRecentlyActiveTopics(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(ft) FROM ForumTopic ft WHERE ft.isActive = true")
    Long countActiveTopics();
    
    @Query("SELECT COUNT(ft) FROM ForumTopic ft WHERE ft.category = :category AND ft.isActive = true")
    Long countActiveTopicsByCategory(@Param("category") String category);
    
    @Query("SELECT ft.category, COUNT(ft) FROM ForumTopic ft WHERE ft.isActive = true GROUP BY ft.category")
    List<Object[]> getTopicCountByCategory();
    
    @Query("SELECT ft FROM ForumTopic ft WHERE ft.isActive = true AND ft.isLocked = false " +
           "ORDER BY ft.lastActivityAt DESC")
    List<ForumTopic> findUnlockedTopics();

    @Query("SELECT ft FROM ForumTopic ft WHERE ft.isActive = true AND " +
           "(LOWER(ft.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ft.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY ft.lastActivityAt DESC")
    List<ForumTopic> searchActiveTopics(@Param("searchTerm") String searchTerm);
}

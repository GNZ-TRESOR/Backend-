package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.SupportGroup;
import rw.health.ubuzima.entity.User;

import java.util.List;

@Repository
public interface SupportGroupRepository extends JpaRepository<SupportGroup, Long> {
    
    List<SupportGroup> findByIsActiveTrueOrderByCreatedAtDesc();
    
    List<SupportGroup> findByCategoryAndIsActiveTrueOrderByMemberCountDesc(String category);
    
    List<SupportGroup> findByCreatorAndIsActiveTrueOrderByCreatedAtDesc(User creator);
    
    @Query("SELECT sg FROM SupportGroup sg WHERE sg.isActive = true AND " +
           "(LOWER(sg.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(sg.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY sg.memberCount DESC")
    List<SupportGroup> searchActiveGroups(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT sg FROM SupportGroup sg WHERE sg.isActive = true AND sg.isPrivate = false " +
           "ORDER BY sg.memberCount DESC")
    List<SupportGroup> findPublicGroupsByPopularity();
    
    @Query("SELECT sg FROM SupportGroup sg WHERE sg.isActive = true AND " +
           "sg.memberCount < COALESCE(sg.maxMembers, 999999) " +
           "ORDER BY sg.createdAt DESC")
    List<SupportGroup> findAvailableGroups();
    
    @Query("SELECT COUNT(sg) FROM SupportGroup sg WHERE sg.isActive = true")
    Long countActiveGroups();
    
    @Query("SELECT COUNT(sg) FROM SupportGroup sg WHERE sg.category = :category AND sg.isActive = true")
    Long countActiveGroupsByCategory(@Param("category") String category);
    
    @Query("SELECT sg.category, COUNT(sg) FROM SupportGroup sg WHERE sg.isActive = true GROUP BY sg.category")
    List<Object[]> getGroupCountByCategory();

    @Query("SELECT sg FROM SupportGroup sg WHERE sg.isActive = true " +
           "ORDER BY sg.memberCount DESC, sg.createdAt DESC")
    List<SupportGroup> findPopularGroups();
}

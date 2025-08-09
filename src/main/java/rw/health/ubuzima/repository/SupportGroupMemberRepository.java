package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.SupportGroup;
import rw.health.ubuzima.entity.SupportGroupMember;
import rw.health.ubuzima.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportGroupMemberRepository extends JpaRepository<SupportGroupMember, Long> {
    
    List<SupportGroupMember> findByGroupAndIsActiveTrueOrderByJoinedAtAsc(SupportGroup group);
    
    List<SupportGroupMember> findByUserAndIsActiveTrueOrderByJoinedAtDesc(User user);
    
    Optional<SupportGroupMember> findByGroupAndUserAndIsActiveTrue(SupportGroup group, User user);
    
    @Query("SELECT sgm FROM SupportGroupMember sgm WHERE sgm.user = :user AND sgm.isActive = true " +
           "AND sgm.group.isActive = true ORDER BY sgm.lastActivityAt DESC")
    List<SupportGroupMember> findActiveGroupsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(sgm) FROM SupportGroupMember sgm WHERE sgm.group = :group AND sgm.isActive = true")
    Long countActiveMembers(@Param("group") SupportGroup group);
    
    @Query("SELECT COUNT(sgm) FROM SupportGroupMember sgm WHERE sgm.user = :user AND sgm.isActive = true")
    Long countUserActiveGroups(@Param("user") User user);
    
    @Query("SELECT sgm FROM SupportGroupMember sgm WHERE sgm.group = :group AND sgm.role = :role " +
           "AND sgm.isActive = true ORDER BY sgm.joinedAt ASC")
    List<SupportGroupMember> findByGroupAndRole(@Param("group") SupportGroup group, @Param("role") String role);
    
    boolean existsByGroupAndUserAndIsActiveTrue(SupportGroup group, User user);
}

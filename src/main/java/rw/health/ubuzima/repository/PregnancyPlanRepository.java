package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.PregnancyPlan;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.PregnancyPlanStatus;

import java.util.List;

@Repository
public interface PregnancyPlanRepository extends JpaRepository<PregnancyPlan, Long> {

    List<PregnancyPlan> findByUser(User user);

    List<PregnancyPlan> findByUserAndCurrentStatus(User user, PregnancyPlanStatus status);

    @Query("SELECT pp FROM PregnancyPlan pp WHERE pp.user = :user OR pp.partner = :user")
    List<PregnancyPlan> findByUserOrPartner(@Param("user") User user);

    @Query("SELECT pp FROM PregnancyPlan pp WHERE (pp.user = :user OR pp.partner = :user) AND pp.currentStatus = :status")
    List<PregnancyPlan> findByUserOrPartnerAndStatus(@Param("user") User user, @Param("status") PregnancyPlanStatus status);

    List<PregnancyPlan> findByPartner(User partner);

    @Query("SELECT pp FROM PregnancyPlan pp WHERE pp.user.id = :userId ORDER BY pp.createdAt DESC")
    List<PregnancyPlan> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT pp FROM PregnancyPlan pp WHERE pp.currentStatus IN ('PLANNING', 'TRYING') ORDER BY pp.targetConceptionDate ASC")
    List<PregnancyPlan> findActivePlans();

    // Additional methods for FamilyPlanningController
    @Query("SELECT COUNT(pp) FROM PregnancyPlan pp WHERE pp.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(pp) FROM PregnancyPlan pp WHERE pp.user.id = :userId AND pp.currentStatus = :status")
    long countByUserIdAndCurrentStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT pp FROM PregnancyPlan pp WHERE pp.user.id = :userId AND pp.currentStatus = :status ORDER BY pp.createdAt DESC")
    List<PregnancyPlan> findByUserIdAndCurrentStatus(@Param("userId") Long userId, @Param("status") String status);
}

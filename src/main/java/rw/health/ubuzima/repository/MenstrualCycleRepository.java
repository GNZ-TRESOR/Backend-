package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.MenstrualCycle;
import rw.health.ubuzima.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenstrualCycleRepository extends JpaRepository<MenstrualCycle, Long> {
    
    List<MenstrualCycle> findByUserOrderByStartDateDesc(User user);
    
    List<MenstrualCycle> findByUserAndStartDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    Optional<MenstrualCycle> findByUserAndStartDate(User user, LocalDate startDate);
    
    @Query("SELECT mc FROM MenstrualCycle mc WHERE mc.user = :user AND mc.startDate <= :date AND (mc.endDate IS NULL OR mc.endDate >= :date)")
    Optional<MenstrualCycle> findCurrentCycle(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT mc FROM MenstrualCycle mc WHERE mc.user = :user ORDER BY mc.startDate DESC LIMIT 1")
    Optional<MenstrualCycle> findLatestCycle(@Param("user") User user);
    
    List<MenstrualCycle> findByUserAndIsPredicted(User user, Boolean isPredicted);

    // Additional methods for FamilyPlanningController
    @Query("SELECT COUNT(mc) FROM MenstrualCycle mc WHERE mc.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT mc FROM MenstrualCycle mc WHERE mc.user.id = :userId ORDER BY mc.startDate DESC")
    List<MenstrualCycle> findByUserIdOrderByStartDateDesc(@Param("userId") Long userId);
}

package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.ContraceptionMethod;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.ContraceptionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContraceptionMethodRepository extends JpaRepository<ContraceptionMethod, Long> {
    
    List<ContraceptionMethod> findByUserOrderByStartDateDesc(User user);

    // Find available methods (where user is null) - created by health workers for selection
    List<ContraceptionMethod> findByUserIsNullOrderByNameAsc();

    List<ContraceptionMethod> findByUserAndIsActive(User user, Boolean isActive);
    
    List<ContraceptionMethod> findByUserAndIsActiveTrue(User user);
    
    List<ContraceptionMethod> findByUserAndType(User user, ContraceptionType type);
    
    @Query("SELECT cm FROM ContraceptionMethod cm WHERE cm.user = :user AND cm.nextAppointment BETWEEN :startDate AND :endDate")
    List<ContraceptionMethod> findMethodsWithAppointmentsBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT cm FROM ContraceptionMethod cm WHERE cm.user = :user AND cm.startDate <= :date AND (cm.endDate IS NULL OR cm.endDate >= :date) AND cm.isActive = true")
    List<ContraceptionMethod> findActiveMethodsForDate(@Param("user") User user, @Param("date") LocalDate date);

    // Additional methods for FamilyPlanningController
    @Query("SELECT COUNT(cm) FROM ContraceptionMethod cm WHERE cm.user.id = :userId AND cm.isActive = true")
    long countByUserIdAndIsActiveTrue(@Param("userId") Long userId);

    @Query("SELECT cm FROM ContraceptionMethod cm WHERE cm.user.id = :userId AND cm.isActive = true ORDER BY cm.startDate DESC")
    List<ContraceptionMethod> findByUserIdAndIsActiveTrueOrderByStartDateDesc(@Param("userId") Long userId);

    @Query("SELECT cm FROM ContraceptionMethod cm WHERE cm.user.id = :userId ORDER BY cm.startDate DESC")
    List<ContraceptionMethod> findByUserIdOrderByStartDateDesc(@Param("userId") Long userId);

    // Additional methods for health worker reports
    List<ContraceptionMethod> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    List<ContraceptionMethod> findByIsActiveTrue();

    long countByIsActiveTrue();

    List<ContraceptionMethod> findByNextAppointmentBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT cm FROM ContraceptionMethod cm WHERE cm.user IS NOT NULL AND cm.startDate BETWEEN :startDate AND :endDate")
    List<ContraceptionMethod> findUserMethodsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT cm.type, COUNT(cm) FROM ContraceptionMethod cm WHERE cm.user IS NOT NULL GROUP BY cm.type")
    List<Object[]> getMethodTypeStatistics();

    @Query("SELECT COUNT(DISTINCT cm.user.id) FROM ContraceptionMethod cm WHERE cm.user IS NOT NULL")
    long countDistinctUsers();

    // Additional methods for the redesigned controller
    List<ContraceptionMethod> findByUserAndIsActiveOrderByStartDateDesc(User user, Boolean isActive);

    long countByIsActive(Boolean isActive);

    long countByType(ContraceptionType type);

    // Additional methods needed by ContraceptionServiceImpl
    List<ContraceptionMethod> findByUserIdIsNull();

    List<ContraceptionMethod> findByUserId(Long userId);

    List<ContraceptionMethod> findByUserIdAndIsActiveTrue(Long userId);

    List<ContraceptionMethod> findByType(ContraceptionType type);

    List<ContraceptionMethod> findByUserIdAndType(Long userId, ContraceptionType type);

    boolean existsByUserIdAndIsActiveTrue(Long userId);

    List<ContraceptionMethod> findByPrescribedBy(String prescribedBy);

    List<ContraceptionMethod> findByNextAppointmentBeforeAndIsActiveTrue(LocalDate date);

    List<ContraceptionMethod> findByEndDateBeforeAndIsActiveTrue(LocalDate date);
}

package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.Medication;
import rw.health.ubuzima.entity.User;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    
    List<Medication> findByUserOrderByStartDateDesc(User user);
    
    List<Medication> findByUserAndIsActive(User user, Boolean isActive);
    
    @Query("SELECT m FROM Medication m WHERE m.user = :user AND m.startDate <= :date AND (m.endDate IS NULL OR m.endDate >= :date) AND m.isActive = true")
    List<Medication> findActiveMedicationsForDate(@Param("user") User user, @Param("date") LocalDate date);
    
    List<Medication> findByUserAndPurposeContainingIgnoreCase(User user, String purpose);
    
    @Query("SELECT m FROM Medication m WHERE m.user = :user AND m.endDate IS NOT NULL AND m.endDate BETWEEN :startDate AND :endDate")
    List<Medication> findMedicationsEndingBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

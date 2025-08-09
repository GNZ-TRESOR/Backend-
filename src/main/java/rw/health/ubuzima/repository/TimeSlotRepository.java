package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.TimeSlot;
import rw.health.ubuzima.entity.HealthFacility;
import rw.health.ubuzima.entity.User;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    List<TimeSlot> findByHealthFacilityAndStartTimeBetween(
        HealthFacility facility, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    List<TimeSlot> findByHealthWorkerAndStartTimeBetween(
        User healthWorker, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.healthFacility = :facility AND ts.healthWorker = :healthWorker AND ts.startTime BETWEEN :startDate AND :endDate AND ts.isAvailable = true")
    List<TimeSlot> findAvailableSlots(
        @Param("facility") HealthFacility facility,
        @Param("healthWorker") User healthWorker,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.healthFacility = :facility AND ts.startTime BETWEEN :startDate AND :endDate AND ts.isAvailable = true")
    List<TimeSlot> findAvailableSlotsByFacility(
        @Param("facility") HealthFacility facility,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Additional methods for health worker functionality
    List<TimeSlot> findByHealthWorker(User healthWorker);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.healthWorker = :healthWorker AND DATE(ts.startTime) = :date")
    List<TimeSlot> findByHealthWorkerAndDate(
        @Param("healthWorker") User healthWorker,
        @Param("date") LocalDate date
    );
}

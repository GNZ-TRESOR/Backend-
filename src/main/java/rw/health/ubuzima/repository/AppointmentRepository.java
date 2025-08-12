package rw.health.ubuzima.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.Appointment;
import rw.health.ubuzima.entity.HealthFacility;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUser(User user);

    List<Appointment> findByHealthWorker(User healthWorker);

    List<Appointment> findByHealthFacility(HealthFacility healthFacility);

    List<Appointment> findByStatus(AppointmentStatus status);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND a.scheduledDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByUserAndDateRange(@Param("user") User user, 
                                           @Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.healthWorker = :healthWorker AND a.scheduledDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByHealthWorkerAndDateRange(@Param("healthWorker") User healthWorker, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    List<Appointment> findByUserOrderByScheduledDateDesc(User user);

    @Query("SELECT a FROM Appointment a WHERE a.scheduledDate < :now AND a.status = :status AND a.reminderSent = false")
    List<Appointment> findUpcomingAppointmentsForReminder(@Param("now") LocalDateTime now, @Param("status") AppointmentStatus status);

    // Check for duplicate appointments
    List<Appointment> findByUserAndHealthFacilityAndScheduledDate(User user, HealthFacility healthFacility, LocalDateTime scheduledDate);

    // Analytics methods needed by AnalyticsServiceImpl
    long countByStatus(AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user.id = :userId AND DATE(a.scheduledDate) BETWEEN :startDate AND :endDate")
    long countByUserIdAndScheduledDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.healthFacility.id = :facilityId AND DATE(a.scheduledDate) BETWEEN :startDate AND :endDate")
    long countByHealthFacilityIdAndScheduledDateBetween(@Param("facilityId") Long facilityId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.healthWorker.id = :workerId AND DATE(a.scheduledDate) BETWEEN :startDate AND :endDate")
    long countByHealthWorkerIdAndScheduledDateBetween(@Param("workerId") Long workerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Methods for appointment status scheduler
    List<Appointment> findByStatusAndScheduledDateBefore(AppointmentStatus status, LocalDateTime dateTime);

    List<Appointment> findByStatusAndScheduledDateBetween(AppointmentStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.scheduledDate BETWEEN :startTime AND :endTime AND a.status IN ('SCHEDULED', 'CONFIRMED') AND a.reminderSent = false")
    List<Appointment> findAppointmentsForReminder(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // Enhanced queries for health worker dashboard
    // Note: Removed findTodayAppointmentsByHealthWorker as it's not used and was causing startup issues

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.healthWorker.id = :healthWorkerId AND a.status = 'COMPLETED'")
    long countCompletedAppointmentsByHealthWorker(@Param("healthWorkerId") Long healthWorkerId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.healthWorker.id = :healthWorkerId")
    long countTotalAppointmentsByHealthWorker(@Param("healthWorkerId") Long healthWorkerId);
}

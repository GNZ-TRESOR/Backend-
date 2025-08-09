package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.CommunityEvent;
import rw.health.ubuzima.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommunityEventRepository extends JpaRepository<CommunityEvent, Long> {
    
    @Query("SELECT ce FROM CommunityEvent ce WHERE ce.isActive = :isActive AND ce.isCancelled = :isCancelled ORDER BY ce.eventDate ASC")
    List<CommunityEvent> findByIsActiveAndIsCancelledOrderByEventDateAsc(
        @Param("isActive") boolean isActive,
        @Param("isCancelled") boolean isCancelled
    );
    
    // Convenience method for active, non-cancelled events
    List<CommunityEvent> findByIsActiveTrueAndIsCancelledFalseOrderByEventDateAsc();
    
    @Query("SELECT ce FROM CommunityEvent ce WHERE ce.organizer.id = :organizerId ORDER BY ce.eventDate DESC")
    List<CommunityEvent> findByOrganizerIdOrderByEventDateDesc(
        @Param("organizerId") String organizerId
    );
    
    @Query("SELECT ce FROM CommunityEvent ce WHERE ce.isActive = true AND ce.isCancelled = false " +
           "AND ce.eventDate >= :now ORDER BY ce.eventDate ASC")
    List<CommunityEvent> findUpcomingEvents(@Param("now") LocalDateTime now);
    
    @Query("SELECT ce FROM CommunityEvent ce WHERE ce.isActive = true AND ce.isCancelled = false " +
           "AND ce.eventDate < :now ORDER BY ce.eventDate DESC")
    List<CommunityEvent> findPastEvents(@Param("now") LocalDateTime now);
    
    @Query("SELECT ce FROM CommunityEvent ce WHERE ce.type = :type AND ce.isActive = true " +
           "AND ce.isCancelled = false ORDER BY ce.eventDate ASC")
    List<CommunityEvent> findByTypeAndActive(@Param("type") String type);
    
    @Query("SELECT ce FROM CommunityEvent ce WHERE ce.isActive = true AND ce.isCancelled = false " +
           "AND (LOWER(ce.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ce.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY ce.eventDate ASC")
    List<CommunityEvent> searchEvents(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT ce FROM CommunityEvent ce WHERE ce.isActive = true AND ce.isCancelled = false " +
           "AND ce.registrationRequired = true AND ce.registrationDeadline >= :now " +
           "AND ce.currentParticipants < COALESCE(ce.maxParticipants, 999999) " +
           "ORDER BY ce.registrationDeadline ASC")
    List<CommunityEvent> findEventsOpenForRegistration(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(ce) FROM CommunityEvent ce WHERE ce.isActive = true AND ce.isCancelled = false")
    Long countActiveEvents();
    
    @Query("SELECT COUNT(ce) FROM CommunityEvent ce WHERE ce.isActive = true AND ce.isCancelled = false " +
           "AND ce.eventDate >= :now")
    Long countUpcomingEvents(@Param("now") LocalDateTime now);
    
    @Query("SELECT ce.type, COUNT(ce) FROM CommunityEvent ce WHERE ce.isActive = true " +
           "AND ce.isCancelled = false GROUP BY ce.type")
    List<Object[]> getEventCountByType();

    // Analytics methods needed by AnalyticsServiceImpl
    long countByEventDateAfter(LocalDate date);
    
    // Find events by organizer
    @Query("SELECT ce FROM CommunityEvent ce WHERE ce.organizer = :organizer ORDER BY ce.eventDate DESC")
    List<CommunityEvent> findByOrganizer(@Param("organizer") User organizer);
}

package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.SupportTicket;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.TicketType;
import rw.health.ubuzima.enums.TicketStatus;
import rw.health.ubuzima.enums.TicketPriority;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUser(User user);

    List<SupportTicket> findByUserAndStatus(User user, TicketStatus status);

    List<SupportTicket> findByStatus(TicketStatus status);

    List<SupportTicket> findByTicketType(TicketType ticketType);

    List<SupportTicket> findByPriority(TicketPriority priority);

    List<SupportTicket> findByAssignedTo(User assignedTo);

    @Query("SELECT st FROM SupportTicket st WHERE st.user.id = :userId ORDER BY st.createdAt DESC")
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT st FROM SupportTicket st WHERE st.assignedTo.id = :assigneeId ORDER BY st.priority DESC, st.createdAt ASC")
    List<SupportTicket> findByAssigneeIdOrderByPriorityAndCreatedAt(@Param("assigneeId") Long assigneeId);

    @Query("SELECT st FROM SupportTicket st WHERE st.status IN ('OPEN', 'IN_PROGRESS') ORDER BY st.priority DESC, st.createdAt ASC")
    List<SupportTicket> findOpenTicketsOrderByPriorityAndCreatedAt();

    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE st.status = :status")
    long countByStatus(@Param("status") TicketStatus status);

    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE st.assignedTo.id = :assigneeId AND st.status IN ('OPEN', 'IN_PROGRESS')")
    long countOpenTicketsByAssignee(@Param("assigneeId") Long assigneeId);
}

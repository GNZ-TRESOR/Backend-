package rw.health.ubuzima.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.Message;
import rw.health.ubuzima.entity.User;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE (m.sender = :user OR m.receiver = :user) ORDER BY m.createdAt DESC")
    Page<Message> findMessagesByUser(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") String conversationId);
    
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :user AND m.isRead = false")
    Long countUnreadMessages(@Param("user") User user);
    
    @Query("SELECT m FROM Message m WHERE m.receiver = :user AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessages(@Param("user") User user);
    
    @Query("SELECT m FROM Message m WHERE m.isEmergency = true AND (m.sender = :user OR m.receiver = :user) ORDER BY m.createdAt DESC")
    List<Message> findEmergencyMessages(@Param("user") User user);
    
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN (" +
           "SELECT DISTINCT m.receiver.id FROM Message m WHERE m.sender = :user " +
           "UNION " +
           "SELECT DISTINCT m.sender.id FROM Message m WHERE m.receiver = :user AND m.sender != :user" +
           ")")
    List<User> findConversationPartners(@Param("user") User user);

    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender = :user1 AND m.receiver = :user2) OR " +
           "(m.sender = :user2 AND m.receiver = :user1) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findConversationMessages(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);

    @Query("SELECT m FROM Message m " +
           "LEFT JOIN FETCH m.sender " +
           "LEFT JOIN FETCH m.receiver " +
           "WHERE m.id = :messageId")
    Message findByIdWithUsers(@Param("messageId") Long messageId);
}

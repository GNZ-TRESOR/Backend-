package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.StiTestRecord;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.StiTestType;
import rw.health.ubuzima.enums.TestResultStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StiTestRecordRepository extends JpaRepository<StiTestRecord, Long> {

    List<StiTestRecord> findByUser(User user);

    List<StiTestRecord> findByUserAndTestType(User user, StiTestType testType);

    List<StiTestRecord> findByUserAndResultStatus(User user, TestResultStatus resultStatus);

    @Query("SELECT str FROM StiTestRecord str WHERE str.user = :user AND str.testDate BETWEEN :startDate AND :endDate")
    List<StiTestRecord> findByUserAndTestDateBetween(
            @Param("user") User user, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    @Query("SELECT str FROM StiTestRecord str WHERE str.user.id = :userId ORDER BY str.testDate DESC")
    List<StiTestRecord> findByUserIdOrderByTestDateDesc(@Param("userId") Long userId);

    @Query("SELECT str FROM StiTestRecord str WHERE str.followUpRequired = true AND str.followUpDate <= :date")
    List<StiTestRecord> findFollowUpsDue(@Param("date") LocalDate date);

    List<StiTestRecord> findByUserAndFollowUpRequiredTrue(User user);
}

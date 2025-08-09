package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.PartnerDecision;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.DecisionType;
import rw.health.ubuzima.enums.DecisionStatus;

import java.util.List;

@Repository
public interface PartnerDecisionRepository extends JpaRepository<PartnerDecision, Long> {

    List<PartnerDecision> findByUser(User user);

    List<PartnerDecision> findByUserAndDecisionType(User user, DecisionType decisionType);

    List<PartnerDecision> findByUserAndDecisionStatus(User user, DecisionStatus decisionStatus);

    @Query("SELECT pd FROM PartnerDecision pd WHERE pd.user = :user OR pd.partner = :user")
    List<PartnerDecision> findByUserOrPartner(@Param("user") User user);

    @Query("SELECT pd FROM PartnerDecision pd WHERE (pd.user = :user OR pd.partner = :user) AND pd.decisionStatus = :status")
    List<PartnerDecision> findByUserOrPartnerAndStatus(@Param("user") User user, @Param("status") DecisionStatus status);

    List<PartnerDecision> findByPartner(User partner);

    @Query("SELECT pd FROM PartnerDecision pd WHERE pd.user.id = :userId ORDER BY pd.createdAt DESC")
    List<PartnerDecision> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}

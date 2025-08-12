package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.enums.UserStatus;
import rw.health.ubuzima.enums.Gender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Count only verified clients
    long countByRoleAndEmailVerified(rw.health.ubuzima.enums.UserRole role, Boolean emailVerified);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);

    List<User> findByRoleNotAndIdNot(UserRole role, Long id);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status")
    List<User> findByRoleAndStatus(@Param("role") UserRole role, @Param("status") UserStatus status);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    // Admin analytics methods
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE'")
    long countByIsActiveTrue();

    @Query("SELECT COUNT(u) FROM User u WHERE YEAR(u.createdAt) = YEAR(CURRENT_DATE) AND MONTH(u.createdAt) = MONTH(CURRENT_DATE)")
    long countNewUsersThisMonth();

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    // Health facility related methods
    @Query("SELECT u FROM User u WHERE u.facilityId = :facilityId AND u.role = 'HEALTH_WORKER'")
    List<User> findHealthWorkersByFacilityId(@Param("facilityId") String facilityId);

    // Method that takes HealthFacility entity and finds health workers
    default List<User> findHealthWorkersByFacility(rw.health.ubuzima.entity.HealthFacility facility) {
        return findHealthWorkersByFacilityId(String.valueOf(facility.getId()));
    }

    // Push notification related methods
    List<User> findByDeviceTokenIsNotNull();

    // Analytics methods needed by AnalyticsServiceImpl
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByStatus(UserStatus status);
    long countByRole(UserRole role);
    long countByGender(Gender gender);
    long countByDistrict(String district);
    long countByFacilityIdAndRole(String facilityId, UserRole role);
    List<User> findTop10ByOrderByCreatedAtDesc();

    List<User> findByFacilityIdAndRole(String facilityId, UserRole role);

    // Village-based assignment methods for health workers
    List<User> findByVillageAndRole(String village, UserRole role);

    @Query("SELECT u FROM User u WHERE u.village = :village AND u.role = :role AND u.status = 'ACTIVE'")
    List<User> findActiveUsersByVillageAndRole(@Param("village") String village, @Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.village = :village AND u.role = 'CLIENT' AND u.status = 'ACTIVE'")
    List<User> findActiveClientsByVillage(@Param("village") String village);

    // ============ ADVANCED SEARCH METHODS ============

    List<User> findByNameContainingIgnoreCase(String name);

    List<User> findByEmailContainingIgnoreCase(String email);

    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    org.springframework.data.domain.Page<User> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<User> findByEmailContainingIgnoreCase(String email, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<User> findByRole(UserRole role, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<User> findByDistrictContainingIgnoreCaseOrSectorContainingIgnoreCaseOrCellContainingIgnoreCaseOrVillageContainingIgnoreCase(
        String district, String sector, String cell, String village, org.springframework.data.domain.Pageable pageable);

    // ============ ANALYTICS METHODS ============

    long countByCreatedAtAfter(LocalDateTime date);

    long countByLastLoginAtAfter(LocalDateTime date);
}

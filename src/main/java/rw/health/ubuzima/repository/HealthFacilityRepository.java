package rw.health.ubuzima.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.HealthFacility;
import rw.health.ubuzima.enums.FacilityType;

import java.util.List;

@Repository
public interface HealthFacilityRepository extends JpaRepository<HealthFacility, Long> {

    List<HealthFacility> findByFacilityType(FacilityType facilityType);

    List<HealthFacility> findByIsActiveTrue();

    @Query("SELECT hf FROM HealthFacility hf WHERE " +
           "LOWER(hf.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(hf.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<HealthFacility> searchFacilities(@Param("searchTerm") String searchTerm);

    @Query("SELECT hf FROM HealthFacility hf WHERE hf.isActive = true AND " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(hf.latitude)) * " +
           "cos(radians(hf.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(hf.latitude)))) < :radiusKm")
    List<HealthFacility> findNearbyFacilities(@Param("latitude") Double latitude,
                                            @Param("longitude") Double longitude,
                                            @Param("radiusKm") Double radiusKm);

    // Pageable methods for admin
    Page<HealthFacility> findByFacilityType(FacilityType facilityType, Pageable pageable);

    Page<HealthFacility> findByIsActive(Boolean isActive, Pageable pageable);

    Page<HealthFacility> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
        String name, String address, Pageable pageable);

    // Analytics methods needed by AnalyticsServiceImpl
    long countByIsActive(boolean isActive);
}

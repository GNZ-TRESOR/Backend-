package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.HealthFacility;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.FacilityType;
import rw.health.ubuzima.repository.HealthFacilityRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HealthFacilityController {

    private final HealthFacilityRepository healthFacilityRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFacilities(
            @RequestParam(required = false) FacilityType type,
            @RequestParam(required = false) String search) {
        
        try {
            List<HealthFacility> facilities;

            if (search != null && !search.isEmpty()) {
                facilities = healthFacilityRepository.searchFacilities(search);
            } else if (type != null) {
                facilities = healthFacilityRepository.findByFacilityType(type);
            } else {
                facilities = healthFacilityRepository.findByIsActiveTrue();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "facilities", facilities
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch facilities: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{facilityId}")
    public ResponseEntity<Map<String, Object>> getFacilityById(@PathVariable Long facilityId) {
        try {
            HealthFacility facility = healthFacilityRepository.findById(facilityId).orElse(null);
            
            if (facility == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "facility", facility
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch facility: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyFacilities(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius) {
        
        try {
            List<HealthFacility> facilities = healthFacilityRepository.findNearbyFacilities(
                latitude, longitude, radius);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "facilities", facilities
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch nearby facilities: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFacility(@RequestBody Map<String, Object> request) {
        try {
            HealthFacility facility = new HealthFacility();
            facility.setName(request.get("name").toString());
            facility.setFacilityType(FacilityType.valueOf(request.get("facilityType").toString().toUpperCase()));
            facility.setAddress(request.get("address").toString());
            facility.setPhoneNumber(request.get("phoneNumber") != null ? request.get("phoneNumber").toString() : null);
            facility.setEmail(request.get("email") != null ? request.get("email").toString() : null);
            facility.setLatitude(request.get("latitude") != null ? Double.valueOf(request.get("latitude").toString()) : null);
            facility.setLongitude(request.get("longitude") != null ? Double.valueOf(request.get("longitude").toString()) : null);
            facility.setOperatingHours(request.get("operatingHours") != null ? request.get("operatingHours").toString() : null);
            facility.setServicesOffered(request.get("servicesOffered") != null ? request.get("servicesOffered").toString() : null);
            facility.setIsActive(true);

            HealthFacility savedFacility = healthFacilityRepository.save(facility);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Health facility created successfully",
                "facility", savedFacility
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create facility: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{facilityId}/health-workers")
    public ResponseEntity<Map<String, Object>> getFacilityHealthWorkers(@PathVariable Long facilityId) {
        try {
            HealthFacility facility = healthFacilityRepository.findById(facilityId).orElse(null);

            if (facility == null) {
                return ResponseEntity.notFound().build();
            }

            // Get health workers assigned to this facility
            List<User> healthWorkers = userRepository.findHealthWorkersByFacility(facility);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "healthWorkers", healthWorkers
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch facility health workers: " + e.getMessage()
            ));
        }
    }
}

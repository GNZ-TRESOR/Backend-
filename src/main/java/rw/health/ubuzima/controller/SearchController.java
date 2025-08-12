package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.*;
import rw.health.ubuzima.repository.*;
import rw.health.ubuzima.enums.UserRole;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Search Controller
 * Provides comprehensive search functionality across all entities
 */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final UserRepository userRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final EducationLessonRepository educationLessonRepository;
    private final HealthFacilityRepository healthFacilityRepository;
    private final MedicationRepository medicationRepository;
    private final ContraceptionMethodRepository contraceptionMethodRepository;
    private final SupportGroupRepository supportGroupRepository;
    private final MessageRepository messageRepository;

    /**
     * Global search across all entities
     */
    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> globalSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> results = new HashMap<>();
            
            // Search users
            List<User> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                query, query);
            results.put("users", users.stream().limit(5).collect(Collectors.toList()));
            
            // Search health facilities
            List<HealthFacility> facilities = healthFacilityRepository.findByNameContainingIgnoreCase(query);
            results.put("facilities", facilities.stream().limit(5).collect(Collectors.toList()));
            
            // Search education lessons
            List<EducationLesson> lessons = educationLessonRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                query, query);
            results.put("lessons", lessons.stream().limit(5).collect(Collectors.toList()));
            
            // Search medications
            List<Medication> medications = medicationRepository.findByNameContainingIgnoreCase(query);
            results.put("medications", medications.stream().limit(5).collect(Collectors.toList()));
            
            // Search contraception methods
            List<ContraceptionMethod> methods = contraceptionMethodRepository.findByNameContainingIgnoreCase(query);
            results.put("contraceptionMethods", methods.stream().limit(5).collect(Collectors.toList()));
            
            // Search support groups
            List<SupportGroup> groups = supportGroupRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query);
            results.put("supportGroups", groups.stream().limit(5).collect(Collectors.toList()));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "query", query,
                "results", results,
                "totalCategories", results.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Search failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Search users with advanced filters
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users;

            if (name != null && !name.isEmpty()) {
                users = userRepository.findByNameContainingIgnoreCase(name, pageable);
            } else if (email != null && !email.isEmpty()) {
                users = userRepository.findByEmailContainingIgnoreCase(email, pageable);
            } else if (role != null && !role.isEmpty()) {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                users = userRepository.findByRole(userRole, pageable);
            } else if (location != null && !location.isEmpty()) {
                users = userRepository.findByDistrictContainingIgnoreCaseOrSectorContainingIgnoreCaseOrCellContainingIgnoreCaseOrVillageContainingIgnoreCase(
                    location, location, location, location, pageable);
            } else {
                users = userRepository.findAll(pageable);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "users", users.getContent(),
                "totalElements", users.getTotalElements(),
                "totalPages", users.getTotalPages(),
                "currentPage", page
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "User search failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Search health facilities with filters
     */
    @GetMapping("/facilities")
    public ResponseEntity<Map<String, Object>> searchFacilities(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<HealthFacility> facilities;

            if (name != null && !name.isEmpty()) {
                facilities = healthFacilityRepository.findByNameContainingIgnoreCase(name);
            } else if (location != null && !location.isEmpty()) {
                facilities = healthFacilityRepository.findByAddressContainingIgnoreCase(location);
            } else {
                facilities = healthFacilityRepository.findAll();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "facilities", facilities,
                "total", facilities.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Facility search failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Search education content
     */
    @GetMapping("/education")
    public ResponseEntity<Map<String, Object>> searchEducation(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<EducationLesson> lessons = educationLessonRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "lessons", lessons,
                "total", lessons.size(),
                "query", query
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Education search failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Search appointments with filters
     */
    @GetMapping("/appointments")
    public ResponseEntity<Map<String, Object>> searchAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String facilityName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<Appointment> appointments = appointmentRepository.findAll();
            
            // Apply filters
            if (patientName != null && !patientName.isEmpty()) {
                appointments = appointments.stream()
                    .filter(apt -> apt.getUser().getName().toLowerCase().contains(patientName.toLowerCase()))
                    .collect(Collectors.toList());
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "appointments", appointments,
                "total", appointments.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Appointment search failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Search suggestions for autocomplete
     */
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<String> suggestions = new ArrayList<>();
            
            // User name suggestions
            List<User> users = userRepository.findByNameContainingIgnoreCase(query);
            suggestions.addAll(users.stream()
                .map(User::getName)
                .limit(3)
                .collect(Collectors.toList()));
            
            // Facility name suggestions
            List<HealthFacility> facilities = healthFacilityRepository.findByNameContainingIgnoreCase(query);
            suggestions.addAll(facilities.stream()
                .map(HealthFacility::getName)
                .limit(3)
                .collect(Collectors.toList()));
            
            // Education lesson suggestions
            List<EducationLesson> lessons = educationLessonRepository.findByTitleContainingIgnoreCase(query);
            suggestions.addAll(lessons.stream()
                .map(EducationLesson::getTitle)
                .limit(3)
                .collect(Collectors.toList()));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "suggestions", suggestions.stream().distinct().limit(limit).collect(Collectors.toList())
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Suggestions failed: " + e.getMessage()
            ));
        }
    }
}

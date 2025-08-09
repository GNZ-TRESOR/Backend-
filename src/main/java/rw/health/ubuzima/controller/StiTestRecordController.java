package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.StiTestRecord;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.StiTestType;
import rw.health.ubuzima.enums.TestResultStatus;
import rw.health.ubuzima.repository.StiTestRecordRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sti-test-records")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StiTestRecordController {

    private final StiTestRecordRepository stiTestRecordRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStiTestRecords(
            @RequestParam Long userId,
            @RequestParam(required = false) String testType,
            @RequestParam(required = false) String resultStatus) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<StiTestRecord> records;
            
            if (testType != null && resultStatus != null) {
                StiTestType type = StiTestType.valueOf(testType.toUpperCase());
                TestResultStatus status = TestResultStatus.valueOf(resultStatus.toUpperCase());
                records = stiTestRecordRepository.findByUserAndTestType(user, type)
                    .stream()
                    .filter(r -> r.getResultStatus() == status)
                    .toList();
            } else if (testType != null) {
                StiTestType type = StiTestType.valueOf(testType.toUpperCase());
                records = stiTestRecordRepository.findByUserAndTestType(user, type);
            } else if (resultStatus != null) {
                TestResultStatus status = TestResultStatus.valueOf(resultStatus.toUpperCase());
                records = stiTestRecordRepository.findByUserAndResultStatus(user, status);
            } else {
                records = stiTestRecordRepository.findByUserIdOrderByTestDateDesc(userId);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "records", records
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch test records: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createStiTestRecord(
            @RequestBody Map<String, Object> request) {
        try {
            // Support both userId and clientId for compatibility
            Long userId = null;
            if (request.get("userId") != null) {
                userId = Long.valueOf(request.get("userId").toString());
            } else if (request.get("clientId") != null) {
                userId = Long.valueOf(request.get("clientId").toString());
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Either userId or clientId is required"
                ));
            }
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            StiTestRecord record = new StiTestRecord();
            record.setUser(user);
            record.setTestType(StiTestType.valueOf(request.get("testType").toString().toUpperCase()));
            record.setTestDate(LocalDate.parse(request.get("testDate").toString()));
            
            if (request.get("testLocation") != null) {
                record.setTestLocation(request.get("testLocation").toString());
            }
            
            if (request.get("testProvider") != null) {
                record.setTestProvider(request.get("testProvider").toString());
            }
            
            if (request.get("resultStatus") != null) {
                record.setResultStatus(TestResultStatus.valueOf(request.get("resultStatus").toString().toUpperCase()));
            }
            
            if (request.get("resultDate") != null) {
                record.setResultDate(LocalDate.parse(request.get("resultDate").toString()));
            }
            
            if (request.get("followUpRequired") != null) {
                record.setFollowUpRequired(Boolean.valueOf(request.get("followUpRequired").toString()));
            }
            
            if (request.get("followUpDate") != null) {
                record.setFollowUpDate(LocalDate.parse(request.get("followUpDate").toString()));
            }
            
            if (request.get("notes") != null) {
                record.setNotes(request.get("notes").toString());
            }
            
            if (request.get("isConfidential") != null) {
                record.setIsConfidential(Boolean.valueOf(request.get("isConfidential").toString()));
            }

            StiTestRecord savedRecord = stiTestRecordRepository.save(record);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "STI test record created successfully",
                "record", savedRecord
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create test record: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<Map<String, Object>> updateStiTestRecord(
            @PathVariable Long recordId,
            @RequestBody Map<String, Object> request) {
        try {
            StiTestRecord record = stiTestRecordRepository.findById(recordId).orElse(null);
            
            if (record == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.get("testLocation") != null) {
                record.setTestLocation(request.get("testLocation").toString());
            }
            
            if (request.get("testProvider") != null) {
                record.setTestProvider(request.get("testProvider").toString());
            }
            
            if (request.get("resultStatus") != null) {
                record.setResultStatus(TestResultStatus.valueOf(request.get("resultStatus").toString().toUpperCase()));
            }
            
            if (request.get("resultDate") != null) {
                record.setResultDate(LocalDate.parse(request.get("resultDate").toString()));
            }
            
            if (request.get("followUpRequired") != null) {
                record.setFollowUpRequired(Boolean.valueOf(request.get("followUpRequired").toString()));
            }
            
            if (request.get("followUpDate") != null) {
                record.setFollowUpDate(LocalDate.parse(request.get("followUpDate").toString()));
            }
            
            if (request.get("notes") != null) {
                record.setNotes(request.get("notes").toString());
            }

            StiTestRecord updatedRecord = stiTestRecordRepository.save(record);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Test record updated successfully",
                "record", updatedRecord
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update test record: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<Map<String, Object>> deleteStiTestRecord(
            @PathVariable Long recordId,
            @RequestParam Long userId) {
        try {
            StiTestRecord record = stiTestRecordRepository.findById(recordId).orElse(null);
            
            if (record == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if user owns this record
            if (!record.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You can only delete your own test records"
                ));
            }

            stiTestRecordRepository.delete(record);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Test record deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete test record: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/follow-ups")
    public ResponseEntity<Map<String, Object>> getFollowUpsDue(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<StiTestRecord> followUps = stiTestRecordRepository.findByUserAndFollowUpRequiredTrue(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "followUps", followUps
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch follow-ups: " + e.getMessage()
            ));
        }
    }
}

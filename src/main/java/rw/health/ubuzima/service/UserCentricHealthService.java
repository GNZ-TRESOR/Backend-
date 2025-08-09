package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rw.health.ubuzima.entity.HealthRecord;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.repository.HealthRecordRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCentricHealthService {

    private final HealthRecordRepository healthRecordRepository;
    private final UserRepository userRepository;

    /**
     * Get user's health record (user-centric approach - one record per user)
     */
    public HealthRecord getUserHealthRecord(Long userId) {
        return healthRecordRepository.findByUserId(userId).orElse(null);
    }

    /**
     * Get or create user's health record
     */
    public HealthRecord getOrCreateUserHealthRecord(Long userId) {
        Optional<HealthRecord> existingRecord = healthRecordRepository.findByUserId(userId);

        if (existingRecord.isPresent()) {
            return existingRecord.get();
        }

        // Create new record for user
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        HealthRecord newRecord = new HealthRecord();
        newRecord.setUser(user);
        newRecord.setHealthStatus("normal");
        newRecord.setIsVerified(false);

        return healthRecordRepository.save(newRecord);
    }

    /**
     * Update heart rate for a user
     */
    public HealthRecord updateHeartRate(Long userId, Integer heartRateValue, String unit) {
        HealthRecord record = getOrCreateUserHealthRecord(userId);
        record.setHeartRateValue(heartRateValue);
        record.setHeartRateUnit(unit != null ? unit : "bpm");
        return healthRecordRepository.save(record);
    }

    /**
     * Update blood pressure for a user
     */
    public HealthRecord updateBloodPressure(Long userId, String bpValue, String unit) {
        HealthRecord record = getOrCreateUserHealthRecord(userId);
        record.setBpValue(bpValue);
        record.setBpUnit(unit != null ? unit : "mmHg");
        return healthRecordRepository.save(record);
    }

    /**
     * Update blood pressure with systolic/diastolic values
     */
    public HealthRecord updateBloodPressure(Long userId, int systolic, int diastolic, String unit) {
        HealthRecord record = getOrCreateUserHealthRecord(userId);
        record.setBloodPressure(systolic, diastolic);
        record.setBpUnit(unit != null ? unit : "mmHg");
        return healthRecordRepository.save(record);
    }

    /**
     * Update weight for a user
     */
    public HealthRecord updateWeight(Long userId, BigDecimal kgValue, String unit) {
        HealthRecord record = getOrCreateUserHealthRecord(userId);
        record.setKgValue(kgValue);
        record.setKgUnit(unit != null ? unit : "kg");
        return healthRecordRepository.save(record);
    }

    /**
     * Update temperature for a user
     */
    public HealthRecord updateTemperature(Long userId, BigDecimal tempValue, String unit) {
        HealthRecord record = getOrCreateUserHealthRecord(userId);
        record.setTempValue(tempValue);
        record.setTempUnit(unit != null ? unit : "°C");
        return healthRecordRepository.save(record);
    }

    /**
     * Update height for a user
     */
    public HealthRecord updateHeight(Long userId, BigDecimal heightValue, String unit) {
        HealthRecord record = getOrCreateUserHealthRecord(userId);
        record.setHeightValue(heightValue);
        record.setHeightUnit(unit != null ? unit : "cm");
        return healthRecordRepository.save(record);
    }

    /**
     * Update multiple health metrics at once
     */
    public HealthRecord updateMultipleMetrics(Long userId,
                                            Integer heartRate, String heartRateUnit,
                                            String bloodPressure, String bpUnit,
                                            BigDecimal weight, String weightUnit,
                                            BigDecimal temperature, String tempUnit,
                                            BigDecimal height, String heightUnit) {
        HealthRecord record = getOrCreateUserHealthRecord(userId);

        if (heartRate != null) {
            record.setHeartRateValue(heartRate);
            record.setHeartRateUnit(heartRateUnit != null ? heartRateUnit : "bpm");
        }

        if (bloodPressure != null) {
            record.setBpValue(bloodPressure);
            record.setBpUnit(bpUnit != null ? bpUnit : "mmHg");
        }

        if (weight != null) {
            record.setKgValue(weight);
            record.setKgUnit(weightUnit != null ? weightUnit : "kg");
        }

        if (temperature != null) {
            record.setTempValue(temperature);
            record.setTempUnit(tempUnit != null ? tempUnit : "°C");
        }

        if (height != null) {
            record.setHeightValue(height);
            record.setHeightUnit(heightUnit != null ? heightUnit : "cm");
        }

        return healthRecordRepository.save(record);
    }

    /**
     * Get all users' health records
     */
    public List<HealthRecord> getAllUsersHealthData() {
        return healthRecordRepository.findAllByOrderByLastUpdatedDesc();
    }

    /**
     * Get users with critical health status
     */
    public List<HealthRecord> getCriticalHealthUsers() {
        return healthRecordRepository.findByHealthStatus("critical");
    }

    /**
     * Get users with concerning health status
     */
    public List<HealthRecord> getConcerningHealthUsers() {
        return healthRecordRepository.findByHealthStatus("concerning");
    }

    /**
     * Get health records with complete vitals
     */
    public List<HealthRecord> getRecordsWithCompleteVitals() {
        return healthRecordRepository.findRecordsWithCompleteVitals();
    }

    /**
     * Search health records by user name or email
     */
    public List<HealthRecord> searchHealthRecords(String searchTerm) {
        return healthRecordRepository.searchRecords(searchTerm);
    }

    /**
     * Check if user has a health record
     */
    public boolean userHasHealthRecord(Long userId) {
        return healthRecordRepository.existsByUserId(userId);
    }

    /**
     * Delete user's health record
     */
    public void deleteUserHealthRecord(Long userId) {
        healthRecordRepository.deleteByUserId(userId);
    }
}

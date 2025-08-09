package rw.health.ubuzima.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordRequest {

    @NotNull(message = "Record type is required")
    private String recordType; // WEIGHT, HEIGHT, TEMPERATURE, HEART_RATE, BLOOD_PRESSURE

    // For single value records (weight, height, temperature, heart rate)
    private String value;

    // For blood pressure records
    @Min(value = 50, message = "Systolic pressure must be at least 50")
    @Max(value = 300, message = "Systolic pressure must not exceed 300")
    private Integer systolic;

    @Min(value = 30, message = "Diastolic pressure must be at least 30")
    @Max(value = 200, message = "Diastolic pressure must not exceed 200")
    private Integer diastolic;

    private String unit;

    private String notes;

    private String recordedBy; // Self-reported, Health Worker, Device

    private String recordingMethod; // Manual, Voice, Device, Import

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordedAt;

    // Validation helper methods
    public boolean isValidWeightRecord() {
        if (!"WEIGHT".equalsIgnoreCase(recordType)) return true;

        try {
            double weight = Double.parseDouble(value);
            return weight > 0 && weight <= 1000; // Reasonable weight range
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidHeightRecord() {
        if (!"HEIGHT".equalsIgnoreCase(recordType)) return true;

        try {
            double height = Double.parseDouble(value);
            return height > 0 && height <= 300; // Reasonable height range in cm
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidTemperatureRecord() {
        if (!"TEMPERATURE".equalsIgnoreCase(recordType)) return true;

        try {
            double temp = Double.parseDouble(value);
            return temp >= 30.0 && temp <= 45.0; // Reasonable temperature range
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidHeartRateRecord() {
        if (!"HEART_RATE".equalsIgnoreCase(recordType)) return true;

        try {
            int heartRate = Integer.parseInt(value);
            return heartRate > 0 && heartRate <= 300; // Reasonable heart rate range
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidBloodPressureRecord() {
        if (!"BLOOD_PRESSURE".equalsIgnoreCase(recordType)) return true;

        return systolic != null && diastolic != null &&
               systolic > diastolic &&
               systolic >= 50 && systolic <= 300 &&
               diastolic >= 30 && diastolic <= 200;
    }

    public boolean isValid() {
        if (recordType == null) return false;

        switch (recordType.toUpperCase()) {
            case "WEIGHT":
                return isValidWeightRecord();
            case "HEIGHT":
                return isValidHeightRecord();
            case "TEMPERATURE":
                return isValidTemperatureRecord();
            case "HEART_RATE":
                return isValidHeartRateRecord();
            case "BLOOD_PRESSURE":
                return isValidBloodPressureRecord();
            default:
                return false;
        }
    }

    public String getDisplayValue() {
        switch (recordType.toUpperCase()) {
            case "BLOOD_PRESSURE":
                return systolic + "/" + diastolic + " " + (unit != null ? unit : "mmHg");
            case "WEIGHT":
            case "HEIGHT":
            case "TEMPERATURE":
            case "HEART_RATE":
                return value + " " + (unit != null ? unit : "");
            default:
                return value != null ? value : "";
        }
    }

    public void setDefaultUnit() {
        if (unit == null || unit.isEmpty()) {
            switch (recordType.toUpperCase()) {
                case "WEIGHT":
                    unit = "kg";
                    break;
                case "HEIGHT":
                    unit = "cm";
                    break;
                case "TEMPERATURE":
                    unit = "°C";
                    break;
                case "HEART_RATE":
                    unit = "bpm";
                    break;
                case "BLOOD_PRESSURE":
                    unit = "mmHg";
                    break;
            }
        }
    }

    public void setDefaultRecordingInfo() {
        if (recordedBy == null || recordedBy.isEmpty()) {
            recordedBy = "Self-reported";
        }
        
        if (recordingMethod == null || recordingMethod.isEmpty()) {
            recordingMethod = "Manual";
        }
        
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }

    // Static factory methods for common record types
    public static HealthRecordRequest createWeightRecord(String weight, String unit) {
        return HealthRecordRequest.builder()
                .recordType("WEIGHT")
                .value(weight)
                .unit(unit != null ? unit : "kg")
                .build();
    }

    public static HealthRecordRequest createHeightRecord(String height, String unit) {
        return HealthRecordRequest.builder()
                .recordType("HEIGHT")
                .value(height)
                .unit(unit != null ? unit : "cm")
                .build();
    }

    public static HealthRecordRequest createTemperatureRecord(String temperature, String unit) {
        return HealthRecordRequest.builder()
                .recordType("TEMPERATURE")
                .value(temperature)
                .unit(unit != null ? unit : "°C")
                .build();
    }

    public static HealthRecordRequest createHeartRateRecord(String heartRate, String unit) {
        return HealthRecordRequest.builder()
                .recordType("HEART_RATE")
                .value(heartRate)
                .unit(unit != null ? unit : "bpm")
                .build();
    }

    public static HealthRecordRequest createBloodPressureRecord(Integer systolic, Integer diastolic, String unit) {
        return HealthRecordRequest.builder()
                .recordType("BLOOD_PRESSURE")
                .systolic(systolic)
                .diastolic(diastolic)
                .unit(unit != null ? unit : "mmHg")
                .build();
    }
}

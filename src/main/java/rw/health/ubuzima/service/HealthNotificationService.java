package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.health.ubuzima.entity.HealthRecord;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HealthNotificationService {

    private final UserRepository userRepository;
    private final InteractiveNotificationService interactiveNotificationService;

    /**
     * Create a health data notification for a health worker when their client records health data
     */
    public void createHealthDataNotification(Long healthWorkerId, Long clientId, Long healthRecordId,
                                           HealthRecord healthRecord) {
        try {
            User healthWorker = userRepository.findById(healthWorkerId)
                    .orElseThrow(() -> new RuntimeException("Health worker not found"));
            
            User client = userRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            String title = buildNotificationTitle(client, healthRecord);
            String message = buildNotificationMessage(client, healthRecord);

            // Determine notification priority based on health record severity
            String priority = determineNotificationPriority(healthRecord);

            // Send notification based on record type and value
            if (isCriticalReading(healthRecord)) {
                interactiveNotificationService.sendErrorNotification(
                    healthWorkerId,
                    "CRITICAL_HEALTH_READING",
                    message,
                    "CRITICAL_READING"
                );
            } else if (isConcerningReading(healthRecord)) {
                interactiveNotificationService.sendWarningNotification(
                    healthWorkerId,
                    "CONCERNING_HEALTH_READING",
                    message
                );
            } else {
                interactiveNotificationService.sendInfoNotification(
                    healthWorkerId,
                    "HEALTH_DATA_RECORDED",
                    message
                );
            }

            log.info("Health notification sent to health worker {} for client {} health record {}", 
                    healthWorkerId, clientId, healthRecordId);

        } catch (Exception e) {
            log.error("Failed to create health notification for health worker {}: {}", 
                    healthWorkerId, e.getMessage());
        }
    }

    /**
     * Create a notification when a client's health status changes significantly
     */
    public void createHealthStatusChangeNotification(Long healthWorkerId, Long clientId, 
                                                   String oldStatus, String newStatus) {
        try {
            User client = userRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            String title = String.format("Health Status Update - %s", client.getName());
            String message = String.format(
                "Client %s's health status has changed from %s to %s. Please review their recent health data.",
                client.getName(), oldStatus, newStatus
            );

            if ("critical".equalsIgnoreCase(newStatus)) {
                interactiveNotificationService.sendErrorNotification(
                    healthWorkerId, 
                    "HEALTH_STATUS_CRITICAL", 
                    message, 
                    "STATUS_CRITICAL"
                );
            } else if ("concerning".equalsIgnoreCase(newStatus)) {
                interactiveNotificationService.sendWarningNotification(
                    healthWorkerId, 
                    "HEALTH_STATUS_CONCERNING", 
                    message
                );
            } else {
                interactiveNotificationService.sendInfoNotification(
                    healthWorkerId, 
                    "HEALTH_STATUS_IMPROVED", 
                    message
                );
            }

            log.info("Health status change notification sent to health worker {} for client {}", 
                    healthWorkerId, clientId);

        } catch (Exception e) {
            log.error("Failed to create health status change notification: {}", e.getMessage());
        }
    }

    /**
     * Create a notification for missed health data recording
     */
    public void createMissedDataNotification(Long healthWorkerId, Long clientId, int daysSinceLastRecord) {
        try {
            User client = userRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            String title = String.format("Missing Health Data - %s", client.getName());
            String message = String.format(
                "Client %s hasn't recorded health data for %d days. Consider reaching out to check on their wellbeing.",
                client.getName(), daysSinceLastRecord
            );

            interactiveNotificationService.sendWarningNotification(
                healthWorkerId, 
                "MISSED_HEALTH_DATA", 
                message
            );

            log.info("Missed data notification sent to health worker {} for client {}", 
                    healthWorkerId, clientId);

        } catch (Exception e) {
            log.error("Failed to create missed data notification: {}", e.getMessage());
        }
    }

    /**
     * Create a notification for health trends that need attention
     */
    public void createTrendAlertNotification(Long healthWorkerId, Long clientId, 
                                           String trendType, String trendDescription) {
        try {
            User client = userRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            String title = String.format("Health Trend Alert - %s", client.getName());
            String message = String.format(
                "Client %s shows a concerning trend in %s: %s. Please review their health data and consider intervention.",
                client.getName(), trendType, trendDescription
            );

            interactiveNotificationService.sendWarningNotification(
                healthWorkerId, 
                "HEALTH_TREND_ALERT", 
                message
            );

            log.info("Trend alert notification sent to health worker {} for client {}", 
                    healthWorkerId, clientId);

        } catch (Exception e) {
            log.error("Failed to create trend alert notification: {}", e.getMessage());
        }
    }

    // Private helper methods

    private String buildNotificationTitle(User client, HealthRecord healthRecord) {
        if (isCriticalReading(healthRecord)) {
            return String.format("🚨 CRITICAL: %s - Health Reading", client.getName());
        } else if (isConcerningReading(healthRecord)) {
            return String.format("⚠️ ATTENTION: %s - Health Reading", client.getName());
        } else {
            return String.format("📊 New Data: %s - Health Update", client.getName());
        }
    }

    private String buildNotificationMessage(User client, HealthRecord healthRecord) {
        StringBuilder message = new StringBuilder();

        message.append("New health data recorded:\n");

        // Build message based on available health data
        if (healthRecord.getHeartRateValue() != null) {
            message.append(String.format("• Heart Rate: %d %s\n",
                healthRecord.getHeartRateValue(),
                healthRecord.getHeartRateUnit()));
        }

        if (healthRecord.getBpValue() != null) {
            message.append(String.format("• Blood Pressure: %s %s\n",
                healthRecord.getBpValue(),
                healthRecord.getBpUnit()));
        }

        if (healthRecord.getKgValue() != null) {
            message.append(String.format("• Weight: %.1f %s\n",
                healthRecord.getKgValue(),
                healthRecord.getKgUnit()));
        }

        if (healthRecord.getTempValue() != null) {
            message.append(String.format("• Temperature: %.1f %s\n",
                healthRecord.getTempValue(),
                healthRecord.getTempUnit()));
        }

        if (isCriticalReading(healthRecord)) {
            message.append("\n🚨 This reading requires immediate attention!");
        } else if (isConcerningReading(healthRecord)) {
            message.append("\n⚠️ This reading may need your attention.");
        }

        if (healthRecord.getNotes() != null && !healthRecord.getNotes().isEmpty()) {
            message.append(String.format("\n\nClient notes: %s", healthRecord.getNotes()));
        }

        message.append(String.format("\n\nRecorded: %s",
                healthRecord.getLastUpdated() != null ? healthRecord.getLastUpdated().toString() : "Unknown"));

        return message.toString();
    }

    private String determineNotificationPriority(HealthRecord healthRecord) {
        if (isCriticalReading(healthRecord)) {
            return "HIGH";
        } else if (isConcerningReading(healthRecord)) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Create a notification for successful health data submission (for clients)
     */
    public void createHealthDataSubmissionNotification(Long clientId, HealthRecord healthRecord) {
        try {
            String message = "Your health data has been recorded successfully and shared with your health worker.";

            // Add specific details if available
            StringBuilder details = new StringBuilder();
            if (healthRecord.getHeartRateValue() != null) {
                details.append(String.format(" Heart Rate: %d %s.",
                    healthRecord.getHeartRateValue(), healthRecord.getHeartRateUnit()));
            }
            if (healthRecord.getBpValue() != null) {
                details.append(String.format(" Blood Pressure: %s %s.",
                    healthRecord.getBpValue(), healthRecord.getBpUnit()));
            }
            if (healthRecord.getKgValue() != null) {
                details.append(String.format(" Weight: %.1f %s.",
                    healthRecord.getKgValue(), healthRecord.getKgUnit()));
            }
            if (healthRecord.getTempValue() != null) {
                details.append(String.format(" Temperature: %.1f %s.",
                    healthRecord.getTempValue(), healthRecord.getTempUnit()));
            }

            if (details.length() > 0) {
                message += details.toString();
            }

            interactiveNotificationService.sendSuccessNotification(
                clientId,
                "HEALTH_DATA_RECORDED",
                message
            );

            log.info("Health data submission notification sent to client {}", clientId);

        } catch (Exception e) {
            log.error("Failed to create health data submission notification: {}", e.getMessage());
        }
    }

    /**
     * Create a notification when health worker adds notes to a client's record
     */
    public void createHealthWorkerNoteNotification(Long clientId, String healthWorkerName, String notes) {
        try {
            String message = String.format(
                "Your health worker %s has added notes to your health record:\n\n%s",
                healthWorkerName, notes
            );

            interactiveNotificationService.sendInfoNotification(
                clientId, 
                "HEALTH_WORKER_NOTE", 
                message
            );

            log.info("Health worker note notification sent to client {}", clientId);

        } catch (Exception e) {
            log.error("Failed to create health worker note notification: {}", e.getMessage());
        }
    }

    // Helper methods to determine if readings are critical or concerning
    private boolean isCriticalReading(HealthRecord healthRecord) {
        try {
            // Check blood pressure
            if (healthRecord.getBpValue() != null && healthRecord.getBpValue().contains("/")) {
                String[] bpValues = healthRecord.getBpValue().split("/");
                if (bpValues.length == 2) {
                    int systolic = Integer.parseInt(bpValues[0]);
                    int diastolic = Integer.parseInt(bpValues[1]);
                    if (systolic >= 180 || diastolic >= 120) {
                        return true;
                    }
                }
            }

            // Check heart rate
            if (healthRecord.getHeartRateValue() != null) {
                int heartRate = healthRecord.getHeartRateValue();
                if (heartRate < 40 || heartRate > 150) {
                    return true;
                }
            }

            // Check temperature
            if (healthRecord.getTempValue() != null) {
                double temperature = healthRecord.getTempValue().doubleValue();
                if (temperature >= 40.0 || temperature <= 34.0) {
                    return true;
                }
            }

        } catch (NumberFormatException e) {
            log.warn("Could not parse health record value for critical assessment", e);
        }
        return false;
    }

    private boolean isConcerningReading(HealthRecord healthRecord) {
        try {
            // Check blood pressure
            if (healthRecord.getBpValue() != null && healthRecord.getBpValue().contains("/")) {
                String[] bpValues = healthRecord.getBpValue().split("/");
                if (bpValues.length == 2) {
                    int systolic = Integer.parseInt(bpValues[0]);
                    int diastolic = Integer.parseInt(bpValues[1]);
                    if (systolic >= 140 || diastolic >= 90) {
                        return true;
                    }
                }
            }

            // Check heart rate
            if (healthRecord.getHeartRateValue() != null) {
                int heartRate = healthRecord.getHeartRateValue();
                if (heartRate < 50 || heartRate > 120) {
                    return true;
                }
            }

            // Check temperature
            if (healthRecord.getTempValue() != null) {
                double temperature = healthRecord.getTempValue().doubleValue();
                if (temperature >= 39.0 || temperature <= 35.0) {
                    return true;
                }
            }

            // Check BMI if both weight and height are available
            if (healthRecord.getBmi() != null) {
                double bmi = healthRecord.getBmi().doubleValue();
                if (bmi < 18.5 || bmi > 30.0) {
                    return true;
                }
            }

        } catch (NumberFormatException e) {
            log.warn("Could not parse health record value for concerning assessment", e);
        }
        return false;
    }
}

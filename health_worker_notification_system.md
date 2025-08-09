# 🏥 Health Worker Notification System - Backend Implementation

## 📊 Database Schema

### 1. Client-Health Worker Assignments Table
```sql
CREATE TABLE client_health_worker_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    health_worker_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    assigned_by BIGINT, -- Admin who made the assignment
    notes TEXT,
    
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (health_worker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_active_assignment (client_id, health_worker_id, is_active),
    INDEX idx_client_active (client_id, is_active),
    INDEX idx_health_worker_active (health_worker_id, is_active)
);
```

### 2. Health Worker Notifications Table
```sql
CREATE TABLE health_worker_notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    health_worker_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    health_record_id BIGINT,
    notification_type ENUM('HEALTH_DATA_RECORDED', 'CRITICAL_READING', 'MISSED_MEDICATION', 'APPOINTMENT_REMINDER') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    is_read BOOLEAN DEFAULT FALSE,
    is_acknowledged BOOLEAN DEFAULT FALSE,
    metadata JSON, -- Additional data like health values, trends, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    acknowledged_at TIMESTAMP NULL,
    
    FOREIGN KEY (health_worker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (health_record_id) REFERENCES health_records(id) ON DELETE SET NULL,
    INDEX idx_health_worker_unread (health_worker_id, is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_severity (severity)
);
```

### 3. Health Alert Rules Table
```sql
CREATE TABLE health_alert_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_type ENUM('heart_rate', 'weight', 'blood_pressure', 'temperature') NOT NULL,
    condition_type ENUM('ABOVE', 'BELOW', 'RAPID_CHANGE', 'MISSING_DATA') NOT NULL,
    threshold_value DECIMAL(10,2),
    threshold_percentage DECIMAL(5,2), -- For percentage changes
    time_period_hours INT, -- For missing data alerts
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_metric_active (metric_type, is_active)
);
```

## 🎯 Backend API Implementation

### 1. Health Worker Notification Controller
```java
@RestController
@RequestMapping("/api/health-worker-notifications")
@CrossOrigin(origins = "*")
public class HealthWorkerNotificationController {

    @Autowired
    private HealthWorkerNotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            Long healthWorkerId = getUserIdFromAuth(authentication);
            Page<HealthWorkerNotification> notifications = notificationService.getNotifications(
                healthWorkerId, unreadOnly, PageRequest.of(page, size));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", notifications.getContent());
            response.put("totalElements", notifications.getTotalElements());
            response.put("unreadCount", notificationService.getUnreadCount(healthWorkerId));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{notificationId}/mark-read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        try {
            Long healthWorkerId = getUserIdFromAuth(authentication);
            notificationService.markAsRead(notificationId, healthWorkerId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{notificationId}/acknowledge")
    public ResponseEntity<Map<String, Object>> acknowledgeNotification(
            @PathVariable Long notificationId,
            @RequestBody(required = false) Map<String, String> response,
            Authentication authentication) {
        try {
            Long healthWorkerId = getUserIdFromAuth(authentication);
            String responseNote = response != null ? response.get("note") : null;
            notificationService.acknowledgeNotification(notificationId, healthWorkerId, responseNote);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification acknowledged"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/clients")
    public ResponseEntity<Map<String, Object>> getAssignedClients(
            Authentication authentication) {
        try {
            Long healthWorkerId = getUserIdFromAuth(authentication);
            List<ClientSummary> clients = notificationService.getAssignedClients(healthWorkerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("clients", clients);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(
            Authentication authentication) {
        try {
            Long healthWorkerId = getUserIdFromAuth(authentication);
            Map<String, Object> summary = notificationService.getDashboardSummary(healthWorkerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getId();
    }
}
```

### 2. Health Worker Notification Service
```java
@Service
@Transactional
public class HealthWorkerNotificationService {

    @Autowired
    private HealthWorkerNotificationRepository notificationRepository;

    @Autowired
    private ClientHealthWorkerAssignmentRepository assignmentRepository;

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HealthAlertRuleRepository alertRuleRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    public void createHealthDataNotification(Long clientId, HealthRecord healthRecord) {
        // Find assigned health workers for this client
        List<ClientHealthWorkerAssignment> assignments = assignmentRepository
            .findByClientIdAndIsActiveTrue(clientId);

        for (ClientHealthWorkerAssignment assignment : assignments) {
            // Check if this reading triggers any alerts
            NotificationSeverity severity = evaluateHealthReading(healthRecord);
            
            String title = generateNotificationTitle(healthRecord, severity);
            String message = generateNotificationMessage(healthRecord, severity);
            
            HealthWorkerNotification notification = new HealthWorkerNotification();
            notification.setHealthWorkerId(assignment.getHealthWorkerId());
            notification.setClientId(clientId);
            notification.setHealthRecordId(healthRecord.getId());
            notification.setNotificationType(NotificationType.HEALTH_DATA_RECORDED);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setSeverity(severity);
            
            // Add metadata with health values and trends
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("recordType", healthRecord.getType().getValue());
            metadata.put("value", healthRecord.getValue());
            metadata.put("unit", healthRecord.getUnit());
            metadata.put("recordedAt", healthRecord.getRecordedAt());
            
            // Add trend analysis
            addTrendAnalysis(metadata, clientId, healthRecord.getType());
            
            notification.setMetadata(metadata);
            
            HealthWorkerNotification saved = notificationRepository.save(notification);
            
            // Send push notification for high/critical severity
            if (severity == NotificationSeverity.HIGH || severity == NotificationSeverity.CRITICAL) {
                sendPushNotification(assignment.getHealthWorkerId(), saved);
            }
        }
    }

    private NotificationSeverity evaluateHealthReading(HealthRecord record) {
        List<HealthAlertRule> rules = alertRuleRepository
            .findByMetricTypeAndIsActiveTrue(record.getType());

        NotificationSeverity maxSeverity = NotificationSeverity.LOW;

        for (HealthAlertRule rule : rules) {
            if (isRuleTriggered(record, rule)) {
                if (rule.getSeverity().ordinal() > maxSeverity.ordinal()) {
                    maxSeverity = rule.getSeverity();
                }
            }
        }

        return maxSeverity;
    }

    private boolean isRuleTriggered(HealthRecord record, HealthAlertRule rule) {
        switch (rule.getConditionType()) {
            case ABOVE:
                return record.getValue() != null && 
                       record.getValue().compareTo(rule.getThresholdValue()) > 0;
            case BELOW:
                return record.getValue() != null && 
                       record.getValue().compareTo(rule.getThresholdValue()) < 0;
            case RAPID_CHANGE:
                return checkRapidChange(record, rule);
            default:
                return false;
        }
    }

    private boolean checkRapidChange(HealthRecord record, HealthAlertRule rule) {
        // Get previous reading of same type
        Optional<HealthRecord> previousRecord = healthRecordRepository
            .findTopByUserIdAndTypeAndRecordedAtBeforeOrderByRecordedAtDesc(
                record.getUserId(), record.getType(), record.getRecordedAt());

        if (previousRecord.isPresent() && record.getValue() != null && 
            previousRecord.get().getValue() != null) {
            
            BigDecimal change = record.getValue().subtract(previousRecord.get().getValue())
                .abs().divide(previousRecord.get().getValue(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            
            return change.compareTo(rule.getThresholdPercentage()) > 0;
        }
        
        return false;
    }

    private void addTrendAnalysis(Map<String, Object> metadata, Long clientId, HealthRecordType type) {
        // Get last 7 days of data for trend analysis
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<HealthRecord> recentRecords = healthRecordRepository
            .findByUserIdAndTypeAndRecordedAtAfterOrderByRecordedAtAsc(clientId, type, weekAgo);

        if (recentRecords.size() >= 2) {
            // Calculate trend
            BigDecimal firstValue = recentRecords.get(0).getValue();
            BigDecimal lastValue = recentRecords.get(recentRecords.size() - 1).getValue();
            
            if (firstValue != null && lastValue != null) {
                BigDecimal change = lastValue.subtract(firstValue);
                String trend = change.compareTo(BigDecimal.ZERO) > 0 ? "INCREASING" : 
                              change.compareTo(BigDecimal.ZERO) < 0 ? "DECREASING" : "STABLE";
                
                metadata.put("weeklyTrend", trend);
                metadata.put("weeklyChange", change);
                metadata.put("dataPoints", recentRecords.size());
            }
        }
    }

    private String generateNotificationTitle(HealthRecord record, NotificationSeverity severity) {
        String clientName = userRepository.findById(record.getUserId())
            .map(user -> user.getName()).orElse("Client");
        
        String metricName = getMetricDisplayName(record.getType());
        
        switch (severity) {
            case CRITICAL:
                return "🚨 CRITICAL: " + clientName + " - " + metricName;
            case HIGH:
                return "⚠️ HIGH ALERT: " + clientName + " - " + metricName;
            case MEDIUM:
                return "📊 " + clientName + " recorded " + metricName;
            default:
                return "📝 " + clientName + " updated health data";
        }
    }

    private String generateNotificationMessage(HealthRecord record, NotificationSeverity severity) {
        String value = formatHealthValue(record);
        String metricName = getMetricDisplayName(record.getType());
        
        StringBuilder message = new StringBuilder();
        message.append("New ").append(metricName.toLowerCase()).append(" reading: ").append(value);
        
        if (severity == NotificationSeverity.CRITICAL || severity == NotificationSeverity.HIGH) {
            message.append("\n\n⚠️ This reading requires immediate attention.");
        }
        
        if (record.getNotes() != null && !record.getNotes().trim().isEmpty()) {
            message.append("\n\nClient notes: ").append(record.getNotes());
        }
        
        return message.toString();
    }

    private String formatHealthValue(HealthRecord record) {
        switch (record.getType()) {
            case BLOOD_PRESSURE:
                return record.getSystolic() + "/" + record.getDiastolic() + " " + record.getUnit();
            default:
                return record.getValue() + " " + record.getUnit();
        }
    }

    private String getMetricDisplayName(HealthRecordType type) {
        switch (type) {
            case HEART_RATE: return "Heart Rate";
            case WEIGHT: return "Weight";
            case BLOOD_PRESSURE: return "Blood Pressure";
            case TEMPERATURE: return "Temperature";
            default: return "Health Metric";
        }
    }

    public Page<HealthWorkerNotification> getNotifications(Long healthWorkerId, boolean unreadOnly, Pageable pageable) {
        if (unreadOnly) {
            return notificationRepository.findByHealthWorkerIdAndIsReadFalseOrderByCreatedAtDesc(
                healthWorkerId, pageable);
        } else {
            return notificationRepository.findByHealthWorkerIdOrderByCreatedAtDesc(
                healthWorkerId, pageable);
        }
    }

    public long getUnreadCount(Long healthWorkerId) {
        return notificationRepository.countByHealthWorkerIdAndIsReadFalse(healthWorkerId);
    }

    public void markAsRead(Long notificationId, Long healthWorkerId) {
        HealthWorkerNotification notification = notificationRepository
            .findByIdAndHealthWorkerId(notificationId, healthWorkerId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public void acknowledgeNotification(Long notificationId, Long healthWorkerId, String responseNote) {
        HealthWorkerNotification notification = notificationRepository
            .findByIdAndHealthWorkerId(notificationId, healthWorkerId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setIsAcknowledged(true);
        notification.setAcknowledgedAt(LocalDateTime.now());
        
        if (responseNote != null) {
            Map<String, Object> metadata = notification.getMetadata();
            if (metadata == null) metadata = new HashMap<>();
            metadata.put("healthWorkerResponse", responseNote);
            notification.setMetadata(metadata);
        }
        
        notificationRepository.save(notification);
    }

    private void sendPushNotification(Long healthWorkerId, HealthWorkerNotification notification) {
        // Implementation for push notifications
        pushNotificationService.sendToHealthWorker(healthWorkerId, notification.getTitle(), 
            notification.getMessage(), notification.getSeverity());
    }
}
```

## 🔄 Integration with Health Record Creation

### Updated Health Record Controller
```java
@PostMapping
public ResponseEntity<Map<String, Object>> createHealthRecord(
        @RequestBody HealthRecordRequest request,
        Authentication authentication) {
    try {
        Long userId = getUserIdFromAuth(authentication);
        HealthRecord record = healthRecordService.createHealthRecord(userId, request);

        // Trigger health worker notifications
        healthWorkerNotificationService.createHealthDataNotification(userId, record);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("record", record);
        response.put("message", "Health record created successfully");

        return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "message", e.getMessage()));
    }
}
```

### Sample Health Alert Rules (Insert into database)
```sql
INSERT INTO health_alert_rules (metric_type, condition_type, threshold_value, severity, is_active) VALUES
-- Blood Pressure Alerts
('blood_pressure', 'ABOVE', 140, 'HIGH', true),  -- Systolic > 140
('blood_pressure', 'ABOVE', 180, 'CRITICAL', true),  -- Systolic > 180

-- Heart Rate Alerts
('heart_rate', 'ABOVE', 100, 'MEDIUM', true),  -- HR > 100
('heart_rate', 'ABOVE', 120, 'HIGH', true),    -- HR > 120
('heart_rate', 'BELOW', 50, 'HIGH', true),     -- HR < 50

-- Weight Alerts (rapid changes)
('weight', 'RAPID_CHANGE', NULL, 'MEDIUM', true),  -- threshold_percentage = 5%

-- Temperature Alerts
('temperature', 'ABOVE', 38.0, 'HIGH', true),     -- Fever > 38°C
('temperature', 'ABOVE', 39.5, 'CRITICAL', true), -- High fever > 39.5°C
('temperature', 'BELOW', 35.0, 'HIGH', true);     -- Hypothermia < 35°C

-- Update the rapid change rule with percentage
UPDATE health_alert_rules
SET threshold_percentage = 5.0
WHERE condition_type = 'RAPID_CHANGE' AND metric_type = 'weight';
```

### Sample Client-Health Worker Assignments
```sql
INSERT INTO client_health_worker_assignments (client_id, health_worker_id, assigned_by, is_active) VALUES
(1, 2, 3, true),  -- Client 1 assigned to Health Worker 2 by Admin 3
(4, 2, 3, true),  -- Client 4 assigned to Health Worker 2 by Admin 3
(5, 6, 3, true);  -- Client 5 assigned to Health Worker 6 by Admin 3
```

## 📱 Frontend Integration Flow

### 1. Client Records Health Data
```
Client App → HealthDataInputScreen → HealthTrackingService.addHealthRecord() → Backend API
```

### 2. Backend Processing
```
Backend API → Save Health Record → Evaluate Alert Rules → Create Notifications → Send Push Notifications
```

### 3. Health Worker Receives Notifications
```
Health Worker App → Dashboard (shows unread count) → Notifications Screen → View/Acknowledge Alerts
```

### 4. Real-time Updates
```
Push Notification → Health Worker Mobile → Open App → View Client Details → Take Action
```

## 🚨 Critical Alert Workflow

### When Critical Reading is Detected:
1. **Immediate Notification**: Health worker gets push notification
2. **Dashboard Badge**: Unread count updates in real-time
3. **Severity Indicators**: Red badges for critical alerts
4. **Quick Actions**: One-tap calling and acknowledgment
5. **Trend Analysis**: Historical context provided
6. **Follow-up Tracking**: System tracks response times

## 📊 Sample Notification Scenarios

### Scenario 1: Critical Blood Pressure
```
Client: Marie Uwimana records BP 180/110 mmHg
System: Evaluates against rules (>180 = CRITICAL)
Notification: "🚨 CRITICAL: Marie Uwimana - Blood Pressure"
Health Worker: Gets immediate push notification + dashboard alert
Action: Can call client directly or acknowledge with response plan
```

### Scenario 2: Weight Trend Alert
```
Client: Jean records weight showing 10% increase over week
System: Detects rapid change pattern
Notification: "⚠️ HIGH ALERT: Jean Mukamana - Weight Trend"
Health Worker: Reviews trend data and client notes
Action: Schedule consultation or provide guidance
```

### Scenario 3: Regular Health Update
```
Client: Alice records normal heart rate 72 bpm
System: No alert rules triggered
Notification: "📊 Alice Nyirahabimana - Heart Rate"
Health Worker: Routine notification for record keeping
Action: Review when convenient, no immediate action needed
```

This backend implementation provides:

1. **Automatic Notifications**: When clients record health data, assigned health workers get notified
2. **Smart Alerts**: Critical readings trigger immediate high-priority notifications
3. **Trend Analysis**: Notifications include trend information (increasing/decreasing patterns)
4. **Severity Levels**: Different alert levels based on configurable health rules
5. **Client Assignment System**: Manages which health workers are assigned to which clients
6. **Dashboard Integration**: Summary data for health worker dashboards
7. **Push Notifications**: Real-time alerts for critical readings
8. **Response Tracking**: System tracks health worker acknowledgments and response times
9. **Historical Context**: Notifications include trend analysis and previous readings
10. **Flexible Rules Engine**: Configurable alert thresholds for different health metrics

The system automatically evaluates health readings against configurable rules and sends appropriate notifications to assigned health workers, ensuring timely medical intervention when needed.

package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.service.VoiceNoteCleanupService;

import java.util.Map;

/**
 * Admin controller for voice note management and cleanup
 * Only accessible by admin users
 */
@RestController
@RequestMapping("/admin/voice-notes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VoiceNoteAdminController {

    private final VoiceNoteCleanupService voiceNoteCleanupService;

    /**
     * Get voice note statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getVoiceNoteStats() {
        try {
            VoiceNoteCleanupService.VoiceNoteStats stats = voiceNoteCleanupService.getVoiceNoteStats();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "totalVoiceMessages", stats.totalVoiceMessages,
                    "expiredVoiceMessages", stats.expiredVoiceMessages,
                    "totalFileSize", stats.totalFileSize,
                    "totalFileSizeMB", stats.totalFileSize / (1024 * 1024),
                    "retentionDays", stats.retentionDays
                ),
                "message", "Voice note statistics retrieved successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error getting voice note stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get voice note statistics: " + e.getMessage()
            ));
        }
    }

    /**
     * Manually trigger voice note cleanup
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerCleanup(
            @RequestParam(defaultValue = "7") int daysOld) {
        
        try {
            if (daysOld < 1) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Days old must be at least 1"
                ));
            }
            
            if (daysOld > 365) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Days old cannot exceed 365"
                ));
            }
            
            log.info("Admin triggered manual voice note cleanup for {} days old", daysOld);
            voiceNoteCleanupService.manualCleanup(daysOld);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Voice note cleanup completed for messages older than " + daysOld + " days"
            ));
            
        } catch (Exception e) {
            log.error("Error during manual cleanup: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to cleanup voice notes: " + e.getMessage()
            ));
        }
    }

    /**
     * Get cleanup configuration
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCleanupConfig() {
        try {
            VoiceNoteCleanupService.VoiceNoteStats stats = voiceNoteCleanupService.getVoiceNoteStats();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "retentionDays", stats.retentionDays,
                    "cleanupSchedule", "Daily at 2:00 AM",
                    "description", "Voice notes are automatically deleted after " + stats.retentionDays + " days"
                ),
                "message", "Cleanup configuration retrieved successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error getting cleanup config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get cleanup configuration: " + e.getMessage()
            ));
        }
    }

    /**
     * Health check for voice note cleanup service
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            VoiceNoteCleanupService.VoiceNoteStats stats = voiceNoteCleanupService.getVoiceNoteStats();
            
            boolean isHealthy = true;
            String status = "healthy";
            String message = "Voice note cleanup service is running normally";
            
            // Check if there are too many expired messages (potential issue)
            if (stats.expiredVoiceMessages > 1000) {
                isHealthy = false;
                status = "warning";
                message = "Large number of expired voice messages detected (" + stats.expiredVoiceMessages + ")";
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "status", status,
                    "healthy", isHealthy,
                    "expiredMessages", stats.expiredVoiceMessages,
                    "totalMessages", stats.totalVoiceMessages,
                    "retentionDays", stats.retentionDays
                ),
                "message", message
            ));
            
        } catch (Exception e) {
            log.error("Error during health check: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "data", Map.of(
                    "status", "error",
                    "healthy", false
                ),
                "message", "Voice note cleanup service health check failed: " + e.getMessage()
            ));
        }
    }
}

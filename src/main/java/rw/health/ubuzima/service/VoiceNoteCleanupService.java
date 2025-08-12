package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.health.ubuzima.entity.Message;
import rw.health.ubuzima.enums.MessageType;
import rw.health.ubuzima.repository.MessageRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for cleaning up old voice note files
 * Implements 1-week retention policy for voice messages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceNoteCleanupService {

    private final MessageRepository messageRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.voice-note.retention-days:7}")
    private int retentionDays;

    /**
     * Scheduled task to clean up old voice notes
     * Runs daily at 2:00 AM to clean up voice notes older than 7 days
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2:00 AM
    @Transactional
    public void cleanupOldVoiceNotes() {
        log.info("Starting scheduled voice note cleanup...");

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            
            // Find old voice/audio messages
            List<Message> oldVoiceMessages = messageRepository.findOldAudioMessages(cutoffDate);
            
            int deletedFiles = 0;
            int deletedRecords = 0;
            
            for (Message message : oldVoiceMessages) {
                try {
                    // Delete physical audio file
                    if (deleteAudioFile(message)) {
                        deletedFiles++;
                    }
                    
                    // Update message to remove audio data but keep text record
                    cleanupMessageAudioData(message);
                    deletedRecords++;
                    
                } catch (Exception e) {
                    log.warn("Failed to cleanup voice message {}: {}", 
                        message.getId(), e.getMessage());
                }
            }
            
            log.info("Voice note cleanup completed: {} files deleted, {} records cleaned", 
                deletedFiles, deletedRecords);
                
        } catch (Exception e) {
            log.error("Error during voice note cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete the physical audio file
     */
    private boolean deleteAudioFile(Message message) {
        if (message.getAudioUrl() == null) {
            return false;
        }

        try {
            // Extract filename from URL (e.g., "/audio/download/filename.m4a" -> "filename.m4a")
            String filename = message.getAudioUrl().substring(
                message.getAudioUrl().lastIndexOf("/") + 1);
            
            Path filePath = Paths.get(uploadDir, "audio").resolve(filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("Deleted audio file: {}", filename);
                return true;
            } else {
                log.debug("Audio file not found: {}", filename);
                return false;
            }
            
        } catch (IOException e) {
            log.warn("Failed to delete audio file for message {}: {}", 
                message.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Clean up audio-related data from message but keep the message record
     * This preserves chat history while removing the audio file references
     */
    private void cleanupMessageAudioData(Message message) {
        // Clear audio-specific fields
        message.setAudioUrl(null);
        message.setAudioDuration(null);
        message.setFileSize(null);
        message.setMimeType(null);
        
        // Update content to indicate voice note was deleted
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            message.setContent("🎤 Voice message (expired after " + retentionDays + " days)");
        }
        
        // Change message type to TEXT to indicate it's no longer playable
        message.setMessageType(MessageType.TEXT);
        
        // Save the updated message
        messageRepository.save(message);
        
        log.debug("Cleaned up audio data for message {}", message.getId());
    }

    /**
     * Manual cleanup method for testing or admin use
     */
    public void manualCleanup(int daysOld) {
        log.info("Starting manual voice note cleanup for messages older than {} days", daysOld);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<Message> oldVoiceMessages = messageRepository.findOldAudioMessages(cutoffDate);
        
        int deletedFiles = 0;
        int deletedRecords = 0;
        
        for (Message message : oldVoiceMessages) {
            try {
                if (deleteAudioFile(message)) {
                    deletedFiles++;
                }
                cleanupMessageAudioData(message);
                deletedRecords++;
            } catch (Exception e) {
                log.warn("Failed to cleanup voice message {}: {}", 
                    message.getId(), e.getMessage());
            }
        }
        
        log.info("Manual cleanup completed: {} files deleted, {} records cleaned", 
            deletedFiles, deletedRecords);
    }

    /**
     * Get statistics about voice note storage
     */
    public VoiceNoteStats getVoiceNoteStats() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            
            long totalVoiceMessages = messageRepository.countByMessageTypeIn(
                List.of(MessageType.AUDIO, MessageType.VOICE));
            
            long expiredVoiceMessages = messageRepository.countOldAudioMessages(cutoffDate);
            
            // Calculate total file size (approximate)
            long totalFileSize = messageRepository.sumAudioFileSize();
            
            return new VoiceNoteStats(
                totalVoiceMessages,
                expiredVoiceMessages,
                totalFileSize,
                retentionDays
            );
            
        } catch (Exception e) {
            log.error("Error getting voice note stats: {}", e.getMessage());
            return new VoiceNoteStats(0, 0, 0, retentionDays);
        }
    }

    /**
     * Voice note statistics data class
     */
    public static class VoiceNoteStats {
        public final long totalVoiceMessages;
        public final long expiredVoiceMessages;
        public final long totalFileSize;
        public final int retentionDays;

        public VoiceNoteStats(long totalVoiceMessages, long expiredVoiceMessages, 
                             long totalFileSize, int retentionDays) {
            this.totalVoiceMessages = totalVoiceMessages;
            this.expiredVoiceMessages = expiredVoiceMessages;
            this.totalFileSize = totalFileSize;
            this.retentionDays = retentionDays;
        }
    }
}

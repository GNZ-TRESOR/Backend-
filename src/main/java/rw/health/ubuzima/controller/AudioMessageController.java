package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.health.ubuzima.entity.Message;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.MessageType;
import rw.health.ubuzima.repository.MessageRepository;
import rw.health.ubuzima.repository.UserRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling audio message uploads and downloads
 * Supports WhatsApp-like voice messaging functionality
 */
@RestController
@RequestMapping("/audio")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AudioMessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:10MB}")
    private String maxFileSize;

    /**
     * Upload audio file for voice message
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("senderId") Long senderId,
            @RequestParam("receiverId") Long receiverId,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "conversationId", required = false) String conversationId) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Audio file is required"
                ));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !isValidAudioType(contentType)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid audio file type. Supported: MP3, WAV, M4A, AAC"
                ));
            }

            // Validate file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Audio file too large. Maximum size is 10MB"
                ));
            }

            // Validate users
            User sender = userRepository.findById(senderId).orElse(null);
            User receiver = userRepository.findById(receiverId).orElse(null);
            
            if (sender == null || receiver == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid sender or receiver"
                ));
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "audio");
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".m4a";
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create audio URL
            String audioUrl = "/audio/download/" + filename;

            // Create message
            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setMessageType(MessageType.AUDIO);
            message.setAudioUrl(audioUrl);
            message.setAudioDuration(duration);
            message.setFileSize(file.getSize());
            message.setMimeType(contentType);
            message.setMessageStatus("SENT");
            
            // Generate conversation ID if not provided
            if (conversationId == null || conversationId.trim().isEmpty()) {
                conversationId = generateConversationId(senderId, receiverId);
            }
            message.setConversationId(conversationId);

            // Save message
            Message savedMessage = messageRepository.save(message);

            log.info("Audio message uploaded successfully: {} ({}KB)", 
                filename, file.getSize() / 1024);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Audio uploaded successfully",
                "audioUrl", audioUrl,
                "messageId", savedMessage.getId(),
                "duration", duration != null ? duration : 0,
                "fileSize", file.getSize()
            ));

        } catch (IOException e) {
            log.error("Failed to upload audio file", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to upload audio file: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error during audio upload", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Unexpected error: " + e.getMessage()
            ));
        }
    }

    /**
     * Download audio file
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadAudio(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, "audio").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "audio/mpeg"; // Default to MP3
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + filename + "\"")
                .body(resource);

        } catch (MalformedURLException e) {
            log.error("Invalid file path: {}", filename, e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Error reading audio file: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stream audio file with range support for better playback
     */
    @GetMapping("/stream/{filename}")
    public ResponseEntity<Resource> streamAudio(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        
        try {
            Path filePath = Paths.get(uploadDir, "audio").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            long fileSize = resource.contentLength();
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "audio/mpeg";
            }

            // Handle range requests for audio streaming
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && !ranges[1].isEmpty() 
                    ? Long.parseLong(ranges[1]) 
                    : fileSize - 1;

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_RANGE, 
                        String.format("bytes %d-%d/%d", start, end, fileSize))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(end - start + 1))
                    .body(resource);
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                .body(resource);

        } catch (Exception e) {
            log.error("Error streaming audio file: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get audio message details
     */
    @GetMapping("/message/{messageId}")
    public ResponseEntity<Map<String, Object>> getAudioMessage(@PathVariable Long messageId) {
        try {
            Message message = messageRepository.findById(messageId).orElse(null);
            
            if (message == null) {
                return ResponseEntity.notFound().build();
            }

            if (message.getMessageType() != MessageType.AUDIO) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Message is not an audio message"
                ));
            }

            Map<String, Object> audioInfo = new HashMap<>();
            audioInfo.put("id", message.getId());
            audioInfo.put("audioUrl", message.getAudioUrl());
            audioInfo.put("duration", message.getAudioDuration());
            audioInfo.put("fileSize", message.getFileSize());
            audioInfo.put("mimeType", message.getMimeType());
            audioInfo.put("createdAt", message.getCreatedAt());
            audioInfo.put("senderId", message.getSender().getId());
            audioInfo.put("senderName", message.getSender().getName());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "audioMessage", audioInfo
            ));

        } catch (Exception e) {
            log.error("Error getting audio message details", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get audio message details"
            ));
        }
    }

    /**
     * Delete audio file and message
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteAudioMessage(
            @PathVariable Long messageId,
            @RequestParam Long requesterId) {
        
        try {
            Message message = messageRepository.findById(messageId).orElse(null);
            
            if (message == null) {
                return ResponseEntity.notFound().build();
            }

            // Only sender can delete
            if (!message.getSender().getId().equals(requesterId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You can only delete your own messages"
                ));
            }

            // Delete file if exists
            if (message.getAudioUrl() != null) {
                String filename = message.getAudioUrl().substring(
                    message.getAudioUrl().lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir, "audio").resolve(filename);
                
                try {
                    Files.deleteIfExists(filePath);
                    log.info("Deleted audio file: {}", filename);
                } catch (IOException e) {
                    log.warn("Failed to delete audio file: {}", filename, e);
                }
            }

            // Mark message as deleted
            message.setDeletedAt(LocalDateTime.now());
            messageRepository.save(message);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Audio message deleted successfully"
            ));

        } catch (Exception e) {
            log.error("Error deleting audio message", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete audio message"
            ));
        }
    }

    /**
     * Check if file type is valid audio format
     */
    private boolean isValidAudioType(String contentType) {
        return contentType.equals("audio/mpeg") ||      // MP3
               contentType.equals("audio/wav") ||       // WAV
               contentType.equals("audio/x-wav") ||     // WAV alternative
               contentType.equals("audio/mp4") ||       // M4A
               contentType.equals("audio/aac") ||       // AAC
               contentType.equals("audio/x-m4a") ||     // M4A alternative
               contentType.equals("audio/webm") ||      // WebM audio
               contentType.equals("audio/ogg");         // OGG
    }

    /**
     * Generate conversation ID between two users
     */
    private String generateConversationId(Long userId1, Long userId2) {
        long smaller = Math.min(userId1, userId2);
        long larger = Math.max(userId1, userId2);
        return "conv_" + smaller + "_" + larger;
    }
}

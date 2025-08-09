package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import rw.health.ubuzima.dto.response.FileUploadResponse;
import rw.health.ubuzima.entity.FileUpload;
import rw.health.ubuzima.repository.FileUploadRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    @Value("${ubuzima.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    private final FileUploadRepository fileUploadRepository;

    public FileUploadResponse storeFile(MultipartFile file, String userId, String fileType) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file");
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Invalid file path: " + originalFilename);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            Path targetLocation = uploadPath.resolve(uniqueFilename);

            // Copy file to target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Save file metadata to database
            FileUpload fileUpload = FileUpload.builder()
                .id(UUID.randomUUID().toString())
                .filename(uniqueFilename)
                .originalFilename(originalFilename)
                .fileType(fileType != null ? fileType : "general")
                .mimeType(file.getContentType())
                .size(file.getSize())
                .userId(userId)
                .filePath(targetLocation.toString())
                .uploadedAt(LocalDateTime.now())
                .build();

            fileUpload = fileUploadRepository.save(fileUpload);

            // Generate URLs
            String fileUrl = generateFileUrl(fileUpload.getId());
            String downloadUrl = generateDownloadUrl(fileUpload.getId());

            return FileUploadResponse.builder()
                .id(fileUpload.getId())
                .url(fileUrl)
                .filename(uniqueFilename)
                .originalFilename(originalFilename)
                .size(file.getSize())
                .mimeType(file.getContentType())
                .fileType(fileType)
                .userId(userId)
                .uploadedAt(fileUpload.getUploadedAt())
                .downloadUrl(downloadUrl)
                .build();

        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    public Resource loadFileAsResource(String fileId) {
        try {
            FileUpload fileUpload = fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

            Path filePath = Paths.get(fileUpload.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + fileId);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + fileId);
        }
    }

    public String getFileContentType(String fileId) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found: " + fileId));
        return fileUpload.getMimeType();
    }

    public boolean deleteFile(String fileId) {
        try {
            FileUpload fileUpload = fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

            // Delete physical file
            Path filePath = Paths.get(fileUpload.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete database record
            fileUploadRepository.delete(fileUpload);

            return true;
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage());
            return false;
        }
    }

    public List<FileUploadResponse> getUserFiles(String userId) {
        List<FileUpload> files = fileUploadRepository.findByUserIdOrderByUploadedAtDesc(userId);
        return files.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    private FileUploadResponse convertToResponse(FileUpload fileUpload) {
        return FileUploadResponse.builder()
            .id(fileUpload.getId())
            .url(generateFileUrl(fileUpload.getId()))
            .filename(fileUpload.getFilename())
            .originalFilename(fileUpload.getOriginalFilename())
            .size(fileUpload.getSize())
            .mimeType(fileUpload.getMimeType())
            .fileType(fileUpload.getFileType())
            .userId(fileUpload.getUserId())
            .uploadedAt(fileUpload.getUploadedAt())
            .downloadUrl(generateDownloadUrl(fileUpload.getId()))
            .lessonId(fileUpload.getLessonId())
            .description(fileUpload.getDescription())
            .metadata(fileUpload.getMetadata())
            .thumbnailUrl(fileUpload.getThumbnailUrl())
            .duration(fileUpload.getDuration())
            .resolution(fileUpload.getResolution())
            .category(fileUpload.getCategory())
            .build();
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private String generateFileUrl(String fileId) {
        return "http://localhost:" + serverPort + "/files/download/" + fileId;
    }

    private String generateDownloadUrl(String fileId) {
        return "http://localhost:" + serverPort + "/files/download/" + fileId;
    }

    // Education-specific file storage methods

    public FileUploadResponse storeEducationFile(MultipartFile file, String userId, String fileType,
                                               Long lessonId, String title, String description) {
        try {
            // Validate file type for education content
            validateEducationFile(file, fileType);

            // Store the file using existing method
            FileUploadResponse response = storeFile(file, userId, fileType);

            // Update the file record with education-specific metadata
            FileUpload fileUpload = fileUploadRepository.findById(response.getId())
                .orElseThrow(() -> new RuntimeException("File not found after upload"));

            fileUpload.setLessonId(lessonId);
            fileUpload.setDescription(description);
            fileUpload.setCategory("education");

            // Extract additional metadata based on file type
            if (fileType.contains("video")) {
                // For videos, you could extract duration, resolution, etc.
                // This would require additional libraries like FFmpeg
                fileUpload.setMetadata(createVideoMetadata(file));
            } else if (fileType.contains("audio")) {
                fileUpload.setMetadata(createAudioMetadata(file));
            } else if (fileType.contains("document")) {
                fileUpload.setMetadata(createDocumentMetadata(file));
            }

            fileUploadRepository.save(fileUpload);

            return convertToResponse(fileUpload);

        } catch (Exception e) {
            log.error("Failed to store education file: {}", e.getMessage());
            throw new RuntimeException("Failed to store education file: " + e.getMessage());
        }
    }

    public List<FileUploadResponse> getLessonFiles(Long lessonId) {
        List<FileUpload> files = fileUploadRepository.findByLessonIdOrderByUploadedAtDesc(lessonId);
        return files.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public List<FileUploadResponse> getFilesByType(String fileType) {
        List<FileUpload> files = fileUploadRepository.findByFileTypeOrderByUploadedAtDesc(fileType);
        return files.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    private void validateEducationFile(MultipartFile file, String fileType) {
        String contentType = file.getContentType();
        long maxSize = getMaxFileSize(fileType);

        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds maximum allowed size for " + fileType);
        }

        if (!isValidContentType(contentType, fileType)) {
            throw new RuntimeException("Invalid file type for " + fileType + ": " + contentType);
        }
    }

    private long getMaxFileSize(String fileType) {
        switch (fileType) {
            case "education_video":
                return 500 * 1024 * 1024; // 500MB for videos
            case "education_audio":
                return 50 * 1024 * 1024;  // 50MB for audio
            case "education_document":
                return 25 * 1024 * 1024;  // 25MB for documents
            case "education_image":
                return 10 * 1024 * 1024;  // 10MB for images
            default:
                return 10 * 1024 * 1024;  // 10MB default
        }
    }

    private boolean isValidContentType(String contentType, String fileType) {
        if (contentType == null) return false;

        switch (fileType) {
            case "education_video":
                return contentType.startsWith("video/");
            case "education_audio":
                return contentType.startsWith("audio/");
            case "education_document":
                return contentType.equals("application/pdf") ||
                       contentType.equals("application/msword") ||
                       contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                       contentType.equals("text/plain");
            case "education_image":
                return contentType.startsWith("image/");
            default:
                return true;
        }
    }

    private String createVideoMetadata(MultipartFile file) {
        // Basic metadata - in production, you'd use FFmpeg or similar
        return String.format("{\"type\":\"video\",\"size\":%d,\"contentType\":\"%s\"}",
                           file.getSize(), file.getContentType());
    }

    private String createAudioMetadata(MultipartFile file) {
        return String.format("{\"type\":\"audio\",\"size\":%d,\"contentType\":\"%s\"}",
                           file.getSize(), file.getContentType());
    }

    private String createDocumentMetadata(MultipartFile file) {
        return String.format("{\"type\":\"document\",\"size\":%d,\"contentType\":\"%s\"}",
                           file.getSize(), file.getContentType());
    }
}

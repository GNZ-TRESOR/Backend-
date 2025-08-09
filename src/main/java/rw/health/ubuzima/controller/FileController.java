package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.health.ubuzima.dto.response.ApiResponse;
import rw.health.ubuzima.dto.response.FileUploadResponse;
import rw.health.ubuzima.service.FileStorageService;
import rw.health.ubuzima.util.ResponseUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "type", required = false) String type) {
        try {
            FileUploadResponse response = fileStorageService.storeFile(file, userId, type);
            return ResponseUtil.clientSuccess(response, "File Upload");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "File Upload",
                "Failed to upload file: " + e.getMessage(),
                "FILE_UPLOAD_ERROR"
            );
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "type", required = false) String type) {
        try {
            List<FileUploadResponse> responses = files.stream()
                .map(file -> fileStorageService.storeFile(file, userId, type))
                .collect(Collectors.toList());
            return ResponseUtil.clientSuccess(responses, "Multiple File Upload");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Multiple File Upload",
                "Failed to upload files: " + e.getMessage(),
                "FILE_UPLOAD_ERROR"
            );
        }
    }

    @PostMapping("/upload/profile-image")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId) {
        try {
            FileUploadResponse response = fileStorageService.storeFile(file, userId, "profile_image");
            return ResponseUtil.clientSuccess(response, "Profile Image Upload");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Profile Image Upload",
                "Failed to upload profile image: " + e.getMessage(),
                "FILE_UPLOAD_ERROR"
            );
        }
    }

    @PostMapping("/upload/health-document")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadHealthDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "metadata", required = false) String metadata) {
        try {
            FileUploadResponse response = fileStorageService.storeFile(file, userId, "health_document");
            return ResponseUtil.clientSuccess(response, "Health Document Upload");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Health Document Upload",
                "Failed to upload health document: " + e.getMessage(),
                "FILE_UPLOAD_ERROR"
            );
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileId);
            String contentType = fileStorageService.getFileContentType(fileId);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileId) {
        try {
            boolean deleted = fileStorageService.deleteFile(fileId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "File deleted successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete file: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> getUserFiles(@PathVariable String userId) {
        try {
            List<FileUploadResponse> files = fileStorageService.getUserFiles(userId);
            return ResponseUtil.clientSuccess(files, "User Files");
        } catch (Exception e) {
            return ResponseUtil.clientError(
                "User Files",
                "Failed to retrieve user files: " + e.getMessage(),
                "FILE_RETRIEVAL_ERROR",
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // Education-specific file upload endpoints

    @PostMapping("/upload/education/video")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadEducationVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description) {
        try {
            FileUploadResponse response = fileStorageService.storeEducationFile(
                file, userId, "education_video", lessonId, title, description);
            return ResponseUtil.clientSuccess(response, "Education Video Upload");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Education Video Upload",
                "Failed to upload video: " + e.getMessage(),
                "VIDEO_UPLOAD_ERROR"
            );
        }
    }

    @PostMapping("/upload/education/document")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadEducationDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description) {
        try {
            FileUploadResponse response = fileStorageService.storeEducationFile(
                file, userId, "education_document", lessonId, title, description);
            return ResponseUtil.clientSuccess(response, "Education Document Upload");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Education Document Upload",
                "Failed to upload document: " + e.getMessage(),
                "DOCUMENT_UPLOAD_ERROR"
            );
        }
    }

    @PostMapping("/upload/education/audio")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadEducationAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description) {
        try {
            FileUploadResponse response = fileStorageService.storeEducationFile(
                file, userId, "education_audio", lessonId, title, description);
            return ResponseUtil.clientSuccess(response, "Education Audio Upload");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Education Audio Upload",
                "Failed to upload audio: " + e.getMessage(),
                "AUDIO_UPLOAD_ERROR"
            );
        }
    }

    @PostMapping("/upload/education/image")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadEducationImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description) {
        try {
            FileUploadResponse response = fileStorageService.storeEducationFile(
                file, userId, "education_image", lessonId, title, description);
            return ResponseUtil.clientSuccess(response, "Education Image Upload");
        } catch (Exception e) {
            return ResponseUtil.clientBadRequest(
                "Education Image Upload",
                "Failed to upload image: " + e.getMessage(),
                "IMAGE_UPLOAD_ERROR"
            );
        }
    }

    @GetMapping("/education/lesson/{lessonId}")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> getLessonFiles(@PathVariable Long lessonId) {
        try {
            List<FileUploadResponse> files = fileStorageService.getLessonFiles(lessonId);
            return ResponseUtil.clientSuccess(files, "Lesson Files");
        } catch (Exception e) {
            return ResponseUtil.clientError(
                "Lesson Files",
                "Failed to retrieve lesson files: " + e.getMessage(),
                "FILE_RETRIEVAL_ERROR",
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/education/type/{fileType}")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> getFilesByType(@PathVariable String fileType) {
        try {
            List<FileUploadResponse> files = fileStorageService.getFilesByType(fileType);
            return ResponseUtil.clientSuccess(files, "Files by Type");
        } catch (Exception e) {
            return ResponseUtil.clientError(
                "Files by Type",
                "Failed to retrieve files: " + e.getMessage(),
                "FILE_RETRIEVAL_ERROR",
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}

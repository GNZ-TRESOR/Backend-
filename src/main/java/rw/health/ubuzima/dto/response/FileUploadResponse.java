package rw.health.ubuzima.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String id;
    private String url;
    private String filename;
    private String originalFilename;
    private Long size;
    private String mimeType;
    private String fileType;
    private String userId;
    private LocalDateTime uploadedAt;
    private String downloadUrl;

    // Education-specific fields
    private Long lessonId;
    private String description;
    private String metadata;
    private String thumbnailUrl;
    private Integer duration;
    private String resolution;
    private String category;
}

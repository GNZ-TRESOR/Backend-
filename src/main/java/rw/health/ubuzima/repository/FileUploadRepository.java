package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.FileUpload;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, String> {

    // Find by uploaded user
    List<FileUpload> findByUserIdOrderByUploadedAtDesc(String userId);

    // Find by category
    List<FileUpload> findByCategoryOrderByUploadedAtDesc(String category);

    // Find by content type
    List<FileUpload> findByMimeTypeOrderByUploadedAtDesc(String mimeType);

    // Find by filename
    Optional<FileUpload> findByFilename(String filename);

    // Find all files ordered by upload date
    List<FileUpload> findAllByOrderByUploadedAtDesc();

    // Get total file size
    @Query("SELECT COALESCE(SUM(f.size), 0) FROM FileUpload f")
    Long sumFileSize();

    // Education-specific queries
    List<FileUpload> findByUserIdAndCategoryOrderByUploadedAtDesc(String userId, String category);

    // Additional methods needed by FileStorageService
    List<FileUpload> findByLessonIdOrderByUploadedAtDesc(Long lessonId);
    List<FileUpload> findByFileTypeOrderByUploadedAtDesc(String fileType);
}

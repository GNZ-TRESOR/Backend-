package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.health.ubuzima.entity.EducationLesson;
import rw.health.ubuzima.entity.EducationProgress;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.EducationCategory;
import rw.health.ubuzima.enums.EducationLevel;
import rw.health.ubuzima.repository.EducationLessonRepository;
import rw.health.ubuzima.repository.EducationProgressRepository;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.service.FileStorageService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/education")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EducationController {

    private final EducationLessonRepository educationLessonRepository;
    private final EducationProgressRepository educationProgressRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @GetMapping("/lessons")
    public ResponseEntity<Map<String, Object>> getEducationLessons(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String language) {
        
        try {
            List<EducationLesson> lessons;

            if (category != null && level != null) {
                EducationCategory cat = EducationCategory.valueOf(category.toUpperCase());
                EducationLevel lvl = EducationLevel.valueOf(level.toUpperCase());
                lessons = educationLessonRepository.findByCategoryAndLevelAndIsPublishedTrueOrderByOrderIndexAsc(cat, lvl);
            } else if (category != null) {
                EducationCategory cat = EducationCategory.valueOf(category.toUpperCase());
                lessons = educationLessonRepository.findByCategoryAndIsPublishedTrueOrderByOrderIndexAsc(cat);
            } else if (level != null) {
                EducationLevel lvl = EducationLevel.valueOf(level.toUpperCase());
                lessons = educationLessonRepository.findByLevelAndIsPublishedTrueOrderByOrderIndexAsc(lvl);
            } else if (language != null) {
                lessons = educationLessonRepository.findByLanguageAndIsPublishedTrueOrderByOrderIndexAsc(language);
            } else {
                lessons = educationLessonRepository.findByIsPublishedTrueOrderByOrderIndexAsc();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "lessons", lessons
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch education lessons: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/lessons/{id}")
    public ResponseEntity<Map<String, Object>> getEducationLesson(@PathVariable Long id) {
        try {
            EducationLesson lesson = educationLessonRepository.findById(id).orElse(null);
            
            if (lesson == null) {
                return ResponseEntity.notFound().build();
            }

            // Increment view count
            lesson.setViewCount(lesson.getViewCount() + 1);
            educationLessonRepository.save(lesson);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "lesson", lesson
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch education lesson: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/progress/{userId}")
    public ResponseEntity<Map<String, Object>> getEducationProgress(@PathVariable Long userId) {
        try {
            System.out.println("Getting education progress for user ID: " + userId);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                System.out.println("User not found with ID: " + userId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }

            System.out.println("Found user: " + user.getName());

            // Get all progress records for the user
            System.out.println("Fetching progress list...");
            List<EducationProgress> progressList = educationProgressRepository.findByUserOrderByLastAccessedAtDesc(user);
            System.out.println("Found " + progressList.size() + " progress records");

            // Get completed lessons
            System.out.println("Fetching completed lessons...");
            List<EducationProgress> completedLessons = educationProgressRepository.findByUserAndIsCompletedTrue(user);
            System.out.println("Found " + completedLessons.size() + " completed lessons");

            // Get in-progress lessons
            System.out.println("Fetching in-progress lessons...");
            List<EducationProgress> inProgressLessons = educationProgressRepository.findInProgressLessons(user);
            System.out.println("Found " + inProgressLessons.size() + " in-progress lessons");

            // Calculate statistics
            System.out.println("Calculating statistics...");
            Double averageProgress = educationProgressRepository.calculateAverageProgress(user);
            Long completedCount = educationProgressRepository.countCompletedLessons(user);
            Long totalTimeSpent = educationProgressRepository.calculateTotalTimeSpent(user);

            System.out.println("Statistics calculated successfully");

            // Create simplified response to avoid circular references
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("progress", progressList.stream().map(this::simplifyProgressForResponse).collect(Collectors.toList()));
            response.put("completedLessons", completedLessons.stream().map(this::simplifyProgressForResponse).collect(Collectors.toList()));
            response.put("inProgressLessons", inProgressLessons.stream().map(this::simplifyProgressForResponse).collect(Collectors.toList()));
            response.put("statistics", Map.of(
                "averageProgress", averageProgress != null ? averageProgress : 0.0,
                "completedLessons", completedCount != null ? completedCount : 0,
                "totalTimeSpent", totalTimeSpent != null ? totalTimeSpent : 0,
                "inProgressCount", inProgressLessons.size()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting education progress: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch education progress: " + e.getMessage()
            ));
        }
    }

    /// Get progress for a specific lesson
    @GetMapping("/progress/{userId}/{lessonId}")
    public ResponseEntity<Map<String, Object>> getLessonProgress(@PathVariable Long userId, @PathVariable Long lessonId) {
        try {
            System.out.println("Getting lesson progress for user ID: " + userId + ", lesson ID: " + lessonId);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                System.out.println("User not found with ID: " + userId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }

            EducationLesson lesson = educationLessonRepository.findById(lessonId).orElse(null);
            if (lesson == null) {
                System.out.println("Lesson not found with ID: " + lessonId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lesson not found with ID: " + lessonId
                ));
            }

            System.out.println("Found user: " + user.getName() + ", lesson: " + lesson.getTitle());

            // Get progress for this specific lesson
            Optional<EducationProgress> progressOpt = educationProgressRepository.findByUserAndLesson(user, lesson);

            if (progressOpt.isPresent()) {
                EducationProgress progress = progressOpt.get();
                System.out.println("Found existing progress: " + progress.getProgressPercentage() + "%");

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "progress", simplifyProgressForResponse(progress)
                ));
            } else {
                System.out.println("No progress found for this lesson, creating new progress record");

                // Create a new progress record with 0% progress
                EducationProgress newProgress = new EducationProgress();
                newProgress.setUser(user);
                newProgress.setLesson(lesson);
                newProgress.setProgressPercentage(0.0);
                newProgress.setIsCompleted(false);
                newProgress.setTimeSpentMinutes(0);
                newProgress.setQuizAttempts(0);
                newProgress.setLastAccessedAt(LocalDateTime.now());

                EducationProgress savedProgress = educationProgressRepository.save(newProgress);
                System.out.println("Created new progress record with ID: " + savedProgress.getId());

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "progress", simplifyProgressForResponse(savedProgress)
                ));
            }

        } catch (Exception e) {
            System.err.println("Error getting lesson progress: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get lesson progress: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/progress")
    public ResponseEntity<Map<String, Object>> updateEducationProgress(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long lessonId = Long.valueOf(request.get("lessonId").toString());
            
            User user = userRepository.findById(userId).orElse(null);
            EducationLesson lesson = educationLessonRepository.findById(lessonId).orElse(null);
            
            if (user == null || lesson == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User or lesson not found"
                ));
            }

            EducationProgress progress = educationProgressRepository
                .findByUserAndLesson(user, lesson)
                .orElse(new EducationProgress());

            progress.setUser(user);
            progress.setLesson(lesson);
            
            if (request.get("progressPercentage") != null) {
                progress.setProgressPercentage(Double.valueOf(request.get("progressPercentage").toString()));
            }
            
            if (request.get("timeSpentMinutes") != null) {
                progress.setTimeSpentMinutes(Integer.valueOf(request.get("timeSpentMinutes").toString()));
            }
            
            if (request.get("isCompleted") != null) {
                Boolean isCompleted = Boolean.valueOf(request.get("isCompleted").toString());
                progress.setIsCompleted(isCompleted);
                if (isCompleted) {
                    progress.setCompletedAt(LocalDateTime.now());
                    progress.setProgressPercentage(100.0);
                }
            }
            
            if (request.get("quizScore") != null) {
                progress.setQuizScore(Double.valueOf(request.get("quizScore").toString()));
            }
            
            if (request.get("notes") != null) {
                progress.setNotes(request.get("notes").toString());
            }

            progress.setLastAccessedAt(LocalDateTime.now());

            EducationProgress savedProgress = educationProgressRepository.save(progress);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Education progress updated successfully",
                "progress", savedProgress
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update education progress: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularLessons() {
        try {
            System.out.println("Getting popular lessons...");
            long totalLessons = educationLessonRepository.count();
            System.out.println("Total lessons in database: " + totalLessons);

            List<EducationLesson> popularLessons = educationLessonRepository.findMostPopularLessons();
            System.out.println("Found " + popularLessons.size() + " popular lessons");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "popularLessons", popularLessons
            ));

        } catch (Exception e) {
            System.err.println("Error getting popular lessons: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch popular lessons: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchLessons(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String language) {
        try {
            List<EducationLesson> searchResults;

            if (category != null || level != null || language != null) {
                // Advanced search with filters
                searchResults = educationLessonRepository.searchLessonsWithFilters(
                    query,
                    category != null ? EducationCategory.valueOf(category.toUpperCase()) : null,
                    level != null ? EducationLevel.valueOf(level.toUpperCase()) : null,
                    language
                );
            } else {
                // Simple search
                searchResults = educationLessonRepository.searchLessons(query);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "searchResults", searchResults,
                "total", searchResults.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to search lessons: " + e.getMessage()
            ));
        }
    }

    /**
     * Client: Get recommended lessons for user
     */
    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<Map<String, Object>> getRecommendedLessons(@PathVariable Long userId) {
        try {
            System.out.println("Getting recommendations for user ID: " + userId);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                System.out.println("User not found with ID: " + userId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }

            System.out.println("Found user: " + user.getName());

            // Get completed lesson IDs
            List<EducationProgress> completedProgress = educationProgressRepository.findByUserAndIsCompletedTrue(user);
            System.out.println("Found " + completedProgress.size() + " completed lessons");

            List<Long> completedLessonIds = completedProgress.stream()
                .map(progress -> progress.getLesson().getId())
                .collect(Collectors.toList());

            System.out.println("Completed lesson IDs: " + completedLessonIds);

            // Get recommended lessons (not completed)
            List<EducationLesson> recommendedLessons;
            if (completedLessonIds.isEmpty()) {
                System.out.println("No completed lessons, getting popular lessons");
                recommendedLessons = educationLessonRepository.findMostPopularLessons().stream().limit(10).collect(Collectors.toList());
            } else {
                System.out.println("Getting recommended lessons excluding completed ones");
                recommendedLessons = educationLessonRepository.findRecommendedLessons(completedLessonIds).stream().limit(10).collect(Collectors.toList());
            }

            System.out.println("Found " + recommendedLessons.size() + " recommended lessons");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "recommendations", recommendedLessons
            ));

        } catch (Exception e) {
            System.err.println("Error getting recommendations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch recommendations: " + e.getMessage()
            ));
        }
    }

    /**
     * Client: Get lessons by category with progress info
     */
    @GetMapping("/categories/{category}/lessons")
    public ResponseEntity<Map<String, Object>> getLessonsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Long userId) {
        try {
            EducationCategory cat = EducationCategory.valueOf(category.toUpperCase());
            List<EducationLesson> lessons = educationLessonRepository.findByCategoryAndIsPublishedTrueOrderByOrderIndexAsc(cat);

            // If userId provided, include progress information
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    Map<Long, EducationProgress> progressMap = educationProgressRepository.findByUserOrderByLastAccessedAtDesc(user)
                        .stream()
                        .collect(Collectors.toMap(
                            progress -> progress.getLesson().getId(),
                            progress -> progress,
                            (existing, replacement) -> existing
                        ));

                    // Add progress info to response
                    List<Map<String, Object>> lessonsWithProgress = lessons.stream()
                        .map(lesson -> {
                            Map<String, Object> lessonData = new HashMap<>();
                            lessonData.put("lesson", lesson);
                            lessonData.put("progress", progressMap.get(lesson.getId()));
                            return lessonData;
                        })
                        .collect(Collectors.toList());

                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "category", category,
                        "lessonsWithProgress", lessonsWithProgress,
                        "total", lessons.size()
                    ));
                }
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "category", category,
                "lessons", lessons,
                "total", lessons.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch lessons by category: " + e.getMessage()
            ));
        }
    }

    /**
     * Client: Save or update lesson notes
     */
    @PutMapping("/lessons/{lessonId}/notes")
    public ResponseEntity<Map<String, Object>> saveLessonNotes(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String notes = request.get("notes").toString();

            User user = userRepository.findById(userId).orElse(null);
            EducationLesson lesson = educationLessonRepository.findById(lessonId).orElse(null);

            if (user == null || lesson == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User or lesson not found"
                ));
            }

            EducationProgress progress = educationProgressRepository
                .findByUserAndLesson(user, lesson)
                .orElse(new EducationProgress());

            progress.setUser(user);
            progress.setLesson(lesson);
            progress.setNotes(notes);
            progress.setLastAccessedAt(LocalDateTime.now());

            EducationProgress savedProgress = educationProgressRepository.save(progress);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notes saved successfully",
                "progress", savedProgress
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to save notes: " + e.getMessage()
            ));
        }
    }

    /**
     * Client: Mark lesson as completed
     */
    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<Map<String, Object>> markLessonComplete(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer timeSpentMinutes = request.get("timeSpentMinutes") != null ?
                Integer.valueOf(request.get("timeSpentMinutes").toString()) : 0;
            Double quizScore = request.get("quizScore") != null ?
                Double.valueOf(request.get("quizScore").toString()) : null;

            User user = userRepository.findById(userId).orElse(null);
            EducationLesson lesson = educationLessonRepository.findById(lessonId).orElse(null);

            if (user == null || lesson == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User or lesson not found"
                ));
            }

            EducationProgress progress = educationProgressRepository
                .findByUserAndLesson(user, lesson)
                .orElse(new EducationProgress());

            progress.setUser(user);
            progress.setLesson(lesson);
            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            progress.setProgressPercentage(100.0);
            progress.setTimeSpentMinutes(timeSpentMinutes);
            progress.setLastAccessedAt(LocalDateTime.now());

            if (quizScore != null) {
                progress.setQuizScore(quizScore);
                progress.setQuizAttempts(progress.getQuizAttempts() + 1);
            }

            EducationProgress savedProgress = educationProgressRepository.save(progress);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lesson marked as completed",
                "progress", savedProgress
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to mark lesson as completed: " + e.getMessage()
            ));
        }
    }

    /**
     * Client: Get user's learning dashboard
     */
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<Map<String, Object>> getUserLearningDashboard(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Get recent progress
            List<EducationProgress> recentProgress = educationProgressRepository
                .findByUserOrderByLastAccessedAtDesc(user)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

            // Get completed lessons
            List<EducationProgress> completedLessons = educationProgressRepository.findByUserAndIsCompletedTrue(user);

            // Get in-progress lessons
            List<EducationProgress> inProgressLessons = educationProgressRepository.findInProgressLessons(user);

            // Calculate statistics
            Double averageProgress = educationProgressRepository.calculateAverageProgress(user);
            Long completedCount = educationProgressRepository.countCompletedLessons(user);
            Long totalTimeSpent = educationProgressRepository.calculateTotalTimeSpent(user);

            // Get recommended lessons
            List<Long> completedLessonIds = completedLessons.stream()
                .map(progress -> progress.getLesson().getId())
                .collect(Collectors.toList());

            List<EducationLesson> recommendedLessons = completedLessonIds.isEmpty() ?
                educationLessonRepository.findMostPopularLessons().stream().limit(5).collect(Collectors.toList()) :
                educationLessonRepository.findRecommendedLessons(completedLessonIds).stream().limit(5).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "dashboard", Map.of(
                    "recentProgress", recentProgress,
                    "completedLessons", completedLessons,
                    "inProgressLessons", inProgressLessons,
                    "recommendedLessons", recommendedLessons,
                    "statistics", Map.of(
                        "averageProgress", averageProgress != null ? averageProgress : 0.0,
                        "completedCount", completedCount != null ? completedCount : 0,
                        "totalTimeSpent", totalTimeSpent != null ? totalTimeSpent : 0,
                        "inProgressCount", inProgressLessons.size()
                    )
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch learning dashboard: " + e.getMessage()
            ));
        }
    }

    // ==================== ADMIN EDUCATION MANAGEMENT ENDPOINTS ====================

    /**
     * Admin: Create new education lesson
     */
    @PostMapping("/admin/lessons")
    public ResponseEntity<Map<String, Object>> createLesson(@RequestBody Map<String, Object> request) {
        try {
            EducationLesson lesson = new EducationLesson();
            lesson.setTitle(request.get("title").toString());
            lesson.setDescription(request.get("description") != null ? request.get("description").toString() : null);
            lesson.setContent(request.get("content") != null ? request.get("content").toString() : null);
            lesson.setCategory(EducationCategory.valueOf(request.get("category").toString().toUpperCase()));
            lesson.setLevel(EducationLevel.valueOf(request.get("level").toString().toUpperCase()));
            lesson.setAuthor(request.get("author") != null ? request.get("author").toString() : null);
            lesson.setLanguage(request.get("language") != null ? request.get("language").toString() : "rw");
            lesson.setDurationMinutes(request.get("durationMinutes") != null ?
                Integer.valueOf(request.get("durationMinutes").toString()) : null);
            lesson.setVideoUrl(request.get("videoUrl") != null ? request.get("videoUrl").toString() : null);
            lesson.setAudioUrl(request.get("audioUrl") != null ? request.get("audioUrl").toString() : null);
            lesson.setOrderIndex(request.get("orderIndex") != null ?
                Integer.valueOf(request.get("orderIndex").toString()) : 0);
            lesson.setIsPublished(request.get("isPublished") != null ?
                Boolean.valueOf(request.get("isPublished").toString()) : false);

            // Handle tags
            if (request.get("tags") != null) {
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) request.get("tags");
                lesson.setTags(tags);
            }

            // Handle image URLs
            if (request.get("imageUrls") != null) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) request.get("imageUrls");
                lesson.setImageUrls(imageUrls);
            }

            EducationLesson savedLesson = educationLessonRepository.save(lesson);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lesson created successfully",
                "lesson", savedLesson
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to create lesson: " + e.getMessage()
            ));
        }
    }

    /**
     * Admin: Update existing education lesson
     */
    @PutMapping("/admin/lessons/{id}")
    public ResponseEntity<Map<String, Object>> updateLesson(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            EducationLesson lesson = educationLessonRepository.findById(id).orElse(null);
            if (lesson == null) {
                return ResponseEntity.notFound().build();
            }

            // Update fields if provided
            if (request.get("title") != null) {
                lesson.setTitle(request.get("title").toString());
            }
            if (request.get("description") != null) {
                lesson.setDescription(request.get("description").toString());
            }
            if (request.get("content") != null) {
                lesson.setContent(request.get("content").toString());
            }
            if (request.get("category") != null) {
                lesson.setCategory(EducationCategory.valueOf(request.get("category").toString().toUpperCase()));
            }
            if (request.get("level") != null) {
                lesson.setLevel(EducationLevel.valueOf(request.get("level").toString().toUpperCase()));
            }
            if (request.get("author") != null) {
                lesson.setAuthor(request.get("author").toString());
            }
            if (request.get("language") != null) {
                lesson.setLanguage(request.get("language").toString());
            }
            if (request.get("durationMinutes") != null) {
                lesson.setDurationMinutes(Integer.valueOf(request.get("durationMinutes").toString()));
            }
            if (request.get("videoUrl") != null) {
                lesson.setVideoUrl(request.get("videoUrl").toString());
            }
            if (request.get("audioUrl") != null) {
                lesson.setAudioUrl(request.get("audioUrl").toString());
            }
            if (request.get("orderIndex") != null) {
                lesson.setOrderIndex(Integer.valueOf(request.get("orderIndex").toString()));
            }
            if (request.get("isPublished") != null) {
                lesson.setIsPublished(Boolean.valueOf(request.get("isPublished").toString()));
            }

            // Update tags if provided
            if (request.get("tags") != null) {
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) request.get("tags");
                lesson.setTags(tags);
            }

            // Update image URLs if provided
            if (request.get("imageUrls") != null) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) request.get("imageUrls");
                lesson.setImageUrls(imageUrls);
            }

            EducationLesson savedLesson = educationLessonRepository.save(lesson);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lesson updated successfully",
                "lesson", savedLesson
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update lesson: " + e.getMessage()
            ));
        }
    }

    /**
     * Admin: Delete education lesson
     */
    @DeleteMapping("/admin/lessons/{id}")
    public ResponseEntity<Map<String, Object>> deleteLesson(@PathVariable Long id) {
        try {
            EducationLesson lesson = educationLessonRepository.findById(id).orElse(null);
            if (lesson == null) {
                return ResponseEntity.notFound().build();
            }

            // Delete associated progress records first
            List<EducationProgress> progressRecords = educationProgressRepository.findByLesson(lesson);
            educationProgressRepository.deleteAll(progressRecords);

            // Delete the lesson
            educationLessonRepository.delete(lesson);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lesson deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete lesson: " + e.getMessage()
            ));
        }
    }

    /**
     * Admin: Get all lessons (including unpublished)
     */
    @GetMapping("/admin/lessons")
    public ResponseEntity<Map<String, Object>> getAllLessonsForAdmin(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean isPublished) {
        try {
            List<EducationLesson> lessons;

            if (isPublished != null) {
                if (category != null) {
                    EducationCategory cat = EducationCategory.valueOf(category.toUpperCase());
                    lessons = isPublished ?
                        educationLessonRepository.findByCategoryAndIsPublishedTrueOrderByOrderIndexAsc(cat) :
                        educationLessonRepository.findByCategoryAndIsPublishedFalseOrderByOrderIndexAsc(cat);
                } else {
                    lessons = isPublished ?
                        educationLessonRepository.findByIsPublishedTrueOrderByOrderIndexAsc() :
                        educationLessonRepository.findByIsPublishedFalseOrderByOrderIndexAsc();
                }
            } else {
                // Get all lessons regardless of published status
                if (category != null && level != null) {
                    EducationCategory cat = EducationCategory.valueOf(category.toUpperCase());
                    EducationLevel lvl = EducationLevel.valueOf(level.toUpperCase());
                    lessons = educationLessonRepository.findByCategoryAndLevelOrderByOrderIndexAsc(cat, lvl);
                } else if (category != null) {
                    EducationCategory cat = EducationCategory.valueOf(category.toUpperCase());
                    lessons = educationLessonRepository.findByCategoryOrderByOrderIndexAsc(cat);
                } else if (level != null) {
                    EducationLevel lvl = EducationLevel.valueOf(level.toUpperCase());
                    lessons = educationLessonRepository.findByLevelOrderByOrderIndexAsc(lvl);
                } else if (language != null) {
                    lessons = educationLessonRepository.findByLanguageOrderByOrderIndexAsc(language);
                } else {
                    lessons = educationLessonRepository.findAllByOrderByOrderIndexAsc();
                }
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "lessons", lessons,
                "total", lessons.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch lessons: " + e.getMessage()
            ));
        }
    }

    /**
     * Admin: Toggle lesson publish status
     */
    @PutMapping("/admin/lessons/{id}/toggle-publish")
    public ResponseEntity<Map<String, Object>> toggleLessonPublishStatus(@PathVariable Long id) {
        try {
            EducationLesson lesson = educationLessonRepository.findById(id).orElse(null);
            if (lesson == null) {
                return ResponseEntity.notFound().build();
            }

            lesson.setIsPublished(!lesson.getIsPublished());
            EducationLesson savedLesson = educationLessonRepository.save(lesson);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", lesson.getIsPublished() ? "Lesson published successfully" : "Lesson unpublished successfully",
                "lesson", savedLesson
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to toggle lesson publish status: " + e.getMessage()
            ));
        }
    }

    /**
     * Admin: Upload media file for lesson
     */
    @PostMapping("/admin/lessons/{id}/upload-media")
    public ResponseEntity<Map<String, Object>> uploadLessonMedia(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("mediaType") String mediaType,
            @RequestParam("userId") String userId) {
        try {
            EducationLesson lesson = educationLessonRepository.findById(id).orElse(null);
            if (lesson == null) {
                return ResponseEntity.notFound().build();
            }

            // Upload file using FileStorageService
            var uploadResponse = fileStorageService.storeFile(file, userId, "education_" + mediaType);

            // Update lesson with media URL
            String mediaUrl = uploadResponse.getUrl();
            if ("video".equalsIgnoreCase(mediaType)) {
                lesson.setVideoUrl(mediaUrl);
            } else if ("audio".equalsIgnoreCase(mediaType)) {
                lesson.setAudioUrl(mediaUrl);
            } else if ("image".equalsIgnoreCase(mediaType)) {
                List<String> imageUrls = new ArrayList<>(lesson.getImageUrls());
                imageUrls.add(mediaUrl);
                lesson.setImageUrls(imageUrls);
            }

            EducationLesson savedLesson = educationLessonRepository.save(lesson);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Media uploaded successfully",
                "mediaUrl", mediaUrl,
                "lesson", savedLesson
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to upload media: " + e.getMessage()
            ));
        }
    }

    /**
     * Admin: Get education analytics
     */
    @GetMapping("/admin/analytics")
    public ResponseEntity<Map<String, Object>> getEducationAnalytics() {
        try {
            // Total lessons
            Long totalLessons = educationLessonRepository.count();
            Long publishedLessons = educationLessonRepository.countByIsPublishedTrue();
            Long unpublishedLessons = totalLessons - publishedLessons;

            // Category breakdown
            Map<String, Long> categoryBreakdown = new HashMap<>();
            for (EducationCategory category : EducationCategory.values()) {
                Long count = educationLessonRepository.countByCategoryAndIsPublishedTrue(category);
                categoryBreakdown.put(category.name(), count);
            }

            // Level breakdown
            Map<String, Long> levelBreakdown = new HashMap<>();
            for (EducationLevel level : EducationLevel.values()) {
                Long count = educationLessonRepository.countByLevelAndIsPublishedTrue(level);
                levelBreakdown.put(level.name(), count);
            }

            // Most popular lessons
            List<EducationLesson> popularLessons = educationLessonRepository.findMostPopularLessons()
                .stream().limit(10).collect(Collectors.toList());

            // Total progress records
            Long totalProgressRecords = educationProgressRepository.count();
            Long completedLessonsCount = educationProgressRepository.countByIsCompletedTrue();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "analytics", Map.of(
                    "totalLessons", totalLessons,
                    "publishedLessons", publishedLessons,
                    "unpublishedLessons", unpublishedLessons,
                    "categoryBreakdown", categoryBreakdown,
                    "levelBreakdown", levelBreakdown,
                    "popularLessons", popularLessons,
                    "totalProgressRecords", totalProgressRecords,
                    "completedLessonsCount", completedLessonsCount,
                    "completionRate", totalProgressRecords > 0 ?
                        (double) completedLessonsCount / totalProgressRecords * 100 : 0.0
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch analytics: " + e.getMessage()
            ));
        }
    }

    /**
     * Admin: Bulk update lesson order
     */
    @PutMapping("/admin/lessons/reorder")
    public ResponseEntity<Map<String, Object>> reorderLessons(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lessonOrders = (List<Map<String, Object>>) request.get("lessons");

            for (Map<String, Object> lessonOrder : lessonOrders) {
                Long lessonId = Long.valueOf(lessonOrder.get("id").toString());
                Integer orderIndex = Integer.valueOf(lessonOrder.get("orderIndex").toString());

                EducationLesson lesson = educationLessonRepository.findById(lessonId).orElse(null);
                if (lesson != null) {
                    lesson.setOrderIndex(orderIndex);
                    educationLessonRepository.save(lesson);
                }
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lesson order updated successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to reorder lessons: " + e.getMessage()
            ));
        }
    }

    // Helper method to create simplified progress response without circular references
    private Map<String, Object> simplifyProgressForResponse(EducationProgress progress) {
        Map<String, Object> simplified = new HashMap<>();
        simplified.put("id", progress.getId());
        simplified.put("progressPercentage", progress.getProgressPercentage());
        simplified.put("isCompleted", progress.getIsCompleted());
        simplified.put("completedAt", progress.getCompletedAt());
        simplified.put("timeSpentMinutes", progress.getTimeSpentMinutes());
        simplified.put("quizScore", progress.getQuizScore());
        simplified.put("quizAttempts", progress.getQuizAttempts());
        simplified.put("lastAccessedAt", progress.getLastAccessedAt());
        simplified.put("notes", progress.getNotes());
        simplified.put("createdAt", progress.getCreatedAt());
        simplified.put("updatedAt", progress.getUpdatedAt());

        // Add simplified lesson data
        if (progress.getLesson() != null) {
            Map<String, Object> lessonData = new HashMap<>();
            lessonData.put("id", progress.getLesson().getId());
            lessonData.put("title", progress.getLesson().getTitle());
            lessonData.put("description", progress.getLesson().getDescription());
            lessonData.put("content", progress.getLesson().getContent());
            lessonData.put("category", progress.getLesson().getCategory());
            lessonData.put("level", progress.getLesson().getLevel());
            lessonData.put("durationMinutes", progress.getLesson().getDurationMinutes());
            lessonData.put("tags", progress.getLesson().getTags());
            lessonData.put("videoUrl", progress.getLesson().getVideoUrl());
            lessonData.put("audioUrl", progress.getLesson().getAudioUrl());
            lessonData.put("imageUrls", progress.getLesson().getImageUrls());
            lessonData.put("isPublished", progress.getLesson().getIsPublished());
            lessonData.put("viewCount", progress.getLesson().getViewCount());
            lessonData.put("language", progress.getLesson().getLanguage());
            lessonData.put("author", progress.getLesson().getAuthor());
            lessonData.put("orderIndex", progress.getLesson().getOrderIndex());
            lessonData.put("createdAt", progress.getLesson().getCreatedAt());
            lessonData.put("updatedAt", progress.getLesson().getUpdatedAt());
            simplified.put("lesson", lessonData);
        }

        // Add simplified user data (just basic info)
        if (progress.getUser() != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", progress.getUser().getId());
            userData.put("name", progress.getUser().getName());
            userData.put("email", progress.getUser().getEmail());
            simplified.put("user", userData);
        }

        return simplified;
    }
}

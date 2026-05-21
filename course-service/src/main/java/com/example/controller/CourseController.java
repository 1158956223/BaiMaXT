package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.dto.CourseRequest;
import com.example.domain.vo.CourseCoverUploadResponse;
import com.example.domain.vo.CourseDetailResponse;
import com.example.domain.vo.CourseListResponse;
import com.example.exception.BusinessException;
import com.example.service.CourseService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final String TEACHER_ROLE = "TEACHER";
    private static final long MAX_COVER_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_COVER_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final CourseService courseService;
    private final Path courseCoverDir;

    public CourseController(CourseService courseService,
                            @Value("${baimaxt.upload.course-cover-dir:uploads/course-covers}") String courseCoverDir) {
        this.courseService = courseService;
        this.courseCoverDir = Paths.get(courseCoverDir).toAbsolutePath().normalize();
    }

    @GetMapping
    public ApiResponse<List<CourseListResponse>> listOnSale(@RequestParam(required = false) Long categoryId,
                                                            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(courseService.listOnSale(categoryId, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseDetailResponse> getPublicDetail(@PathVariable Long id) {
        return ApiResponse.success(courseService.getPublicDetail(id));
    }

    @GetMapping("/covers/{filename:.+}")
    public ResponseEntity<Resource> getCover(@PathVariable String filename) {
        try {
            Path file = courseCoverDir.resolve(filename).normalize();
            if (!file.startsWith(courseCoverDir) || !Files.exists(file) || !Files.isRegularFile(file)) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Course cover does not exist");
            }
            Resource resource = new UrlResource(file.toUri());
            return ResponseEntity.ok()
                    .contentType(resolveMediaType(filename))
                    .body(resource);
        } catch (MalformedURLException exception) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Course cover does not exist");
        }
    }

    @GetMapping("/admin")
    public ApiResponse<List<CourseListResponse>> listAll(@RequestParam(required = false) Long categoryId,
                                                         @RequestParam(required = false) String keyword) {
        return ApiResponse.success(courseService.listAll(categoryId, keyword));
    }

    @GetMapping("/admin/{id}")
    public ApiResponse<CourseDetailResponse> getAdminDetail(@PathVariable Long id) {
        return ApiResponse.success(courseService.getAdminDetail(id));
    }

    @GetMapping("/teacher")
    public ApiResponse<List<CourseListResponse>> listTeacherCourses(@RequestHeader("X-User-Id") Long userId,
                                                                    @RequestHeader("X-User-Role") String role,
                                                                    @RequestParam(required = false) Long categoryId,
                                                                    @RequestParam(required = false) String keyword) {
        requireTeacher(role);
        return ApiResponse.success(courseService.listTeacherCourses(userId, categoryId, keyword));
    }

    @GetMapping("/teacher/{id}")
    public ApiResponse<CourseDetailResponse> getTeacherDetail(@PathVariable Long id,
                                                              @RequestHeader("X-User-Id") Long userId,
                                                              @RequestHeader("X-User-Role") String role) {
        requireTeacher(role);
        return ApiResponse.success(courseService.getTeacherDetail(id, userId));
    }

    @PostMapping("/teacher")
    public ApiResponse<CourseDetailResponse> createTeacherCourse(@RequestBody CourseRequest request,
                                                                 @RequestHeader("X-User-Id") Long userId,
                                                                 @RequestHeader("X-User-Role") String role) {
        requireTeacher(role);
        return ApiResponse.success(courseService.createTeacherCourse(request, userId));
    }

    @PostMapping("/teacher/covers")
    public ApiResponse<CourseCoverUploadResponse> uploadTeacherCover(@RequestParam("file") MultipartFile file,
                                                                     @RequestHeader("X-User-Role") String role) {
        requireTeacher(role);
        validateCover(file);
        try {
            Files.createDirectories(courseCoverDir);
            String extension = coverExtension(file.getContentType());
            String filename = UUID.randomUUID() + extension;
            Path target = courseCoverDir.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return ApiResponse.success(new CourseCoverUploadResponse("/api/courses/covers/" + filename));
        } catch (IOException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload course cover");
        }
    }

    @PutMapping("/teacher/{id}")
    public ApiResponse<CourseDetailResponse> updateTeacherCourse(@PathVariable Long id,
                                                                 @RequestBody CourseRequest request,
                                                                 @RequestHeader("X-User-Id") Long userId,
                                                                 @RequestHeader("X-User-Role") String role) {
        requireTeacher(role);
        return ApiResponse.success(courseService.updateTeacherCourse(id, request, userId));
    }

    @DeleteMapping("/teacher/{id}")
    public ApiResponse<CourseDetailResponse> deleteTeacherCourse(@PathVariable Long id,
                                                                 @RequestHeader("X-User-Id") Long userId,
                                                                 @RequestHeader("X-User-Role") String role) {
        requireTeacher(role);
        return ApiResponse.success(courseService.deleteTeacherCourse(id, userId));
    }

    @PostMapping("/admin")
    public ApiResponse<CourseDetailResponse> create(@RequestBody CourseRequest request) {
        return ApiResponse.success(courseService.create(request));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<CourseDetailResponse> update(@PathVariable Long id, @RequestBody CourseRequest request) {
        return ApiResponse.success(courseService.update(id, request));
    }

    @PatchMapping("/admin/{id}/on-sale")
    public ApiResponse<CourseDetailResponse> onSale(@PathVariable Long id) {
        return ApiResponse.success(courseService.onSale(id));
    }

    @PatchMapping("/admin/{id}/off-sale")
    public ApiResponse<CourseDetailResponse> offSale(@PathVariable Long id) {
        return ApiResponse.success(courseService.offSale(id));
    }

    private void requireTeacher(String role) {
        if (!TEACHER_ROLE.equals(role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Teacher permission required");
        }
    }

    private void validateCover(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course cover file must not be empty");
        }
        if (file.getSize() > MAX_COVER_SIZE_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course cover file must be 5MB or smaller");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_COVER_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course cover must be JPG, PNG, or WebP");
        }
    }

    private String coverExtension(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private MediaType resolveMediaType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }
}

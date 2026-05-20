package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.vo.CourseSearchResponse;
import com.example.domain.vo.IndexSyncResponse;
import com.example.domain.vo.SearchPageResponse;
import com.example.service.CourseSearchService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class CourseSearchController {

    private final CourseSearchService courseSearchService;

    public CourseSearchController(CourseSearchService courseSearchService) {
        this.courseSearchService = courseSearchService;
    }

    @GetMapping("/courses")
    public ApiResponse<SearchPageResponse<CourseSearchResponse>> searchCourses(@RequestParam(required = false, name = "q") String keyword,
                                                                               @RequestParam(name = "categoryId", required = false) Long categoryId,
                                                                               @RequestParam(name = "courseType", required = false) String courseType,
                                                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        return ApiResponse.success(courseSearchService.search(keyword, categoryId, courseType, page, size));
    }

    @PostMapping("/admin/courses/rebuild")
    public ApiResponse<IndexSyncResponse> rebuildCourseIndex() {
        return ApiResponse.success(courseSearchService.rebuildIndex());
    }

    @PostMapping("/admin/courses/{id}")
    public ApiResponse<IndexSyncResponse> syncCourse(@PathVariable("id") Long id) {
        return ApiResponse.success(courseSearchService.syncCourse(id));
    }

    @DeleteMapping("/admin/courses/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable("id") Long id) {
        courseSearchService.deleteCourse(id);
        return ApiResponse.success(null);
    }
}

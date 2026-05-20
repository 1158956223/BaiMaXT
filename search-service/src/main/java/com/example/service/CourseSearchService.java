package com.example.service;

import com.example.domain.vo.CourseSearchResponse;
import com.example.domain.vo.IndexSyncResponse;
import com.example.domain.vo.SearchPageResponse;

public interface CourseSearchService {

    SearchPageResponse<CourseSearchResponse> search(String keyword, Long categoryId, String courseType, int page, int size);

    IndexSyncResponse rebuildIndex();

    IndexSyncResponse syncCourse(Long courseId);

    void deleteCourse(Long courseId);
}

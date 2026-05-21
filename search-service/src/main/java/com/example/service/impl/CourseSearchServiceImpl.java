package com.example.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.api.ApiResponse;
import com.example.client.CourseClient;
import com.example.domain.document.CourseDocument;
import com.example.domain.vo.CourseSearchResponse;
import com.example.domain.vo.IndexSyncResponse;
import com.example.domain.vo.SearchPageResponse;
import com.example.exception.BusinessException;
import com.example.repository.CourseSearchRepository;
import com.example.service.CourseSearchService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    private static final String ON_SALE = "ON_SALE";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final ElasticsearchOperations elasticsearchOperations;
    private final CourseSearchRepository courseSearchRepository;
    private final CourseClient courseClient;

    public CourseSearchServiceImpl(ElasticsearchOperations elasticsearchOperations,
                                   CourseSearchRepository courseSearchRepository,
                                   CourseClient courseClient) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.courseSearchRepository = courseSearchRepository;
        this.courseClient = courseClient;
    }

    @Override
    public SearchPageResponse<CourseSearchResponse> search(String keyword, Long categoryId, String courseType, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        NativeQuery query = NativeQuery.builder()
                .withQuery(buildSearchQuery(keyword, categoryId, trimToNull(courseType)))
                .withPageable(PageRequest.of(safePage, safeSize))
                .withSort(Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.desc("id")))
                .build();

        SearchHits<CourseDocument> hits = elasticsearchOperations.search(query, CourseDocument.class);
        List<CourseSearchResponse> records = hits.stream()
                .map(this::toSearchResponse)
                .toList();
        long total = hits.getTotalHits();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new SearchPageResponse<>(records, total, safePage, safeSize, totalPages);
    }

    @Override
    public IndexSyncResponse rebuildIndex() {
        ApiResponse<List<CourseClient.CourseListClientResponse>> response = courseClient.listAll(null, null);
        List<CourseClient.CourseListClientResponse> courses = requireData(response, "Failed to load courses");
        courseSearchRepository.deleteAll();
        List<CourseDocument> documents = courses.stream()
                .map(course -> loadCourseDetail(course.id()))
                .map(this::toDocument)
                .toList();
        courseSearchRepository.saveAll(documents);
        return new IndexSyncResponse(documents.size(), "Course search index rebuilt");
    }

    @Override
    public IndexSyncResponse syncCourse(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course id must not be null");
        }
        CourseClient.CourseDetailClientResponse course = loadCourseDetail(courseId);
        courseSearchRepository.save(toDocument(course));
        return new IndexSyncResponse(1, "Course search index synced");
    }

    @Override
    public void deleteCourse(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course id must not be null");
        }
        courseSearchRepository.deleteById(courseId);
    }

    private Query buildSearchQuery(String keyword, Long categoryId, String courseType) {
        String text = trimToNull(keyword);
        return Query.of(q -> q.bool(bool -> {
            bool.filter(filter -> filter.term(term -> term.field("status").value(FieldValue.of(ON_SALE))));
            if (categoryId != null) {
                bool.filter(filter -> filter.term(term -> term.field("categoryId").value(FieldValue.of(categoryId))));
            }
            if (courseType != null) {
                bool.filter(filter -> filter.term(term -> term.field("courseType").value(FieldValue.of(courseType))));
            }
            if (text == null) {
                bool.must(must -> must.matchAll(matchAll -> matchAll));
            } else {
                bool.must(must -> must.multiMatch(multiMatch -> multiMatch
                        .query(text)
                        .fields("title^4", "subtitle^3", "categoryName", "teacherName", "durationDesc", "targetAudience", "intro", "outline")));
            }
            return bool;
        }));
    }

    private CourseDocument toDocument(CourseClient.CourseDetailClientResponse course) {
        Long teacherId = course.teacher() == null ? null : course.teacher().id();
        String teacherName = course.teacher() == null ? null : course.teacher().name();
        return new CourseDocument(
                course.id(),
                course.categoryId(),
                course.categoryName(),
                teacherId,
                teacherName,
                course.title(),
                course.subtitle(),
                course.coverUrl(),
                course.price(),
                course.originalPrice(),
                course.courseType(),
                course.durationDesc(),
                course.targetAudience(),
                course.intro(),
                course.outline(),
                course.status(),
                course.sortOrder()
        );
    }

    private CourseClient.CourseDetailClientResponse loadCourseDetail(Long courseId) {
        ApiResponse<CourseClient.CourseDetailClientResponse> response = courseClient.getAdminDetail(courseId);
        return requireData(response, "Failed to load course");
    }

    private CourseSearchResponse toSearchResponse(SearchHit<CourseDocument> hit) {
        CourseDocument course = hit.getContent();
        return new CourseSearchResponse(
                course.getId(),
                course.getCategoryId(),
                course.getCategoryName(),
                course.getTeacherId(),
                course.getTeacherName(),
                course.getTitle(),
                course.getSubtitle(),
                course.getCoverUrl(),
                course.getPrice(),
                course.getOriginalPrice(),
                course.getCourseType(),
                course.getDurationDesc(),
                course.getStatus(),
                course.getSortOrder(),
                hit.getScore()
        );
    }

    private <T> T requireData(ApiResponse<T> response, String message) {
        if (response == null || response.code() != 200 || response.data() == null) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, message);
        }
        return response.data();
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.vo.CourseDetailResponse;
import com.example.domain.vo.CourseListResponse;
import com.example.domain.dto.CourseRequest;
import com.example.domain.po.Course;
import java.util.List;

public interface CourseService extends IService<Course> {

    List<CourseListResponse> listOnSale(Long categoryId, String keyword);

    List<CourseListResponse> listAll(Long categoryId, String keyword);

    List<CourseListResponse> listTeacherCourses(Long userId, Long categoryId, String keyword);

    CourseDetailResponse getPublicDetail(Long id);

    CourseDetailResponse getAdminDetail(Long id);

    CourseDetailResponse getTeacherDetail(Long id, Long userId);

    CourseDetailResponse create(CourseRequest request);

    CourseDetailResponse createTeacherCourse(CourseRequest request, Long userId);

    CourseDetailResponse update(Long id, CourseRequest request);

    CourseDetailResponse updateTeacherCourse(Long id, CourseRequest request, Long userId);

    CourseDetailResponse onSale(Long id);

    CourseDetailResponse offSale(Long id);

    CourseDetailResponse deleteTeacherCourse(Long id, Long userId);

    void decreaseStock(Long id);

    void restoreStock(Long id);
}

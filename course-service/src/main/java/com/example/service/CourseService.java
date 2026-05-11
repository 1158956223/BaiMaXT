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

    CourseDetailResponse getPublicDetail(Long id);

    CourseDetailResponse getAdminDetail(Long id);

    CourseDetailResponse create(CourseRequest request);

    CourseDetailResponse update(Long id, CourseRequest request);

    CourseDetailResponse onSale(Long id);

    CourseDetailResponse offSale(Long id);
}

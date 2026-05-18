package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.dto.TeacherRequest;
import com.example.domain.vo.TeacherResponse;
import com.example.domain.po.Teacher;
import com.example.dto.teacher.CreateTeacherProfileRequest;
import com.example.dto.teacher.TeacherProfileResponse;
import java.util.List;

public interface TeacherService extends IService<Teacher> {

    List<TeacherResponse> listEnabled();

    List<TeacherResponse> listAll();

    TeacherResponse getDetail(Long id);

    TeacherResponse create(TeacherRequest request);

    TeacherProfileResponse createInternal(CreateTeacherProfileRequest request);

    TeacherResponse update(Long id, TeacherRequest request);

    TeacherResponse enable(Long id);

    TeacherResponse disable(Long id);
}

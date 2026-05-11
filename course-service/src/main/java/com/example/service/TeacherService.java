package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.dto.TeacherRequest;
import com.example.domain.vo.TeacherResponse;
import com.example.domain.po.Teacher;
import java.util.List;

public interface TeacherService extends IService<Teacher> {

    List<TeacherResponse> listEnabled();

    List<TeacherResponse> listAll();

    TeacherResponse getDetail(Long id);

    TeacherResponse create(TeacherRequest request);

    TeacherResponse update(Long id, TeacherRequest request);

    TeacherResponse enable(Long id);

    TeacherResponse disable(Long id);
}

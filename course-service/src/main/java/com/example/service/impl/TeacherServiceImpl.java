package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.dto.TeacherRequest;
import com.example.domain.vo.TeacherResponse;
import com.example.domain.enums.EnabledStatus;
import com.example.domain.po.Teacher;
import com.example.exception.BusinessException;
import com.example.mapper.TeacherMapper;
import com.example.service.TeacherService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private final TeacherMapper teacherMapper;

    public TeacherServiceImpl(TeacherMapper teacherMapper) {
        this.teacherMapper = teacherMapper;
    }

    @Override
    public List<TeacherResponse> listEnabled() {
        return teacherMapper.selectList(baseQuery().eq(Teacher::getStatus, EnabledStatus.ENABLED)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TeacherResponse> listAll() {
        return teacherMapper.selectList(baseQuery()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TeacherResponse getDetail(Long id) {
        return toResponse(getTeacher(id));
    }

    @Override
    public TeacherResponse create(TeacherRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        LocalDateTime now = LocalDateTime.now();
        Teacher teacher = new Teacher();
        teacher.setName(requireText(request.name(), "Teacher name must not be blank"));
        teacher.setAvatarUrl(trimToNull(request.avatarUrl()));
        teacher.setTitle(trimToNull(request.title()));
        teacher.setBio(trimToNull(request.bio()));
        teacher.setSpecialties(trimToNull(request.specialties()));
        teacher.setYearsExperience(defaultYears(request.yearsExperience()));
        teacher.setStatus(EnabledStatus.ENABLED);
        teacher.setCreatedAt(now);
        teacher.setUpdatedAt(now);
        teacherMapper.insert(teacher);
        return toResponse(teacher);
    }

    @Override
    public TeacherResponse update(Long id, TeacherRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        Teacher teacher = getTeacher(id);
        teacher.setName(requireText(request.name(), "Teacher name must not be blank"));
        teacher.setAvatarUrl(trimToNull(request.avatarUrl()));
        teacher.setTitle(trimToNull(request.title()));
        teacher.setBio(trimToNull(request.bio()));
        teacher.setSpecialties(trimToNull(request.specialties()));
        teacher.setYearsExperience(defaultYears(request.yearsExperience()));
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherMapper.updateById(teacher);
        return toResponse(teacher);
    }

    @Override
    public TeacherResponse enable(Long id) {
        Teacher teacher = getTeacher(id);
        teacher.setStatus(EnabledStatus.ENABLED);
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherMapper.updateById(teacher);
        return toResponse(teacher);
    }

    @Override
    public TeacherResponse disable(Long id) {
        Teacher teacher = getTeacher(id);
        teacher.setStatus(EnabledStatus.DISABLED);
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherMapper.updateById(teacher);
        return toResponse(teacher);
    }

    private Teacher getTeacher(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Teacher id must not be null");
        }
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Teacher does not exist");
        }
        return teacher;
    }

    private LambdaQueryWrapper<Teacher> baseQuery() {
        return new LambdaQueryWrapper<Teacher>().orderByDesc(Teacher::getId);
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getName(),
                teacher.getAvatarUrl(),
                teacher.getTitle(),
                teacher.getBio(),
                teacher.getSpecialties(),
                teacher.getYearsExperience(),
                teacher.getStatus(),
                teacher.getCreatedAt(),
                teacher.getUpdatedAt()
        );
    }

    private String requireText(String value, String message) {
        String text = trimToNull(value);
        if (text == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return text;
    }

    private Integer defaultYears(Integer yearsExperience) {
        if (yearsExperience == null) {
            return 0;
        }
        if (yearsExperience < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Years experience must not be negative");
        }
        return yearsExperience;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

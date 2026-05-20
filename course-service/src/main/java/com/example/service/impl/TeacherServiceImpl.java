package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.dto.TeacherRequest;
import com.example.domain.vo.TeacherResponse;
import com.example.domain.enums.EnabledStatus;
import com.example.domain.po.Teacher;
import com.example.dto.teacher.CreateTeacherProfileRequest;
import com.example.dto.teacher.TeacherProfileResponse;
import com.example.exception.BusinessException;
import com.example.mapper.TeacherMapper;
import com.example.mapper.UserMapper;
import com.example.domain.po.User;
import com.example.service.TeacherService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static com.example.config.RedisCacheConfig.COURSE_CATEGORY_ENABLED_CACHE;
import static com.example.config.RedisCacheConfig.COURSE_PUBLIC_DETAIL_CACHE;
import static com.example.config.RedisCacheConfig.COURSE_PUBLIC_LIST_CACHE;
import static com.example.config.RedisCacheConfig.COURSE_TEACHER_ENABLED_CACHE;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private final TeacherMapper teacherMapper;
    private final UserMapper userMapper;

    public TeacherServiceImpl(TeacherMapper teacherMapper, UserMapper userMapper) {
        this.teacherMapper = teacherMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Cacheable(cacheNames = COURSE_TEACHER_ENABLED_CACHE, key = "'all'")
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
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
    public TeacherResponse create(TeacherRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        LocalDateTime now = LocalDateTime.now();
        Teacher teacher = new Teacher();
        teacher.setUserId(null);
        teacher.setName(requireText(request.name(), "Teacher name must not be blank"));
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
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
    public TeacherProfileResponse createInternal(CreateTeacherProfileRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        Long userId = request.userId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User id must not be null");
        }
        Teacher existing = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUserId, userId)
                .last("limit 1"));
        if (existing != null) {
            return toProfileResponse(existing);
        }

        LocalDateTime now = LocalDateTime.now();
        Teacher teacher = new Teacher();
        teacher.setUserId(userId);
        teacher.setName(requireText(request.name(), "Teacher name must not be blank"));
        teacher.setTitle(trimToNull(request.title()));
        teacher.setBio(trimToNull(request.bio()));
        teacher.setSpecialties(trimToNull(request.specialties()));
        teacher.setYearsExperience(defaultYears(request.yearsExperience()));
        teacher.setStatus(EnabledStatus.ENABLED);
        teacher.setCreatedAt(now);
        teacher.setUpdatedAt(now);
        teacherMapper.insert(teacher);
        return toProfileResponse(teacher);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
    public TeacherResponse update(Long id, TeacherRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        Teacher teacher = getTeacher(id);
        teacher.setName(requireText(request.name(), "Teacher name must not be blank"));
        teacher.setTitle(trimToNull(request.title()));
        teacher.setBio(trimToNull(request.bio()));
        teacher.setSpecialties(trimToNull(request.specialties()));
        teacher.setYearsExperience(defaultYears(request.yearsExperience()));
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherMapper.updateById(teacher);
        return toResponse(teacher);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
    public TeacherResponse enable(Long id) {
        Teacher teacher = getTeacher(id);
        teacher.setStatus(EnabledStatus.ENABLED);
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherMapper.updateById(teacher);
        return toResponse(teacher);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
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
                teacher.getUserId(),
                teacherDisplayName(teacher),
                teacher.getTitle(),
                teacher.getBio(),
                teacher.getSpecialties(),
                teacher.getYearsExperience(),
                teacher.getStatus(),
                teacher.getCreatedAt(),
                teacher.getUpdatedAt()
        );
    }

    private TeacherProfileResponse toProfileResponse(Teacher teacher) {
        return new TeacherProfileResponse(
                teacher.getId(),
                teacher.getUserId(),
                teacherDisplayName(teacher),
                teacher.getTitle(),
                teacher.getBio(),
                teacher.getSpecialties(),
                teacher.getYearsExperience(),
                teacher.getStatus() == null ? null : teacher.getStatus().getCode(),
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

    private String teacherDisplayName(Teacher teacher) {
        if (teacher.getUserId() != null) {
            User user = userMapper.selectById(teacher.getUserId());
            String username = user == null ? null : trimToNull(user.getUsername());
            if (username != null) {
                return username;
            }
        }
        return teacher.getName();
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

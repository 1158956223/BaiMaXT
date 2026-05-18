package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.vo.CourseDetailResponse;
import com.example.domain.vo.CourseListResponse;
import com.example.domain.dto.CourseRequest;
import com.example.domain.vo.TeacherResponse;
import com.example.domain.vo.TeacherSummaryResponse;
import com.example.domain.enums.CourseStatus;
import com.example.domain.enums.CourseType;
import com.example.domain.enums.EnabledStatus;
import com.example.domain.po.Course;
import com.example.domain.po.CourseCategory;
import com.example.domain.po.Teacher;
import com.example.exception.BusinessException;
import com.example.mapper.CourseCategoryMapper;
import com.example.mapper.CourseMapper;
import com.example.mapper.TeacherMapper;
import com.example.service.CourseService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseCategoryMapper categoryMapper;
    private final TeacherMapper teacherMapper;

    public CourseServiceImpl(CourseMapper courseMapper,
                             CourseCategoryMapper categoryMapper,
                             TeacherMapper teacherMapper) {
        this.courseMapper = courseMapper;
        this.categoryMapper = categoryMapper;
        this.teacherMapper = teacherMapper;
    }

    @Override
    public List<CourseListResponse> listOnSale(Long categoryId, String keyword) {
        LambdaQueryWrapper<Course> query = baseQuery(categoryId, keyword)
                .eq(Course::getStatus, CourseStatus.ON_SALE);
        return courseMapper.selectList(query).stream()
                .map(this::toListResponse)
                .toList();
    }

    @Override
    public List<CourseListResponse> listAll(Long categoryId, String keyword) {
        return courseMapper.selectList(baseQuery(categoryId, keyword)).stream()
                .map(this::toListResponse)
                .toList();
    }

    @Override
    public CourseDetailResponse getPublicDetail(Long id) {
        Course course = getCourse(id);
        if (course.getStatus() != CourseStatus.ON_SALE) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Course does not exist");
        }
        return toDetailResponse(course);
    }

    @Override
    public CourseDetailResponse getAdminDetail(Long id) {
        return toDetailResponse(getCourse(id));
    }

    @Override
    public CourseDetailResponse create(CourseRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        CourseCategory category = requireEnabledCategory(request.categoryId());
        Teacher teacher = requireEnabledTeacher(request.teacherId());
        LocalDateTime now = LocalDateTime.now();

        Course course = new Course();
        fillCourse(course, request, category, teacher);
        course.setStatus(request.status() == null ? CourseStatus.DRAFT : request.status());
        course.setCreatedAt(now);
        course.setUpdatedAt(now);
        courseMapper.insert(course);
        return toDetailResponse(course);
    }

    @Override
    public CourseDetailResponse update(Long id, CourseRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        Course course = getCourse(id);
        CourseCategory category = requireEnabledCategory(request.categoryId());
        Teacher teacher = requireEnabledTeacher(request.teacherId());
        fillCourse(course, request, category, teacher);
        if (request.status() != null) {
            course.setStatus(request.status());
        }
        course.setUpdatedAt(LocalDateTime.now());
        courseMapper.updateById(course);
        return toDetailResponse(course);
    }

    @Override
    public CourseDetailResponse onSale(Long id) {
        Course course = getCourse(id);
        requireEnabledCategory(course.getCategoryId());
        requireEnabledTeacher(course.getTeacherId());
        course.setStatus(CourseStatus.ON_SALE);
        course.setUpdatedAt(LocalDateTime.now());
        courseMapper.updateById(course);
        return toDetailResponse(course);
    }

    @Override
    public CourseDetailResponse offSale(Long id) {
        Course course = getCourse(id);
        course.setStatus(CourseStatus.OFF_SALE);
        course.setUpdatedAt(LocalDateTime.now());
        courseMapper.updateById(course);
        return toDetailResponse(course);
    }

    private void fillCourse(Course course, CourseRequest request, CourseCategory category, Teacher teacher) {
        course.setCategoryId(category.getId());
        course.setTeacherId(teacher.getId());
        course.setTitle(requireText(request.title(), "Course title must not be blank"));
        course.setSubtitle(trimToNull(request.subtitle()));
        course.setCoverUrl(trimToNull(request.coverUrl()));
        course.setPrice(requireNonNegativeMoney(request.price(), "Course price must not be null"));
        course.setOriginalPrice(optionalNonNegativeMoney(request.originalPrice(), "Original price must not be negative"));
        course.setCourseType(request.courseType() == null ? CourseType.ONLINE : request.courseType());
        course.setDurationDesc(trimToNull(request.durationDesc()));
        course.setTargetAudience(trimToNull(request.targetAudience()));
        course.setIntro(trimToNull(request.intro()));
        course.setOutline(trimToNull(request.outline()));
        course.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private Course getCourse(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course id must not be null");
        }
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Course does not exist");
        }
        return course;
    }

    private CourseCategory requireEnabledCategory(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Category id must not be null");
        }
        CourseCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Category does not exist");
        }
        if (category.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Category is disabled");
        }
        return category;
    }

    private Teacher requireEnabledTeacher(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Teacher id must not be null");
        }
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Teacher does not exist");
        }
        if (teacher.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Teacher is disabled");
        }
        return teacher;
    }

    private LambdaQueryWrapper<Course> baseQuery(Long categoryId, String keyword) {
        LambdaQueryWrapper<Course> query = new LambdaQueryWrapper<Course>()
                .orderByAsc(Course::getSortOrder)
                .orderByDesc(Course::getId);
        if (categoryId != null) {
            query.eq(Course::getCategoryId, categoryId);
        }
        String text = trimToNull(keyword);
        if (text != null) {
            query.and(wrapper -> wrapper.like(Course::getTitle, text)
                    .or()
                    .like(Course::getSubtitle, text));
        }
        return query;
    }

    private CourseListResponse toListResponse(Course course) {
        CourseCategory category = categoryMapper.selectById(course.getCategoryId());
        Teacher teacher = teacherMapper.selectById(course.getTeacherId());
        return new CourseListResponse(
                course.getId(),
                course.getCategoryId(),
                category == null ? null : category.getName(),
                course.getTitle(),
                course.getSubtitle(),
                course.getCoverUrl(),
                course.getPrice(),
                course.getOriginalPrice(),
                course.getCourseType(),
                course.getDurationDesc(),
                course.getStatus(),
                course.getSortOrder(),
                toTeacherSummary(teacher)
        );
    }

    private CourseDetailResponse toDetailResponse(Course course) {
        CourseCategory category = categoryMapper.selectById(course.getCategoryId());
        Teacher teacher = teacherMapper.selectById(course.getTeacherId());
        return new CourseDetailResponse(
                course.getId(),
                course.getCategoryId(),
                category == null ? null : category.getName(),
                course.getTitle(),
                course.getSubtitle(),
                course.getCoverUrl(),
                course.getPrice(),
                course.getOriginalPrice(),
                course.getCourseType(),
                course.getDurationDesc(),
                course.getTargetAudience(),
                course.getIntro(),
                course.getOutline(),
                course.getStatus(),
                course.getSortOrder(),
                toTeacherResponse(teacher),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    private TeacherSummaryResponse toTeacherSummary(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        return new TeacherSummaryResponse(teacher.getId(), teacher.getName(), teacher.getTitle());
    }

    private TeacherResponse toTeacherResponse(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        return new TeacherResponse(
                teacher.getId(),
                teacher.getUserId(),
                teacher.getName(),
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

    private BigDecimal requireNonNegativeMoney(BigDecimal value, String message) {
        if (value == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return optionalNonNegativeMoney(value, "Money value must not be negative");
    }

    private BigDecimal optionalNonNegativeMoney(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

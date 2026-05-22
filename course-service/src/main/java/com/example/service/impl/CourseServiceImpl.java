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
import com.example.domain.po.User;
import com.example.exception.BusinessException;
import com.example.mapper.CourseCategoryMapper;
import com.example.mapper.CourseMapper;
import com.example.mapper.TeacherMapper;
import com.example.mapper.UserMapper;
import com.example.mq.CourseIndexEventType;
import com.example.mq.CourseIndexMessagePublisher;
import com.example.service.CourseService;
import java.math.BigDecimal;
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
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseCategoryMapper categoryMapper;
    private final TeacherMapper teacherMapper;
    private final UserMapper userMapper;
    private final CourseIndexMessagePublisher courseIndexMessagePublisher;

    public CourseServiceImpl(CourseMapper courseMapper,
                             CourseCategoryMapper categoryMapper,
                             TeacherMapper teacherMapper,
                             UserMapper userMapper,
                             CourseIndexMessagePublisher courseIndexMessagePublisher) {
        this.courseMapper = courseMapper;
        this.categoryMapper = categoryMapper;
        this.teacherMapper = teacherMapper;
        this.userMapper = userMapper;
        this.courseIndexMessagePublisher = courseIndexMessagePublisher;
    }

    @Override
    @Cacheable(cacheNames = COURSE_PUBLIC_LIST_CACHE, keyGenerator = "courseCacheKeyGenerator")
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
    public List<CourseListResponse> listTeacherCourses(Long userId, Long categoryId, String keyword) {
        Teacher teacher = requireCurrentTeacher(userId);
        return courseMapper.selectList(baseQuery(categoryId, keyword)
                        .eq(Course::getTeacherId, teacher.getId())
                        .ne(Course::getStatus, CourseStatus.OFF_SALE))
                .stream()
                .map(this::toListResponse)
                .toList();
    }

    @Override
    @Cacheable(cacheNames = COURSE_PUBLIC_DETAIL_CACHE, key = "#p0")
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
    public CourseDetailResponse getTeacherDetail(Long id, Long userId) {
        Teacher teacher = requireCurrentTeacher(userId);
        Course course = getTeacherOwnedCourse(id, teacher);
        return toDetailResponse(course);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
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
        courseIndexMessagePublisher.publish(course.getId(), CourseIndexEventType.CREATED);
        return toDetailResponse(course);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
    public CourseDetailResponse createTeacherCourse(CourseRequest request, Long userId) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        Teacher teacher = requireCurrentTeacher(userId);
        CourseCategory category = requireEnabledCategory(request.categoryId());
        LocalDateTime now = LocalDateTime.now();

        Course course = new Course();
        fillCourse(course, request, category, teacher);
        course.setStatus(normalizeTeacherStatus(request.status()));
        course.setCreatedAt(now);
        course.setUpdatedAt(now);
        courseMapper.insert(course);
        courseIndexMessagePublisher.publish(course.getId(), CourseIndexEventType.CREATED);
        return toDetailResponse(course);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
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
        courseIndexMessagePublisher.publish(course.getId(), CourseIndexEventType.UPDATED);
        return toDetailResponse(course);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
    public CourseDetailResponse updateTeacherCourse(Long id, CourseRequest request, Long userId) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        Teacher teacher = requireCurrentTeacher(userId);
        Course course = getTeacherOwnedCourse(id, teacher);
        CourseCategory category = requireEnabledCategory(request.categoryId());
        fillCourse(course, request, category, teacher);
        course.setStatus(normalizeTeacherStatus(request.status()));
        course.setUpdatedAt(LocalDateTime.now());
        courseMapper.updateById(course);
        courseIndexMessagePublisher.publish(course.getId(), CourseIndexEventType.UPDATED);
        return toDetailResponse(course);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
    public CourseDetailResponse onSale(Long id) {
        Course course = getCourse(id);
        requireEnabledCategory(course.getCategoryId());
        requireEnabledTeacher(course.getTeacherId());
        course.setStatus(CourseStatus.ON_SALE);
        course.setUpdatedAt(LocalDateTime.now());
        courseMapper.updateById(course);
        courseIndexMessagePublisher.publish(course.getId(), CourseIndexEventType.ON_SALE);
        return toDetailResponse(course);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
    public CourseDetailResponse offSale(Long id) {
        Course course = getCourse(id);
        course.setStatus(CourseStatus.OFF_SALE);
        course.setUpdatedAt(LocalDateTime.now());
        courseMapper.updateById(course);
        courseIndexMessagePublisher.publish(course.getId(), CourseIndexEventType.OFF_SALE);
        return toDetailResponse(course);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE, COURSE_CATEGORY_ENABLED_CACHE, COURSE_TEACHER_ENABLED_CACHE}, allEntries = true)
    public CourseDetailResponse deleteTeacherCourse(Long id, Long userId) {
        Teacher teacher = requireCurrentTeacher(userId);
        Course course = getTeacherOwnedCourse(id, teacher);
        course.setStatus(CourseStatus.OFF_SALE);
        course.setUpdatedAt(LocalDateTime.now());
        courseMapper.updateById(course);
        courseIndexMessagePublisher.publish(course.getId(), CourseIndexEventType.OFF_SALE);
        return toDetailResponse(course);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE}, allEntries = true)
    public void decreaseStock(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course id must not be null");
        }
        int affected = courseMapper.decreaseStock(id);
        if (affected == 0) {
            Course course = getCourse(id);
            if (course.getStatus() != CourseStatus.ON_SALE) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Course is not on sale");
            }
            throw new BusinessException(HttpStatus.CONFLICT, "Course stock is sold out");
        }
        courseIndexMessagePublisher.publish(id, CourseIndexEventType.UPDATED);
    }

    @Override
    @CacheEvict(cacheNames = {COURSE_PUBLIC_LIST_CACHE, COURSE_PUBLIC_DETAIL_CACHE}, allEntries = true)
    public void restoreStock(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course id must not be null");
        }
        courseMapper.restoreStock(id);
        courseIndexMessagePublisher.publish(id, CourseIndexEventType.UPDATED);
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
        course.setStock(requireNonNegativeInteger(request.stock(), "Course stock must not be null"));
        if (course.getSoldCount() == null) {
            course.setSoldCount(0);
        }
        if (course.getStock() < course.getSoldCount()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course stock must not be less than sold count");
        }
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

    private Teacher requireCurrentTeacher(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User id must not be null");
        }
        Teacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUserId, userId)
                .last("LIMIT 1"));
        if (teacher == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Teacher profile does not exist");
        }
        if (teacher.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Teacher profile is disabled");
        }
        return teacher;
    }

    private Course getTeacherOwnedCourse(Long id, Teacher teacher) {
        Course course = getCourse(id);
        if (!teacher.getId().equals(course.getTeacherId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Course does not belong to current teacher");
        }
        return course;
    }

    private CourseStatus normalizeTeacherStatus(CourseStatus status) {
        if (status == null) {
            return CourseStatus.DRAFT;
        }
        if (status == CourseStatus.OFF_SALE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Teacher course status must be DRAFT or ON_SALE");
        }
        return status;
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
                course.getStock(),
                course.getSoldCount(),
                availableStock(course),
                course.getStatus(),
                course.getSortOrder(),
                toTeacherSummary(teacher),
                course.getCreatedAt(),
                course.getUpdatedAt()
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
                course.getStock(),
                course.getSoldCount(),
                availableStock(course),
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
        return new TeacherSummaryResponse(teacher.getId(), teacherDisplayName(teacher), teacher.getTitle());
    }

    private TeacherResponse toTeacherResponse(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
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

    private Integer requireNonNegativeInteger(Integer value, String message) {
        if (value == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        if (value < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Integer value must not be negative");
        }
        return value;
    }

    private int availableStock(Course course) {
        int stock = course.getStock() == null ? 0 : course.getStock();
        int soldCount = course.getSoldCount() == null ? 0 : course.getSoldCount();
        return Math.max(stock - soldCount, 0);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.Course;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface CourseMapper extends BaseMapper<Course> {

    @Update("""
            UPDATE db_course
            SET sold_count = sold_count + 1,
                updated_at = NOW()
            WHERE id = #{courseId}
              AND status = 'ON_SALE'
              AND stock > sold_count
            """)
    int decreaseStock(@Param("courseId") Long courseId);

    @Update("""
            UPDATE db_course
            SET sold_count = sold_count - 1,
                updated_at = NOW()
            WHERE id = #{courseId}
              AND sold_count > 0
            """)
    int restoreStock(@Param("courseId") Long courseId);
}

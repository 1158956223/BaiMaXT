package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.enums.CourseStatus;
import com.example.domain.enums.CourseType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_course")
public class Course {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("category_id")
    private Long categoryId; // 课程分类ID

    @TableField("teacher_id")
    private Long teacherId; // 主讲老师ID

    @TableField("title")
    private String title;

    @TableField("subtitle")
    private String subtitle;

    @TableField("cover_url")
    private String coverUrl; // 封面图

    @TableField("price")
    private BigDecimal price;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("course_type")
    private CourseType courseType; // 课程类型

    @TableField("duration_desc")
    private String durationDesc; // 课程周期

    @TableField("stock")
    private Integer stock;

    @TableField("sold_count")
    private Integer soldCount;

    @TableField("target_audience")
    private String targetAudience; // 适合人群

    @TableField("intro")
    private String intro; // 课程介绍

    @TableField("outline")
    private String outline; // 课程大纲

    @TableField("status")
    private CourseStatus status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

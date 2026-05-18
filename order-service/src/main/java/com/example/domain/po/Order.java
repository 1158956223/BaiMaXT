package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.PayStatus;
import com.example.domain.enums.PayType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_order")
public class Order {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("course_id")
    private Long courseId;

    @TableField("course_title")
    private String courseTitle;

    @TableField("course_subtitle")
    private String courseSubtitle;

    @TableField("course_cover_url")
    private String courseCoverUrl;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField("teacher_name")
    private String teacherName;

    @TableField("original_amount")
    private BigDecimal originalAmount;

    @TableField("pay_amount")
    private BigDecimal payAmount;

    @TableField("discount_amount")
    private BigDecimal discountAmount;

    @TableField("order_status")
    private OrderStatus orderStatus;

    @TableField("pay_status")
    private PayStatus payStatus;

    @TableField("pay_type")
    private PayType payType;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("remark")
    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

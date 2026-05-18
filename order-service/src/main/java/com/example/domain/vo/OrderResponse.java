package com.example.domain.vo;

import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.PayStatus;
import com.example.domain.enums.PayType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String orderNo,
        Long userId,
        Long courseId,
        String courseTitle,
        String courseSubtitle,
        String courseCoverUrl,
        Long teacherId,
        String teacherName,
        BigDecimal originalAmount,
        BigDecimal payAmount,
        BigDecimal discountAmount,
        OrderStatus orderStatus,
        PayStatus payStatus,
        PayType payType,
        LocalDateTime payTime,
        LocalDateTime cancelTime,
        LocalDateTime expireTime,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("db_pay_order")
public class PayOrder {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("pay_no")
    private String payNo;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("pay_amount")
    private BigDecimal payAmount;

    @TableField("pay_type")
    private PayType payType;

    @TableField("pay_status")
    private PayStatus payStatus;

    @TableField("subject")
    private String subject;

    @TableField("description")
    private String description;

    @TableField("trade_no")
    private String tradeNo;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("close_time")
    private LocalDateTime closeTime;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

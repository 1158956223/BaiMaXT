package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.enums.OrderOperateType;
import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.PayStatus;
import com.example.enums.UserRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_order_status_log")
public class OrderStatusLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("old_order_status")
    private OrderStatus oldOrderStatus;

    @TableField("new_order_status")
    private OrderStatus newOrderStatus;

    @TableField("old_pay_status")
    private PayStatus oldPayStatus;

    @TableField("new_pay_status")
    private PayStatus newPayStatus;

    @TableField("operate_type")
    private OrderOperateType operateType;

    @TableField("operate_by")
    private Long operateBy;

    @TableField("operate_role")
    private UserRole operateRole;

    @TableField("remark")
    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

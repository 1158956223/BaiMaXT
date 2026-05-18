package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.enums.PayFlowType;
import com.example.domain.enums.PayType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_pay_flow")
public class PayFlow {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("pay_no")
    private String payNo;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("flow_type")
    private PayFlowType flowType;

    @TableField("pay_type")
    private PayType payType;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("trade_no")
    private String tradeNo;

    @TableField("remark")
    private String remark;

    @TableField("raw_content")
    private String rawContent;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

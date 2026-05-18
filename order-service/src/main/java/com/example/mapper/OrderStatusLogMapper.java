package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
}

package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.AuthAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthAccountMapper extends BaseMapper<AuthAccount> {
}

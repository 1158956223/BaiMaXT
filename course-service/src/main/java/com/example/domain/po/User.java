package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("db_user")
public class User {
    @TableId(value = "uid", type = IdType.AUTO)
    private Long uid;

    @TableField("username")
    private String username;
}

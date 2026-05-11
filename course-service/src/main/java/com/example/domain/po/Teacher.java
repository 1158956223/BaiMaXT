package com.example.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.enums.EnabledStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_teacher")
public class Teacher {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("avatar_url")
    private String avatarUrl; // 头像地址

    @TableField("title")
    private String title; // 头衔

    @TableField("bio")
    private String bio; // 简介

    @TableField("specialties")
    private String specialties; // 擅长领域

    @TableField("years_experience")
    private Integer yearsExperience; // 教龄

    @TableField("status")
    private EnabledStatus status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.dto.CreateUserRequest;
import com.example.domain.dto.InternalCreateUserRequest;
import com.example.domain.dto.UpdateUserRequest;
import com.example.domain.dto.UserResponse;
import com.example.domain.po.User;

import java.util.List;

public interface UserService extends IService<User> {

    UserResponse create(CreateUserRequest request);

    UserResponse createInternal(InternalCreateUserRequest request);

    List<UserResponse> listUser();

    UserResponse get(Long id);

    UserResponse update(Long id, UpdateUserRequest request);

    UserResponse disable(Long id);

    UserResponse enable(Long id);
}

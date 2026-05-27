package com.example.service;

@FunctionalInterface
public interface UserIdResolver {

    Long parseUserId(String authorization);
}

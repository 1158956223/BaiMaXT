package com.example.domain.dto;

public record AgentChatRequest(
        String message,
        String sessionId,
        Long candidateCourseId,
        Long pendingCourseId,
        Long selectedCourseId,
        String humanResponse
) {
}

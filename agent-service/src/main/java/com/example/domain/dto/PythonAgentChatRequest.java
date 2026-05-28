package com.example.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PythonAgentChatRequest(
        @JsonProperty("user_id")
        Long userId,
        String message,
        @JsonProperty("session_id")
        String sessionId,
        @JsonProperty("candidate_course_id")
        Long candidateCourseId,
        @JsonProperty("pending_course_id")
        Long pendingCourseId,
        @JsonProperty("selected_course_id")
        Long selectedCourseId,
        @JsonProperty("human_response")
        String humanResponse
) {
}

package com.example.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record AgentChatResponse(
        String reply,
        @JsonProperty("candidate_course_id")
        Long candidateCourseId,
        @JsonProperty("pending_course_id")
        Long pendingCourseId,
        @JsonProperty("tool_result")
        Map<String, Object> toolResult
) {
}

package com.example.service;

import com.example.domain.dto.AgentChatRequest;
import com.example.domain.vo.AgentChatResponse;
import org.springframework.stereotype.Service;

@Service
public class AgentChatService {

    private final UserIdResolver userIdResolver;
    private final PythonAgentClient pythonAgentClient;

    public AgentChatService(UserIdResolver userIdResolver, PythonAgentClient pythonAgentClient) {
        this.userIdResolver = userIdResolver;
        this.pythonAgentClient = pythonAgentClient;
    }

    public AgentChatResponse chat(AgentChatRequest request, String authorization) {
        Long userId = userIdResolver.parseUserId(authorization);
        PythonAgentChatRequest pythonRequest = new PythonAgentChatRequest(
                userId,
                request.message(),
                request.sessionId() == null || request.sessionId().isBlank() ? "default" : request.sessionId(),
                request.candidateCourseId(),
                request.pendingCourseId(),
                request.selectedCourseId(),
                request.humanResponse()
        );
        return pythonAgentClient.chat(authorization, pythonRequest);
    }
}

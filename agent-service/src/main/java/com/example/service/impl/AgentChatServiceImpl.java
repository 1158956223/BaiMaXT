package com.example.service.impl;

import com.example.domain.dto.AgentChatRequest;
import com.example.domain.dto.PythonAgentChatRequest;
import com.example.domain.vo.AgentChatResponse;
import com.example.service.AgentChatService;
import com.example.service.JwtTokenService;
import com.example.service.PythonAgentClient;
import org.springframework.stereotype.Service;

@Service
public class AgentChatServiceImpl implements AgentChatService {

    private final JwtTokenService jwtTokenService;
    private final PythonAgentClient pythonAgentClient;

    public AgentChatServiceImpl(JwtTokenService jwtTokenService, PythonAgentClient pythonAgentClient) {
        this.jwtTokenService = jwtTokenService;
        this.pythonAgentClient = pythonAgentClient;
    }

    @Override
    public AgentChatResponse chat(AgentChatRequest request, String authorization) {
        Long userId = jwtTokenService.parseUserId(authorization);
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

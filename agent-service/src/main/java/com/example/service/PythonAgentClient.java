package com.example.service;

import com.example.domain.vo.AgentChatResponse;

@FunctionalInterface
public interface PythonAgentClient {

    AgentChatResponse chat(String authorization, PythonAgentChatRequest request);
}

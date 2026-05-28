package com.example.service;

import com.example.domain.dto.AgentChatRequest;
import com.example.domain.vo.AgentChatResponse;

public interface AgentChatService {

    AgentChatResponse chat(AgentChatRequest request, String authorization);
}

package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.dto.AgentChatRequest;
import com.example.domain.vo.AgentChatResponse;
import com.example.service.AgentChatService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentChatService agentChatService;

    public AgentController(AgentChatService agentChatService) {
        this.agentChatService = agentChatService;
    }

    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chat(@RequestBody AgentChatRequest request,
                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ApiResponse.success(agentChatService.chat(request, authorization));
    }
}

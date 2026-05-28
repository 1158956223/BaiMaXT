package com.example.client;

import com.example.domain.dto.PythonAgentChatRequest;
import com.example.domain.vo.AgentChatResponse;
import com.example.service.PythonAgentClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PythonAgentRestClient implements PythonAgentClient {

    private final RestClient pythonAgentHttpClient;

    public PythonAgentRestClient(RestClient pythonAgentHttpClient) {
        this.pythonAgentHttpClient = pythonAgentHttpClient;
    }

    @Override
    public AgentChatResponse chat(String authorization, PythonAgentChatRequest request) {
        return pythonAgentHttpClient.post()
                .uri("/api/agent/chat")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(AgentChatResponse.class);
    }
}

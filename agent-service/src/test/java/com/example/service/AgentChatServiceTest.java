package com.example.service;

import com.example.domain.dto.AgentChatRequest;
import com.example.domain.vo.AgentChatResponse;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentChatServiceTest {

    @Test
    void injectsUserIdFromJwtAndForwardsAuthorizationToPythonAgent() {
        UserIdResolver userIdResolver = authorization -> 42L;
        AtomicReference<String> forwardedAuthorization = new AtomicReference<>();
        AtomicReference<PythonAgentChatRequest> forwardedRequest = new AtomicReference<>();
        PythonAgentClient pythonAgentClient = (authorization, request) -> {
            forwardedAuthorization.set(authorization);
            forwardedRequest.set(request);
            return new AgentChatResponse("已收到", 101L, 102L, null);
        };
        AgentChatService service = new AgentChatService(userIdResolver, pythonAgentClient);

        AgentChatResponse response = service.chat(
                new AgentChatRequest("我要学 Java", "session-1", 101L, 102L, null, "确认报名"),
                "Bearer signed-token"
        );

        assertThat(response.reply()).isEqualTo("已收到");
        assertThat(forwardedAuthorization).hasValue("Bearer signed-token");
        assertThat(forwardedRequest.get().userId()).isEqualTo(42L);
        assertThat(forwardedRequest.get().message()).isEqualTo("我要学 Java");
        assertThat(forwardedRequest.get().sessionId()).isEqualTo("session-1");
        assertThat(forwardedRequest.get().candidateCourseId()).isEqualTo(101L);
        assertThat(forwardedRequest.get().pendingCourseId()).isEqualTo(102L);
        assertThat(forwardedRequest.get().selectedCourseId()).isNull();
        assertThat(forwardedRequest.get().humanResponse()).isEqualTo("确认报名");
    }
}

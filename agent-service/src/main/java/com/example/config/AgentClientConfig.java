package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AgentClientConfig {

    @Bean
    public RestClient pythonAgentRestClient(
            RestClient.Builder builder,
            @Value("${agent.python.base-url}") String pythonAgentBaseUrl
    ) {
        return builder.baseUrl(pythonAgentBaseUrl).build();
    }
}

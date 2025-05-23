package com.flexrate.flexrate_back.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String openAiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Bean(name = "openAiRestClient")
    public RestClient openAiRestClient() {
        return RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + openAiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

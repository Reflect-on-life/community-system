package com.example.futurecommunity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * GLM AI 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "glm")
public class GlmConfig {
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * API 基础 URL
     */
    private String baseUrl;
    
    /**
     * 模型名称
     */
    private String model;
}

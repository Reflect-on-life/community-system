package com.example.futurecommunity.config;

import com.baidu.aip.imageclassify.AipImageClassify;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaiduAIConfig {

    @Value("${baidu.ai.appId}")
    private String appId;

    @Value("${baidu.ai.apiKey}")
    private String apiKey;

    @Value("${baidu.ai.secretKey}")
    private String secretKey;

    @Bean
    public AipImageClassify aipClient() {
        return new AipImageClassify(appId, apiKey, secretKey);
    }
}
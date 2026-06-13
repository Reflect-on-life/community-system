package com.example.futurecommunity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 模型预置配置中心
 * 管理当前选中的预置方案及各模型 API 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "model-preset")
public class ModelPresetConfig {

    /** 当前选中的预置方案 */
    private String current = "ORIGINAL";

    private QwenVLConfig qwenVl = new QwenVLConfig();
    private GeminiConfig gemini = new GeminiConfig();
    private YoloConfig yolo = new YoloConfig();

    @Data
    public static class QwenVLConfig {
        private String apiKey;
        private String model = "qwen-vl-max";
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    }

    @Data
    public static class GeminiConfig {
        private String apiKey;
        private String model = "gemini-2.5-flash";
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    }

    @Data
    public static class YoloConfig {
        private String serviceUrl = "http://localhost:5000";
        private boolean enabled = false;
    }

    public ModelPreset getCurrentPreset() {
        try {
            return ModelPreset.valueOf(current.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ModelPreset.ORIGINAL;
        }
    }

    public void setCurrentPreset(ModelPreset preset) {
        this.current = preset.name();
    }
}

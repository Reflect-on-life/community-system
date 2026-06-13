package com.example.futurecommunity.config;

/**
 * AI 模型预置组合枚举
 * 三套方案供用户选择
 */
public enum ModelPreset {
    ORIGINAL("原版", "GLM-4V + 百度AI", "默认方案，均衡稳定"),
    ECONOMY("最省钱", "YOLOv11 + Qwen-VL", "本地检测+国产VLM，成本最低"),
    PREMIUM("效果最好", "YOLOv11 + Gemini 2.5 Flash", "最强视觉理解，效果最优");

    private final String displayName;
    private final String models;
    private final String description;

    ModelPreset(String displayName, String models, String description) {
        this.displayName = displayName;
        this.models = models;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getModels() { return models; }
    public String getDescription() { return description; }
}

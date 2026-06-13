package com.example.futurecommunity.service;

/**
 * 视觉语言模型（VLM）通用服务接口
 * 抽象 GLM-4V、Qwen-VL、Gemini 等不同 VLM 后端
 */
public interface VisionService {

    String analyzeImage(String imageUrl);

    String analyzeImageWithPrompt(String imageUrl, String customPrompt);

    String getServiceName();
}

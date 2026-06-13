package com.example.futurecommunity.service;

/**
 * GLM AI 图像识别服务接口
 * 用于调用 GLM-4V 视觉理解模型分析社区安全风险
 */
public interface GLMService {
    
    /**
     * 分析图片中的社区安全风险
     * @param imageUrl 图片路径（本地文件路径或 URL）
     * @return AI 分析结果文本
     */
    String analyzeImage(String imageUrl);
    
    /**
     * 使用自定义提示词分析图片
     * @param imageUrl 图片路径
     * @param customPrompt 自定义提示词
     * @return AI 分析结果文本
     */
    String analyzeImageWithPrompt(String imageUrl, String customPrompt);
}

package com.example.futurecommunity.service;

import java.util.Map;

/**
 * 摄像头 AI 识别服务接口
 * 专门用于摄像头监控页面的 AI 风险识别
 * 复用 GLMService 和百度 AI 的核心能力
 */
public interface CameraAIRecognitionService {
    
    /**
     * 分析摄像头截取的图片（Base64 格式）
     * 
     * @param base64Image Base64 编码的图片
     * @param cameraLocation 摄像头位置
     * @return 包含 riskType、riskLevel、description、isRisk、glmResult、baiduLabels 的 Map
     */
    Map<String, String> analyzeCameraImage(String base64Image, String cameraLocation);
    
    /**
     * 分析摄像头截取的图片（本地文件路径）
     * 
     * @param localFilePath 本地文件路径
     * @param cameraLocation 摄像头位置
     * @return 包含 riskType、riskLevel、description、isRisk、glmResult、baiduLabels 的 Map
     */
    Map<String, String> analyzeCameraImageFromFile(String localFilePath, String cameraLocation);
}

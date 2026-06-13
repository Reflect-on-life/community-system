package com.example.futurecommunity.service;

import java.util.List;
import java.util.Map;

/**
 * AI 识别服务接口
 * 用于识别风险图片并返回风险类型、等级和描述信息
 * 
 * 功能：
 * 1. 接收图片 URL
 * 2. 调用 GLM-4V API 进行图像理解
 * 3. 调用百度 AI 进行图像标签识别
 * 4. 合并两个 AI 的结果进行风险判断
 * 5. 识别风险类型：老人摔倒、火灾、人员聚集、设施损坏、垃圾堆积、广告促销、打架斗殴
 * 6. 返回风险类型、风险等级（HIGH/MEDIUM/LOW）、描述文本
 * 7. 支持连续帧动作识别：摔倒动作、奔跑动作、打斗动作、宠物追逐
 */
public interface AIRecognitionService {
    
    /**
     * 识别风险图片
     * @param imageUrl 图片 URL
     * @return 包含 riskType（风险类型）、riskLevel（风险等级）、description（风险描述）的 Map
     */
    Map<String, String> recognizeRisk(String imageUrl);
    
    /**
     * 识别风险图片（带标题联合识别）
     * @param imageUrl 图片 URL
     * @param riskTitle 风险标题
     * @return 包含 riskType（风险类型）、riskLevel（风险等级）、description（风险描述）的 Map
     */
    Map<String, String> recognizeRiskWithTitle(String imageUrl, String riskTitle);
    
    /**
     * 识别风险图片（完整信息，包含 GLM 和百度结果）
     * @param imageUrl 图片 URL
     * @param riskTitle 风险标题（可选）
     * @return 包含 riskType、riskLevel、description、glmResult、baiduLabels 的 Map
     */
    Map<String, String> recognizeRiskWithFullInfo(String imageUrl, String riskTitle);
    
    /**
     * 识别风险图片（带摄像头位置信息，自动创建风险事件）
     * @param imageUrl 图片 URL
     * @param riskTitle 风险标题（可选）
     * @param cameraLocation 摄像头位置
     * @param cameraId 摄像头ID
     * @return 包含 riskType、riskLevel、description、glmResult、baiduLabels 的 Map
     */
    Map<String, String> recognizeRiskWithCameraInfo(String imageUrl, String riskTitle, String cameraLocation, String cameraId);
    
    /**
     * 连续帧动作识别
     * @param imageUrls 3 张连续帧的图片 URL 列表
     * @param location 位置信息
     * @return 包含 actionType（动作类型）、riskLevel（风险等级）、description（描述）、frameAnalysis（帧分析）的 Map
     */
    Map<String, String> recognizeMotion(List<String> imageUrls, String location);
}
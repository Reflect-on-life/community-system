package com.example.futurecommunity.service;

import com.example.futurecommunity.model.RiskEvent;

import java.util.Map;

/**
 * 风险事件自动创建服务
 * 
 * 功能：
 * 1. 根据 AI 识别结果自动创建风险事件
 * 2. 5 分钟内不重复创建相同类型风险
 * 3. 自动生成标题和描述
 * 4. 发送系统通知
 */
public interface RiskEventAutoCreateService {
    
    /**
     * 自动创建风险事件
     * 
     * @param riskType 风险类型（如：老人摔倒、打架斗殴等）
     * @param riskLevel 风险等级（HIGH/MEDIUM/LOW）
     * @param location 风险位置
     * @param imageUrl 图片 URL
     * @param description AI 识别的详细描述
     * @param cameraId 摄像头 ID
     * @return 创建成功的 RiskEvent 对象，如果 5 分钟内有重复则返回 null
     */
    RiskEvent autoCreateRiskEvent(
        String riskType,
        String riskLevel,
        String location,
        String imageUrl,
        String description,
        String cameraId,
        String glmResult,
        String baiduLabels
    );
    
    /**
     * 检查 5 分钟内是否已存在相同类型的风险事件
     * 
     * @param riskType 风险类型
     * @param cameraId 摄像头 ID
     * @return true-存在重复，false-无重复
     */
    boolean hasRecentSameRisk(String riskType, String cameraId);
    
    /**
     * 自动生成风险事件标题
     * 
     * @param riskType 风险类型
     * @param location 风险位置
     * @return 生成的标题
     */
    String generateTitle(String riskType, String location);
    
    /**
     * 发送系统通知
     * 
     * @param riskEvent 风险事件
     */
    void sendSystemNotification(RiskEvent riskEvent);
}

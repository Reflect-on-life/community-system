package com.example.futurecommunity.model;

import lombok.Data;

/**
 * 行为识别结果封装类
 */
@Data
public class BehaviorDetectionResult {

    /**
     * 是否告警
     */
    private boolean isAlarm;

    /**
     * 告警类型：打架/发传单/无
     */
    private String alarmType;

    /**
     * 置信度（0-1）
     */
    private Double confidence;

    /**
     * 原始识别结果（JSON 字符串）
     */
    private String rawResult;

    /**
     * 检测人数
     */
    private Integer personNum;

    /**
     * 图片保存路径（可选）
     */
    private String imagePath;

    /**
     * 风险事件 ID（保存后填充）
     */
    private Long riskEventId;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 创建正常结果
     */
    public static BehaviorDetectionResult normal() {
        BehaviorDetectionResult result = new BehaviorDetectionResult();
        result.setAlarm(false);
        result.setAlarmType("无");
        result.setConfidence(0.0);
        result.setPersonNum(0);
        return result;
    }

    /**
     * 创建告警结果
     */
    public static BehaviorDetectionResult alarm(String type, Double confidence) {
        BehaviorDetectionResult result = new BehaviorDetectionResult();
        result.setAlarm(true);
        result.setAlarmType(type);
        result.setConfidence(confidence);
        return result;
    }

    /**
     * 创建错误结果
     */
    public static BehaviorDetectionResult error(String message) {
        BehaviorDetectionResult result = new BehaviorDetectionResult();
        result.setAlarm(false);
        result.setAlarmType("无");
        result.setConfidence(0.0);
        result.setErrorMessage(message);
        return result;
    }
}

package com.example.futurecommunity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 告警消息封装类
 * 用于推送实时告警到前端
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmMessage {

    /**
     * 告警类型（打架/发传单）
     */
    private String type;

    /**
     * 置信度（0-1）
     */
    private Double confidence;

    /**
     * 告警时间字符串
     */
    private String time;

    /**
     * 截图路径（可为空）
     */
    private String imageUrl;

    /**
     * 告警位置
     */
    private String location;

    /**
     * 创建告警消息
     */
    public static AlarmMessage of(String type, Double confidence, String time, String imageUrl, String location) {
        return new AlarmMessage(type, confidence, time, imageUrl, location);
    }
}

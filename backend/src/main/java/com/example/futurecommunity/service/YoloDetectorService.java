package com.example.futurecommunity.service;

import java.util.List;
import java.util.Map;

/**
 * YOLOv11 物体检测服务接口
 * 本地快速检测：人、火、车辆、宠物等
 */
public interface YoloDetectorService {

    /**
     * 检测图片中的物体
     * @param imagePath 图片本地路径或 URL
     * @return 检测结果列表 [{label, confidence, bbox}]
     */
    List<Map<String, Object>> detect(String imagePath);

    /**
     * 以文本形式返回检测摘要
     */
    String detectSummary(String imagePath);

    /**
     * 检测服务是否可用
     */
    boolean isAvailable();
}

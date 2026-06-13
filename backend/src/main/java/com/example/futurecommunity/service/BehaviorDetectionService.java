package com.example.futurecommunity.service;

import com.example.futurecommunity.model.BehaviorDetectionResult;

/**
 * 行为识别服务接口
 */
public interface BehaviorDetectionService {

    /**
     * 行为识别（接收 Base64 编码的图片）
     * 
     * @param base64Image Base64 编码的图片字符串
     * @return 识别结果
     */
    BehaviorDetectionResult detectBehavior(String base64Image);

    /**
     * 行为识别（接收图片文件路径）
     * 
     * @param imagePath 图片文件路径
     * @return 识别结果
     */
    BehaviorDetectionResult detectBehaviorByPath(String imagePath);
}

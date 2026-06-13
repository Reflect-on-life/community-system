package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.AlarmRecord;
import com.example.futurecommunity.service.AlarmRecordService;
import com.example.futurecommunity.service.CameraAIRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 摄像头 AI 识别控制器
 * 专门用于摄像头监控页面的 AI 风险识别
 * 
 * 功能：
 * 1. 接收摄像头截取的 Base64 图片进行实时分析
 * 2. 接收本地文件路径进行离线分析
 * 3. 返回风险类型、风险等级、是否风险等信息
 */
@Slf4j
@RestController
@RequestMapping("/api/camera-ai")
public class CameraAIController {
    
    @Autowired
    private CameraAIRecognitionService cameraAIRecognitionService;
    
    @Autowired
    private AlarmRecordService alarmRecordService;
    
    /**
     * 分析摄像头截取的 Base64 图片
     * 
     * @param request 包含 base64Image 和 cameraLocation 的请求体
     * @return AI 识别结果
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeCameraImage(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String base64Image = request.get("base64Image");
            String cameraLocation = request.getOrDefault("cameraLocation", "本地摄像头");
            
            if (base64Image == null || base64Image.isEmpty()) {
                response.put("code", 400);
                response.put("message", "图片不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("收到摄像头 AI 识别请求，位置：{}", cameraLocation);
            
            // 调用 AI 识别服务
            Map<String, String> result = cameraAIRecognitionService.analyzeCameraImage(base64Image, cameraLocation);
            
            response.put("code", 200);
            response.put("success", true);
            response.put("message", "识别成功");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("摄像头 AI 识别失败：{}", e.getMessage(), e);
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "识别失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 分析本地文件路径的图片
     * 
     * @param request 包含 localFilePath 和 cameraLocation 的请求体
     * @return AI 识别结果
     */
    @PostMapping("/analyze-file")
    public ResponseEntity<Map<String, Object>> analyzeCameraImageFromFile(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String localFilePath = request.get("localFilePath");
            String cameraLocation = request.getOrDefault("cameraLocation", "本地摄像头");
            
            if (localFilePath == null || localFilePath.isEmpty()) {
                response.put("code", 400);
                response.put("message", "文件路径不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("收到摄像头 AI 识别请求（文件），路径：{}，位置：{}", localFilePath, cameraLocation);
            
            // 调用 AI 识别服务
            Map<String, String> result = cameraAIRecognitionService.analyzeCameraImageFromFile(localFilePath, cameraLocation);
            
            // 如果是风险事件，保存到 AlarmRecord
            String isRisk = result.get("isRisk");
            if ("true".equals(isRisk)) {
                try {
                    AlarmRecord alarmRecord = new AlarmRecord();
                    alarmRecord.setAlarmType(result.get("riskType"));
                    
                    // 解析置信度
                    String confidenceStr = result.get("confidence");
                    if (confidenceStr != null && !confidenceStr.isEmpty()) {
                        alarmRecord.setConfidence(Double.parseDouble(confidenceStr));
                    } else {
                        alarmRecord.setConfidence(0.9);
                    }
                    
                    alarmRecord.setImagePath(localFilePath);
                    alarmRecord.setDescription(result.get("description"));
                    alarmRecord.setLocation(cameraLocation);
                    alarmRecord.setAlarmTime(new Date());
                    alarmRecord.setIsRead(0);
                    alarmRecord.setCreatedAt(new Date());
                    
                    alarmRecordService.save(alarmRecord);
                    result.put("alarmRecordId", String.valueOf(alarmRecord.getId()));
                    
                    log.info("告警记录已保存，ID: {}", alarmRecord.getId());
                } catch (Exception e) {
                    log.error("保存告警记录失败: {}", e.getMessage(), e);
                }
            }
            
            response.put("code", 200);
            response.put("success", true);
            response.put("message", "识别成功");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("摄像头 AI 识别失败（文件）：{}", e.getMessage(), e);
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "识别失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

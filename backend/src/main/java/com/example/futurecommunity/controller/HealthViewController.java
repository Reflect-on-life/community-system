package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.HealthRecord;
import com.example.futurecommunity.service.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/health")
public class HealthViewController {

    @Autowired
    private HealthRecordService healthRecordService;

    /**
     * 居民查看自己的健康档案列表
     * 按时间倒序排列
     */
    @GetMapping("/records")
    @PreAuthorize("hasRole('RESIDENT') or hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getMyHealthRecords(@RequestParam(required = false) Long userId) {
        // 获取当前登录用户 ID
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = Long.parseLong(userIdStr);
        
        // 如果是医生角色，需要使用传入的 userId；否则使用当前用户 ID
        Long targetUserId = (userId != null) ? userId : currentUserId;

        // 查询健康档案
        List<HealthRecord> records = healthRecordService.getHealthRecordsByUserId(targetUserId);

        // 按时间倒序排列
        records.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

        // 构建响应
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", records);
        response.put("message", "获取健康档案成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 居民查看自己的健康统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('RESIDENT') or hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getMyHealthStats(@RequestParam(required = false) Long userId) {
        // 获取当前登录用户 ID
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = Long.parseLong(userIdStr);
        
        // 如果是医生角色，需要使用传入的 userId；否则使用当前用户 ID
        Long targetUserId = (userId != null) ? userId : currentUserId;

        // 查询健康档案
        List<HealthRecord> records = healthRecordService.getHealthRecordsByUserId(targetUserId);

        // 计算统计信息
        Map<String, Object> stats = calculateHealthStats(records);

        // 构建响应
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        response.put("message", "获取健康统计信息成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 计算健康统计信息
     */
    private Map<String, Object> calculateHealthStats(List<HealthRecord> records) {
        Map<String, Object> stats = new java.util.HashMap<>();

        if (records.isEmpty()) {
            stats.put("recordCount", 0);
            stats.put("latestBMI", null);
            stats.put("latestBloodPressure", null);
            stats.put("latestBloodSugar", null);
            return stats;
        }

        // 按时间倒序排列，获取最新记录
        records.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        HealthRecord latestRecord = records.get(0);

        // 计算统计信息
        stats.put("recordCount", records.size());
        stats.put("latestBMI", latestRecord.getBmi());
        stats.put("latestBloodPressure", latestRecord.getBloodPressure());
        stats.put("latestBloodSugar", latestRecord.getBloodSugar());

        // 计算平均BMI
        double avgBMI = records.stream()
                .filter(r -> r.getBmi() != null)
                .mapToDouble(HealthRecord::getBmi)
                .average()
                .orElse(0);
        stats.put("averageBMI", Math.round(avgBMI * 100.0) / 100.0);

        // 提取最近6次的血压和血糖数据，用于前端图表展示
        List<Map<String, Object>> trends = records.stream()
                .limit(6)
                .map(record -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("date", record.getCreatedAt());
                    item.put("bloodPressure", record.getBloodPressure());
                    item.put("bloodSugar", record.getBloodSugar());
                    item.put("bmi", record.getBmi());
                    return item;
                })
                .collect(Collectors.toList());
        stats.put("trends", trends);

        return stats;
    }
}

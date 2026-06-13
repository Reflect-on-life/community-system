package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.model.HealthRecord;
import com.example.futurecommunity.model.ActivitySignup;
import com.example.futurecommunity.model.Appointment;
import com.example.futurecommunity.service.UserService;
import com.example.futurecommunity.service.HealthRecordService;
import com.example.futurecommunity.service.ActivitySignupService;
import com.example.futurecommunity.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elderly/statistics")
public class ElderlyCareStatisticsController {

    @Autowired
    private UserService userService;

    @Autowired
    private HealthRecordService healthRecordService;

    @Autowired
    private ActivitySignupService activitySignupService;

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 获取养老服务基础统计数据
     * 返回老人总数、今日活动报名、今日健康检查、紧急呼叫数量
     */
    @GetMapping("/basic")
    public Map<String, Object> getBasicStatistics() {
        Map<String, Object> basicStats = new HashMap<>();
        
        // 计算今日开始和结束时间
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        
        // 老人总数：tags 包含 elderly 的用户
        long elderlyCount = userService.lambdaQuery()
                .like(User::getTags, "elderly")
                .count();
        
        // 今日活动报名：今日报名的老人用户
        long todayActivitySignups = activitySignupService.lambdaQuery()
                .between(ActivitySignup::getSignupTime, todayStart, todayEnd)
                .inSql(ActivitySignup::getUserId, "SELECT id FROM user WHERE tags LIKE '%elderly%'")
                .count();
        
        // 今日健康检查：今日创建的健康记录
        long todayHealthChecks = healthRecordService.lambdaQuery()
                .between(HealthRecord::getCreatedAt, todayStart, todayEnd)
                .inSql(HealthRecord::getUserId, "SELECT id FROM user WHERE tags LIKE '%elderly%'")
                .count();
        
        // 紧急呼叫数量：这里使用模拟数据，因为没有找到对应的表结构
        int emergencyCalls = 3;
        
        basicStats.put("elderlyCount", elderlyCount);
        basicStats.put("todayActivitySignups", todayActivitySignups);
        basicStats.put("todayHealthChecks", todayHealthChecks);
        basicStats.put("emergencyCalls", emergencyCalls);
        
        return basicStats;
    }

    /**
     * 获取近7日活动参与趋势
     * 返回每日参与人数
     */
    @GetMapping("/activity-trend")
    public List<Map<String, Object>> getActivityTrend() {
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        // 生成近7天的日期
        LocalDate endDate = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            
            // 统计当天老人的活动报名数
            long signupCount = activitySignupService.lambdaQuery()
                    .between(ActivitySignup::getSignupTime, dayStart, dayEnd)
                    .inSql(ActivitySignup::getUserId, "SELECT id FROM user WHERE tags LIKE '%elderly%'")
                    .count();
            
            Map<String, Object> dailyData = new HashMap<>();
            dailyData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dailyData.put("count", signupCount);
            trendData.add(dailyData);
        }
        
        return trendData;
    }

    /**
     * 获取健康状态分布
     * 返回不同健康状态的老人数量
     */
    @GetMapping("/health-status")
    public List<Map<String, Object>> getHealthStatusDistribution() {
        List<Map<String, Object>> healthData = new ArrayList<>();
        
        // 获取所有老人用户的ID
        List<Long> elderlyUserIds = userService.lambdaQuery()
                .like(User::getTags, "elderly")
                .list()
                .stream()
                .map(User::getId)
                .toList();
        
        // 健康：没有慢性病的老人
        long healthyCount = elderlyUserIds.size() - healthRecordService.lambdaQuery()
                .in(HealthRecord::getUserId, elderlyUserIds)
                .isNotNull(HealthRecord::getChronicDiseaseTypeId)
                .count();
        
        // 轻度慢病：有慢性病但病情较轻的老人
        long mildChronicCount = healthRecordService.lambdaQuery()
                .in(HealthRecord::getUserId, elderlyUserIds)
                .isNotNull(HealthRecord::getChronicDiseaseTypeId)
                .count();
        
        // 重度慢病：这里使用模拟数据，因为没有找到对应的病情严重程度字段
        long severeChronicCount = 10;
        
        Map<String, Object> healthy = new HashMap<>();
        healthy.put("status", "健康");
        healthy.put("count", healthyCount);
        healthData.add(healthy);
        
        Map<String, Object> mild = new HashMap<>();
        mild.put("status", "轻度慢病");
        mild.put("count", mildChronicCount);
        healthData.add(mild);
        
        Map<String, Object> severe = new HashMap<>();
        severe.put("status", "重度慢病");
        severe.put("count", severeChronicCount);
        healthData.add(severe);
        
        return healthData;
    }

    /**
     * 获取养老服务满意度
     * 返回满意度百分比
     */
    @GetMapping("/satisfaction")
    public Map<String, Object> getSatisfactionRate() {
        Map<String, Object> satisfaction = new HashMap<>();
        
        // 这里使用模拟数据，因为没有找到对应的满意度评价表结构
        int satisfactionRate = 92;
        
        satisfaction.put("rate", satisfactionRate);
        
        return satisfaction;
    }
}

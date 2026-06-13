package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.futurecommunity.model.*;
import com.example.futurecommunity.service.ActivitySignupService;
import com.example.futurecommunity.service.ComplaintService;
import com.example.futurecommunity.service.NoticeReadService;
import com.example.futurecommunity.service.NoticeService;
import com.example.futurecommunity.service.RepairService;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统首页数据驾驶舱控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private RepairService repairService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeReadService noticeReadService;

    @Autowired
    private ActivitySignupService activitySignupService;

    /**
     * 获取首页数据摘要
     * @return 数据摘要
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    @GetMapping("/summary")
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> result = new HashMap<>();

        // 1. 统计用户数量
        long userCount = userService.count();
        result.put("userCount", userCount);

        // 2. 统计今日报修数量
        long todayRepairCount = repairService.count(getTodayRepairQueryWrapper());
        result.put("todayRepairCount", todayRepairCount);

        // 3. 统计待处理投诉数量
        LambdaQueryWrapper<Complaint> complaintQueryWrapper = new LambdaQueryWrapper<>();
        complaintQueryWrapper.eq(Complaint::getStatus, "pending");
        long pendingComplaintCount = complaintService.count(complaintQueryWrapper);
        result.put("pendingComplaintCount", pendingComplaintCount);

        // 4. 统计公告数量
        long noticeCount = noticeService.count();
        result.put("noticeCount", noticeCount);

        // 5. 统计活动报名数量
        long activitySignupCount = activitySignupService.count();
        result.put("activitySignupCount", activitySignupCount);

        // 6. 计算平均阅读率
        double avgReadRate = calculateAvgReadRate();
        result.put("avgReadRate", avgReadRate);

        // 7. 统计用户角色分布
        Map<String, Long> userRoleStats = getUserRoleStats();
        result.put("userRoleStats", userRoleStats);

        // 8. 获取近7天报修趋势
        List<Map<String, Object>> repairTrend = getRepairTrend();
        result.put("repairTrend", repairTrend);

        // 9. 获取近7天投诉趋势
        List<Map<String, Object>> complaintTrend = getComplaintTrend();
        result.put("complaintTrend", complaintTrend);

        return result;
    }

    /**
     * 获取今日查询条件
     * @param clazz 实体类
     * @return 查询条件
     */
    private LambdaQueryWrapper<Repair> getTodayRepairQueryWrapper() {
        LambdaQueryWrapper<Repair> queryWrapper = new LambdaQueryWrapper<>();
        
        // 获取今日开始和结束时间
        LocalDate today = LocalDate.now();
        Date startDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        queryWrapper.ge(Repair::getCreatedAt, startDate)
                .lt(Repair::getCreatedAt, endDate);
        
        return queryWrapper;
    }
    
    private LambdaQueryWrapper<Complaint> getTodayComplaintQueryWrapper() {
        LambdaQueryWrapper<Complaint> queryWrapper = new LambdaQueryWrapper<>();
        
        // 获取今日开始和结束时间
        LocalDate today = LocalDate.now();
        Date startDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        queryWrapper.ge(Complaint::getCreatedAt, startDate)
                .lt(Complaint::getCreatedAt, endDate);
        
        return queryWrapper;
    }

    /**
     * 计算平均阅读率
     * @return 平均阅读率
     */
    private double calculateAvgReadRate() {
        // 获取所有公告数量
        long noticeCount = noticeService.count();
        if (noticeCount == 0) {
            return 0;
        }
        
        // 获取所有公告阅读记录数量
        long readCount = noticeReadService.count();
        
        // 获取总用户数量
        long userCount = userService.count();
        if (userCount == 0) {
            return 0;
        }
        
        // 计算平均阅读率
        double totalPossibleReads = noticeCount * userCount;
        if (totalPossibleReads == 0) {
            return 0;
        }
        
        return readCount / totalPossibleReads;
    }

    /**
     * 统计用户角色分布
     * @return 角色分布
     */
    private Map<String, Long> getUserRoleStats() {
        Map<String, Long> stats = new HashMap<>();
        
        // 初始化角色统计
        stats.put("resident", 0L);
        stats.put("doctor", 0L);
        stats.put("property", 0L);
        stats.put("admin", 0L);
        
        // 统计各角色用户数量
        List<User> users = userService.list();
        for (User user : users) {
            String type = user.getType();
            if (type != null) {
                stats.put(type, stats.getOrDefault(type, 0L) + 1);
            }
        }
        
        return stats;
    }

    /**
     * 获取近7天报修趋势
     * @return 报修趋势
     */
    private List<Map<String, Object>> getRepairTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 生成近7天日期
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);
            
            // 统计当天报修数量
            Date startDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            LambdaQueryWrapper<Repair> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(Repair::getCreatedAt, startDate)
                    .lt(Repair::getCreatedAt, endDate);
            
            long count = repairService.count(queryWrapper);
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put("count", count);
            trend.add(item);
        }
        
        return trend;
    }

    /**
     * 获取近7天投诉趋势
     * @return 投诉趋势
     */
    private List<Map<String, Object>> getComplaintTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 生成近7天日期
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);
            
            // 统计当天投诉数量
            Date startDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            LambdaQueryWrapper<Complaint> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(Complaint::getCreatedAt, startDate)
                    .lt(Complaint::getCreatedAt, endDate);
            
            long count = complaintService.count(queryWrapper);
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put("count", count);
            trend.add(item);
        }
        
        return trend;
    }
}
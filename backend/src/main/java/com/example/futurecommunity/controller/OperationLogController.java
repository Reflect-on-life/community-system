package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.model.OperationLog;
import com.example.futurecommunity.model.vo.OperationLogVO;
import com.example.futurecommunity.service.OperationLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 操作日志控制器 - 系统管理端
 */
@RestController
@RequestMapping("/api/admin/logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public Page<OperationLogVO> getLogsByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Date startTime,
            @RequestParam(required = false) Date endTime) {
        
        // 创建分页对象
        Page<OperationLog> logPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加筛选条件
        if (userId != null) {
            queryWrapper.eq(OperationLog::getUserId, userId);
        }
        
        if (action != null && !action.isEmpty()) {
            queryWrapper.eq(OperationLog::getAction, action);
        }
        
        if (startTime != null) {
            queryWrapper.ge(OperationLog::getCreatedAt, startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(OperationLog::getCreatedAt, endTime);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(OperationLog::getCreatedAt);
        
        // 执行分页查询
        operationLogService.page(logPage, queryWrapper);
        
        // 转换为VO
        Page<OperationLogVO> logVOPage = new Page<>();
        BeanUtils.copyProperties(logPage, logVOPage);
        
        // 转换记录
        logVOPage.setRecords(logPage.getRecords().stream().map(this::convertToVO).toList());
        
        return logVOPage;
    }

    /**
     * 将OperationLog实体转换为OperationLogVO
     */
    private OperationLogVO convertToVO(OperationLog log) {
        OperationLogVO logVO = new OperationLogVO();
        BeanUtils.copyProperties(log, logVO);
        return logVO;
    }

    /**
     * 操作日志统计接口
     * GET /operation-log/statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public Map<String, Integer> getOperationLogStatistics() {
        Map<String, Integer> statistics = new HashMap<>();
        
        // 获取今日开始时间
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        Date todayStartDate = Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
        
        // 1. 今日操作总次数
        LambdaQueryWrapper<OperationLog> todayTotalWrapper = new LambdaQueryWrapper<>();
        todayTotalWrapper.ge(OperationLog::getCreatedAt, todayStartDate);
        long todayTotal = operationLogService.count(todayTotalWrapper);
        statistics.put("todayTotal", (int) todayTotal);
        
        // 2. 今日失败次数
        LambdaQueryWrapper<OperationLog> todayFailWrapper = new LambdaQueryWrapper<>();
        todayFailWrapper.ge(OperationLog::getCreatedAt, todayStartDate);
        todayFailWrapper.eq(OperationLog::getStatus, "FAIL");
        long todayFail = operationLogService.count(todayFailWrapper);
        statistics.put("todayFail", (int) todayFail);
        
        // 3. 管理员操作次数
        LambdaQueryWrapper<OperationLog> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.ge(OperationLog::getCreatedAt, todayStartDate);
        adminWrapper.eq(OperationLog::getRole, "ADMIN");
        long adminCount = operationLogService.count(adminWrapper);
        statistics.put("adminCount", (int) adminCount);
        
        // 4. 普通用户操作次数
        LambdaQueryWrapper<OperationLog> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.ge(OperationLog::getCreatedAt, todayStartDate);
        userWrapper.eq(OperationLog::getRole, "USER");
        long userCount = operationLogService.count(userWrapper);
        statistics.put("userCount", (int) userCount);
        
        return statistics;
    }
}
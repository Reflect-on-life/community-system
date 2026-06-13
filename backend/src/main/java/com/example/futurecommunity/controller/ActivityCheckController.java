package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.model.ActivitySignup;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.ActivitySignupService;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 活动报名核销控制器
 */
@RestController
@RequestMapping("/api/admin/activity-check")
public class ActivityCheckController {

    @Autowired
    private ActivitySignupService activitySignupService;

    @Autowired
    private UserService userService;

    /**
     * 分页查看报名记录
     * @param page 页码
     * @param size 每页大小
     * @param activityId 活动ID
     * @return 分页结果
     */
    @PreAuthorize("hasRole('PROPERTY') or hasRole('ADMIN')")
    @GetMapping("/page")
    public Page<Map<String, Object>> getSignupPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long activityId) {
        
        // 创建分页对象
        Page<ActivitySignup> signupPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加活动ID筛选条件
        if (activityId != null) {
            queryWrapper.eq(ActivitySignup::getActivityId, activityId);
        }
        
        // 按报名时间倒序排序
        queryWrapper.orderByDesc(ActivitySignup::getSignupTime);
        
        // 执行分页查询
        activitySignupService.page(signupPage, queryWrapper);
        
        // 转换为带用户信息的结果
        Page<Map<String, Object>> resultPage = new Page<>();
        resultPage.setCurrent(signupPage.getCurrent());
        resultPage.setSize(signupPage.getSize());
        resultPage.setTotal(signupPage.getTotal());
        resultPage.setPages(signupPage.getPages());
        
        // 转换记录
        resultPage.setRecords(signupPage.getRecords().stream().map(signup -> {
            Map<String, Object> map = new HashMap<>();
            map.put("signupId", signup.getId());
            map.put("userId", signup.getUserId());
            
            // 获取用户信息
            User user = userService.getById(signup.getUserId());
            if (user != null) {
                map.put("userName", user.getName());
            } else {
                map.put("userName", "未知用户");
            }
            
            // 状态转换
            String statusStr = signup.getStatus();
            map.put("status", statusStr);
            map.put("createdAt", signup.getSignupTime());
            
            return map;
        }).collect(java.util.stream.Collectors.toList()));
        
        return resultPage;
    }

    /**
     * 核销报名
     * @param signupId 报名ID
     * @return 成功状态
     */
    @PreAuthorize("hasRole('PROPERTY') or hasRole('ADMIN')")
    @Transactional
    @PutMapping("/{signupId}/check")
    public Map<String, Object> checkSignup(@PathVariable Long signupId) {
        // 查询报名记录
        ActivitySignup signup = activitySignupService.getById(signupId);
        if (signup == null) {
            throw new RuntimeException("报名记录不存在");
        }
        
        // 修改状态为已核销（使用confirmed表示）
        signup.setStatus("confirmed");
        
        // 保存修改
        boolean success = activitySignupService.updateById(signup);
        
        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (success) {
            result.put("message", "核销成功");
        } else {
            result.put("message", "核销失败");
        }
        
        return result;
    }
}

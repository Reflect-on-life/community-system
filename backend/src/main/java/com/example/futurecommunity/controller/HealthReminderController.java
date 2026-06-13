package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.futurecommunity.model.HealthReminder;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.HealthReminderService;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthReminderController {

    @Autowired
    private HealthReminderService healthReminderService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userIdStr == null || userIdStr.equals("anonymousUser")) {
            return null;
        }
        try {
            Long userId = Long.parseLong(userIdStr);
            return userService.getById(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @PostMapping("/reminder")
    @PreAuthorize("hasRole('DOCTOR')")
    public HealthReminder createReminder(@RequestBody HealthReminder reminder) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        reminder.setCreatorId(currentUser.getId());
        healthReminderService.save(reminder);
        return reminder;
    }

    @GetMapping("/reminder/my")
    @PreAuthorize("hasRole('RESIDENT')")
    public List<HealthReminder> getMyReminders() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 暂时返回所有健康提醒，后续可根据用户的慢性病类型进行过滤
        LambdaQueryWrapper<HealthReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(HealthReminder::getCreateTime);
        
        return healthReminderService.list(wrapper);
    }
    
    /**
     * 获取医生发布的健康提醒列表
     * 仅医生可以访问
     */
    @GetMapping("/reminder/doctor/list")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<HealthReminder> getDoctorReminders() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 查询当前医生发布的健康提醒
        LambdaQueryWrapper<HealthReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthReminder::getCreatorId, currentUser.getId());
        wrapper.orderByDesc(HealthReminder::getCreateTime);
        
        return healthReminderService.list(wrapper);
    }
    
    /**
     * 修改健康提醒
     * 仅医生可以修改自己发布的提醒
     */
    @PutMapping("/reminder/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public HealthReminder updateReminder(@PathVariable Long id, @RequestBody HealthReminder reminder) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        HealthReminder existing = healthReminderService.getById(id);
        if (existing == null) {
            throw new RuntimeException("提醒不存在");
        }
        
        if (!existing.getCreatorId().equals(currentUser.getId())) {
            throw new RuntimeException("只能修改自己发布的提醒");
        }
        
        reminder.setId(id);
        healthReminderService.updateById(reminder);
        return reminder;
    }
    
    /**
     * 删除健康提醒
     * 仅医生可以删除自己发布的提醒
     */
    @DeleteMapping("/reminder/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void deleteReminder(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        HealthReminder existing = healthReminderService.getById(id);
        if (existing == null) {
            throw new RuntimeException("提醒不存在");
        }
        
        if (!existing.getCreatorId().equals(currentUser.getId())) {
            throw new RuntimeException("只能删除自己发布的提醒");
        }
        
        healthReminderService.removeById(id);
    }
}

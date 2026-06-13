package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.futurecommunity.model.HealthEducation;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.HealthEducationService;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/health")
public class HealthEducationController {

    private static final Logger logger = LoggerFactory.getLogger(HealthEducationController.class);

    @Autowired
    private HealthEducationService healthEducationService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String userIdStr = authentication.getName();
        if (userIdStr == null || userIdStr.isEmpty()) {
            return null;
        }
        try {
            Long userId = Long.parseLong(userIdStr);
            return userService.getById(userId);
        } catch (NumberFormatException e) {
            logger.error("用户ID格式错误: {}", userIdStr, e);
            return null;
        }
    }

    @PostMapping("/education")
    @PreAuthorize("hasRole('DOCTOR')")
    public Map<String, Object> createEducation(@RequestBody HealthEducation education) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("code", "USER_NOT_FOUND");
                response.put("message", "当前用户未找到");
                response.put("data", null);
                return response;
            }
            education.setCreatorId(currentUser.getId());
            education.setCreateTime(new Date());
            boolean saved = healthEducationService.save(education);
            Map<String, Object> response = new HashMap<>();
            if (saved) {
                response.put("success", true);
                response.put("code", "SUCCESS");
                response.put("message", "发布成功");
                response.put("data", education);
            } else {
                response.put("success", false);
                response.put("code", "SAVE_FAILED");
                response.put("message", "发布失败");
                response.put("data", null);
            }
            return response;
        } catch (Exception e) {
            logger.error("发布健康宣教异常: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "SYSTEM_ERROR");
            response.put("message", "发布过程中发生系统错误");
            response.put("data", null);
            return response;
        }
    }

    @GetMapping("/education/list")
    public Map<String, Object> getEducationList() {
        try {
            LambdaQueryWrapper<HealthEducation> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(HealthEducation::getCreateTime);
            List<HealthEducation> list = healthEducationService.list(wrapper);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", "SUCCESS");
            response.put("message", "获取成功");
            response.put("data", list);
            return response;
        } catch (Exception e) {
            logger.error("获取健康宣教列表异常: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "SYSTEM_ERROR");
            response.put("message", "获取健康宣教列表失败");
            response.put("data", null);
            return response;
        }
    }

    @GetMapping("/education/doctor/list")
    @PreAuthorize("hasRole('DOCTOR')")
    public Map<String, Object> getDoctorEducationList() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "USER_NOT_FOUND");
            response.put("message", "当前用户未找到");
            response.put("data", null);
            return response;
        }
        LambdaQueryWrapper<HealthEducation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthEducation::getCreatorId, currentUser.getId());
        wrapper.orderByDesc(HealthEducation::getCreateTime);
        List<HealthEducation> list = healthEducationService.list(wrapper);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "SUCCESS");
        response.put("message", "获取成功");
        response.put("data", list);
        return response;
    }

    @PutMapping("/education/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public Map<String, Object> updateEducation(@PathVariable Long id, @RequestBody HealthEducation education) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "USER_NOT_FOUND");
            response.put("message", "当前用户未找到");
            response.put("data", null);
            return response;
        }
        HealthEducation existing = healthEducationService.getById(id);
        if (existing == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "NOT_FOUND");
            response.put("message", "健康宣教内容未找到");
            response.put("data", null);
            return response;
        }
        if (!existing.getCreatorId().equals(currentUser.getId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "PERMISSION_DENIED");
            response.put("message", "您只能修改自己发布的内容");
            response.put("data", null);
            return response;
        }
        education.setId(id);
        education.setCreatorId(currentUser.getId());
        education.setCreateTime(existing.getCreateTime());
        boolean updated = healthEducationService.updateById(education);
        Map<String, Object> response = new HashMap<>();
        if (updated) {
            response.put("success", true);
            response.put("code", "SUCCESS");
            response.put("message", "修改成功");
            response.put("data", education);
        } else {
            response.put("success", false);
            response.put("code", "UPDATE_FAILED");
            response.put("message", "修改失败");
            response.put("data", null);
        }
        return response;
    }

    @DeleteMapping("/education/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public Map<String, Object> deleteEducation(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "USER_NOT_FOUND");
            response.put("message", "当前用户未找到");
            response.put("data", null);
            return response;
        }
        HealthEducation existing = healthEducationService.getById(id);
        if (existing == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "NOT_FOUND");
            response.put("message", "健康宣教内容未找到");
            response.put("data", null);
            return response;
        }
        if (!existing.getCreatorId().equals(currentUser.getId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "PERMISSION_DENIED");
            response.put("message", "您只能删除自己发布的内容");
            response.put("data", null);
            return response;
        }
        boolean deleted = healthEducationService.removeById(id);
        Map<String, Object> response = new HashMap<>();
        if (deleted) {
            response.put("success", true);
            response.put("code", "SUCCESS");
            response.put("message", "删除成功");
            response.put("data", null);
        } else {
            response.put("success", false);
            response.put("code", "DELETE_FAILED");
            response.put("message", "删除失败");
            response.put("data", null);
        }
        return response;
    }
}

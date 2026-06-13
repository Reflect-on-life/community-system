package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.futurecommunity.dto.PasswordChangeRequest;
import com.example.futurecommunity.dto.UserThemePreferenceDTO;
import com.example.futurecommunity.mapper.TechnicianSkillMapper;
import com.example.futurecommunity.mapper.TechnicianSkillRelationMapper;
import com.example.futurecommunity.model.TechnicianSkill;
import com.example.futurecommunity.model.TechnicianSkillRelation;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 个人资料控制器 - 用户个人信息维护
 */
@RestController
@RequestMapping("/api/user")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TechnicianSkillMapper technicianSkillMapper;

    @Autowired
    private TechnicianSkillRelationMapper technicianSkillRelationMapper;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile() {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getById(Long.parseLong(currentUserId));

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("phone", user.getPhone());
        result.put("name", user.getName());
        result.put("gender", user.getGender());
        result.put("birthday", user.getBirthday());
        result.put("avatar", user.getAvatar());
        result.put("type", user.getType());

        return ResponseEntity.ok(result);
    }

    /**
     * 修改个人信息
     * 仅允许修改非敏感字段（name、gender、birthday、avatar）
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> request) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getById(Long.parseLong(currentUserId));

        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户不存在");
            return ResponseEntity.badRequest().body(error);
        }

        if (request.containsKey("name")) {
            user.setName((String) request.get("name"));
        }
        if (request.containsKey("gender")) {
            user.setGender((String) request.get("gender"));
        }
        if (request.containsKey("birthday")) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date parsedDate = sdf.parse((String) request.get("birthday"));
                    user.setBirthday(new Date(parsedDate.getTime()));
                } catch (Exception e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "出生日期格式错误");
                    return ResponseEntity.badRequest().body(error);
                }
            }
        if (request.containsKey("avatar")) {
            user.setAvatar((String) request.get("avatar"));
        }

            userService.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "个人信息修改成功");
        result.put("data", user);

        return ResponseEntity.ok(result);
    }

    /**
     * 修改密码
     * 需要验证原密码
     */
    @PutMapping("/profile/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody PasswordChangeRequest request) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getById(Long.parseLong(currentUserId));

        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户不存在");
            return ResponseEntity.badRequest().body(error);
        }

        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        // 验证原密码是否为空
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "请输入原密码");
            return ResponseEntity.badRequest().body(error);
        }

        // 验证新密码是否为空
        if (newPassword == null || newPassword.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "请输入新密码");
            return ResponseEntity.badRequest().body(error);
        }

        // 验证新密码长度
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "新密码长度必须在 6-20 位之间");
            return ResponseEntity.badRequest().body(error);
        }

        // 验证原密码是否正确
        boolean passwordMatch = false;
        if (user.getPassword() != null && user.getPassword().startsWith("$2a$")) {
            // 密码是 BCrypt 格式
            passwordMatch = passwordEncoder.matches(oldPassword, user.getPassword());
        } else {
            // 密码是明文格式（兼容旧数据）
            passwordMatch = oldPassword.equals(user.getPassword());
        }
        
        if (!passwordMatch) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "原密码错误");
            return ResponseEntity.badRequest().body(error);
        }

        // 加密新密码并更新
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userService.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "密码修改成功，请重新登录");

        return ResponseEntity.ok(result);
    }

    /**
     * 获取当前登录用户的主题偏好
     */
    @GetMapping("/theme")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserThemePreference() {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getById(Long.parseLong(currentUserId));

        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户不存在");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", new UserThemePreferenceDTO(
            user.getThemeMode() != null ? user.getThemeMode() : "system",
            user.getThemeColor() != null ? user.getThemeColor() : "teal"
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * 更新当前登录用户的主题偏好
     */
    @PutMapping("/theme")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateUserThemePreference(@RequestBody UserThemePreferenceDTO preference) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getById(Long.parseLong(currentUserId));

        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户不存在");
            return ResponseEntity.badRequest().body(error);
        }

        if (preference.getMode() != null) {
            user.setThemeMode(preference.getMode());
        }
        if (preference.getColor() != null) {
            user.setThemeColor(preference.getColor());
        }

        userService.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "主题偏好保存成功");
        result.put("data", new UserThemePreferenceDTO(
            user.getThemeMode(),
            user.getThemeColor()
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * 根据用户类型获取用户列表
     * 用于获取维修师傅列表等场景
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getUsersByType(@PathVariable String type) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getType, type);
        queryWrapper.eq(User::getStatus, 1);
        
        List<User> users = userService.list(queryWrapper);
        
        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("phone", user.getPhone());
            userMap.put("type", user.getType());
            
            // 如果是维修师傅用户，查询其技能
            if ("property".equals(type)) {
                LambdaQueryWrapper<TechnicianSkillRelation> skillQueryWrapper = new LambdaQueryWrapper<>();
                skillQueryWrapper.eq(TechnicianSkillRelation::getUserId, user.getId());
                List<TechnicianSkillRelation> skillRelations = technicianSkillRelationMapper.selectList(skillQueryWrapper);
                
                if (skillRelations.isEmpty()) {
                    userMap.put("skills", Collections.emptyList());
                } else {
                    List<Long> skillIds = skillRelations.stream()
                            .map(TechnicianSkillRelation::getSkillId)
                            .collect(Collectors.toList());
                    
                    List<TechnicianSkill> skills = technicianSkillMapper.selectBatchIds(skillIds);
                    userMap.put("skills", skills);
                }
            }
            
            return userMap;
        }).toList();
        
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID获取用户信息
     * 用于获取发布人信息等场景
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        
        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户不存在");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("phone", user.getPhone());
        userData.put("type", user.getType());
        userData.put("gender", user.getGender());
        userData.put("birthday", user.getBirthday());
        userData.put("avatar", user.getAvatar());
        
        result.put("success", true);
        result.put("data", userData);
        
        return ResponseEntity.ok(result);
    }
}

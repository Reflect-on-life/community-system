package com.example.futurecommunity.controller;

import com.example.futurecommunity.annotation.RequirePermission;
import com.example.futurecommunity.util.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private PermissionUtil permissionUtil;

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "测试接口，无需权限");
        return result;
    }

    @GetMapping("/repair/process")
    @RequirePermission("repair:process")
    public Map<String, Object> repairProcess() {
        logger.info("处理报修接口被调用");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "处理报修成功");
        return result;
    }

    @PostMapping("/repair/assign")
    @RequirePermission("repair:assign")
    public Map<String, Object> repairAssign(@RequestBody Map<String, Object> request) {
        logger.info("分配报修接口被调用");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "分配报修成功");
        result.put("data", request);
        return result;
    }

    @GetMapping("/user/view")
    @RequirePermission("user:view")
    public Map<String, Object> userView() {
        logger.info("查看用户接口被调用");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "查看用户成功");
        return result;
    }

    @GetMapping("/user/manage")
    @RequirePermission(value = "user:manage", message = "您没有用户管理权限")
    public Map<String, Object> userManage() {
        logger.info("用户管理接口被调用");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "用户管理成功");
        return result;
    }

    @GetMapping("/permission/check")
    public Map<String, Object> checkPermission(@RequestParam String permission) {
        logger.info("检查权限: {}", permission);
        
        boolean hasPermission = permissionUtil.hasPermission(permission);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("permission", permission);
        result.put("hasPermission", hasPermission);
        return result;
    }

    @GetMapping("/permission/list")
    public Map<String, Object> getPermissions() {
        logger.info("获取当前用户权限列表");
        
        var permissions = permissionUtil.getCurrentUserPermissions();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("permissions", permissions);
        return result;
    }
}
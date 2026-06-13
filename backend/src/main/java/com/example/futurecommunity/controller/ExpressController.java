package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.Express;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.ExpressService;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/express")
public class ExpressController {

    @Autowired
    private ExpressService expressService;
    
    @Autowired
    private UserService userService;

    @PostMapping("/inbound")
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    public Express inbound(@RequestBody Express express) {
        System.out.println("=== 快递入库操作 ===");
        System.out.println("快递单号：" + express.getTrackingNo());
        System.out.println("用户 ID: " + express.getUserId());
        System.out.println("===================");
        return expressService.createExpressWithPickupCode(express);
    }

    @PostMapping("/pickup/{pickupCode}")
    @PreAuthorize("hasAnyRole('RESIDENT', 'PROPERTY', 'ADMIN')")
    public Express pickup(@PathVariable String pickupCode) {
        System.out.println("=== 快递取件操作 ===");
        System.out.println("取件码：" + pickupCode);
        System.out.println("===================");
        return expressService.pickupExpress(pickupCode);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESIDENT', 'PROPERTY', 'ADMIN')")
    public Object list() {
        // 获取当前登录用户的信息
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        String userType = "ROLE_RESIDENT"; // 默认角色
        
        // 安全地获取用户权限
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            userType = authentication.getAuthorities().iterator().next().getAuthority();
        }
        
        // 根据用户类型返回不同的数据
        if ("ROLE_RESIDENT".equals(userType)) {
            // 居民只能看到自己的快递
            List<Express> expressList = expressService.listByUserId(Long.parseLong(currentUserId));
            return convertExpressListWithUserName(expressList);
        } else if ("ROLE_PROPERTY".equals(userType) || "ROLE_ADMIN".equals(userType)) {
            // 物业和管理员可以看到所有快递
            List<Express> expressList = expressService.list();
            return convertExpressListWithUserName(expressList);
        } else {
            // 其他角色返回空列表
            return List.of();
        }
    }
    
    // 将快递列表转换为包含用户名字的列表
    private List<Map<String, Object>> convertExpressListWithUserName(List<Express> expressList) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Express express : expressList) {
            Map<String, Object> expressMap = new HashMap<>();
            expressMap.put("id", express.getId());
            expressMap.put("trackingNo", express.getTrackingNo());
            expressMap.put("userId", express.getUserId());
            
            // 获取用户名字
            User user = userService.getById(express.getUserId());
            expressMap.put("userName", user != null ? user.getName() : "未知用户");
            
            expressMap.put("pickupCode", express.getPickupCode());
            expressMap.put("status", express.getStatus());
            expressMap.put("receivedAt", express.getReceivedAt());
            expressMap.put("pickedAt", express.getPickedAt());
            
            result.add(expressMap);
        }
        
        return result;
    }
}

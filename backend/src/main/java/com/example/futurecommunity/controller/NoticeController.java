package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.Notice;
import com.example.futurecommunity.model.Repair;
import com.example.futurecommunity.service.NoticeService;
import com.example.futurecommunity.service.RepairService;
import com.example.futurecommunity.service.UserService;
import com.example.futurecommunity.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private RepairService repairService;

    /**
     * 获取所有公告
     */
    @GetMapping
    public List<Map<String, Object>> getNotices() {
        return noticeService.lambdaQuery()
                .orderByDesc(Notice::getCreatedAt)
                .list()
                .stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有公告（需认证）
     */
    @PreAuthorize("hasAnyRole('RESIDENT', 'PROPERTY', 'ADMIN', 'DOCTOR', 'PROPERTY_TECHNICIAN')")
    @GetMapping("/auth")
    public List<Map<String, Object>> getNoticesAuth() {
        return noticeService.lambdaQuery()
                .orderByDesc(Notice::getCreatedAt)
                .list()
                .stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户可以看到的公告
     * 1. 非报修相关的公告：根据 target_type 判断可见范围
     *    - all: 所有用户可见
     *    - resident: 居民可见
     *    - property/property_staff/admin: 物业人员可见
     *    - doctor: 医生可见
     *    - property_technician: 维修师傅可见
     * 2. 报修相关的公告：只有该报修的提交者可以看到
     */
    @PreAuthorize("hasAnyRole('RESIDENT', 'PROPERTY', 'ADMIN', 'DOCTOR', 'PROPERTY_TECHNICIAN')")
    @GetMapping("/user")
    public List<Map<String, Object>> getUserNotices() {
        String currentUserIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        String userType = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
        
        if (currentUserIdStr == null || currentUserIdStr.equals("anonymousUser")) {
            return noticeService.lambdaQuery()
                .isNull(Notice::getRepairId)
                .orderByDesc(Notice::getCreatedAt)
                .list()
                .stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        }
        
        Long currentUserId;
        try {
            currentUserId = Long.parseLong(currentUserIdStr);
        } catch (NumberFormatException e) {
            return noticeService.lambdaQuery()
                .isNull(Notice::getRepairId)
                .orderByDesc(Notice::getCreatedAt)
                .list()
                .stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        }
        
        return noticeService.lambdaQuery()
                .orderByDesc(Notice::getCreatedAt)
                .list()
                .stream()
            .filter(notice -> {
                // 报修相关的公告：只有该报修的提交者可以看到
                if (notice.getRepairId() != null) {
                    Repair repair = repairService.getById(notice.getRepairId());
                    return repair != null && repair.getUserId() != null && repair.getUserId().equals(currentUserId);
                }
                
                // 非报修相关的公告：根据 target_type 判断可见范围
                String targetType = notice.getTargetType();
                if (targetType == null || "ALL".equalsIgnoreCase(targetType) || "all".equalsIgnoreCase(targetType)) {
                    return true; // 全员公告，所有人都可以看到
                }
                
                // 提取用户角色名称（去掉 ROLE_ 前缀）
                String roleName = userType.replace("ROLE_", "").toUpperCase();
                
                // 将 targetType 按逗号分隔后判断（不区分大小写）
                String[] targetTypes = targetType.toUpperCase().split(",");
                for (String type : targetTypes) {
                    if (type.trim().equals(roleName)) {
                        return true;
                    }
                }
                
                return false;
            })
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }

    /**
     * 根据报修 ID 获取相关公告
     */
    @PreAuthorize("hasAnyRole('RESIDENT', 'PROPERTY', 'ADMIN', 'PROPERTY_TECHNICIAN')")
    @GetMapping("/repair/{repairId}")
    public List<Map<String, Object>> getNoticesByRepairId(@PathVariable Long repairId) {
        return noticeService.lambdaQuery()
                .eq(Notice::getRepairId, repairId)
                .orderByAsc(Notice::getCreatedAt)
                .list()
                .stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 发布公告
     */
    @PostMapping
    public Map<String, Object> publishNotice(@RequestBody Notice notice) {
        noticeService.save(notice);
        return convertToMap(notice);
    }

    /**
     * 根据 ID 获取公告
     */
    @GetMapping("/{id}")
    public Map<String, Object> getNotice(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        return convertToMap(notice);
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        noticeService.updateById(notice);
        return convertToMap(noticeService.getById(id));
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public void deleteNotice(@PathVariable Long id) {
        noticeService.removeById(id);
    }

    /**
     * 将Notice转换为包含发布人姓名的Map
     */
    private Map<String, Object> convertToMap(Notice notice) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", notice.getId());
        map.put("title", notice.getTitle());
        map.put("content", notice.getContent());
        map.put("targetType", notice.getTargetType());
        map.put("repairId", notice.getRepairId());
        map.put("createdBy", notice.getCreatedBy());
        map.put("createdAt", notice.getCreatedAt());
        
        // 添加发布人姓名
        if (notice.getCreatedBy() != null) {
            User user = userService.getById(notice.getCreatedBy());
            if (user != null) {
                map.put("publisher", user.getName());
            } else {
                map.put("publisher", "未知");
            }
        } else {
            map.put("publisher", "未知");
        }
        
        return map;
    }
}
